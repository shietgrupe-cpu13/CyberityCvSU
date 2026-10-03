package com.cyberity.cvsu

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class NetworkSecurityContentTest {
    @Test
    fun allSixLevelsUseTheSharedLabWithReachableEvidenceAndValidAnswers() {
        val unit = sampleLearningUnits().single { it.id == 5 }
        assertEquals(listOf(501, 502, 503, 504, 550, 505), unit.levels.map { it.id })
        val assets = listOf(File("src/main/assets"), File("app/src/main/assets"))
            .first { it.isDirectory }
        for (level in unit.levels) {
            assertTrue(level.type in setOf(LevelType.SIMULATION, LevelType.CHALLENGE))
            val content = contentFor(level.id) as LevelContent.Lab
            val folder = File(assets, "simulations/${content.lab.assetDir}")
            val script = File(folder, "script.js").readText()
            assertTrue(File(folder, "styles.css").isFile)
            assertEquals("desk.html?fresh=1", content.lab.startPage)
            assertEquals(level.id, content.lab.levelId)
            assertEquals(3, content.lab.tasks.size)
            assertEquals(3, content.lab.tasks.map { it.id }.distinct().size)
            assertEquals(3, content.lab.tasks.map { it.entryPage }.distinct().size)
            assertEquals(6, (content.clueLabels.keys - content.lab.dangerousClues.toSet()).size)
            assertTrue(content.clueLabels.keys.containsAll(content.lab.dangerousClues))
            content.lab.tasks.forEachIndexed { index, task ->
                assertTrue(task.guide.isNotEmpty())
                assertTrue(task.steps.isNotEmpty())
                assertEquals(listOf("net_${level.id}_${index}_inspected", "net_${level.id}_${index}_verified"), task.requiredClues)
                assertTrue(content.clueLabels.keys.containsAll(task.requiredClues))
                assertFalse(task.requiredClues.any { it in content.lab.dangerousClues })
                assertEquals(2, task.hints.size)
                val page = File(folder, task.entryPage!!.substringBefore('?'))
                assertTrue(page.isFile)
                assertTrue(page.readText().contains(if (index == 0) "renderNetworkDashboard()" else "mountNetworkCase(${level.id},$index)"))
                assertTrue(page.readText().contains("src=\"script.js\""))
                assertTrue(page.readText().contains("href=\"styles.css\""))
                when (val answer = task.answer) {
                    is LabAnswer.Choice -> {
                        assertTrue(answer.correctIndex in answer.options.indices)
                        answer.options.indices.forEach { option ->
                            assertEquals(option == answer.correctIndex, LabValidator.isCorrect(answer, "", option))
                        }
                    }
                    is LabAnswer.Flag -> {
                        assertEquals(505, level.id)
                        assertEquals(2, index)
                        val code = Regex("CYBERITY\\{[^}]+}").find(script)!!.value
                        assertTrue(LabValidator.isCorrect(answer, code, null))
                        assertFalse(LabValidator.isCorrect(answer, "CYBERITY{wrong}", null))
                    }
                    else -> fail("Unexpected network answer type")
                }
            }
            assertTrue(script.contains("\"${level.id}\":"))
        }
    }

    @Test
    fun unitFiveUnlocksInCurriculumOrderAndLeadsToIncidentResponse() {
        var units = sampleLearningUnits()
        units.flatMap { it.levels }.takeWhile { it.id != 501 }.forEach { level ->
            units = units.withLevelCompleted(level.id)
        }
        for ((id, next) in listOf(501 to 502, 502 to 503, 503 to 504, 504 to 550, 550 to 505, 505 to 601)) {
            assertEquals(LevelStatus.CURRENT, units.flatMap { it.levels }.single { it.id == id }.status)
            units = units.withLevelCompleted(id)
            assertEquals(LevelStatus.COMPLETED, units.flatMap { it.levels }.single { it.id == id }.status)
            assertEquals(LevelStatus.CURRENT, units.flatMap { it.levels }.single { it.id == next }.status)
        }
        assertTrue(units.single { it.id == 5 }.isComplete)
        val completed = units.flatMap { it.levels }.filter { it.status == LevelStatus.COMPLETED }.map { it.id }.toSet()
        assertEquals(units, sampleLearningUnits().withLevelsCompleted(completed))
    }
}
