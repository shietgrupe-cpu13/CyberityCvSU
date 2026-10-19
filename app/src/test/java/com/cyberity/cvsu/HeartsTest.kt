package com.cyberity.cvsu

import org.junit.Assert.*
import org.junit.Test

class HeartsTest {
    @Test fun cachedServerTimeSurvivesProcessRestartOnlyOnTheSameBoot() {
        val anchor = HeartClockAnchor(serverAt = 1_000_000L, elapsedAt = 50_000L, boot = 12)
        assertEquals(1_900_000L, anchor.timeAt(950_000L, 12))
        assertNull(anchor.timeAt(950_000L, 13))
        assertNull(anchor.timeAt(1_000L, 12))
        assertNull(anchor.timeAt(950_000L, -1))
    }
    @Test fun oneHeartReturnsEveryFifteenMinutes() {
        val start = 1_000L
        val state = HeartState(1, start)
        assertEquals(1, state.heartsAt(start + 899_999L))
        assertEquals(2, state.heartsAt(start + 900_000L))
        assertEquals(3, state.heartsAt(start + 1_800_000L))
        assertEquals(MAX_HEARTS, state.heartsAt(start + 9_000_000L))
        assertEquals(1L, state.millisToNextHeart(start + 899_999L))
        assertEquals("15:00", formatRefill(state.millisToNextHeart(start)!!))
    }

    @Test fun accountStatesKeepIndependentRefillTimes() {
        val first = HeartState(0, 1_000L)
        val second = HeartState(0, 451_000L)
        assertEquals(1, first.heartsAt(901_000L))
        assertEquals(0, second.heartsAt(901_000L))
        assertEquals(450_000L, second.millisToNextHeart(901_000L))
    }
}
