package com.cyberity.cvsu

import org.junit.Assert.*
import org.junit.Test

class AuthThrottleTest {
    @Test fun sameBootRestartCountsActualElapsedTime() {
        val state = AuthThrottle.State(blockedUntil = 130_000, lastFailure = 100_000)
        val restored = AuthThrottle.restore(state, 100_000, 110_000, true)
        assertEquals(20L, AuthThrottle(restored, { 110_000 }).retryAfterSeconds())
    }

    @Test fun rebootReappliesWaitAndPreservesFailures() {
        val state = AuthThrottle.State(failures = 4, penalty = 2,
            blockedUntil = 130_000, lastFailure = 100_000, nextRequest = 101_000)
        val restored = AuthThrottle.restore(state, 100_000, 5_000, false)
        assertEquals(30L, AuthThrottle(restored, { 5_000 }).retryAfterSeconds())
        assertEquals(4, restored.failures)
        assertEquals(2, restored.penalty)
        assertEquals(6_000L, restored.nextRequest)
    }

    @Test fun elapsedClockRollbackCannotClearSavedWait() {
        val restored = AuthThrottle.restore(AuthThrottle.State(blockedUntil = 130_000),
            100_000, 5_000, true)
        assertEquals(30L, AuthThrottle(restored, { 5_000 }).retryAfterSeconds())
    }

    @Test fun passwordFailuresAndMfaFailuresShareBudgetAcrossRestart() {
        val passwords = guard()
        repeat(3) { reject(passwords) }
        passwords.finished() // password accepted, MFA still pending
        val mfa = guard()
        reject(mfa)
        assertTrue(mfa.tryStart())
        mfa.rejected()
        assertEquals(30L, mfa.retryAfterSeconds())
    }

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
