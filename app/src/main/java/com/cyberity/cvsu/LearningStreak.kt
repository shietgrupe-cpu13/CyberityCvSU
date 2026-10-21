package com.cyberity.cvsu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.delay

internal data class LearningStreak(val days: Int = 0, val activeToday: Boolean = false)

@Composable
internal fun rememberLearningStreak(uid: String?): LearningStreak {
    var count by remember(uid) { mutableIntStateOf(0) }
    var lastAt by remember(uid) { mutableStateOf<Long?>(null) }
    var now by remember(uid) { mutableStateOf(ServerClock.trustedNow()) }
    DisposableEffect(uid) {
        val listener = uid?.let { id ->
            FirebaseFirestore.getInstance().collection("userProgress").document(id)
                .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, _ ->
                    if (snapshot != null && !snapshot.metadata.hasPendingWrites() &&
                        FirebaseAuth.getInstance().currentUser?.uid == id) {
                        count = (snapshot.getLong("streakDays") ?: 0).toInt()
                        lastAt = snapshot.getTimestamp("streakLastActivityAt")?.toDate()?.time
                        now = ServerClock.trustedNow()
                    }
                }
        }
        onDispose { listener?.remove() }
    }
    LaunchedEffect(uid) {
        if (uid != null && ServerClock.trustedNow() == null) {
            ServerClock.sync(uid) { now = ServerClock.trustedNow() }
        }
        while (true) { now = ServerClock.trustedNow(); delay(30_000) }
    }
    val trustedAt = now ?: return LearningStreak()
    return LearningStreak(StreakPolicy.visible(count, lastAt, trustedAt),
        count > 0 && lastAt?.let { StreakPolicy.day(it) == StreakPolicy.day(trustedAt) } == true)
}

@Composable
internal fun StreakIndicator(days: Int, activeToday: Boolean) {
    var showDetails by remember { mutableStateOf(false) }
    val tint = if (activeToday) AppStreak else AppGray
    Row(Modifier.clickable { showDetails = true }, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(Icons.Filled.LocalFireDepartment,
            "$days day learning streak. " + if (activeToday) "Today's goal completed" else "Complete a level today",
            tint = tint, modifier = Modifier.size(24.dp))
        Text(days.toString(), color = tint, fontWeight = FontWeight.Bold)
    }
    if (showDetails) AlertDialog(onDismissRequest = { showDetails = false },
        title = { Text("Your learning streak") },
        text = { Text("$days day streak\n\n" +
            if (activeToday) "Your flame is lit! Come back tomorrow and complete a level to keep it going.\n\nDays reset at midnight, Philippine time."
            else "Complete a level or quiz today to light your flame. Replays count; the optional walkthrough does not. Missing a full day resets your streak.\n\nDays reset at midnight, Philippine time.") },
        confirmButton = { TextButton(onClick = { showDetails = false }) { Text("Got it") } })
}
