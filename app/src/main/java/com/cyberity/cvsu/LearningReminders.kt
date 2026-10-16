package com.cyberity.cvsu

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

internal data class ReminderPreferences(val enabled: Boolean = false, val hour: Int = 18, val minute: Int = 0,
    val days: Set<Int> = (1..7).toSet())

internal object LearningReminders {
    private const val CHANNEL = "learning_reminders"
    private const val REQUEST = 4601
    const val ACTION = "com.cyberity.cvsu.LEARNING_REMINDER"
    private fun prefs(context: Context) = context.getSharedPreferences("learning_reminders", Context.MODE_PRIVATE)

    fun read(context: Context): ReminderPreferences {
        val p = prefs(context)
        val days = p.getStringSet("days", (1..7).map { it.toString() }.toSet()).orEmpty()
            .mapNotNull { it.toIntOrNull()?.takeIf { day -> day in 1..7 } }.toSet()
        return ReminderPreferences(p.getBoolean("enabled", false), p.getInt("hour", 18).coerceIn(0, 23),
            p.getInt("minute", 0).coerceIn(0, 59), days)
    }

    fun save(context: Context, value: ReminderPreferences) {
        require(value.hour in 0..23 && value.minute in 0..59 && value.days.all { it in 1..7 })
        prefs(context).edit().putBoolean("enabled", value.enabled && value.days.isNotEmpty())
            .putInt("hour", value.hour).putInt("minute", value.minute)
            .putStringSet("days", value.days.map { it.toString() }.toSet()).apply()
        schedule(context)
        if (!value.enabled || value.days.isEmpty()) NotificationManagerCompat.from(context).cancel(REQUEST)
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL, "Learning reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Reminders to continue your Cyberity lessons"
                })
        }
    }

    fun permissionGranted(context: Context) = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun canNotify(context: Context): Boolean {
        if (!permissionGranted(context) || !NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        return Build.VERSION.SDK_INT < 26 ||
            context.getSystemService(NotificationManager::class.java).getNotificationChannel(CHANNEL)?.importance != NotificationManager.IMPORTANCE_NONE
    }

    fun schedule(context: Context) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        val intent = PendingIntent.getBroadcast(context, REQUEST,
            Intent(context, LearningReminderReceiver::class.java).setAction(ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarm.cancel(intent)
        val value = read(context)
        if (!value.enabled) return
        val next = ReminderSchedule.next(System.currentTimeMillis(), value.hour, value.minute, value.days) ?: return
        // Inexact scheduling avoids requiring special exact-alarm access.
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, intent)
    }

    fun notify(context: Context, test: Boolean = false): Boolean {
        createChannel(context)
        if (!canNotify(context)) return false
        val open = PendingIntent.getActivity(context, REQUEST,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_learning_notification)
            .setContentTitle(if (test) "Your reminders are ready" else "Time for a little cybersecurity practice")
            .setContentText(if (test) "Cyberity can send learning reminders on this phone." else "Continue a lesson or try a lab in Cyberity.")
            .setContentIntent(open).setAutoCancel(true).setOnlyAlertOnce(false).build()
        return try {
            NotificationManagerCompat.from(context).notify(REQUEST, notification)
            true
        } catch (_: SecurityException) { false }
    }
}

class LearningReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            LearningReminders.ACTION -> {
                val preferences = LearningReminders.read(context)
                if (preferences.enabled && ReminderSchedule.isDue(System.currentTimeMillis(),
                        preferences.hour, preferences.minute, preferences.days) &&
                    FirebaseAuth.getInstance().currentUser?.isEmailVerified == true) {
                    LearningReminders.notify(context)
                }
                LearningReminders.schedule(context)
            }
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> LearningReminders.schedule(context)
        }
    }
}
