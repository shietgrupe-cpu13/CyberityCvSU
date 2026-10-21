package com.cyberity.cvsu

/** Calendar days use Philippine time for every student, regardless of device timezone. */
internal object StreakPolicy {
    private const val DAY = 86_400_000L
    fun day(at: Long): Long = Math.floorDiv(at + 8 * 3_600_000L, DAY)
    fun next(previous: Int, lastAt: Long?, now: Long): Int = when {
        previous <= 0 -> 1
        lastAt == null -> 1
        day(now) == day(lastAt) -> previous
        day(now) == day(lastAt) + 1 -> previous + 1
        else -> 1
    }
    fun visible(previous: Int, lastAt: Long?, now: Long): Int =
        if (lastAt != null && day(now) - day(lastAt) in 0L..1L) previous else 0
}
