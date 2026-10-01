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
        for (level in unit.levels.filter { it.id != 200 }) {
            val content = contentFor(level.id) as LevelContent.Lab
            val lab = content.lab
            assertEquals(level.id, lab.levelId)
            assertEquals(3, lab.tasks.size)
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
                val answer = task.answer as LabAnswer.Text
                assertTrue(LabValidator.isCorrect(answer, answer.accepted.single().lowercase(), null))
                assertFalse(LabValidator.isCorrect(answer, "CASE-WRONG", null))
            }
        }
    }
}
