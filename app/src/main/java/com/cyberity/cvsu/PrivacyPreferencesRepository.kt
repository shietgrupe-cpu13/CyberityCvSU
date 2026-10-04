package com.cyberity.cvsu

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.FieldValue
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

internal suspend fun <T> Task<T>.privacyAwait(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
    addOnFailureListener { if (continuation.isActive) continuation.resumeWithException(it) }
    addOnCanceledListener { continuation.cancel() }
}

internal data class PrivacyPreferences(val leaderboardVisible: Boolean = true, val deleting: Boolean = false)

/** Private preference document also retains a deletion tombstone to block older clients. */
internal object PrivacyPreferencesRepository {
    private val db get() = FirebaseFirestore.getInstance()
    @Volatile var activeDeletionUid: String? = null
        internal set

    suspend fun readDeletionState(uid: String): Boolean = load(uid).deleting

    fun checkDeletion(uid: String, onResult: (Boolean) -> Unit, onError: (Exception) -> Unit) {
        db.collection("privacyPreferences").document(uid).get(Source.SERVER)
            .addOnSuccessListener { onResult(it.getBoolean("deleting") == true) }
            .addOnFailureListener(onError)
    }

    suspend fun load(uid: String): PrivacyPreferences {
        val doc = db.collection("privacyPreferences").document(uid).get(Source.SERVER).privacyAwait()
        return PrivacyPreferences(doc.getBoolean("leaderboardVisible") ?: true, doc.getBoolean("deleting") ?: false)
    }

    suspend fun setLeaderboardVisible(uid: String, visible: Boolean) {
        check(FirebaseAuth.getInstance().currentUser?.uid == uid) { "Please sign in again." }
        val preferences = db.collection("privacyPreferences").document(uid)
        db.runTransaction { transaction ->
            val existing = transaction.get(preferences)
            check(existing.getBoolean("deleting") != true) { "Account deletion is in progress." }
            val profile = if (visible) transaction.get(db.collection("users").document(uid)) else null
            val progress = if (visible) transaction.get(db.collection("userProgress").document(uid)) else null
            transaction.set(preferences, mapOf("leaderboardVisible" to visible, "deleting" to false))
            if (!visible) transaction.delete(db.collection("leaderboard").document(uid))
            else if (profile?.getString("displayName") != null) {
                val earned = (progress?.get("levelXp") as? Map<*, *>)?.values
                    ?.sumOf { (it as? Number)?.toLong() ?: 0L } ?: 0L
                val spent = progress?.getLong("xpSpent") ?: 0L
                transaction.set(db.collection("leaderboard").document(uid), mapOf(
                    "displayName" to profile.getString("displayName"),
                    "xp" to (earned - spent).coerceIn(0, Int.MAX_VALUE.toLong()),
                    "isTester" to (profile.getBoolean("isTester") ?: false),
                    "updatedAt" to FieldValue.serverTimestamp()))
            }
        }.privacyAwait()
    }
}
