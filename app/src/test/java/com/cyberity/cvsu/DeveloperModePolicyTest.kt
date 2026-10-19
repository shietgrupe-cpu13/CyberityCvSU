package com.cyberity.cvsu

import org.junit.Assert.*
import org.junit.Test

class DeveloperModePolicyTest {
    @Test fun unlimitedHeartsNeverRewriteTheNormalBalanceAndRevocationRestoresIt() {
        val normal = HeartState(2, 1_000L)
        assertEquals(2, DeveloperModePolicy.heartBalance(normal, 2_000L, false))
        assertEquals(5, DeveloperModePolicy.heartBalance(normal, 2_000L, true))
        assertFalse(DeveloperModePolicy.spendsHearts(true))
        assertTrue(DeveloperModePolicy.spendsHearts(false))
        assertEquals(2, DeveloperModePolicy.heartBalance(normal, 2_000L, false))
        assertEquals(3, DeveloperModePolicy.heartBalance(normal, 901_000L, false))
    }
    @Test fun missingOrMalformedGrantsNeverUnlockLevels() {
        assertFalse(DeveloperModePolicy.unlockAllLevels(null, false, false))
        assertFalse(DeveloperModePolicy.unlockAllLevels(emptyMap(), false, false))
        assertFalse(DeveloperModePolicy.unlockAllLevels(mapOf("isTester" to "true"), false, false))
        assertFalse(DeveloperModePolicy.unlockAllLevels(mapOf("unlockAllLevels" to true), false, false))
    }
    @Test fun onlyConfirmedExplicitTrueEnablesAccess() {
        val grant = mapOf<String, Any>("isTester" to true)
        assertTrue(DeveloperModePolicy.unlockAllLevels(grant, false, false))
        assertFalse(DeveloperModePolicy.unlockAllLevels(grant, true, false))
        assertFalse(DeveloperModePolicy.unlockAllLevels(grant, false, true))
        assertFalse(DeveloperModePolicy.unlockAllLevels(mapOf("isTester" to false), false, false))
    }
    @Test fun unlockingIsDisplayOnlyAndNormalProgressionRemainsRecoverable() {
        val normal = sampleLearningUnits().withLevelCompleted(101)
        val unlocked = normal.withAllLevelsUnlocked()
        assertTrue(unlocked.flatMap { it.levels }.none { it.status == LevelStatus.LOCKED })
        assertTrue(normal.flatMap { it.levels }.any { it.status == LevelStatus.LOCKED })
        assertEquals(listOf(101), unlocked.flatMap { it.levels }.filter { it.status == LevelStatus.COMPLETED }.map { it.id })
        assertFalse(CertificatePolicy.canDownload(UserProfile("202310502", "Tester", yearLevel = 3), setOf(101), CertificatePolicy.requiredLevels(unlocked)))
    }
}
