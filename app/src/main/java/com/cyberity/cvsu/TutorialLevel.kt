package com.cyberity.cvsu

/** Shared entry for the welcome banner and both settings menus. */
fun tutorialLevel() = LearningLevel(
    100, "Level 0: CYBERITY Tutorial",
    "Learn how CYBERITY works before starting your cybersecurity training.",
    25, LevelType.LESSON, LevelStatus.CURRENT, durationMinutes = 3
)
