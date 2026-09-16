package com.cyberity.cvsu

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

// ===========================================================================
// PROGRESS PERSISTENCE
// ===========================================================================
// One document per signed-in user, holding the ids of every level they've
// completed. That's the only state that needs to survive a session — everyone
// else (status, XP, which level is CURRENT) is derived from it by replaying
// withLevelCompleted() over the curriculum, so there's nothing else to store
// or keep in sync.

object ProgressRepository {

    private val db by lazy { FirebaseFirestore.getInstance() }

    private const val COLLECTION = "userProgress"
    private const val FIELD_COMPLETED = "completedLevels"

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
}
