package com.cyberity.cvsu

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val next = if (prefs.enabled) ReminderSchedule.next(System.currentTimeMillis(), prefs.hour, prefs.minute, prefs.days) else null
    SettingsCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Learning reminders", color = AppWhite, fontWeight = FontWeight.SemiBold)
                Text(when {
                    prefs.enabled && !allowed -> "Paused by phone permissions"
                    prefs.enabled && next != null -> "On · next: " + java.text.DateFormat.getDateTimeInstance(
                        java.text.DateFormat.SHORT, java.text.DateFormat.SHORT).format(Date(next))
                    prefs.enabled -> "On"
                    else -> "Off"
                }, color = AppGray, fontSize = 13.sp)
            }
            Switch(checked = prefs.enabled, onCheckedChange = { enable ->
                if (!enable) save(prefs.copy(enabled = false))
                else if (Build.VERSION.SDK_INT >= 33 && !LearningReminders.permissionGranted(context))
                    permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                else if (LearningReminders.canNotify(context)) save(prefs.copy(enabled = true))
                else message = "Enable notifications in your phone settings before turning on reminders."
            })
        }
    }
    val time = remember(prefs.hour, prefs.minute) { Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, prefs.hour); set(Calendar.MINUTE, prefs.minute)
    }.time }
    SettingsCard {
        Row(Modifier.fillMaxWidth().clickable {
            TimePickerDialog(context, { _, hour, minute -> save(prefs.copy(hour = hour, minute = minute)) },
                prefs.hour, prefs.minute, DateFormat.is24HourFormat(context)).show()
        }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = AppCyan)
            Text("Reminder time", color = AppWhite, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text(DateFormat.getTimeFormat(context).format(time), color = AppCyan, fontWeight = FontWeight.Medium)
        }
        HorizontalDivider(color = AppBorder)
        Text("Repeat on", color = AppGray, fontSize = 13.sp)
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
    }
    SettingsNote("Reminders are saved on this phone. Battery saving can delay them.")
    if (!allowed) SettingsCard {
        Text("Phone notifications are blocked", color = AppDanger, fontWeight = FontWeight.SemiBold)
        Text("Allow notifications for Cyberity so reminders can appear.", color = AppGray, fontSize = 13.sp)
        OutlinedButton(onClick = {
            val intent = if (Build.VERSION.SDK_INT >= 26) Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:${context.packageName}"))
            context.startActivity(intent)
        }, modifier = Modifier.fillMaxWidth()) { Text("Open phone settings") }
    }
    message?.let { Text(it, color = AppCyan) }
}
