package com.cyberity.cvsu

/** Mirrors the bounded score schema in firestore.rules. Spark rules cannot prove lab completion. */
internal object ScorePolicy {
    val levelIds = setOf(101, 102, 103, 104, 105, 106, 200, 201, 202, 203, 204, 205, 250,
        301, 302, 303, 304, 305, 350, 401, 402, 403, 404, 405, 450,
        501, 502, 503, 504, 505, 550, 601, 602, 603, 604, 605)

    fun cap(id: Int): Int = if (id == 200 || id !in levelIds) 0 else MAX_LEVEL_XP

    fun levelXp(raw: Map<*, *>?): Map<Int, Int> = raw.orEmpty().entries.mapNotNull { (key, value) ->
        val id = (key as? String)?.toIntOrNull() ?: return@mapNotNull null
        if (id !in levelIds || value !is Number) return@mapNotNull null
        id to value.toLong().coerceIn(0, cap(id).toLong()).toInt()
    }.toMap()

    fun balance(raw: Map<*, *>?, spent: Long): Int =
        (STARTING_XP.toLong() + levelXp(raw).values.sum() - spent.coerceAtLeast(0))
            .coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
}
