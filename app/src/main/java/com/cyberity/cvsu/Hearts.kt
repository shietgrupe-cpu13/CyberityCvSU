package com.cyberity.cvsu

import androidx.compose.runtime.Immutable
import java.util.Locale

// ===========================================================================
// HEARTS
// ===========================================================================
// Only two values are stored: how many hearts the student had at [HeartState.updatedAt],
// and that timestamp. Refill is never written anywhere — it's calculated from
// elapsed time whenever it's read, so there are no timers or background jobs.

const val MAX_HEARTS = 5
const val MIN_HEARTS_TO_START = 1
const val HEART_REFILL_MILLIS = 10 * 1000L  // TESTING ONLY — revert to 30 * 60 * 1000L before shipping

@Immutable
data class HeartState(val hearts: Int, val updatedAt: Long) {

    /** Hearts actually available at [now], including refill since [updatedAt]. */
    fun heartsAt(now: Long): Int {
        if (hearts >= MAX_HEARTS) return MAX_HEARTS
        val gained = (now - updatedAt).coerceAtLeast(0L) / HEART_REFILL_MILLIS
        return (hearts + gained).coerceAtMost(MAX_HEARTS.toLong()).toInt()
    }

    /** Time until the next heart refills, or null when already full. */
    fun millisToNextHeart(now: Long): Long? {
        if (heartsAt(now) >= MAX_HEARTS) return null
        val elapsed = (now - updatedAt).coerceAtLeast(0L)
        return HEART_REFILL_MILLIS - (elapsed % HEART_REFILL_MILLIS)
    }

    /**
     * State after one mistake. The refill clock restarts from [now] — which is
     * also what the server records, so local and stored state agree.
     */
    fun loseHeart(now: Long): HeartState =
        HeartState((heartsAt(now) - 1).coerceAtLeast(0), now)

    companion object {
        val FULL = HeartState(MAX_HEARTS, 0L)
    }
}

/** 29:05 style countdown. The wait for one heart is never more than 30 minutes. */
fun formatRefill(millis: Long): String {
    val seconds = (millis + 999) / 1000
    return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60)
}
