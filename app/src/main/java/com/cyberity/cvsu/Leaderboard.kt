package com.cyberity.cvsu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
// Firestore rules this needs (add in the Firebase console):
//   match /leaderboard/{uid} {
//     allow read: if request.auth != null;
//     allow write: if request.auth != null && request.auth.uid == uid;
//   }

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
    fun publish(uid: String, profile: UserProfile, xp: Int) {
        db.collection(COLLECTION).document(uid).set(
            mapOf(
                FIELD_NAME to profile.displayName,
                FIELD_XP to xp,
                FIELD_IS_TESTER to profile.isTester,
                FIELD_UPDATED_AT to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        )
    }

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
}

// ===========================================================================
// SCREEN
// ===========================================================================

@Composable
fun LeaderboardScreen(modifier: Modifier = Modifier) {
    val myUid = FirebaseAuth.getInstance().currentUser?.uid

    var entries by remember { mutableStateOf<List<LeaderboardEntry>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val registration = LeaderboardRepository.listenTop(
            onResult = { entries = it; error = null },
            onError = { error = it }
        )
        onDispose { registration.remove() }
    }

    Column(modifier = modifier.fillMaxSize().background(AppNavy)) {
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)) {
            Text("Leaderboard", color = AppWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(
                "Top ${LeaderboardRepository.TOP_N} students, ranked by XP",
                color = AppGray,
                fontSize = 13.sp
            )
        }

        val list = entries
        when {
            error != null && list == null -> LeaderboardMessage(
                title = "Leaderboard unavailable",
                body = "Check your connection and try again in a moment."
            )
            list == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppCyan)
            }
            list.isEmpty() -> LeaderboardMessage(
                title = "No rankings yet",
                body = "Finish a level to earn XP and claim the first spot."
            )
            else -> {
                val myIndex = list.indexOfFirst { it.uid == myUid }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(list, key = { _, e -> e.uid }) { index, entry ->
                        LeaderboardRow(rank = index + 1, entry = entry, isMe = entry.uid == myUid)
                    }
                }
                // Outside the top: say so, rather than leaving the student hunting for themselves.
                if (myIndex < 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppCard)
                            .padding(horizontal = 24.dp, vertical = 14.dp)
                    ) {
                        Text(
                            "You're not in the top ${LeaderboardRepository.TOP_N} yet. " +
                                    "Keep finishing levels to climb.",
                            color = AppGray,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(rank: Int, entry: LeaderboardEntry, isMe: Boolean) {
    val shape = RoundedCornerShape(14.dp)
    // The top three get a filled rank badge; everyone else a plain number.
    val podium = rank <= 3

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isMe) AppCyan.copy(alpha = 0.12f) else AppCard, shape)
            .border(1.dp, if (isMe) AppCyan else AppBorder, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (podium) AppXp else AppNavy, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$rank",
                color = if (podium) AppNavy else AppGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(12.dp))

        Box(
            modifier = Modifier.size(36.dp).background(AppBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                entry.displayName.firstOrNull()?.uppercase() ?: "?",
                color = AppOnBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
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
                Box(
                    modifier = Modifier
                        .background(AppCyan, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text("YOU", color = AppNavy, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        Icon(XpBolt, contentDescription = null, tint = AppXp, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text("${entry.xp}", color = AppWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
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
