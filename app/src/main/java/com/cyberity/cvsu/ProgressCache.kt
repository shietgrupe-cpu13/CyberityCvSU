package com.cyberity.cvsu

import android.content.Context

/**
 * A same-device cache of completed level ids, checked synchronously when
 * LearnScreen builds its initial state. Firestore (ProgressRepository) is
 * still the source of truth and reconciles this in the background — this
 * cache exists purely so the first frame shows real progress instead of a
 * blank "level 101 only" default while that network read is in flight.
 */
object ProgressCache {

    private const val PREFS_NAME = "cyberity_progress_cache"

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
}
