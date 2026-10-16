package com.cyberity.cvsu

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cyberity.cvsu.ui.theme.CyberityThemeState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.TotpMultiFactorGenerator
import java.util.Calendar
import kotlinx.coroutines.CancellationException

private enum class SettingsSection(val title: String, val icon: ImageVector) {
    STUDENT("Student information", Icons.Default.Badge),
    CERTIFICATE("Completion certificate", Icons.Default.WorkspacePremium),
    APPEARANCE("Appearance", Icons.Default.Palette),
    LEARNING("Learning", Icons.Default.School),
    NOTIFICATIONS("Notifications", Icons.Default.Notifications),
    SECURITY("Security", Icons.Default.Shield),
    PRIVACY("Privacy", Icons.Default.PrivacyTip),
    HELP("Help & About", Icons.AutoMirrored.Filled.HelpOutline)
}

private val settingsGroups = listOf(
    "Preferences" to listOf(SettingsSection.APPEARANCE, SettingsSection.LEARNING, SettingsSection.NOTIFICATIONS),
    "Account" to listOf(SettingsSection.STUDENT, SettingsSection.CERTIFICATE, SettingsSection.SECURITY, SettingsSection.PRIVACY),
    "Support" to listOf(SettingsSection.HELP)
)

/** [xp] shows on the profile card when the caller has it (Home and Learn do; labs and quizzes don't). */
@Composable
fun SettingsDialog(onDismiss: () -> Unit, onSignOut: () -> Unit = {}, onReplayTutorial: () -> Unit = {},
    startInPrivacy: Boolean = false, xp: Int? = null) {
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
    // Opaque behind the fade, so the app below never shows through mid-change.
    Box(Modifier.fillMaxSize().background(AppNavy)) {
        AnimatedContent(
            targetState = section,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "settingsSection"
        ) { shown ->
            if (shown == SettingsSection.SECURITY) {
                AccountSecurityDialog(onDismiss = { selected = null }, onClose = onDismiss,
                    onBusyChanged = { securityBusy = it }, onSignOut = { onDismiss(); onSignOut() })
            } else {
                SettingsPage(title = shown?.title ?: "Settings", onBack = if (shown != null && !startInPrivacy) ({ selected = null }) else null,
                    onClose = onDismiss, canDismiss = !securityBusy) {
                    when (shown) {
                        null -> SettingsHome(xp = xp, onOpen = { selected = it.name }, onSignOut = { confirmSignOut = true })
                        SettingsSection.STUDENT -> StudentInformationScreen(onBusyChanged = { securityBusy = it })
                        SettingsSection.CERTIFICATE -> CompletionCertificateScreen()
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
    }
    }
    if (confirmSignOut) AlertDialog(onDismissRequest = { confirmSignOut = false }, containerColor = AppCard,
        title = { Text("Sign out?", color = AppWhite) }, text = { Text("Sign out of Cyberity on this device?", color = AppGray) },
        confirmButton = { TextButton(onClick = { confirmSignOut = false; onDismiss(); onSignOut() }) { Text("Sign out", color = AppDanger) } },
        dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text("Cancel") } })
}

/** The settings start page: who's signed in, then each section with its current value. */
@Composable
private fun ColumnScope.SettingsHome(xp: Int?, onOpen: (SettingsSection) -> Unit, onSignOut: () -> Unit) {
    val context = LocalContext.current
    val user = remember { FirebaseAuth.getInstance().currentUser }
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
    var onLeaderboard by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(user?.uid) {
        val uid = user?.uid ?: return@LaunchedEffect
        onLeaderboard = try { PrivacyPreferencesRepository.load(uid).leaderboardVisible }
            catch (error: Exception) { if (error is CancellationException) throw error; null }
    }

    val values = remember(onLeaderboard) {
        val reminders = LearningReminders.read(context)
        val authenticators = user?.multiFactor?.enrolledFactors.orEmpty()
            .count { it.factorId == TotpMultiFactorGenerator.FACTOR_ID }
        val version = runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull()
        mapOf(
            SettingsSection.STUDENT to "Profile and year level",
            SettingsSection.CERTIFICATE to "Preview and download",
            SettingsSection.APPEARANCE to when (CyberityThemeState.mode) {
                CyberityThemeState.DARK -> "Dark"; CyberityThemeState.LIGHT -> "Light"; else -> "System"
            },
            SettingsSection.LEARNING to "Text: " + when (prefs.getString("font_size", "medium")?.lowercase()) {
                "small" -> "Small"; "large" -> "Large"; else -> "Medium"
            },
            SettingsSection.NOTIFICATIONS to if (!reminders.enabled) "Off" else DateFormat.getTimeFormat(context).format(
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, reminders.hour); set(Calendar.MINUTE, reminders.minute)
                }.time),
            SettingsSection.SECURITY to if (authenticators > 0) "Authenticator on" else "Authenticator off",
            SettingsSection.PRIVACY to when (onLeaderboard) { true -> "On leaderboard"; false -> "Hidden"; null -> "" },
            SettingsSection.HELP to (version?.let { "v$it" } ?: "")
        )
    }

    ProfileCard(xp = xp)
    settingsGroups.forEach { (label, sections) ->
        SettingsLabel(label)
        Surface(color = AppCard, shape = RoundedCornerShape(18.dp)) {
            Column {
                sections.forEachIndexed { index, item ->
                    if (index > 0) HorizontalDivider(color = AppBorder, modifier = Modifier.padding(start = 64.dp))
                    SectionRow(item.icon, item.title, values[item].orEmpty()) { onOpen(item) }
                }
            }
        }
    }
    OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, AppBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppDanger)) { Text("Sign out") }
}

@Composable
private fun ProfileCard(xp: Int?) {
    val user = remember { FirebaseAuth.getInstance().currentUser }
    val email = user?.email.orEmpty()
    val name = user?.displayName?.takeIf { it.isNotBlank() } ?: email.substringBefore("@").ifBlank { "Student" }
    val initials = name.split(" ", ".", "_").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }.ifBlank { "?" }
    Surface(color = AppCard, shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(AppBlue), contentAlignment = Alignment.Center) {
                Text(initials, color = AppOnBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(name, color = AppWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (email.isNotBlank()) Text(email, color = AppGray, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (user?.isEmailVerified == true) ProfilePill("Email verified", AppSuccess)
                    else ProfilePill("Email not verified", AppDanger)
                    xp?.let { ProfilePill("$it XP", AppXp) }
                }
            }
        }
    }
}

@Composable
private fun ProfilePill(text: String, color: Color) {
    Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(AppNavy).padding(horizontal = 9.dp, vertical = 3.dp))
}

@Composable
private fun SectionRow(icon: ImageVector, title: String, value: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        SettingsIconTile(icon)
        Text(title, color = AppWhite, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        if (value.isNotBlank()) Text(value, color = AppGray, fontSize = 13.sp, maxLines = 1)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppGray)
    }
}

@Composable
private fun SettingsIconTile(icon: ImageVector) {
    Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(AppNavy), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = AppCyan, modifier = Modifier.size(20.dp))
    }
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
                    verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
            }
        }
}

@Composable
private fun AppearanceSettings() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
    SettingsNote("Choose how Cyberity looks. System follows your phone's setting.")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(
            Triple(CyberityThemeState.DARK, "Dark", DarkThemeSwatch to DarkThemeSwatch),
            Triple(CyberityThemeState.LIGHT, "Light", LightThemeSwatch to LightThemeSwatch),
            Triple(CyberityThemeState.SYSTEM, "System", LightThemeSwatch to DarkThemeSwatch)
        ).forEach { (key, label, swatches) ->
            ThemeTile(label, selected = CyberityThemeState.mode == key, left = swatches.first, right = swatches.second,
                modifier = Modifier.weight(1f)) {
                CyberityThemeState.mode = key
                prefs.edit().putString(CyberityThemeState.PREF_KEY, key).apply()
                CyberityThemeState.rememberSplashTheme(context)
            }
        }
    }
}

@Composable
private fun ThemeTile(label: String, selected: Boolean, left: ThemeSwatch, right: ThemeSwatch,
    modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = RoundedCornerShape(16.dp), color = AppCard,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) AppBlue else AppBorder)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(10.dp))) {
                SwatchHalf(left, Modifier.weight(1f))
                if (right != left) SwatchHalf(right, Modifier.weight(1f))
            }
            Text(label, color = AppWhite, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}

@Composable
private fun SwatchHalf(swatch: ThemeSwatch, modifier: Modifier) {
    Column(modifier.fillMaxHeight().background(swatch.background).padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.fillMaxWidth(0.6f).height(7.dp).clip(RoundedCornerShape(4.dp)).background(swatch.accent))
        Box(Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)).background(swatch.card))
        Box(Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)).background(swatch.card))
    }
}

@Composable
private fun LearningSettings(onReplay: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
    var font by remember { mutableStateOf(prefs.getString("font_size", "medium")?.lowercase()?.let { if (it == "normal") "medium" else it } ?: "medium") }
    SettingsLabel("Text size")
    SettingsChoices(listOf("small" to "Small", "medium" to "Medium", "large" to "Large"), font) { choice ->
        font = choice; prefs.edit().putString("font_size", choice).apply()
    }
    SettingsCard {
        Text("Preview", color = AppGray, fontSize = 12.sp)
        Text("Build safer habits, one lesson at a time.", color = AppWhite,
            fontSize = when (font) { "small" -> 14.sp; "large" -> 20.sp; else -> 16.sp })
    }
    SettingsNote("Applies to lessons, labs and quizzes.")
    SettingsLabel("Tutorial")
    SettingsActionCard(Icons.Default.Replay, "Replay guided tutorial", "Walk through how levels work again", onClick = onReplay)
}

@Composable
private fun SettingsChoices(options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (key, label) ->
            Surface(modifier = Modifier.weight(1f).selectable(selected = key == selected, role = Role.RadioButton,
                onClick = { onSelect(key) }), shape = RoundedCornerShape(12.dp),
                color = if (key == selected) AppBlue else AppCard,
                border = BorderStroke(1.dp, if (key == selected) AppBlue else AppBorder)) {
                Box(Modifier.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Text(label, color = if (key == selected) AppOnBlue else AppWhite)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Shared building blocks for every settings section
// ---------------------------------------------------------------------------

/** Small uppercase heading that starts a group of settings. */
@Composable
internal fun SettingsLabel(text: String) {
    Text(text.uppercase(), color = AppCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 4.dp))
}

/** One short line of explanation. */
@Composable
internal fun SettingsNote(text: String, color: Color = AppGray) {
    Text(text, color = color, fontSize = 13.sp)
}

@Composable
internal fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(color = AppCard, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

/** A tappable card that opens or starts something. */
@Composable
internal fun SettingsActionCard(icon: ImageVector, title: String, subtitle: String? = null, enabled: Boolean = true,
    onClick: () -> Unit) {
    Surface(onClick = onClick, enabled = enabled, color = AppCard, shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            SettingsIconTile(icon)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = if (enabled) AppWhite else AppGray, fontWeight = FontWeight.Medium)
                subtitle?.let { Text(it, color = AppGray, fontSize = 13.sp) }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppGray)
        }
    }
}
