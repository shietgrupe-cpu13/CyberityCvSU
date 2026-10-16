package com.cyberity.cvsu

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PhonelinkLock
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.MultiFactorInfo
import com.google.firebase.auth.TotpMultiFactorGenerator
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date

private enum class SecurityAction { CHANGE_PASSWORD, ADD_AUTHENTICATOR, REMOVE_AUTHENTICATOR, LOGOUT_ALL }

@Composable
internal fun AccountSecurityDialog(onDismiss: () -> Unit, onSignOut: () -> Unit, onClose: () -> Unit = onDismiss,
    onBusyChanged: (Boolean) -> Unit = {}) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val accountUid = remember { auth.currentUser?.uid }
    var factors by remember { mutableStateOf(auth.currentUser?.multiFactor?.enrolledFactors.orEmpty()) }
    val totpFactors = factors.filter { it.factorId == TotpMultiFactorGenerator.FACTOR_ID }
    var sessions by remember { mutableStateOf<List<AccountSession>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var slow by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var devicesError by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    var action by remember { mutableStateOf<SecurityAction?>(null) }
    var removeFactor by remember { mutableStateOf<MultiFactorInfo?>(null) }
    var showEnrollment by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var operationBusy by remember { mutableStateOf(false) }
    var verificationBusy by remember { mutableStateOf(false) }
    var enrollmentBusy by remember { mutableStateOf(false) }
    val busy = operationBusy || verificationBusy || enrollmentBusy
    val signedIn = auth.currentUser?.uid == accountUid && accountUid != null
    SideEffect { onBusyChanged(busy) }
    DisposableEffect(Unit) { onDispose { onBusyChanged(false) } }

    fun reloadFactors() {
        val user = auth.currentUser ?: return
        user.reload().addOnSuccessListener {
            factors = auth.currentUser?.multiFactor?.enrolledFactors.orEmpty()
        }.addOnFailureListener { message = "Couldn't refresh authenticator status. Check your connection." }
    }
    fun finishSignOut(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        AccountSessions.clear(context)
        auth.signOut()
        onSignOut()
    }
    fun start(value: SecurityAction) { message = null; action = value }

    LaunchedEffect(refresh) {
        val request = refresh
        loading = true; slow = false; devicesError = null
        reloadFactors()
        AccountSessions.touch(context, onSuccess = {
            AccountSessions.list(context, onResult = {
                if (request == refresh) { sessions = it; loading = false }
            }, onError = {
                if (request == refresh) { devicesError = AccountSessions.message(it); loading = false }
            })
        }, onError = {
            if (request == refresh) { devicesError = AccountSessions.message(it); loading = false }
        })
        delay(8_000)
        if (loading) slow = true
    }

    SettingsPage(title = "Security", onBack = onDismiss, onClose = onClose, canDismiss = !busy) {
        SettingsCard {
            val verified = auth.currentUser?.isEmailVerified == true
            SecurityStatus(if (verified) Icons.Default.Verified else Icons.Default.ErrorOutline,
                if (verified) "Email verified" else "Email not verified", ok = verified)
            SecurityStatus(if (totpFactors.isEmpty()) Icons.Default.GppMaybe else Icons.Default.VerifiedUser,
                if (totpFactors.isEmpty()) "Authenticator off" else "Authenticator on · ${totpFactors.size} " +
                    if (totpFactors.size == 1) "app" else "apps", ok = totpFactors.isNotEmpty())
        }

        SettingsLabel("Password")
        SettingsActionCard(Icons.Default.Key, "Change password", "You'll sign in again afterwards",
            enabled = signedIn && !busy) { start(SecurityAction.CHANGE_PASSWORD) }

        SettingsLabel("Authenticator")
        SettingsCard {
            if (totpFactors.isEmpty()) Text("Add a code from an authenticator app after your password.", color = AppGray, fontSize = 13.sp)
            totpFactors.forEachIndexed { index, factor ->
                if (index > 0) HorizontalDivider(color = AppBorder)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.PhonelinkLock, contentDescription = null, tint = AppCyan)
                    Column(Modifier.weight(1f)) {
                        Text("${factor.displayName ?: "Authenticator"} (${index + 1})", color = AppWhite, fontWeight = FontWeight.Medium)
                        Text("Added ${DateFormat.getDateInstance().format(Date(factor.enrollmentTimestamp))}", color = AppGray, fontSize = 13.sp)
                    }
                    TextButton(enabled = !busy, onClick = { removeFactor = factor; start(SecurityAction.REMOVE_AUTHENTICATOR) }) {
                        Text("Remove", color = AppDanger)
                    }
                }
            }
            OutlinedButton(enabled = signedIn && !busy && factors.size < 5, onClick = { start(SecurityAction.ADD_AUTHENTICATOR) },
                modifier = Modifier.fillMaxWidth()) { Text(if (totpFactors.isEmpty()) "Set up authenticator" else "Add backup authenticator") }
            if (factors.size >= 5) Text("You've reached the limit. Remove one before adding another.", color = AppGray, fontSize = 13.sp)
        }
        SettingsNote("Keep a backup authenticator. If you lose every code, ask your project administrator.")

        SettingsLabel("Signed-in devices")
        SettingsCard {
            if (loading) CircularProgressIndicator(color = AppCyan)
            if (slow && loading) Text("Still loading. Check your connection.", color = AppGray, fontSize = 13.sp)
            devicesError?.let {
                Text(it, color = AppDanger, fontSize = 13.sp)
                TextButton(enabled = !busy, onClick = { refresh++ }) { Text("Try again") }
            }
            sessions.forEachIndexed { index, session ->
                if (index > 0) HorizontalDivider(color = AppBorder)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Smartphone, contentDescription = null, tint = AppGray)
                    Column(Modifier.weight(1f)) {
                        Text(session.device, color = AppWhite, fontWeight = FontWeight.Medium)
                        Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(session.lastActive)),
                            color = AppGray, fontSize = 13.sp)
                    }
                    if (session.current) Text("This device", color = AppCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            if (!loading && sessions.isEmpty() && devicesError == null) Text("No devices listed yet.", color = AppGray, fontSize = 13.sp)
        }
        // Global cutoff protects old, unlisted devices too; do not gate this on list availability.
        Button(enabled = signedIn && !busy, onClick = { start(SecurityAction.LOGOUT_ALL) }, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AppDanger, contentColor = AppOnBlue)) { Text("Sign out all devices") }
        SettingsNote("Includes this device. Offline devices are signed out when they reconnect.")
        message?.let { Text(it, color = AppCyan) }
    }

    action?.let { pending ->
        val title = when (pending) {
            SecurityAction.CHANGE_PASSWORD -> "Change password"
            SecurityAction.ADD_AUTHENTICATOR -> "Add authenticator"
            SecurityAction.REMOVE_AUTHENTICATOR -> "Remove authenticator?"
            SecurityAction.LOGOUT_ALL -> "Sign out all devices?"
        }
        val description = when (pending) {
            SecurityAction.REMOVE_AUTHENTICATOR -> "Confirm your password and authenticator code to remove ${removeFactor?.displayName ?: "this authenticator"}. ${if (totpFactors.size == 1) "This turns off authenticator protection." else "Keep access to another enrolled authenticator."} You may need to sign in again afterward."
            SecurityAction.LOGOUT_ALL -> "Confirm your password and authenticator code, when enabled. All devices, including this one, will need to sign in again."
            else -> "Confirm your current password and authenticator code, when enabled, before continuing."
        }
        AccountVerificationDialog(title, description, onBusyChanged = { verificationBusy = it },
            onCancel = { action = null; removeFactor = null }, onVerified = {
                action = null
                if (auth.currentUser?.uid != accountUid) message = "Your account changed. Sign in again."
                else when (pending) {
                    SecurityAction.CHANGE_PASSWORD -> showPassword = true
                    SecurityAction.ADD_AUTHENTICATOR -> showEnrollment = true
                    SecurityAction.LOGOUT_ALL -> {
                        operationBusy = true
                        AccountSessions.revokeAll(onSuccess = { operationBusy = false; finishSignOut("All devices signed out. Sign in again to continue.") },
                            onError = { operationBusy = false; message = AccountSessions.message(it) })
                    }
                    SecurityAction.REMOVE_AUTHENTICATOR -> {
                        val factor = removeFactor
                        removeFactor = null
                        if (factor != null) {
                            operationBusy = true
                            auth.currentUser!!.multiFactor.unenroll(factor.uid).addOnSuccessListener {
                                operationBusy = false
                                factors = auth.currentUser?.multiFactor?.enrolledFactors.orEmpty()
                                if (auth.currentUser == null) onSignOut()
                                else { message = "Authenticator removed."; refresh++ }
                            }.addOnFailureListener {
                                operationBusy = false
                                if (it is FirebaseAuthException && it.errorCode == "ERROR_USER_TOKEN_EXPIRED")
                                    finishSignOut("Sign in again to check your authenticator settings.")
                                else message = "Couldn't remove the authenticator. Sign in again and retry."
                            }
                        }
                    }
                }
            })
    }
    if (showEnrollment) TotpEnrollmentScreen(onDone = { showEnrollment = false },
        factorName = if (totpFactors.isEmpty()) "Authenticator" else "Backup authenticator",
        onBusyChanged = { enrollmentBusy = it }, onEnrolled = {
            showEnrollment = false; factors = auth.currentUser?.multiFactor?.enrolledFactors.orEmpty()
            message = "Authenticator added. Keep its backup somewhere safe."; refresh++
        })
    if (showPassword) NewPasswordDialog(onCancel = { showPassword = false }, onBusyChanged = { operationBusy = it },
        expectedUid = accountUid, onChanged = { showPassword = false; finishSignOut("Password changed. Sign in with your new password.") })
}

@Composable
private fun SecurityStatus(icon: ImageVector, text: String, ok: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, contentDescription = null, tint = if (ok) AppSuccess else AppDanger, modifier = Modifier.size(20.dp))
        Text(text, color = AppWhite)
    }
}

@Composable
private fun NewPasswordDialog(expectedUid: String?, onCancel: () -> Unit, onChanged: () -> Unit,
    onBusyChanged: (Boolean) -> Unit) {
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    SideEffect { onBusyChanged(busy) }
    DisposableEffect(Unit) { onDispose { onBusyChanged(false) } }
    AlertDialog(onDismissRequest = { if (!busy) onCancel() },
        properties = DialogProperties(securePolicy = SecureFlagPolicy.SecureOn),
        title = { Text("Choose a new password") }, text = { Column {
            Text("Use a unique password with at least 12 characters. You'll sign in again after changing it.")
            OutlinedTextField(value = password, onValueChange = { password = it; error = null }, singleLine = true,
                enabled = !busy, label = { Text("New password") }, visualTransformation = PasswordVisualTransformation())
            OutlinedTextField(value = confirmation, onValueChange = { confirmation = it; error = null }, singleLine = true,
                enabled = !busy, label = { Text("Confirm new password") }, visualTransformation = PasswordVisualTransformation())
            error?.let { Text(it, color = AppDanger) }
            if (busy) CircularProgressIndicator()
        } }, confirmButton = { TextButton(enabled = !busy && password.length >= 12 && password.isNotBlank() && password == confirmation, onClick = {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null || user.uid != expectedUid) error = "Please sign in again."
            else {
                busy = true
                val replacement = password
                password = ""; confirmation = ""
                user.updatePassword(replacement).addOnSuccessListener { busy = false; onChanged() }.addOnFailureListener {
                    busy = false; error = if (it is FirebaseAuthWeakPasswordException) "That password doesn't meet the account policy. Choose a stronger password."
                    else "Couldn't change your password. Cancel, verify your account again, and retry."
                }
            }
        }) { Text("Change password") } },
        dismissButton = { TextButton(enabled = !busy, onClick = onCancel) { Text("Cancel") } })
}
