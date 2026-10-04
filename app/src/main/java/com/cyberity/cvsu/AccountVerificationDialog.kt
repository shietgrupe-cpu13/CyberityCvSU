package com.cyberity.cvsu

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import com.google.firebase.auth.MultiFactorResolver

@Composable
internal fun AccountVerificationDialog(title: String, description: String, onVerified: () -> Unit,
    onCancel: () -> Unit, onBusyChanged: (Boolean) -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    val expectedUid = remember { auth.currentUser?.uid }
    val guard = AuthAttemptGuard.get(LocalContext.current)
    val retry = rememberRetrySeconds(guard)
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var resolver by remember { mutableStateOf<MultiFactorResolver?>(null) }
    SideEffect { onBusyChanged(busy) }
    DisposableEffect(Unit) { onDispose { onBusyChanged(false) } }

    fun finish() {
        val user = auth.currentUser
        if (user == null || user.uid != expectedUid) {
            busy = false; guard.finished(); error = "Your account changed. Cancel and sign in again."
            return
        }
        user.getIdToken(true).addOnSuccessListener {
            if (auth.currentUser?.uid == expectedUid) {
                guard.authenticated(); password = ""; busy = false; onVerified()
            } else { busy = false; guard.finished(); error = "Please sign in again." }
        }.addOnFailureListener { busy = false; guard.finished(); error = "Couldn't refresh your sign-in. Try again." }
    }

    if (resolver == null) AlertDialog(
        onDismissRequest = { if (!busy) { password = ""; onCancel() } },
        properties = DialogProperties(securePolicy = SecureFlagPolicy.SecureOn),
        title = { Text(title) },
        text = { Column {
            Text(description)
            OutlinedTextField(value = password, onValueChange = { password = it; error = null },
                enabled = !busy, label = { Text("Current password") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation())
            error?.let { Text(it, color = AppDanger) }
            if (retry > 0) Text("Try again in ${retry}s.")
            if (busy) CircularProgressIndicator()
        } },
        confirmButton = { TextButton(enabled = password.isNotBlank() && !busy && retry == 0L, onClick = {
            val user = auth.currentUser
            val email = user?.email
            if (user == null || email == null || user.uid != expectedUid) error = "Please sign in again."
            else if (guard.tryStart()) {
                busy = true; error = null
                val credential = EmailAuthProvider.getCredential(email, password)
                password = ""
                user.reauthenticate(credential).addOnSuccessListener { finish() }.addOnFailureListener {
                    if (it is FirebaseAuthMultiFactorException) { guard.finished(); resolver = it.resolver }
                    else { busy = false; error = guard.handleFailure(it) }
                }
            }
        }) { Text("Verify and continue") } },
        dismissButton = { TextButton(enabled = !busy, onClick = { password = ""; onCancel() }) { Text("Cancel") } }
    )
    resolver?.let { challenge ->
        Dialog(onDismissRequest = {}, properties = DialogProperties(usePlatformDefaultWidth = false,
            securePolicy = SecureFlagPolicy.SecureOn)) {
            TotpSignInScreen(resolver = challenge, onSuccess = { resolver = null; finish() },
                onCancel = { resolver = null; busy = false; onCancel() })
        }
    }
}
