package com.cyberity.cvsu

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
            "Privacy access was denied. Sign in again or update the Firestore rules for Privacy."
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
    SettingsIntro("Your information", "Control your public profile and download or delete your account data.")
    Text("Your account stores your email, student ID and display name, lesson progress, XP and hearts. The leaderboard shares your display name and XP with signed-in students. Device management stores model, Android version and sign-in/activity times; it does not collect GPS location. Passwords and authenticator secrets are not included in data exports.", color = AppGray)
    if (preferences?.deleting == true || deletionFailed) {
        Text("Account deletion is unfinished. Some records may already be removed. Verify again to retry cleanup and finish deleting your sign-in account. Learning and leaderboard updates are blocked during deletion.", color = AppDanger)
    } else {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Show me on the leaderboard", color = AppWhite)
                Text("Hiding removes your public entry. Your learning progress stays private and saved.", color = AppGray)
            }
            Switch(checked = preferences?.leaderboardVisible ?: false, enabled = preferences != null && !busy,
                onCheckedChange = { visible ->
                    if (uid != null) scope.launch {
                        busy = true; message = null
                        try {
                            PrivacyPreferencesRepository.setLeaderboardVisible(uid, visible)
                            preferences = PrivacyPreferences(visible, false)
                            message = if (visible) "Leaderboard visibility enabled." else "Your leaderboard entry is hidden."
                        } catch (error: Exception) {
                            if (error is CancellationException) throw error
                            message = errorText(error)
                        } finally { busy = false }
                    }
                })
        }
        OutlinedButton(enabled = !busy && preferences != null, modifier = Modifier.fillMaxWidth(), onClick = {
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
        }) { Text("Download your data (JSON)") }
        Text("The export includes your profile, progress, leaderboard entry, student ID claims, device records and privacy preferences. It requires an internet connection.", color = AppGray)
    }
    OutlinedButton(enabled = !busy && uid != null && preferences != null, modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppDanger), onClick = { verifying = true }) {
        Text(if (preferences?.deleting == true || deletionFailed) "Finish account deletion" else "Delete account")
    }
    Text("Deletion permanently removes your profile, progress, leaderboard entry, student ID claims, tracked device records and Firebase sign-in account. Small access-blocking records remain to prevent old sessions accessing data. Files you already exported remain wherever you saved them.", color = AppGray)
    if (busy) CircularProgressIndicator()
    message?.let { Text(it, color = AppCyan) }
    if (preferences == null && !busy) TextButton(onClick = { refresh() }) { Text("Retry loading Privacy") }
    Text("Privacy policy", color = AppWhite)
    Text("An approved institutional privacy policy has not been supplied in this app. The information above describes the current features; it is not a legal policy.", color = AppGray)

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
