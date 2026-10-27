package com.cyberity.cvsu

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

object AppVersion {
    fun parts(value: String): List<Int>? {
        val text = value.removePrefix("v")
        if (!text.matches(Regex("[0-9]+([.][0-9]+)*"))) return null
        return text.split('.').map { it.toIntOrNull() ?: return null }
    }
    fun isNewer(latest: String, installed: String): Boolean {
        val a = requireNotNull(parts(latest))
        val b = requireNotNull(parts(installed))
        for (i in 0 until maxOf(a.size, b.size)) {
            val comparison = (a.getOrElse(i) { 0 }).compareTo(b.getOrElse(i) { 0 })
            if (comparison != 0) return comparison > 0
        }
        return false
    }
}

@Composable
fun CheckForUpdates() {
    val context = LocalContext.current
    val state by UpdateController.state.collectAsState()
    var enabled by remember { mutableStateOf(UpdateNotifications.enabled(context)) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) UpdateController.check(context)
        else UpdateController.message("Notifications are off on this phone. You can still check for updates here.")
    }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { UpdateController.check(context) }, enabled = !state.checking && !state.downloading,
            modifier = Modifier.fillMaxWidth()) {
            Text(if (state.checking) "Checking for updates…" else "Check for Updates")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Update Notifications", color = AppWhite, modifier = Modifier.weight(1f))
            Switch(checked = enabled, onCheckedChange = { value ->
                enabled = value
                UpdateNotifications.configure(context, value)
                if (value && Build.VERSION.SDK_INT >= 33 && !LearningReminders.permissionGranted(context))
                    permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                else if (value) UpdateController.check(context)
            })
        }
        Text("Checks when you open the app and periodically in the background. Downloads start only when you choose.",
            color = AppGray, style = MaterialTheme.typography.bodySmall)
        if (enabled && !androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            TextButton(onClick = {
                runCatching {
                    val settings = if (Build.VERSION.SDK_INT >= 26) Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.packageName))
                    context.startActivity(settings)
                }
            }) { Text("Allow notifications in phone settings") }
        }
        state.message?.let { Text(it, color = AppCyan) }
    }
}

@Composable
internal fun AppUpdateHost(openFromNotification: Boolean = false) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by UpdateController.state.collectAsState()
    var installing by remember { mutableStateOf(false) }
    var askPermission by rememberSaveable { mutableStateOf(false) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) UpdateController.check(context, manual = false)
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }

    fun install() {
        if (installing) return
        installing = true
        scope.launch {
            try { context.startActivity(UpdateController.verifiedInstaller(context)) }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { UpdateController.message("Couldn't open the installer. Download the update again or use the browser download.") }
            finally { installing = false }
        }
    }
    val allowInstall = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Build.VERSION.SDK_INT < 26 || context.packageManager.canRequestPackageInstalls()) install()
        else UpdateController.message("Installation permission wasn't granted. Tap Install to try again, or use the browser download.")
    }
    LaunchedEffect(Unit) {
        UpdateController.check(context, manual = openFromNotification)
        val prefs = UpdateNotifications.prefs(context)
        if (UpdateNotifications.enabled(context) && Build.VERSION.SDK_INT >= 33 &&
            !LearningReminders.permissionGranted(context) && !prefs.getBoolean("permissionAsked", false)) askPermission = true
    }
    if (askPermission) AlertDialog(onDismissRequest = {
        askPermission = false
        UpdateNotifications.prefs(context).edit().putBoolean("permissionAsked", true).apply()
    }, title = { Text("Get update notifications?") },
        text = { Text("Cyberity can let you know when a new version is ready. You choose when to download and install it.") },
        confirmButton = { TextButton(onClick = {
            askPermission = false
            UpdateNotifications.prefs(context).edit().putBoolean("permissionAsked", true).apply()
            if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }) { Text("Allow") } },
        dismissButton = { TextButton(onClick = {
            askPermission = false
            UpdateNotifications.prefs(context).edit().putBoolean("permissionAsked", true).apply()
        }) { Text("Later") } })

    val update = state.update
    if (state.visible && update != null && !askPermission) {
        AlertDialog(onDismissRequest = { if (!state.downloading && !installing) UpdateController.dismiss(context) },
            title = { Text(if (state.file != null) "Update Ready" else "Update Available") },
            text = {
                Column {
                    Text("Cyberity ${update.version}")
                    Text("Download inside Cyberity, then approve installation on your phone. Your account and progress are kept.")
                    if (state.downloading) {
                        LinearProgressIndicator(progress = { state.progress }, modifier = Modifier.fillMaxWidth())
                        Text("Downloading and verifying… ${(state.progress * 100).toInt()}%")
                    }
                    state.message?.let { Text(it) }
                    if (!state.downloading && !installing) TextButton(onClick = {
                        try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(update.downloadUrl))) }
                        catch (_: Exception) { UpdateController.message("Couldn't open a browser on this phone.") }
                    }) { Text("Download with browser instead") }
                }
            },
            confirmButton = {
                if (!state.downloading) TextButton(enabled = !installing, onClick = {
                    if (state.file == null) UpdateController.download(context)
                    else if (Build.VERSION.SDK_INT >= 26 && !context.packageManager.canRequestPackageInstalls()) {
                        try { allowInstall.launch(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + context.packageName))) }
                        catch (_: Exception) { UpdateController.message("Open your phone settings to allow Cyberity to install updates, or use the browser download.") }
                    } else install()
                }) { Text(if (installing) "Verifying…" else if (state.file == null) "Download in App" else "Install") }
            },
            dismissButton = { TextButton(enabled = !installing, onClick = {
                if (state.downloading) UpdateController.cancel() else UpdateController.dismiss(context)
            }) { Text(if (state.downloading) "Cancel download" else "Later") } })
    }
}

@Composable
internal fun UpdatePrompt(version: String, onDownload: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(onDismissRequest = onCancel,
        title = { Text("Update Available") },
        text = { Text("Cyberity $version is available. Download and verify the update inside the app, then approve installation on your phone.") },
        confirmButton = { TextButton(onClick = onDownload) { Text("Download") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } })
}
