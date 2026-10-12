package com.cyberity.cvsu

import org.junit.Assert.*
import org.junit.Test

class ScorePolicyTest {
    @Test fun scoreCatalogMatchesCurriculumAndActualGrading() {
        assertEquals(sampleLearningUnits().flatMap { it.levels }.map { it.id }.toSet(), ScorePolicy.levelIds)
        assertEquals(150, MAX_LEVEL_XP)
        assertEquals(0, ScorePolicy.cap(200))
    }
    @Test fun oversizedUnknownAndNegativeEntriesDoNotInflateBalance() {
        assertEquals(200, ScorePolicy.balance(mapOf("101" to Long.MAX_VALUE, "102" to -1, "999" to 9000), 0))
        assertEquals(50, ScorePolicy.balance(mapOf("200" to 50), 0))
    }
    @Test fun startingGrantAndHintCostsAreIncludedExactlyOnce() {
        assertEquals(135, ScorePolicy.balance(mapOf("101" to 100), 15))
        assertEquals(0, ScorePolicy.balance(mapOf("101" to 100), Long.MAX_VALUE))
    }
}
