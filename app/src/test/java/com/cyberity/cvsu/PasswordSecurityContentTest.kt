package com.cyberity.cvsu

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class PasswordSecurityContentTest {
    @Test
    fun everyUnitTwoLevelHasResolvableAssetsAndGatedTasks() {
        val unit = sampleLearningUnits().single { it.id == 2 }
        assertEquals(listOf(200, 201, 202, 203, 204, 250, 205), unit.levels.map { it.id })
        assertEquals(LevelType.LESSON, unit.levels.first().type)
        assertEquals(0, unit.levels.first().xpReward)
        assertTrue(unit.levels.first().title.startsWith("Level 0:"))
        val assets = listOf(File("src/main/assets"), File("app/src/main/assets"))
            .first { it.isDirectory }
        val levelFolders = mapOf(
            201 to "strong_passwords", 202 to "password_attacks",
            203 to "multi_factor_authentication", 204 to "account_protection",
            205 to "password_security_challenge", 250 to "hash_cracking_practice"
        )
        for (level in unit.levels.filter { it.id != 200 }) {
            val content = contentFor(level.id) as LevelContent.Lab
            val lab = content.lab
            assertEquals("UNIT 02/" + levelFolders.getValue(level.id), lab.assetDir)
            assertEquals(level.id, lab.levelId)
            assertEquals(3, lab.tasks.size)
            assertFalse(lab.briefing.contains("flag", ignoreCase = true))
            assertFalse(lab.tasks.any { it.objective.contains("paste", ignoreCase = true) })
            if (level.id != 205) {
                assertEquals(3, lab.tasks.map { it.steps }.distinct().size)
                assertTrue(lab.tasks.all { task -> task.steps.any { it.contains("**") } })
            }
            if (level.id == 201) {
                assertTrue(lab.tasks.first().objective.contains("Maya Santos"))
                assertFalse(lab.tasks.first().guide.any { it.contains("RSA") })
            }
            assertTrue(lab.tasks.all { it.objective.contains("same case") || it.objective.contains("Start the case") })
            if (level.id == 205) {
                assertTrue(lab.tasks.all { it.steps.isEmpty() })
                assertTrue(lab.briefing.contains("Hints are optional"))
            }
            assertTrue(File(assets, "simulations/${lab.assetDir}/${lab.startPage}").isFile)
            for (task in lab.tasks) {
                assertTrue(task.successFeedback.contains("What happened"))
                assertTrue(task.successFeedback.contains("Why it matters"))
                assertTrue(task.successFeedback.contains("What to do in real life"))
                assertTrue(task.guide.isNotEmpty())
                assertTrue(task.requiredClues.isNotEmpty())
                assertTrue(content.clueLabels.keys.containsAll(task.requiredClues))
                assertTrue(File(assets, "simulations/${lab.assetDir}/${task.entryPage}").isFile)
                val page = File(assets, "simulations/${lab.assetDir}/${task.entryPage}")
                Regex("(?:src|href)=\"([^\"]+)\"").findAll(page.readText()).forEach { match ->
                    assertTrue("Missing simulation dependency: ${match.groupValues[1]}",
                        File(page.parentFile, match.groupValues[1]).isFile)
                }
                assertTrue(File(page.parentFile, "../password_security/styles.css").isFile)
                val answer = task.answer as LabAnswer.Text
                assertTrue(LabValidator.isCorrect(answer, answer.accepted.single().lowercase(), null))
                assertFalse(LabValidator.isCorrect(answer, "CASE-WRONG", null))
            }
        }
    }
}
