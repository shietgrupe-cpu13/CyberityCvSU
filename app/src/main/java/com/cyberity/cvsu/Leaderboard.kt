package com.cyberity.cvsu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

// ===========================================================================
// LEADERBOARD
// ===========================================================================
// One public document per student in "leaderboard/{uid}": the display name
// and the XP balance, nothing else. Progress stays private in
// "userProgress/{uid}" — hearts and hint spending are never exposed.
//
// The Learn screen publishes the entry whenever the student's XP changes, so
// the board follows the same figure the student sees in the app.
//
// The authoritative firestore.rules requires XP/name/tester state to match
// the server-stored progress/profile, including the starting grant and spending.

@Immutable
data class LeaderboardEntry(
    val uid: String,
    val displayName: String,
    val xp: Int
)

object LeaderboardRepository {

    private val db by lazy { FirebaseFirestore.getInstance() }

    private const val COLLECTION = "leaderboard"
    private const val FIELD_NAME = "displayName"
    private const val FIELD_XP = "xp"
    private const val FIELD_IS_TESTER = "isTester"
    private const val FIELD_UPDATED_AT = "updatedAt"

    /** How many students the board lists. */
    const val TOP_N = 50

    /** Writes this student's public entry. Test accounts are stored but never listed. */
    fun publish(uid: String): com.google.android.gms.tasks.Task<Unit> =
        db.runTransaction { transaction ->
            val privacy = transaction.get(db.collection("privacyPreferences").document(uid))
            if (privacy.getBoolean("leaderboardVisible") == false || privacy.getBoolean("deleting") == true) return@runTransaction
            val profile = transaction.get(db.collection("users").document(uid))
            val progress = transaction.get(db.collection("userProgress").document(uid))
            val name = profile.getString("displayName") ?: return@runTransaction
            val xp = ScorePolicy.balance(progress.get("levelXp") as? Map<*, *>, progress.getLong("xpSpent") ?: 0)
            transaction.set(db.collection(COLLECTION).document(uid), mapOf(
                FIELD_NAME to name,
                FIELD_XP to xp,
                FIELD_IS_TESTER to (profile.getBoolean("isTester") ?: false),
                FIELD_UPDATED_AT to FieldValue.serverTimestamp()
            ), SetOptions.merge())
            Unit
        }.addOnFailureListener { android.util.Log.w("CyberityScores", "Leaderboard synchronization failed", it) }

    /**
     * Listens to the top of the board, highest XP first. Test accounts are
     * filtered here rather than in the query, so no composite index is needed;
     * a few extra rows are fetched to make up for them.
     */
    fun listenTop(
        onResult: (List<LeaderboardEntry>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration =
        db.collection(COLLECTION)
            .orderBy(FIELD_XP, Query.Direction.DESCENDING)
            .limit((TOP_N + 20).toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "Could not load the leaderboard")
                    return@addSnapshotListener
                }
                val entries = snapshot?.documents.orEmpty()
                    .filter { it.getBoolean(FIELD_IS_TESTER) != true }
                    .mapNotNull { doc ->
                        val name = doc.getString(FIELD_NAME) ?: return@mapNotNull null
                        val xp = doc.getLong(FIELD_XP)?.toInt() ?: return@mapNotNull null
                        LeaderboardEntry(doc.id, name, xp)
                    }
                    .take(TOP_N)
                onResult(entries)
            }

    /** Listens to this student's own XP on the board, for the "your rank" bar when they're outside the top. */
    fun listenOwnXp(uid: String, onResult: (Int?) -> Unit): ListenerRegistration =
        db.collection(COLLECTION).document(uid).addSnapshotListener { snapshot, _ ->
            onResult(snapshot?.getLong(FIELD_XP)?.toInt())
        }
}

// ===========================================================================
// SCREEN
// ===========================================================================
// The top three stand on a podium; everyone from #4 down is a list. The
// student's own rank is pinned at the bottom with the XP gap to the next spot,
// worked out from the list itself — no extra data is stored.

private fun formatXp(xp: Int): String = "%,d".format(xp)

private fun initials(name: String): String =
    name.split(" ", ".", "_").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }.ifBlank { "?" }

@Composable
fun LeaderboardScreen(modifier: Modifier = Modifier) {
    val myUid = FirebaseAuth.getInstance().currentUser?.uid

    var entries by remember { mutableStateOf<List<LeaderboardEntry>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var publiclyVisible by remember(myUid) { mutableStateOf(true) }
    var myXp by remember(myUid) { mutableStateOf<Int?>(null) }

    DisposableEffect(myUid) {
        val registration = myUid?.let { uid ->
            FirebaseFirestore.getInstance().collection("privacyPreferences").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) publiclyVisible = snapshot.getBoolean("leaderboardVisible") != false
                }
        }
        val own = myUid?.let { LeaderboardRepository.listenOwnXp(it) { xp -> myXp = xp } }
        onDispose { registration?.remove(); own?.remove() }
    }

    DisposableEffect(Unit) {
        val registration = LeaderboardRepository.listenTop(
            onResult = { entries = it; error = null },
            onError = { error = it }
        )
        onDispose { registration.remove() }
    }

    Column(modifier = modifier.fillMaxSize().background(AppNavy)) {
        val list = entries
        when {
            error != null && list == null -> {
                LeaderboardTitle()
                LeaderboardMessage(
                    title = "Leaderboard unavailable",
                    body = "Check your connection and try again in a moment."
                )
            }
            list == null -> {
                LeaderboardTitle()
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppCyan)
                }
            }
            list.isEmpty() -> {
                LeaderboardTitle()
                LeaderboardMessage(
                    title = "No rankings yet",
                    body = "Finish a level to earn XP and claim the first spot."
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(key = "title") { LeaderboardTitle(padding = false) }
                    item(key = "podium") { Podium(list.take(3), myUid) }
                    itemsIndexed(list.drop(3), key = { _, e -> e.uid }) { index, entry ->
                        LeaderboardRow(rank = index + 4, entry = entry, isMe = entry.uid == myUid)
                    }
                }
                YourRankBar(list, myUid, myXp, publiclyVisible)
            }
        }
    }
}

@Composable
private fun LeaderboardTitle(padding: Boolean = true) {
    Column(
        modifier = if (padding) Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)
        else Modifier.padding(start = 8.dp, end = 8.dp, top = 24.dp)
    ) {
        Text("Leaderboard", color = AppWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Top ${LeaderboardRepository.TOP_N} students by XP", color = AppGray, fontSize = 13.sp)
    }
}

@Composable
private fun Podium(top: List<LeaderboardEntry>, myUid: String?) {
    // Second, first, third: the winner stands in the middle, highest.
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PodiumSpot(top.getOrNull(1), place = 2, medal = AppGray, avatar = 50.dp, block = 56.dp, myUid = myUid, modifier = Modifier.weight(1f))
        PodiumSpot(top.getOrNull(0), place = 1, medal = AppXp, avatar = 62.dp, block = 78.dp, myUid = myUid, modifier = Modifier.weight(1.15f))
        PodiumSpot(top.getOrNull(2), place = 3, medal = AppStreak, avatar = 50.dp, block = 42.dp, myUid = myUid, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PodiumSpot(entry: LeaderboardEntry?, place: Int, medal: Color, avatar: Dp, block: Dp, myUid: String?,
    modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (entry != null) {
            if (place == 1) Icon(Icons.Filled.EmojiEvents, contentDescription = "First place", tint = AppXp, modifier = Modifier.size(24.dp))
            Box(
                modifier = Modifier.size(avatar).clip(CircleShape).background(AppBlue).border(3.dp, medal, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(initials(entry.displayName), color = AppOnBlue, fontWeight = FontWeight.Bold,
                    fontSize = if (place == 1) 22.sp else 18.sp)
            }
            Text(entry.displayName, color = AppWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (entry.uid == myUid) YouChip()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(XpBolt, contentDescription = null, tint = AppXp, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(2.dp))
                Text(formatXp(entry.xp), color = AppWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        // The step itself: a medal-coloured top edge over a card, with the place number.
        val shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
        Column(Modifier.fillMaxWidth().height(block).clip(shape).border(1.dp, AppBorder, shape)) {
            Box(Modifier.fillMaxWidth().height(4.dp).background(medal))
            Box(Modifier.fillMaxSize().background(AppCard), contentAlignment = Alignment.Center) {
                Text("$place", color = medal, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun YouChip() {
    Box(modifier = Modifier.background(AppCyan, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 1.dp)) {
        Text("YOU", color = AppNavy, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LeaderboardRow(rank: Int, entry: LeaderboardEntry, isMe: Boolean) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isMe) AppCyan.copy(alpha = 0.12f) else AppCard, shape)
            .border(1.dp, if (isMe) AppCyan else AppBorder, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(32.dp).background(AppNavy, CircleShape), contentAlignment = Alignment.Center) {
            Text("$rank", color = AppGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.size(36.dp).background(AppBlue, CircleShape), contentAlignment = Alignment.Center) {
            Text(initials(entry.displayName), color = AppOnBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                entry.displayName,
                color = AppWhite,
                fontSize = 15.sp,
                fontWeight = if (isMe) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (isMe) {
                Spacer(Modifier.width(8.dp))
                YouChip()
            }
        }
        Spacer(Modifier.width(8.dp))
        Icon(XpBolt, contentDescription = null, tint = AppXp, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(formatXp(entry.xp), color = AppWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * The student's own standing, pinned under the list. In the top: rank, XP and
 * the XP needed to pass the student above. Outside it: how far to the top, or
 * why they aren't listed.
 */
@Composable
private fun YourRankBar(list: List<LeaderboardEntry>, myUid: String?, myXp: Int?, publiclyVisible: Boolean) {
    val index = list.indexOfFirst { it.uid == myUid }
    HorizontalDivider(color = AppBorder)
    Column(
        modifier = Modifier.fillMaxWidth().background(AppCard).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (index >= 0) {
            val me = list[index]
            val rank = index + 1
            val above = list.getOrNull(index - 1)
            val below = list.getOrNull(index + 1)
            // Where the student sits between the one below and the one above.
            val progress = when {
                above == null -> 1f
                above.xp <= (below?.xp ?: 0) -> 1f
                else -> ((me.xp - (below?.xp ?: 0)).toFloat() / (above.xp - (below?.xp ?: 0))).coerceIn(0f, 1f)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("#$rank", color = AppCyan, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Box(Modifier.size(34.dp).background(AppBlue, CircleShape), contentAlignment = Alignment.Center) {
                    Text(initials(me.displayName), color = AppOnBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Text("You · ${me.displayName}", color = AppWhite, fontWeight = FontWeight.Medium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Icon(XpBolt, contentDescription = null, tint = AppXp, modifier = Modifier.size(16.dp))
                Text(formatXp(me.xp), color = AppWhite, fontWeight = FontWeight.Bold)
            }
            RankProgress(progress)
            if (above == null) {
                Text("You're #1 on the board. Keep finishing levels to stay there.", color = AppGray, fontSize = 13.sp)
            } else {
                GapText(above.xp - me.xp + 1, "to pass #${rank - 1} ${above.displayName}")
            }
        } else {
            val last = list.lastOrNull()
            when {
                !publiclyVisible -> Text("Your leaderboard entry is hidden. Change this in Settings → Privacy.",
                    color = AppGray, fontSize = 13.sp)
                myXp != null && last != null && list.size >= LeaderboardRepository.TOP_N -> {
                    RankProgress((myXp.toFloat() / last.xp.coerceAtLeast(1)).coerceIn(0f, 1f))
                    GapText(last.xp - myXp + 1, "to reach the top ${LeaderboardRepository.TOP_N}")
                }
                else -> Text("You're not on the board yet. Finish a level to earn XP.", color = AppGray, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun RankProgress(progress: Float) {
    Box(Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)).background(AppNavy)) {
        Box(Modifier.fillMaxWidth(progress).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(AppBlue))
    }
}

@Composable
private fun GapText(xp: Int, rest: String) {
    Text(buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = AppWhite)) { append("${formatXp(xp)} XP") }
        append(" $rest")
    }, color = AppGray, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun LeaderboardMessage(title: String, body: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = AppCyan,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(title, color = AppWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(body, color = AppGray, fontSize = 14.sp)
        }
    }
}
