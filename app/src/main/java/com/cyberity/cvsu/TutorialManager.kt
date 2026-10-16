package com.cyberity.cvsu

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect

enum class TutorialStep(val stepNumber: Int, val totalSteps: Int = 9) {
    STEP1_HOME(1),
    STEP2_LEVEL_PREVIEW(2),
    STEP3_SCENARIO(3),
    STEP4_CHOICE(4),
    STEP5_HINT(5),
    STEP6_SUBMIT(6),
    STEP7_FEEDBACK(7),
    STEP8_PROGRESS(8),
    STEP9_FINISH(9)
}

object TutorialManager {
    var isTutorialActive by mutableStateOf(false)
    var currentStep by mutableStateOf(TutorialStep.STEP1_HOME)
    var targetBounds by mutableStateOf<Rect?>(null)

    fun startTutorial() {
        isTutorialActive = true
        currentStep = TutorialStep.STEP1_HOME
        targetBounds = null
    }

    fun finishTutorial() {
        isTutorialActive = false
        targetBounds = null
    }
}
