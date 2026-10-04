package com.cyberity.cvsu

import org.junit.Assert.*
import org.junit.Test

class AuthThrottleTest {
    private var now = 1_000_000L
    private var saved = AuthThrottle.State()
    private fun guard(interval: Long = 1_000L) = AuthThrottle(saved, { now }, { saved = it }, interval)
    private fun reject(guard: AuthThrottle) {
        assertTrue(guard.tryStart())
        guard.rejected()
        now += 1_000L
    }

    @Test fun fifthFailureBlocksAndCooldownSurvivesRecreation() {
        val guard = guard()
        repeat(4) { reject(guard) }
        assertTrue(guard.tryStart())
        guard.rejected()
        assertEquals(30L, guard.retryAfterSeconds())
        assertFalse(guard.tryStart())
        val restarted = guard()
        assertFalse(restarted.tryStart())
        now += 30_000L
        assertTrue(restarted.tryStart())
    }

    @Test fun repeatedFailureGroupsEscalateToFifteenMinutes() {
        val guard = guard()
        for (seconds in listOf(30L, 60L, 120L, 240L, 480L, 900L)) {
            repeat(4) { reject(guard) }
            assertTrue(guard.tryStart())
            guard.rejected()
            assertEquals(seconds, guard.retryAfterSeconds())
            now += seconds * 1_000
        }
    }

    @Test fun duplicateRequestsAreBlockedUntilCallbackAndMinimumInterval() {
        val guard = guard()
        assertTrue(guard.tryStart())
        assertFalse(guard.tryStart())
        now += 2_000
        assertFalse(guard.tryStart())
        guard.finished()
        assertTrue(guard.tryStart())
    }

    @Test fun networkErrorsAndMfaChallengeDoNotCountOrResetFailures() {
        val guard = guard()
        repeat(4) { reject(guard) }
        repeat(3) {
            assertTrue(guard.tryStart())
            guard.finished()
            now += 1_000
        }
        assertTrue(guard.tryStart())
        guard.rejected()
        assertEquals(30L, guard.retryAfterSeconds())
    }

    @Test fun fullAuthenticationResetsFailuresAndPenalty() {
        val guard = guard()
        repeat(5) { reject(guard) }
        now += 30_000
        assertTrue(guard.tryStart())
        guard.authenticated()
        repeat(4) { reject(guard) }
        assertTrue(guard.tryStart())
        guard.rejected()
        assertEquals(30L, guard.retryAfterSeconds())
    }

    @Test fun serverThrottleIsRespectedAfterRestart() {
        val guard = guard()
        assertTrue(guard.tryStart())
        guard.serverThrottled()
        assertEquals(60L, guard().retryAfterSeconds())
        assertFalse(guard().tryStart())
    }

    @Test fun resetRequestsHaveSeparateOneMinuteCooldown() {
        val resets = guard(60_000)
        assertTrue(resets.tryStart())
        resets.finished()
        assertEquals(60L, guard(60_000).retryAfterSeconds())
        now += 59_999
        assertFalse(resets.tryStart())
        now += 1
        assertTrue(resets.tryStart())
    }

    @Test fun idlePeriodClearsOldFailureHistory() {
        val guard = guard()
        repeat(4) { reject(guard) }
        now += AuthThrottle.IDLE_RESET_MILLIS
        reject(guard)
        assertEquals(1, saved.failures)
        assertEquals(0L, guard.retryAfterSeconds())
    }
}
