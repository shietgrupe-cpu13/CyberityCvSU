package com.cyberity.cvsu

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source

// ===========================================================================
// SERVER CLOCK
// ===========================================================================
// Heart refill must not be something a student can grant themselves by moving
// the device clock forward, so every heart timestamp is stamped by Firestore
// (FieldValue.serverTimestamp) and never sent from the app.
//
// The countdown still has to tick locally, though, which needs a current time.
// sync() writes a timestamp the server fills in, reads back what the server
// actually recorded, and keeps the difference against the device clock. now()
// then reports server time without a network call per tick.

object ServerClock {

    private const val COLLECTION = "userProgress"
    private const val FIELD_PING = "clockPing"

    @Volatile
    private var offsetMillis: Long = 0L

    /** False until the first successful sync — until then now() is only device time. */
    @Volatile
    var isSynced: Boolean = false
        private set

    /** Best available estimate of server time, in milliseconds. */
    fun now(): Long = System.currentTimeMillis() + offsetMillis

    /**
     * Measures the offset between this device's clock and Firestore's.
     *
     * The server records the moment it commits the write, which happened
     * somewhere inside the round trip — so the device-side midpoint of that
     * round trip is what the server time is compared against. That keeps
     * network latency from being mistaken for clock drift.
     *
     * Silent on failure: the app falls back to device time rather than
     * blocking the Learn screen when offline.
     */
    fun sync(uid: String, onSynced: () -> Unit = {}) {
        val doc = FirebaseFirestore.getInstance().collection(COLLECTION).document(uid)
        val sentAt = System.currentTimeMillis()

        doc.set(mapOf(FIELD_PING to FieldValue.serverTimestamp()), SetOptions.merge())
            .addOnSuccessListener {
                doc.get(Source.SERVER).addOnSuccessListener { snapshot ->
                    val serverAt = snapshot.getTimestamp(FIELD_PING)?.toDate()?.time ?: return@addOnSuccessListener
                    val deviceMidpoint = (sentAt + System.currentTimeMillis()) / 2
                    offsetMillis = serverAt - deviceMidpoint
                    isSynced = true
                    onSynced()
                }
            }
    }
}
