package com.cyberity.cvsu

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

internal object UpdateNotifications {
    private const val ID = 4602
    private const val CHANNEL = "app_updates"
    fun prefs(context: Context) = context.getSharedPreferences("app_updates", Context.MODE_PRIVATE)
    fun enabled(context: Context) = prefs(context).getBoolean("enabled", true)
    fun clear(context: Context) = NotificationManagerCompat.from(context).cancel(ID)

    fun configure(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean("enabled", enabled).apply()
        if (!enabled) NotificationManagerCompat.from(context).cancel(ID)
        schedule(context)
    }

    fun schedule(context: Context) {
        val scheduler = context.getSystemService(JobScheduler::class.java)
        if (!enabled(context)) { scheduler.cancel(ID); return }
        if (scheduler.getPendingJob(ID) != null) return
        val job = JobInfo.Builder(ID, ComponentName(context, UpdateCheckJob::class.java))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPeriodic(24 * 60 * 60 * 1000L, 60 * 60 * 1000L).setPersisted(true)
        if (Build.VERSION.SDK_INT >= 26) job.setRequiresBatteryNotLow(true)
        scheduler.schedule(job.build())
    }

    fun notify(context: Context, update: AvailableUpdate) {
        if (!enabled(context) || !LearningReminders.permissionGranted(context)) return
        if (Build.VERSION.SDK_INT >= 26) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL, "App updates", NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description = "New versions of Cyberity are available to download" })
            if (context.getSystemService(NotificationManager::class.java).getNotificationChannel(CHANNEL)?.importance == NotificationManager.IMPORTANCE_NONE) return
        }
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled() || prefs(context).getString("notified", null) == update.version) return
        val open = PendingIntent.getActivity(context, ID,
            Intent(context, MainActivity::class.java).putExtra("check_app_update", true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        try {
            manager.notify(ID, NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_learning_notification)
                .setContentTitle("Cyberity ${update.version} is available")
                .setContentText("Tap to review and download the update.")
                .setContentIntent(open).setAutoCancel(true).build())
            prefs(context).edit().putString("notified", update.version).apply()
        } catch (_: SecurityException) { /* Permission can be revoked between the check and notify. */ }
    }
}

class UpdateCheckJob : JobService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var check: Job? = null
    override fun onStartJob(params: JobParameters): Boolean {
        check = scope.launch {
            try {
                if (UpdateNotifications.enabled(this@UpdateCheckJob)) {
                    val update = UpdateDownloads.latest()
                    val installed = packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()
                    if (AppVersion.isNewer(update.version, installed)) UpdateNotifications.notify(this@UpdateCheckJob, update)
                    else UpdateNotifications.clear(this@UpdateCheckJob)
                }
            } catch (_: Exception) { /* Next periodic check retries without disturbing the user. */ }
            finally { if (kotlinx.coroutines.currentCoroutineContext().isActive) jobFinished(params, false) }
        }
        return true
    }
    override fun onStopJob(params: JobParameters): Boolean { check?.cancel(); return true }
    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
