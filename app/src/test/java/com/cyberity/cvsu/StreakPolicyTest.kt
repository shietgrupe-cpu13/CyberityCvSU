package com.cyberity.cvsu

import org.junit.Assert.assertEquals
import org.junit.Test

class StreakPolicyTest {
    private val day = 86_400_000L
    // 16:00 UTC is midnight in the Philippines.
    private val midnight = 20_000 * day + 16 * 3_600_000L

    @Test fun countsCalendarDaysRatherThanElapsedHours() {
        assertEquals(1, StreakPolicy.next(0, null, midnight))
        assertEquals(4, StreakPolicy.next(4, midnight, midnight + day - 1))
        assertEquals(5, StreakPolicy.next(4, midnight - 1, midnight))
        assertEquals(1, StreakPolicy.next(4, midnight - day - 1, midnight))
    }
    @Test fun yesterdayRemainsRecoverableUntilTonight() {
        assertEquals(4, StreakPolicy.visible(4, midnight - 1, midnight + day - 1))
        assertEquals(0, StreakPolicy.visible(4, midnight - 1, midnight + day))
        assertEquals(0, StreakPolicy.visible(4, null, midnight))
    }
    @Test fun administratorCorrectionsContinueNormally() {
        assertEquals(25, StreakPolicy.visible(25, midnight, midnight + 1))
        assertEquals(25, StreakPolicy.next(25, midnight, midnight + 1))
        assertEquals(26, StreakPolicy.next(25, midnight, midnight + day))
        assertEquals(0, StreakPolicy.visible(0, midnight, midnight + 1))
        assertEquals(1, StreakPolicy.next(0, midnight, midnight + 1))
    }
}
