package com.cyberity.cvsu

import org.junit.Assert.*
import org.junit.Test

class LessonContentTest {

    /** Levels that have a lesson so far. Add each new one here. */
    private val lessonLevels = listOf(101, 102, 103, 104)

    @Test
    fun everyLessonCoversEachLabTaskAndCitesItsSources() {
        for (levelId in lessonLevels) {
            val lesson = lessonFor(levelId)
            assertNotNull("Level $levelId has no lesson", lesson)
            lesson!!
            assertEquals(levelId, lesson.levelId)

            // Every task in the level is prepared for by at least one page.
            val lab = (contentFor(levelId) as LevelContent.Lab).lab
            val covered = lesson.pages.mapNotNull { it.task }.toSet()
            assertEquals("Level $levelId tasks without a lesson page", (1..lab.tasks.size).toSet(), covered)

            // Every page cites a source that exists, and every source is used.
            val sourceIds = lesson.sources.map { it.id }
            assertEquals(sourceIds.size, sourceIds.distinct().size)
            for (page in lesson.pages) {
                assertTrue("\"${page.title}\" cites nothing", page.sourceIds.isNotEmpty())
                assertTrue("\"${page.title}\" cites an unknown source", sourceIds.containsAll(page.sourceIds))
                assertTrue(page.paragraphs.isNotEmpty())
            }
            assertEquals(sourceIds.toSet(), lesson.pages.flatMap { it.sourceIds }.toSet())
            lesson.sources.forEach { assertTrue(it.url.startsWith("https://")) }
        }
    }

    @Test
    fun everyLessonEndsWithAtLeastTwoValidCheckQuestions() {
        for (levelId in lessonLevels) {
            val questions = lessonFor(levelId)!!.questions
            assertTrue("Level $levelId needs at least two questions", questions.size >= 2)
            for (q in questions) {
                assertTrue(q.options.size in 3..4)
                assertEquals(q.options.size, q.options.distinct().size)
                assertTrue(q.correctIndex in q.options.indices)
                assertTrue(q.explanation.isNotBlank())
            }
            // Options aren't shuffled, so the right answer must not sit in one spot.
            assertTrue(questions.map { it.correctIndex }.distinct().size > 1)
        }
    }
}
