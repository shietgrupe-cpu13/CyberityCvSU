package com.cyberity.cvsu

import android.content.Context

/**
 * A same-device cache of completed level ids and heart state, checked
 * synchronously when LearnScreen builds its initial state. Firestore
 * (ProgressRepository) is still the source of truth and reconciles this in
 * the background — this cache exists purely so the first frame shows real
 * progress instead of a default while that network read is in flight.
 */
object ProgressCache {

    private const val PREFS_NAME = "cyberity_progress_cache"
    private const val HEARTS_KEY_SUFFIX = ":hearts"

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
