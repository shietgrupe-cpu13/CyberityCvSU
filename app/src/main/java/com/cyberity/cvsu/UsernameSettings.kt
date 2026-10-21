package com.cyberity.cvsu

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

@Composable
fun UsernameSettings(uid: String, currentName: String, enabled: Boolean, onBusy: (Boolean) -> Unit, onSaved: () -> Unit) {
    val context = LocalContext.current
    var name by remember(currentName) { mutableStateOf(currentName) }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    OutlinedTextField(value = name, onValueChange = { name = it; message = null },
        label = { Text("Username") }, singleLine = true, enabled = enabled && !saving,
        supportingText = { Text("After changing your username, wait 30 days before changing it again.") },
        modifier = Modifier.fillMaxWidth())
    Button(enabled = enabled && !saving && name.trim() != currentName && StudentIdRules.validateDisplayName(name) == null,
        modifier = Modifier.fillMaxWidth(), onClick = {
            saving = true; onBusy(true); message = null
            UserProfileRepository.changeUsername(uid, name)
                .addOnSuccessListener { saved ->
                    if (FirebaseAuth.getInstance().currentUser?.uid == uid) {
                        FirebaseAuth.getInstance().currentUser?.updateProfile(userProfileChangeRequest { displayName = saved })
                        ProfileCache.saveDetails(context, uid, saved, ProfileCache.loadStudentId(context, uid).orEmpty())
                        LeaderboardRepository.publish(uid)
                        message = "Username changed. You can change it again in 30 days."
                        onSaved()
                    }
                    saving = false; onBusy(false)
                }.addOnFailureListener { error ->
                    saving = false; onBusy(false)
                    val detail = error.cause?.message.orEmpty()
                    message = if (detail.startsWith("You can change") || detail.startsWith("This username") || detail.startsWith("Choose a different")) detail
                        else "Couldn't change your username. Check your connection and make sure 30 days have passed since your last change."
                }
        }) { Text(if (saving) "Saving…" else "Change username") }
    message?.let { Text(it, color = AppCyan) }
}
