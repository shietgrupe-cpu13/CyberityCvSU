package com.cyberity.cvsu

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cyberity.cvsu.ui.theme.CyberityThemeState

private enum class SettingsSection(val title: String, val description: String, val icon: ImageVector) {
    APPEARANCE("Appearance", "Theme and display preferences", Icons.Default.Palette),
    LEARNING("Learning", "Text size and guided tutorial", Icons.Default.School),
    SECURITY("Security", "Password, authenticator and signed-in devices", Icons.Default.Shield),
    NOTIFICATIONS("Notifications", "Learning reminders and phone permissions", Icons.Default.Notifications),
    PRIVACY("Privacy", "Your information and data preferences", Icons.Default.PrivacyTip),
    HELP("Help & About", "App information and support", Icons.AutoMirrored.Filled.HelpOutline)
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit, onSignOut: () -> Unit = {}, onReplayTutorial: () -> Unit = {},
    startInPrivacy: Boolean = false) {
    var selected by rememberSaveable { mutableStateOf<String?>(if (startInPrivacy) "PRIVACY" else null) }
    var confirmSignOut by remember { mutableStateOf(false) }
    var securityBusy by remember { mutableStateOf(false) }
    val section = selected?.let { SettingsSection.valueOf(it) }

    // Keep this window alive across destinations; replacing it exposes the app below.
    Dialog(onDismissRequest = {
        if (!securityBusy) {
            if (startInPrivacy) onDismiss()
            else if (selected != null) selected = null else onDismiss()
        }
    }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
    if (section == SettingsSection.SECURITY) {
        AccountSecurityDialog(onDismiss = { selected = null }, onClose = onDismiss,
            onBusyChanged = { securityBusy = it }, onSignOut = { onDismiss(); onSignOut() })
    } else {
        SettingsPage(title = section?.title ?: "Settings", onBack = if (section != null && !startInPrivacy) ({ selected = null }) else null,
            onClose = onDismiss, canDismiss = !securityBusy) {
            when (section) {
                null -> {
                    Text("Make Cyberity yours", color = AppWhite, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("Manage your learning, account and preferences.", color = AppGray)
                    SettingsSection.entries.forEach { item ->
                        Surface(onClick = { selected = item.name }, color = AppCard, shape = RoundedCornerShape(18.dp)) {
                            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Icon(item.icon, contentDescription = null, tint = AppCyan, modifier = Modifier.size(26.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(item.title, color = AppWhite, fontWeight = FontWeight.SemiBold)
                                    Text(item.description, color = AppGray, fontSize = 13.sp)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppGray)
                            }
                        }
                    }
                    OutlinedButton(onClick = { confirmSignOut = true }, modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppDanger)) { Text("Sign out of this device") }
                }
                SettingsSection.APPEARANCE -> AppearanceSettings()
                SettingsSection.LEARNING -> LearningSettings { onDismiss(); onReplayTutorial() }
                SettingsSection.NOTIFICATIONS -> NotificationSettings()
                SettingsSection.PRIVACY -> PrivacySettingsScreen(onSignOut = { onDismiss(); onSignOut() },
                    onBusyChanged = { securityBusy = it })
                SettingsSection.HELP -> HelpSettingsScreen()
                SettingsSection.SECURITY -> Unit
            }
        }
    }
    }
    if (confirmSignOut) AlertDialog(onDismissRequest = { confirmSignOut = false }, containerColor = AppCard,
        title = { Text("Sign out?", color = AppWhite) }, text = { Text("Sign out of Cyberity on this device?", color = AppGray) },
        confirmButton = { TextButton(onClick = { confirmSignOut = false; onDismiss(); onSignOut() }) { Text("Sign out", color = AppDanger) } },
        dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text("Cancel") } })
}

/** Content for a destination inside SettingsDialog's single persistent window. */
@Composable
internal fun SettingsPage(title: String, onBack: (() -> Unit)? = null, onClose: () -> Unit,
    canDismiss: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
        Surface(modifier = Modifier.fillMaxSize(), color = AppNavy) {
            Column(Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) IconButton(enabled = canDismiss, onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to settings", tint = AppCyan)
                    } else Icon(Icons.Default.Settings, null, tint = AppCyan, modifier = Modifier.padding(12.dp))
                    Text(title, modifier = Modifier.weight(1f), color = AppWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(enabled = canDismiss, onClick = onClose) { Icon(Icons.Default.Close, "Close settings", tint = AppGray) }
                }
                HorizontalDivider(color = AppBorder)
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
            }
        }
}

@Composable
private fun AppearanceSettings() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
    SettingsIntro("A comfortable view", "Choose the look that works best for you. System follows your phone's light or dark setting.")
    SettingsChoices(listOf(CyberityThemeState.DARK to "Dark", CyberityThemeState.LIGHT to "Light", CyberityThemeState.SYSTEM to "System"),
        CyberityThemeState.mode) { choice ->
        CyberityThemeState.mode = choice
        prefs.edit().putString(CyberityThemeState.PREF_KEY, choice).apply()
    }
    SettingsPlaceholder("Reduced motion", "Use fewer animations for a calmer experience.")
}

@Composable
private fun LearningSettings(onReplay: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
    var font by remember { mutableStateOf(prefs.getString("font_size", "medium")?.lowercase()?.let { if (it == "normal") "medium" else it } ?: "medium") }
    SettingsIntro("Learn your way", "Adjust text scaling for lessons, labs and quizzes.")
    SettingsChoices(listOf("small" to "Small", "medium" to "Medium", "large" to "Large"), font) { choice ->
        font = choice; prefs.edit().putString("font_size", choice).apply()
    }
    Text("Text preview", color = AppCyan)
    Text("Build safer habits, one lesson at a time.", color = AppWhite,
        fontSize = when (font) { "small" -> 14.sp; "large" -> 20.sp; else -> 16.sp })
    Button(onClick = onReplay, modifier = Modifier.fillMaxWidth()) { Text("Replay guided tutorial") }
    SettingsPlaceholder("Offline lessons", "Download supported lessons to study without a connection.")
}

@Composable
internal fun SettingsIntro(title: String, description: String) {
    Text(title, color = AppWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Text(description, color = AppGray)
}

@Composable
private fun SettingsChoices(options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (key, label) ->
            Surface(modifier = Modifier.weight(1f).selectable(selected = key == selected, role = Role.RadioButton,
                onClick = { onSelect(key) }), shape = RoundedCornerShape(12.dp),
                color = if (key == selected) AppBlue else AppCard,
                border = BorderStroke(1.dp, if (key == selected) AppCyan else AppBorder)) {
                Box(Modifier.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Text(label, color = if (key == selected) AppOnBlue else AppWhite)
                }
            }
        }
    }
}

/** Informational roadmap item; no saved preference or action is implied. */
@Composable
internal fun SettingsPlaceholder(title: String, description: String) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AppCard, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = AppWhite, fontWeight = FontWeight.Medium)
            Text("Planned", color = AppCyan, fontSize = 12.sp)
            Text(description, color = AppGray, fontSize = 13.sp)
        }
    }
}
