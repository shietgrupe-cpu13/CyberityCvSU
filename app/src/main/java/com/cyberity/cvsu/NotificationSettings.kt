package com.cyberity.cvsu

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NotificationSettings() {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    var prefs by remember { mutableStateOf(LearningReminders.read(context)) }
    var allowed by remember { mutableStateOf(LearningReminders.canNotify(context)) }
    var message by remember { mutableStateOf<String?>(null) }

    fun save(value: ReminderPreferences) {
        LearningReminders.save(context, value)
        prefs = LearningReminders.read(context)
        message = null
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        allowed = LearningReminders.canNotify(context)
        if (granted && allowed) save(prefs.copy(enabled = true))
        else message = "Notifications are blocked. You can enable them in your phone's notification settings."
    }
    DisposableEffect(lifecycle, context) {
        LearningReminders.createChannel(context)
        allowed = LearningReminders.canNotify(context)
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                allowed = LearningReminders.canNotify(context)
                prefs = LearningReminders.read(context)
            }
        }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { lifecycle.lifecycle.removeObserver(observer) }
    }

    SettingsIntro("A little practice, regularly", "Choose when Cyberity reminds you to continue learning. Reminders are saved on this phone and work without an internet connection.")
    Card(colors = CardDefaults.cardColors(containerColor = AppCard)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Learning reminders", color = AppWhite, fontWeight = FontWeight.SemiBold)
                    Text(if (prefs.enabled && allowed) "On" else if (prefs.enabled) "Paused by phone permissions" else "Off", color = AppGray)
                }
                Switch(checked = prefs.enabled, onCheckedChange = { enable ->
                    if (!enable) save(prefs.copy(enabled = false))
                    else if (Build.VERSION.SDK_INT >= 33 && !LearningReminders.permissionGranted(context))
                        permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else if (LearningReminders.canNotify(context)) save(prefs.copy(enabled = true))
                    else message = "Enable notifications in your phone settings before turning on reminders."
                })
            }
            val time = remember(prefs.hour, prefs.minute) { Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, prefs.hour); set(Calendar.MINUTE, prefs.minute)
            }.time }
            OutlinedButton(onClick = {
                TimePickerDialog(context, { _, hour, minute -> save(prefs.copy(hour = hour, minute = minute)) },
                    prefs.hour, prefs.minute, DateFormat.is24HourFormat(context)).show()
            }, modifier = Modifier.fillMaxWidth()) { Text("Reminder time: ${DateFormat.getTimeFormat(context).format(time)}") }
            Text("Repeat on", color = AppCyan)
            val days = listOf(2 to "Mon", 3 to "Tue", 4 to "Wed", 5 to "Thu", 6 to "Fri", 7 to "Sat", 1 to "Sun")
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    days.forEach { (day, label) ->
                        FilterChip(selected = day in prefs.days, onClick = {
                            val selected = if (day in prefs.days) prefs.days - day else prefs.days + day
                            if (selected.isEmpty()) message = "Keep at least one reminder day selected."
                            else save(prefs.copy(days = selected))
                        }, label = { Text(label) })
                    }
            }
            if (prefs.enabled) {
                val next = ReminderSchedule.next(System.currentTimeMillis(), prefs.hour, prefs.minute, prefs.days)
                next?.let { Text("Next reminder: ${java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT).format(Date(it))}", color = AppGray) }
            }
            Text("Times follow your phone's time zone. Android may deliver reminders later when conserving battery. Reminders delayed past a selected day are skipped. Reminders are shown only while you're signed in with a verified email.", color = AppGray)
        }
    }
    Text(if (allowed) "Phone notifications are allowed" else "Phone notifications are blocked", color = if (allowed) AppCyan else AppDanger)
    OutlinedButton(onClick = {
        val intent = if (Build.VERSION.SDK_INT >= 26) Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:${context.packageName}"))
        context.startActivity(intent)
    }, modifier = Modifier.fillMaxWidth()) { Text("Open phone notification settings") }
    Button(enabled = allowed, onClick = {
        message = if (LearningReminders.notify(context, test = true)) "Test notification sent. Check your notification tray."
        else "Couldn't send a notification. Check your phone permissions."
    }, modifier = Modifier.fillMaxWidth()) { Text("Send a test notification") }
    message?.let { Text(it, color = AppCyan) }
    SettingsPlaceholder("Security alerts", "Background alerts about new sign-ins and account changes need a separate delivery setup. Learning reminders do not monitor account security.")
}
