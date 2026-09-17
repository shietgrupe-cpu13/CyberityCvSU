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
