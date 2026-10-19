package com.cyberity.cvsu

import android.os.SystemClock
import android.content.Context
import android.provider.Settings
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.android.gms.tasks.Task

// ===========================================================================
// SERVER CLOCK
// ===========================================================================
// Heart refill must not be something a student can grant themselves by moving
// the device clock forward, so every heart timestamp is stamped by Firestore
// (FieldValue.serverTimestamp) and never sent from the app.
//
// The countdown still has to tick locally, though, which needs a current time.
// sync() writes a timestamp the server fills in, reads back what the server
// actually recorded, and keeps the difference against the device's uptime
// counter (SystemClock.elapsedRealtime). now() then reports server time without
// a network call per tick. Uptime is used rather than the wall clock because
// changing the phone's date or time doesn't move it, so a student can't speed
// up heart refill by setting the clock forward after a sync.

object ServerClock {
    private const val CACHE = "cyberity_server_clock"
    private fun bootCount(context: Context): Int = runCatching {
        Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1)
    }.getOrDefault(-1)

    /** A server anchor survives process restarts only within the same boot. */
    fun restore(context: Context) {
        if (isSynced) return
        val prefs = context.getSharedPreferences(CACHE, Context.MODE_PRIVATE)
        val boot = bootCount(context)
        val anchor = prefs.getLong("server", 0L)
        val elapsed = prefs.getLong("elapsed", -1L)
        val now = SystemClock.elapsedRealtime()
        if (HeartClockAnchor(anchor, elapsed, prefs.getInt("boot", -2)).timeAt(now, boot) != null) {
            offsetMillis = anchor - elapsed
            isSynced = true
        }
    }

    fun save(context: Context) {
        val now = trustedNow() ?: return
        context.getSharedPreferences(CACHE, Context.MODE_PRIVATE).edit()
            .putLong("server", now).putLong("elapsed", SystemClock.elapsedRealtime())
            .putInt("boot", bootCount(context)).apply()
    }

    private const val COLLECTION = "userProgress"
    private const val FIELD_PING = "clockPing"

    /** Server time minus device uptime, measured by the last sync. */
    @Volatile
    private var offsetMillis: Long = 0L

    /** False until the first successful server sync. */
    @Volatile
    var isSynced: Boolean = false
        private set

    /** Server time only. Zero before sync cannot grant refill. */
    fun now(): Long =
        trustedNow() ?: 0L

    /** Streaks never use the device wall clock, even before the first sync. */
    fun trustedNow(): Long? = if (isSynced) SystemClock.elapsedRealtime() + offsetMillis else null

    /**
     * Measures the offset between this device's clock and Firestore's.
     *
     * The server records the moment it commits the write, which happened
     * somewhere inside the round trip — so the device-side midpoint of that
     * round trip is what the server time is compared against. That keeps
     * network latency from being mistaken for clock drift.
     *
     * A failed sync never substitutes device wall time. Exercises wait for sync.
     */
    fun sync(uid: String, onSynced: () -> Unit = {}) {
        syncTask(uid).addOnSuccessListener { onSynced() }
    }

    /** Callers that require trusted time must wait for a successful server response. */
    fun syncTask(uid: String): Task<Long> {
        val doc = FirebaseFirestore.getInstance().collection(COLLECTION).document(uid)
        val sentAt = SystemClock.elapsedRealtime()

        return doc.set(mapOf(FIELD_PING to FieldValue.serverTimestamp()), SetOptions.merge())
            .continueWithTask { written ->
                written.result // Propagate failure; never substitute device time.
                doc.get(Source.SERVER)
            }.continueWith { read ->
                val snapshot = read.result
                check(!snapshot.metadata.isFromCache && !snapshot.metadata.hasPendingWrites())
                val serverAt = checkNotNull(snapshot.getTimestamp(FIELD_PING)) { "Server time unavailable." }.toDate().time
                val receivedAt = SystemClock.elapsedRealtime()
                check(receivedAt - sentAt <= 8_000L) { "Clock response too old; retry with a fresh request." }
                val deviceMidpoint = (sentAt + receivedAt) / 2
                offsetMillis = serverAt - deviceMidpoint
                isSynced = true
                checkNotNull(trustedNow())
            }
    }
}
