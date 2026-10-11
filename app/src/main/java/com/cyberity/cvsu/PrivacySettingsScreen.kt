package com.cyberity.cvsu

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun PrivacySettingsScreen(onSignOut: () -> Unit, onBusyChanged: (Boolean) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uid = remember { FirebaseAuth.getInstance().currentUser?.uid }
    var preferences by remember { mutableStateOf<PrivacyPreferences?>(null) }
    var busy by remember { mutableStateOf(false) }
    var verifying by remember { mutableStateOf(false) }
    var confirmation by remember { mutableStateOf(false) }
    var deletionFailed by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var pendingExport by remember { mutableStateOf<String?>(null) }
    SideEffect { onBusyChanged(busy || verifying || confirmation) }
    DisposableEffect(Unit) { onDispose { onBusyChanged(false) } }

    fun errorText(error: Exception): String = when {
        error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "You don't have access right now. Sign out, sign in again, then retry."
        else -> "Couldn't complete this action. Check your connection and try again."
    }

    fun refresh() {
        if (uid == null) { message = "Please sign in again."; return }
        scope.launch {
            busy = true
            try { preferences = PrivacyPreferencesRepository.load(uid); message = null }
            catch (error: Exception) { if (error is CancellationException) throw error; message = errorText(error) }
            finally { busy = false }
        }
    }

    val saveExport = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val data = pendingExport
        pendingExport = null
        if (uri == null || data == null) { busy = false; message = "Export cancelled." }
        else scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val stream = context.contentResolver.openOutputStream(uri, "wt")
                        ?: error("Couldn't open the selected file.")
                    stream.bufferedWriter(Charsets.UTF_8).use { it.write(data) }
                }
                message = "Your data was saved. Keep this file private."
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                message = "Couldn't save the file. Choose another location and try again."
            } finally { busy = false }
        }
    }

    LaunchedEffect(uid) { refresh() }
    val unfinishedDeletion = preferences?.deleting == true || deletionFailed
    if (!unfinishedDeletion) {
        SettingsCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Show me on the leaderboard", color = AppWhite, fontWeight = FontWeight.SemiBold)
                    Text("Shares your display name and XP with other students.", color = AppGray, fontSize = 13.sp)
                }
                Switch(checked = preferences?.leaderboardVisible ?: false, enabled = preferences != null && !busy,
                    onCheckedChange = { visible ->
                        if (uid != null) scope.launch {
                            busy = true; message = null
                            try {
                                PrivacyPreferencesRepository.setLeaderboardVisible(uid, visible)
                                preferences = PrivacyPreferences(visible, false)
                                message = if (visible) "You're on the leaderboard." else "Your leaderboard entry is hidden."
                            } catch (error: Exception) {
                                if (error is CancellationException) throw error
                                message = errorText(error)
                            } finally { busy = false }
                        }
                    })
            }
        }
        SettingsActionCard(Icons.Default.Download, "Download your data", "Profile, progress and devices as a JSON file",
            enabled = !busy && preferences != null) {
            if (uid != null) scope.launch {
                busy = true; message = "Preparing your data…"
                try {
                    pendingExport = PrivacyAccountData.export(uid)
                    saveExport.launch("cyberity-account-data.json")
                } catch (error: Exception) {
                    if (error is CancellationException) throw error
                    pendingExport = null; busy = false; message = errorText(error)
                }
            }
        }
        SettingsCard {
            Text("What we store", color = AppWhite, fontWeight = FontWeight.SemiBold)
            Text("Email, student ID, display name, progress, XP and hearts, and the model of devices you sign in on. No location. Passwords are never exported.",
                color = AppGray, fontSize = 13.sp)
        }
    }
    if (busy) CircularProgressIndicator(color = AppCyan)
    message?.let { Text(it, color = AppCyan) }
    if (preferences == null && !busy) TextButton(onClick = { refresh() }) { Text("Try again") }

    Surface(color = AppCard, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, AppDanger)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (unfinishedDeletion) "Finish deleting your account" else "Delete account",
                color = AppDanger, fontWeight = FontWeight.SemiBold)
            Text(if (unfinishedDeletion) "Deletion didn't finish and some data may already be gone. Verify again to finish. Learning is paused until then."
                else "Permanently removes your profile, progress, leaderboard entry and sign-in. This can't be undone.",
                color = AppGray, fontSize = 13.sp)
            OutlinedButton(enabled = !busy && uid != null && preferences != null, modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, AppDanger),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppDanger), onClick = { verifying = true }) {
                Text(if (unfinishedDeletion) "Finish account deletion" else "Delete account")
            }
        }
    }

    if (verifying) AccountVerificationDialog("Verify account deletion", "Enter your current password and authenticator code if requested.",
        onVerified = { verifying = false; confirmation = true }, onCancel = { verifying = false }, onBusyChanged = {})
    if (confirmation) AlertDialog(onDismissRequest = { confirmation = false }, title = { Text("Permanently delete account?") },
        text = { Text("This cannot be undone. Your learning progress and student profile will be removed, and you will be signed out. Export your data first if you need a copy. If interrupted, sign in and return to Privacy to finish deletion.") },
        confirmButton = { TextButton(onClick = {
            confirmation = false
            if (uid != null) scope.launch {
                busy = true; message = "Deleting your account…"
                try {
                    PrivacyAccountData.delete(uid, beforeAuthDeletion = { runCatching {
                        context.getSharedPreferences("account_sessions", Context.MODE_PRIVATE).edit().remove(uid).apply()
                        context.getSharedPreferences("cyberity_profile_cache", Context.MODE_PRIVATE).edit().remove(uid).apply()
                        val progressCache = context.getSharedPreferences("cyberity_progress_cache", Context.MODE_PRIVATE)
                        val progressEditor = progressCache.edit()
                        progressCache.all.keys.filter { it == uid || it.startsWith("$uid:") }.forEach { progressEditor.remove(it) }
                        progressEditor.apply()
                        LearningReminders.save(context, LearningReminders.read(context).copy(enabled = false))
                    } })
                    FirebaseAuth.getInstance().signOut()
                    onSignOut()
                } catch (error: Exception) {
                    if (error is CancellationException) throw error
                    runCatching { preferences = PrivacyPreferencesRepository.load(uid) }
                    deletionFailed = preferences?.deleting == true
                    message = if (deletionFailed) "Deletion did not finish. Verify again and retry; already removed data cannot be restored. " + errorText(error)
                        else "Deletion could not start. " + errorText(error)
                } finally { busy = false }
            }
        }) { Text("Delete permanently", color = AppDanger) } },
        dismissButton = { TextButton(onClick = { confirmation = false }) { Text("Cancel") } })
}
