package com.cyberity.cvsu

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Content hosted in the existing settings window; no additional dialog or navigation stack. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun HelpSettingsScreen() {
    val context = LocalContext.current
    val version = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "Unknown"
    }
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    var category by rememberSaveable { mutableStateOf("Learning") }
    var details by rememberSaveable { mutableStateOf("") }
    var diagnostics by rememberSaveable { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val report = remember(category, details, diagnostics, version) {
        buildString {
            append("Cyberity CvSU problem report\nCategory: $category\n\n")
            append(details.trim())
            if (diagnostics) {
                append("\n\nBasic diagnostics\nApp version: $version")
                append("\nDevice: ${Build.MANUFACTURER} ${Build.MODEL}")
                append("\nAndroid: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            }
        }
    }
    SettingsIntro("Cyberity CvSU", "Practice cybersecurity through lessons, quizzes and hands-on labs.")
    Text("App version $version", color = AppCyan)
    Text("Help topics", color = AppWhite, fontWeight = FontWeight.SemiBold)
    helpTopics.forEach { (title, answer) ->
        Surface(onClick = { expanded = if (expanded == title) null else title },
            modifier = Modifier.fillMaxWidth(), color = AppCard, shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${if (expanded == title) "−" else "+"} $title", color = AppWhite,
                    fontWeight = FontWeight.Medium)
                if (expanded == title) Text(answer, color = AppGray)
            }
        }
    }
    HorizontalDivider(color = AppBorder)
    Text("Report a problem", color = AppWhite, fontWeight = FontWeight.SemiBold)
    Text("Describe what happened and how to reproduce it. Do not include passwords, authenticator codes, student IDs or other private information. Reports stay here until you copy or share them.", color = AppGray)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Learning", "Account", "Notifications", "Display", "Other").forEach { item ->
            FilterChip(selected = category == item, onClick = { category = item; message = null },
                label = { Text(item) })
        }
    }
    OutlinedTextField(value = details, onValueChange = { details = it.take(4000); message = null },
        label = { Text("What happened?") },
        placeholder = { Text("Steps, expected result, and what you saw") },
        modifier = Modifier.fillMaxWidth(), minLines = 4, maxLines = 8,
        supportingText = { Text("${details.length}/4000 characters") })
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = diagnostics, onCheckedChange = { diagnostics = it; message = null })
        Text("Include app version, device model and Android version", color = AppGray,
            modifier = Modifier.weight(1f))
    }
    if (details.isNotBlank()) {
        Text("Report preview", color = AppCyan)
        Surface(color = AppCard, shape = RoundedCornerShape(12.dp)) {
            Text(report, color = AppGray, modifier = Modifier.fillMaxWidth().padding(14.dp))
        }
    }
    OutlinedButton(enabled = details.isNotBlank(), modifier = Modifier.fillMaxWidth(), onClick = {
        message = runCatching {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Cyberity problem report", report))
            "Report copied. Paste it into a message to your chosen recipient."
        }.getOrElse { "Couldn't copy the report. Try sharing it instead." }
    }) { Text("Copy report") }
    Button(enabled = details.isNotBlank(), modifier = Modifier.fillMaxWidth(), onClick = {
        message = runCatching {
            val mailto = "mailto:${SupportConfig.EMAIL}" +
                "?subject=${Uri.encode("Cyberity CvSU: $category problem")}" +
                "&body=${Uri.encode(report)}"
            val send = Intent(Intent.ACTION_SENDTO, Uri.parse(mailto))
            context.startActivity(Intent.createChooser(send, "Email support"))
            "Review the email in your email app, then press Send. Opening it does not submit the report."
        }.getOrElse { "Couldn't open an email app. Copy the report and email it to ${SupportConfig.EMAIL}." }
    }) { Text("Email support") }
    message?.let { Text(it, color = AppCyan) }
    Text("Support: ${SupportConfig.EMAIL}. Your email app sends the report when you press Send. Cyberity does not attach account data or logs.", color = AppGray)
    Text("Terms of use", color = AppWhite, fontWeight = FontWeight.SemiBold)
    Text("An approved terms-of-use document has not been configured for this app. Ask your course or project administrator for the applicable terms.", color = AppGray)
}

private val helpTopics = listOf(
    "Lessons, quizzes and labs" to "Open Learn to continue your course. Follow each lesson's instructions, then complete its quiz or lab. If progress does not update, check your connection and reopen Learn. Avoid clearing app data while progress may still be syncing.",
    "Text size, theme and tutorial" to "Open Settings → Learning to adjust lesson text size or replay the guided tutorial. Open Appearance to select Light, Dark or System theme.",
    "Change or reset a password" to "Open Settings → Security to change your password or send a password reset email. Changing the password asks you to verify your account and then sign in again. Check your email spam folder if a reset message is missing. A reset does not remove authenticator protection.",
    "Authenticator and recovery" to "Security lets you set up an authenticator, add a backup or remove one after verification. Keep access to a backup before removing a device. Cyberity does not issue recovery codes. If all authenticator codes are lost, contact your project administrator for identity verification and access recovery. Never share your setup secret or codes.",
    "Signed-in devices and logout" to "Security lists devices registered by Cyberity. Sign out of this device affects this phone. Sign out all devices includes this phone; connected apps return to sign-in within about a minute, and offline devices are checked when they reconnect. Device tracking uses Firestore and works on the Spark plan. Individual-device logout and device location are not available.",
    "Missing learning reminders" to "Open Settings → Notifications, enable reminders, choose days and time, and allow phone notifications. Send a test notification to check the notification tray. Reminders are local to this phone and appear only while signed in. Android battery saving can delay them. Reminder times follow your phone's time zone. Learning reminders do not send account security alerts."
)
