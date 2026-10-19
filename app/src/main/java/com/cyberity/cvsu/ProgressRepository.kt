package com.cyberity.cvsu

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
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
        return ServerClock.syncTask(uid).continueWithTask { clock ->
            clock.result // A completion must not calculate its streak using the phone date.
            db.runTransaction { tx ->
                val document = tx.get(ref)
                val previous = saved(document)
                val awards = if (levelId in previous.levelXp) previous.levelXp else previous.levelXp + (levelId to xp)
                val result = previous.copy(completed = previous.completed + levelId, levelXp = awards)
                val fields = mutableMapOf<String, Any>(FIELD_COMPLETED to result.completed.toList(),
                    FIELD_LEVEL_XP to awards.mapKeys { it.key.toString() })
                if (levelId != 200) {
                    fields["streakDays"] = StreakPolicy.next(
                        (document.getLong("streakDays") ?: 0).toInt(),
                        document.getTimestamp("streakLastActivityAt")?.toDate()?.time, checkNotNull(ServerClock.trustedNow()))
                    fields["streakLastActivityAt"] = FieldValue.serverTimestamp()
                    fields["streakLevelId"] = levelId
                }
                tx.set(ref, fields, SetOptions.merge())
                result
            }
        }.continueWithTask { completion ->
            if (completion.isSuccessful) {
                Tasks.forResult(completion.result)
            } else {
                val failure = completion.exception ?: IllegalStateException("Completion unavailable.")
                android.util.Log.w("CyberityProgress", "Completion write failed for level $levelId; checking server", failure)
                // A lost acknowledgement, or a failed streak update on a replay, must
                // not strand a result that the server has already confirmed. Never
                // use the local cache as proof of completion.
                ref.get(Source.SERVER).continueWithTask { confirmation ->
                    val snapshot = if (confirmation.isSuccessful) confirmation.result else null
                    val stored = snapshot?.takeIf {
                        !it.metadata.isFromCache && !it.metadata.hasPendingWrites()
                    }?.let { saved(it) }
                    if (stored != null && levelId in stored.completed && levelId in stored.levelXp) {
                        Tasks.forResult(stored)
                    } else {
                        Tasks.forException(failure)
                    }
                }
            }
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
     * A missing record starts full; a failed server read fails the task.
     */
    private fun heartState(snapshot: DocumentSnapshot): HeartState {
        val hearts = snapshot.getLong(FIELD_HEARTS)
        val updatedAt = snapshot.getTimestamp(FIELD_HEARTS_UPDATED_AT)?.toDate()?.time
        return if (hearts != null && updatedAt != null) HeartState(hearts.toInt(), updatedAt)
        else HeartState.FULL
    }

    fun loadHearts(uid: String): Task<HeartState> =
        db.collection(COLLECTION).document(uid).get(Source.SERVER).continueWith { read ->
            heartState(read.result)
        }

    /**
     * Records a mistake: the new count, and the moment it happened as stamped by
     * the server. The app never sends the time, so the refill clock can't be moved
     * by changing the device clock.
     *
     * The transaction serializes concurrent devices and reads the stored balance.
     * Rules validate exactly one debit against server time before committing.
     */
    fun spendHeart(uid: String, operationId: String): Task<HeartState> {
        require(operationId.matches(Regex("[a-f0-9-]{36}")))
        return spendHeartAttempt(uid, operationId, 2)
    }

    private fun spendHeartAttempt(uid: String, operationId: String, retries: Int): Task<HeartState> {
        val doc = db.collection(COLLECTION).document(uid)
        val receipt = doc.collection("heartDebits").document(operationId)
        val clock = ServerClock.trustedNow()?.let { Tasks.forResult(it) } ?: ServerClock.syncTask(uid)
        return clock.continueWithTask { synced ->
            synced.result
            db.runTransaction { transaction ->
                val current = heartState(transaction.get(doc))
                val existing = transaction.get(receipt)
                if (existing.exists()) return@runTransaction current
                val now = checkNotNull(ServerClock.trustedNow())
                val exhausted = current.heartsAt(now) == 0
                val next = if (exhausted) current else current.loseHeart(now)
                if (!exhausted) transaction.set(doc, mapOf(
                    FIELD_HEARTS to next.hearts, "lastHeartDebit" to operationId,
                    FIELD_HEARTS_UPDATED_AT to FieldValue.serverTimestamp()), SetOptions.merge())
                transaction.set(receipt, mapOf("createdAt" to FieldValue.serverTimestamp(),
                    "heartsAfter" to next.heartsAt(now), "exhausted" to exhausted))
                next
            }
        }.continueWithTask { debit ->
            if (debit.isSuccessful) Tasks.forResult(debit.result) else {
                val error = debit.exception ?: IllegalStateException("Heart usage unavailable.")
                val denied = generateSequence<Throwable>(error) { it.cause }.any {
                    it is com.google.firebase.firestore.FirebaseFirestoreException &&
                        it.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED
                }
                // A racing debit or a refill boundary may invalidate the count
                // before rules run. Only rejected writes are safe to retry.
                if (denied && retries > 0) ServerClock.syncTask(uid).continueWithTask { clockSync ->
                    clockSync.result
                    spendHeartAttempt(uid, operationId, retries - 1)
                }
                else Tasks.forException(error)
            }
        }
    }
}
