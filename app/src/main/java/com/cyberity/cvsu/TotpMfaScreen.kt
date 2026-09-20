package com.cyberity.cvsu

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
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
                        if (isEnabled) Color(0xFF00C853).copy(alpha = 0.15f)
                        else AppCyan.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Security,
                    contentDescription = null,
                    tint = if (isEnabled) Color(0xFF00C853) else AppCyan,
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
                    tint = Color(0xFF00C853),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    if (showEnrollment) TotpEnrollmentScreen(onDone = { showEnrollment = false })
}

@Composable
private fun TotpEnrollmentScreen(onDone: () -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    var secret by remember { mutableStateOf<TotpSecret?>(null) }
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!loading) onDone() },
        title = { Text("Set up authenticator app") },
        text = {
            Column {
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
                    Button(onClick = { currentSecret.openInOtpApp(qrCodeUri) }) { Text("Open authenticator app") }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.filter(Char::isDigit).take(6) },
                        label = { Text("6-digit code") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                error?.let { Text(it, color = androidx.compose.ui.graphics.Color.Red, fontSize = 13.sp) }
            }
        },
        confirmButton = {
            if (loading) CircularProgressIndicator(modifier = Modifier.padding(12.dp))
            else if (secret == null) Button(onClick = {
                loading = true; error = null
                auth.currentUser?.multiFactor?.session
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
                auth.currentUser?.multiFactor?.enroll(assertion, "Google Authenticator")
                    ?.addOnSuccessListener { onDone() }
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
    val factor = resolver?.hints?.firstOrNull { it.factorId == TotpMultiFactorGenerator.FACTOR_ID }
    Column(Modifier.fillMaxSize().background(AppNavy).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(96.dp)); Icon(Icons.Filled.Security, null, tint = AppCyan)
        Spacer(Modifier.height(16.dp)); Text("Verify it’s you", color = AppWhite, fontSize = 26.sp)
        Spacer(Modifier.height(8.dp)); Text("Enter the current 6-digit code from your authenticator app.", color = AppGray, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = code, onValueChange = { code = it.filter(Char::isDigit).take(6) }, label = { Text("6-digit code") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        error?.let { Text(it, color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.padding(top = 12.dp)) }
        Spacer(Modifier.height(20.dp))
        if (loading) CircularProgressIndicator(color = AppCyan) else Button(
            enabled = code.length == 6 && factor != null,
            onClick = {
                loading = true
                resolver!!.resolveSignIn(TotpMultiFactorGenerator.getAssertionForSignIn(factor!!.uid, code))
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { loading = false; error = it.localizedMessage ?: "That code could not be verified. Try the current code." }
            }, colors = ButtonDefaults.buttonColors(containerColor = AppBlue), modifier = Modifier.fillMaxWidth()
        ) { Text("Verify") }
        TextButton(onClick = onCancel, enabled = !loading) { Text("Cancel", color = AppCyan) }
    }
}
