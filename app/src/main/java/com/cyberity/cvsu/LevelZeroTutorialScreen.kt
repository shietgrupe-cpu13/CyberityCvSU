package com.cyberity.cvsu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Level 0: the tutorial is a short practice lab played on the real [LabScreen],
 * so the player learns the exact screen every later level uses — investigate the
 * simulation, unlock the answer with evidence, pick or type it, capture a flag.
 *
 * Hearts and XP follow the real rules but live only here: a wrong answer takes a
 * practice heart and a hint takes practice XP, and neither touches the player's
 * saved balance. Content lives in [tutorialLab].
 */
@Composable
fun LevelZeroTutorialScreen(
    onCompleteTutorial: () -> Unit,
    onExit: () -> Unit
) {
    var practiceHearts by remember { mutableIntStateOf(MAX_HEARTS) }
    var practiceXp by remember { mutableIntStateOf(STARTING_XP) }
    val lab = remember { tutorialLab() }
    val coach = remember { tutorialCoach() }

    LabScreen(
        lab = lab,
        coach = coach,
        xpReward = MAX_LEVEL_XP,
        clueLabels = tutorialClueLabels,
        onExit = onExit,
        onComplete = { _, _, _ -> onCompleteTutorial() },
        // Never drops below one: the tutorial can't lock the player out of itself.
        onMistake = { practiceHearts = (practiceHearts - 1).coerceAtLeast(1) },
        hearts = practiceHearts,
        xpBalance = practiceXp,
        onSpendXp = { practiceXp = (practiceXp - it).coerceAtLeast(0) },
        practice = true
    )
}
