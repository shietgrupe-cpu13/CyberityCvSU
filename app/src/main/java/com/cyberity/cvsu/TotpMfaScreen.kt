package com.cyberity.cvsu

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.MultiFactorResolver
import com.google.firebase.auth.TotpMultiFactorGenerator
import com.google.firebase.auth.TotpSecret
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

private const val MFA_ISSUER = "Cyberity CvSU"

fun createTotpQrBitmap(uri: String, size: Int = 512): Bitmap {
    val matrix = QRCodeWriter().encode(uri, BarcodeFormat.QR_CODE, size, size)
    return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).apply {
        for (x in 0 until size) for (y in 0 until size) {
            setPixel(x, y, if (matrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
}

@Composable
fun TotpMfaCard() {
    val auth = remember { FirebaseAuth.getInstance() }
    val isEnabled = auth.currentUser?.multiFactor?.enrolledFactors?.any {
        it.factorId == TotpMultiFactorGenerator.FACTOR_ID
    } == true
    var showEnrollment by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isEnabled) AppSuccess.copy(alpha = 0.15f)
                        else AppCyan.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Security,
                    contentDescription = null,
                    tint = if (isEnabled) AppSuccess else AppCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Authenticator app",
                    color = AppWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (isEnabled) "Enhanced security is active"
                    else "Add an extra layer of protection",
                    color = AppGray,
                    fontSize = 13.sp
                )
            }

            if (!isEnabled) {
                Button(
                    onClick = { showEnrollment = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Setup", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Enabled",
                    tint = AppSuccess,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    if (showEnrollment) TotpEnrollmentScreen(onDone = { showEnrollment = false })
}

@Composable
internal fun TotpEnrollmentScreen(onDone: () -> Unit, onEnrolled: () -> Unit = onDone,
    factorName: String = "Authenticator", onBusyChanged: (Boolean) -> Unit = {}) {
    val auth = remember { FirebaseAuth.getInstance() }
    var secret by remember { mutableStateOf<TotpSecret?>(null) }
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    androidx.compose.runtime.SideEffect { onBusyChanged(loading) }
    androidx.compose.runtime.DisposableEffect(Unit) { onDispose { onBusyChanged(false) } }

    AlertDialog(
        onDismissRequest = { if (!loading) onDone() },
        properties = androidx.compose.ui.window.DialogProperties(securePolicy = androidx.compose.ui.window.SecureFlagPolicy.SecureOn),
        title = { Text("Set up authenticator app") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                val currentSecret = secret
                if (currentSecret == null) {
                    Text("Generate a secure key for your authenticator app.")
                } else {
                    val qrCodeUri = remember(currentSecret) {
                        currentSecret.generateQrCodeUrl(auth.currentUser?.email ?: "CvSU account", MFA_ISSUER)
                    }
                    val qrBitmap = remember(qrCodeUri) { createTotpQrBitmap(qrCodeUri) }
                    Text("Scan this code with Google Authenticator, or enter the setup key manually.")
                    Spacer(Modifier.height(12.dp))
                    Image(qrBitmap.asImageBitmap(), "Authenticator setup QR code", Modifier.fillMaxWidth().height(220.dp))
                    Spacer(Modifier.height(12.dp))
                    SelectionContainer { Text(currentSecret.sharedSecretKey, fontSize = 16.sp, color = AppBlue) }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        try { currentSecret.openInOtpApp(qrCodeUri) }
                        catch (_: android.content.ActivityNotFoundException) { error = "Install an authenticator app, or scan the QR code on another device." }
                    }) { Text("Open authenticator app") }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.filter(Char::isDigit).take(6) },
                        label = { Text("6-digit code") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                error?.let { Text(it, color = AppDanger, fontSize = 13.sp) }
            }
        },
        confirmButton = {
            if (loading) CircularProgressIndicator(modifier = Modifier.padding(12.dp))
            else if (secret == null) Button(onClick = {
                loading = true; error = null
                val user = auth.currentUser
                if (user == null) { loading = false; error = "Please sign in again."; return@Button }
                user.multiFactor.session
                    ?.addOnSuccessListener { session ->
                        TotpMultiFactorGenerator.generateSecret(session)
                            .addOnSuccessListener { secret = it; loading = false }
                            .addOnFailureListener { loading = false; error = it.localizedMessage ?: "Could not generate a setup key. Sign in again and retry." }
                    }
                    ?.addOnFailureListener { loading = false; error = it.localizedMessage ?: "Could not start two-factor setup." }
            }) { Text("Generate key") }
            else Button(enabled = code.length == 6, onClick = {
                loading = true
                val assertion = TotpMultiFactorGenerator.getAssertionForEnrollment(secret!!, code)
                val user = auth.currentUser
                if (user == null) { loading = false; error = "Please sign in again."; return@Button }
                user.multiFactor.enroll(assertion, factorName)
                    .addOnSuccessListener { onEnrolled() }
                    ?.addOnFailureListener { loading = false; error = it.localizedMessage ?: "That code could not be verified. Try the current code." }
            }) { Text("Verify and enable") }
        },
        dismissButton = { TextButton(onClick = onDone, enabled = !loading) { Text("Cancel") } }
    )
}

@Composable
fun TotpSignInScreen(resolver: MultiFactorResolver?, onSuccess: () -> Unit, onCancel: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val factors = resolver?.hints.orEmpty().filter { it.factorId == TotpMultiFactorGenerator.FACTOR_ID }
    var selectedFactor by remember(resolver) { mutableStateOf(factors.firstOrNull()?.uid) }
    val factor = factors.firstOrNull { it.uid == selectedFactor }
    val context = LocalContext.current
    val guard = remember { AuthAttemptGuard.get(context) }
    val retrySeconds = rememberRetrySeconds(guard)
    BackHandler { if (!loading) onCancel() }
    Column(Modifier.fillMaxSize().background(AppNavy).systemBarsPadding().imePadding().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(96.dp)); Icon(Icons.Filled.Security, null, tint = AppCyan)
        Spacer(Modifier.height(16.dp)); Text("Verify it’s you", color = AppWhite, fontSize = 26.sp)
        Spacer(Modifier.height(8.dp)); Text("Enter the current 6-digit code from your authenticator app.", color = AppGray, textAlign = TextAlign.Center)
        if (factors.size > 1) {
            Text("Choose an authenticator", color = AppCyan, modifier = Modifier.padding(top = 16.dp))
            factors.forEachIndexed { index, item ->
                androidx.compose.material3.FilterChip(selected = item.uid == selectedFactor, enabled = !loading,
                    onClick = { selectedFactor = item.uid; code = ""; error = null },
                    label = { Text("${item.displayName ?: "Authenticator"} (${index + 1})") })
            }
        }
        Spacer(Modifier.height(24.dp))
        TotpCodeInput(
            code = code,
            onCodeChange = { code = it; error = null },
            enabled = !loading,
            hasError = error != null
        )
        error?.let { Text(it, color = AppDanger, modifier = Modifier.padding(top = 12.dp)) }
        if (retrySeconds > 1 && !loading) {
            Text("Please wait ${retrySeconds}s before trying again.", color = AppGray, modifier = Modifier.padding(top = 12.dp))
        }
        Spacer(Modifier.height(20.dp))
        if (loading) CircularProgressIndicator(color = AppCyan) else Button(
            enabled = code.length == 6 && factor != null && retrySeconds == 0L,
            onClick = {
                if (!guard.tryStart()) {
                    error = "Please wait before trying again."
                    return@Button
                }
                loading = true
                error = null
                resolver!!.resolveSignIn(TotpMultiFactorGenerator.getAssertionForSignIn(factor!!.uid, code))
                    .addOnSuccessListener { guard.authenticated(); loading = false; onSuccess() }
                    .addOnFailureListener { loading = false; error = guard.handleFailure(it, secondFactor = true) }
            }, colors = ButtonDefaults.buttonColors(containerColor = AppBlue), modifier = Modifier.fillMaxWidth()
        ) { Text("Verify") }
        TextButton(onClick = onCancel, enabled = !loading) { Text("Cancel", color = AppCyan) }
    }
}

/** One editable field keeps paste, deletion, and accessibility working across all six boxes. */
@Composable
private fun TotpCodeInput(
    code: String,
    onCodeChange: (String) -> Unit,
    enabled: Boolean,
    hasError: Boolean
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var focused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    BasicTextField(
        value = code,
        onValueChange = { onCodeChange(it.filter { digit -> digit in '0'..'9' }.take(6)) },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        textStyle = TextStyle(color = Color.Transparent),
        cursorBrush = SolidColor(Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.isFocused }
            .semantics { contentDescription = "6-digit authenticator code" },
        decorationBox = { innerTextField ->
            Box {
                // Keep the actual editor present for keyboard and accessibility input.
                innerTextField()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(6) { index ->
                        val active = focused && index == code.length.coerceAtMost(5)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .background(AppCard, RoundedCornerShape(10.dp))
                                .border(
                                    if (active) 2.dp else 1.dp,
                                    when {
                                        hasError -> AppDanger
                                        active -> AppCyan
                                        else -> AppGray.copy(alpha = 0.45f)
                                    },
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = code.getOrNull(index)?.toString() ?: "",
                                color = AppWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    )
}
