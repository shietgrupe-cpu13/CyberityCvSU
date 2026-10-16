package com.cyberity.cvsu

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot

data class SavedProgress(val completed: Set<Int>, val levelXp: Map<Int, Int>, val xpSpent: Int)

data class HintPurchase(val attemptId: String, val levelId: Int, val taskId: String, val number: Int) {
    val cost: Int get() = nextHintCost(number - 1)
    val id: String get() = LabValidator.sha256("$attemptId:$levelId:$taskId:$number")
}

// ===========================================================================
// PROGRESS PERSISTENCE
// ===========================================================================
// One document per signed-in user, holding the ids of every level they've
// completed, the XP each one paid out, the XP spent on hints, and their heart
// state. Level status and which level is CURRENT
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
    private const val FIELD_LEVEL_XP = "levelXp"
    private const val FIELD_XP_SPENT = "xpSpent"
    private const val FIELD_HEARTS = "hearts"
    private const val FIELD_HEARTS_UPDATED_AT = "heartsUpdatedAt"

    private fun saved(document: DocumentSnapshot) = SavedProgress(
        (document.get(FIELD_COMPLETED) as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }?.toSet().orEmpty(),
        ScorePolicy.levelXp(document.get(FIELD_LEVEL_XP) as? Map<*, *>),
        (document.getLong(FIELD_XP_SPENT) ?: 0).toInt()
    )

    fun load(uid: String): Task<SavedProgress> = db.collection(COLLECTION).document(uid)
        .get(Source.SERVER).continueWith { saved(it.result) }

    /** Preserve the original award when acknowledgement is lost or devices race. */
    fun completeLevel(uid: String, levelId: Int, xp: Int): Task<SavedProgress> {
        require(levelId in ScorePolicy.levelIds && xp in 0..ScorePolicy.cap(levelId))
        val ref = db.collection(COLLECTION).document(uid)
        return db.runTransaction { tx ->
            val previous = saved(tx.get(ref))
            val awards = if (levelId in previous.levelXp) previous.levelXp else previous.levelXp + (levelId to xp)
            val result = previous.copy(completed = previous.completed + levelId, levelXp = awards)
            tx.set(ref, mapOf(FIELD_COMPLETED to result.completed.toList(),
                FIELD_LEVEL_XP to awards.mapKeys { it.key.toString() }), SetOptions.merge())
            result
        }.addOnSuccessListener { LeaderboardRepository.publish(uid) }
    }

    /** Receipt and debit commit together; retrying this request cannot charge twice. */
    fun purchaseHint(uid: String, purchase: HintPurchase): Task<SavedProgress> {
        require(purchase.levelId in ScorePolicy.levelIds && purchase.number in 1..100)
        require(purchase.taskId.length in 1..100 && purchase.attemptId.matches(Regex("^[a-f0-9-]{36}$")))
        val progress = db.collection(COLLECTION).document(uid)
        val receipt = progress.collection("hintPurchases").document(purchase.id)
        return db.runTransaction { tx ->
            val existing = tx.get(receipt)
            val previous = saved(tx.get(progress))
            if (existing.exists()) {
                check(existing.getString("attemptId") == purchase.attemptId &&
                    existing.getLong("levelId") == purchase.levelId.toLong() &&
                    existing.getString("taskId") == purchase.taskId &&
                    existing.getLong("hintNumber") == purchase.number.toLong()) { "Hint purchase does not match." }
                previous
            } else {
                check(xpBalance(previous.levelXp.values.sum(), previous.xpSpent) >= purchase.cost) {
                    "You don't have enough XP for this hint."
                }
                val result = previous.copy(xpSpent = previous.xpSpent + purchase.cost)
                tx.set(progress, mapOf(FIELD_XP_SPENT to result.xpSpent,
                    "lastHintPurchase" to purchase.id), SetOptions.merge())
                tx.set(receipt, mapOf("attemptId" to purchase.attemptId, "levelId" to purchase.levelId,
                    "taskId" to purchase.taskId, "hintNumber" to purchase.number, "cost" to purchase.cost,
                    "spentBefore" to previous.xpSpent, "spentAfter" to result.xpSpent,
                    "createdAt" to FieldValue.serverTimestamp()))
                result
            }
        }.addOnSuccessListener { LeaderboardRepository.publish(uid) }
    }

    /** Reads back the set of completed level ids for [uid]. Empty on first run; failures retain cached state. */
    fun loadCompletedLevels(uid: String, onResult: (Set<Int>) -> Unit) {
        db.collection(COLLECTION).document(uid).get()
            .addOnSuccessListener { snapshot ->
                val ids = (snapshot.get(FIELD_COMPLETED) as? List<*>)
                    ?.mapNotNull { (it as? Number)?.toInt() }
                    ?.toSet()
                    ?: emptySet()
                onResult(ids)
            }
            .addOnFailureListener { android.util.Log.w("CyberityProgress", "Progress unavailable; keeping cache", it) }
    }

    /**
     * XP earned per level, keyed by level id. Stored per level rather than as a
     * running total so a replay can be told apart from a first clear, and so the
     * total is always recomputable from its parts.
     */
    fun loadLevelXp(uid: String, onResult: (Map<Int, Int>) -> Unit) {
        db.collection(COLLECTION).document(uid).get()
            .addOnSuccessListener { snapshot ->
                val raw = snapshot.get(FIELD_LEVEL_XP) as? Map<*, *>
                onResult(
                    ScorePolicy.levelXp(raw)
                )
            }
            .addOnFailureListener { android.util.Log.w("CyberityProgress", "Awards unavailable; keeping cache", it) }
    }

    /**
     * Total XP spent on hints, all-time. Kept as one running figure rather than
     * per level, because a hint stays bought even if the level is abandoned.
     */
    fun loadXpSpent(uid: String, onResult: (Int) -> Unit) {
        db.collection(COLLECTION).document(uid).get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.getLong(FIELD_XP_SPENT)?.toInt() ?: 0)
            }
            .addOnFailureListener { android.util.Log.w("CyberityProgress", "Spending unavailable; keeping cache", it) }
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
