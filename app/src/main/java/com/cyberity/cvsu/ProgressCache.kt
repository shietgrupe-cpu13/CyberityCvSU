package com.cyberity.cvsu

import android.content.Context

/**
 * A same-device cache of completed level ids, earned XP and heart state, checked
 * synchronously when LearnScreen builds its initial state. Firestore
 * (ProgressRepository) is still the source of truth and reconciles this in
 * the background — this cache exists purely so the first frame shows real
 * progress instead of a default while that network read is in flight.
 */
object ProgressCache {

    private const val PREFS_NAME = "cyberity_progress_cache"
    private const val HEARTS_KEY_SUFFIX = ":hearts"
    private const val XP_KEY_SUFFIX = ":levelXp"
    private const val XP_SPENT_KEY_SUFFIX = ":xpSpent"
    private const val LESSONS_KEY_SUFFIX = ":lessonsRead"

    /** A result waiting for acknowledgement survives app restart and is safe to resend. */
    fun pendingAwards(context: Context, uid: String): Map<Int, Int> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("$uid:pendingAwards", "").orEmpty()
        return raw.split(";").mapNotNull {
            val parts = it.split("=")
            val level = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
            val xp = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
            level to xp
        }.toMap()
    }

    fun queueAward(context: Context, uid: String, level: Int, xp: Int): Boolean =
        writePending(context, uid, pendingAwards(context, uid) + (level to xp))

    fun acknowledgeAward(context: Context, uid: String, level: Int) {
        writePending(context, uid, pendingAwards(context, uid) - level)
    }

    private fun writePending(context: Context, uid: String, awards: Map<Int, Int>): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString("$uid:pendingAwards", awards.entries.joinToString(";") { "${it.key}=${it.value}" }).commit()

    fun load(context: Context, uid: String): Set<Int> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(uid, null) ?: return emptySet()
        return raw.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun save(context: Context, uid: String, completedIds: Set<Int>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(uid, completedIds.joinToString(","))
            .apply()
    }

    /** Stored as "levelId=xp;levelId=xp". Empty when nothing is cached yet. */
    fun loadLevelXp(context: Context, uid: String): Map<Int, Int> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(uid + XP_KEY_SUFFIX, null) ?: return emptyMap()
        return raw.split(";").mapNotNull { entry ->
            val parts = entry.split("=")
            val id = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
            val xp = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
            id to xp
        }.toMap()
    }

    fun saveLevelXp(context: Context, uid: String, levelXp: Map<Int, Int>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(uid + XP_KEY_SUFFIX, levelXp.entries.joinToString(";") { "${it.key}=${it.value}" })
            .apply()
    }

    fun loadXpSpent(context: Context, uid: String): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(uid + XP_SPENT_KEY_SUFFIX, 0)

    fun saveXpSpent(context: Context, uid: String, spent: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(uid + XP_SPENT_KEY_SUFFIX, spent)
            .apply()
    }

    /** Level ids whose lesson has been read through on this device. */
    fun loadLessonsRead(context: Context, uid: String): Set<Int> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(uid + LESSONS_KEY_SUFFIX, null) ?: return emptySet()
        return raw.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun saveLessonsRead(context: Context, uid: String, levelIds: Set<Int>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(uid + LESSONS_KEY_SUFFIX, levelIds.joinToString(","))
            .apply()
    }

    /** Stored as "hearts,updatedAt". Null when nothing is cached yet. */
    fun loadHearts(context: Context, uid: String): HeartState? {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(uid + HEARTS_KEY_SUFFIX, null) ?: return null
        val parts = raw.split(",")
        val hearts = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val updatedAt = parts.getOrNull(1)?.toLongOrNull() ?: return null
        return HeartState(hearts, updatedAt)
    }

    fun saveHearts(context: Context, uid: String, state: HeartState) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(uid + HEARTS_KEY_SUFFIX, "${state.hearts},${state.updatedAt}")
            .apply()
    }
}
