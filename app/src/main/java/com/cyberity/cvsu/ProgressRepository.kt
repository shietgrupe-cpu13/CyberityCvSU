package com.cyberity.cvsu

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source

// ===========================================================================
// PROGRESS PERSISTENCE
// ===========================================================================
// One document per signed-in user, holding the ids of every level they've
// completed plus their heart state. Level status and which level is CURRENT
// are derived from the completed ids by replaying withLevelCompleted() over
// the curriculum.
//
// Hearts store a count and the moment that count was last spent. That moment
// is always written by Firestore itself (FieldValue.serverTimestamp), never
// by the app — refill is measured against server time, so moving the device
// clock forward grants nothing. See ServerClock.

object ProgressRepository {

    private val db by lazy { FirebaseFirestore.getInstance() }

    private const val COLLECTION = "userProgress"
    private const val FIELD_COMPLETED = "completedLevels"
    private const val FIELD_HEARTS = "hearts"
    private const val FIELD_HEARTS_UPDATED_AT = "heartsUpdatedAt"

    /** Reads back the set of completed level ids for [uid]. Empty on any failure or first run. */
    fun loadCompletedLevels(uid: String, onResult: (Set<Int>) -> Unit) {
        db.collection(COLLECTION).document(uid).get()
            .addOnSuccessListener { snapshot ->
                val ids = (snapshot.get(FIELD_COMPLETED) as? List<*>)
                    ?.mapNotNull { (it as? Number)?.toInt() }
                    ?.toSet()
                    ?: emptySet()
                onResult(ids)
            }
            .addOnFailureListener { onResult(emptySet()) }
    }

    /**
     * Adds [levelId] to the completed set. arrayUnion is idempotent (no duplicate
     * entries on retry) and merge() creates the document on a user's first ever
     * completion, so there's no separate "create the doc" step.
     */
    fun markLevelCompleted(uid: String, levelId: Int) {
        db.collection(COLLECTION).document(uid)
            .set(mapOf(FIELD_COMPLETED to FieldValue.arrayUnion(levelId)), SetOptions.merge())
    }

    /**
     * Reads the stored heart state, converting the server Timestamp to millis.
     * Null on first run or failure — callers keep whatever they already have.
     */
    fun loadHearts(uid: String, onResult: (HeartState?) -> Unit) {
        db.collection(COLLECTION).document(uid).get()
            .addOnSuccessListener { snapshot ->
                val hearts = snapshot.getLong(FIELD_HEARTS)
                val updatedAt = snapshot.getTimestamp(FIELD_HEARTS_UPDATED_AT)?.toDate()?.time
                onResult(
                    if (hearts != null && updatedAt != null) HeartState(hearts.toInt(), updatedAt)
                    else null
                )
            }
            .addOnFailureListener { onResult(null) }
    }

    /**
     * Records a mistake: the new count, and the moment it happened as stamped by
     * the server. The app never sends the time, so the refill clock can't be moved
     * by changing the device clock.
     *
     * The write-back read returns what the server actually stored, letting the
     * caller replace its optimistic local state with the authoritative one.
     */
    fun spendHeart(uid: String, hearts: Int, onStored: (HeartState) -> Unit = {}) {
        val doc = db.collection(COLLECTION).document(uid)
        doc.set(
            mapOf(
                FIELD_HEARTS to hearts,
                FIELD_HEARTS_UPDATED_AT to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).addOnSuccessListener {
            doc.get(Source.SERVER).addOnSuccessListener { snapshot ->
                val stored = snapshot.getLong(FIELD_HEARTS)
                val at = snapshot.getTimestamp(FIELD_HEARTS_UPDATED_AT)?.toDate()?.time
                if (stored != null && at != null) onStored(HeartState(stored.toInt(), at))
            }
        }
    }
}
