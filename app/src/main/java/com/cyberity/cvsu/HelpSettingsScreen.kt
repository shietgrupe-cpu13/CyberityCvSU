package com.cyberity.cvsu

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Content hosted in the existing settings window; no additional dialog or navigation stack. */
@Composable
internal fun HelpSettingsScreen() {
    val context = LocalContext.current
    val version = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "Unknown"
    }
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    var details by rememberSaveable { mutableStateOf("") }
    var diagnostics by rememberSaveable { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }
    val report = remember(details, diagnostics, version) {
        buildString {
            append("Cyberity CvSU problem report\n\n")
            append(details.trim())
            if (diagnostics) {
                append("\n\nApp version: $version")
                append("\nDevice: ${Build.MANUFACTURER} ${Build.MODEL}")
                append("\nAndroid: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            }
        }
    }

    SettingsCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(AppBlue), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Security, contentDescription = null, tint = AppOnBlue)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Cyberity CvSU", color = AppWhite, fontWeight = FontWeight.SemiBold)
                Text("Version $version · Cybersecurity lessons, quizzes and labs", color = AppGray, fontSize = 13.sp)
            }
        }
    }

    CheckForUpdates()

    SettingsLabel("Help topics")
    helpTopics.forEach { (title, answer) ->
        Surface(onClick = { expanded = if (expanded == title) null else title },
            modifier = Modifier.fillMaxWidth(), color = AppCard, shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = AppWhite, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Icon(if (expanded == title) Icons.Default.Remove else Icons.Default.Add,
                        contentDescription = if (expanded == title) "Collapse" else "Expand", tint = AppGray)
                }
                if (expanded == title) Text(answer, color = AppGray, fontSize = 14.sp)
            }
        }
    }

    SettingsLabel("Report a problem")
    OutlinedTextField(value = details, onValueChange = { details = it.take(4000); message = null },
        label = { Text("What happened?") },
        placeholder = { Text("Don't include passwords or codes.") },
        modifier = Modifier.fillMaxWidth(), minLines = 4, maxLines = 8)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = diagnostics, onCheckedChange = { diagnostics = it; message = null })
        Text("Include app version and phone model", color = AppGray, modifier = Modifier.weight(1f))
    }
    Button(enabled = details.isNotBlank(), modifier = Modifier.fillMaxWidth(), onClick = {
        message = runCatching {
            val mailto = "mailto:${SupportConfig.EMAIL}" +
                "?subject=${Uri.encode("Cyberity CvSU problem report")}" +
                "&body=${Uri.encode(report)}"
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SENDTO, Uri.parse(mailto)), "Email support"))
            "Check the email in your email app, then press Send."
        }.getOrElse {
            // No email app: copy the report so it can be pasted anywhere.
            runCatching {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Cyberity problem report", report))
                "No email app found. Your report was copied; paste it into a message to ${SupportConfig.EMAIL}."
            }.getOrElse { "No email app found. Email your report to ${SupportConfig.EMAIL}." }
        }
    }) { Text("Email support") }
    message?.let { Text(it, color = AppCyan) }
    Text(SupportConfig.EMAIL, color = AppGray, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
}

private val helpTopics = listOf(
    "Lessons, quizzes and labs" to "Open Learn to continue your course. Each level starts with a short lesson, then a quiz or lab. If your progress doesn't update, check your connection and reopen Learn.",
    "Passwords and authenticator" to "Change your password in Settings → Security. Forgot it? Use Forgot password on the sign-in screen. An authenticator app adds a code after your password; add a backup on another device before removing one. If you lose every code, contact your project administrator. Never share your codes.",
    "Signed-in devices" to "Settings → Security lists the phones and tablets signed in to your account. Sign out all devices signs out every device, including this one. Offline devices are signed out when they reconnect.",
    "Learning reminders" to "Turn on reminders in Settings → Notifications, pick a time and days, and allow notifications on your phone. Battery saving can delay reminders."
)
