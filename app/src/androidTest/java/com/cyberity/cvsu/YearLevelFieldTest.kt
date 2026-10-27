package com.cyberity.cvsu

import androidx.compose.runtime.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class YearLevelFieldTest {
    @get:Rule val compose = createComposeRule()
    @Test fun existingStudentsCanSelectARealYearWithoutDefaultingToFirstYear() {
        compose.setContent {
            var year by remember { mutableStateOf<Int?>(null) }
            YearLevelField(year, { year = it })
        }
        compose.onNodeWithText("Select Year Level").assertIsDisplayed().performClick()
        compose.onNodeWithText("3rd year").performClick()
        compose.onNodeWithText("3rd year").assertIsDisplayed()
    }

    @Test fun disablingFieldClosesTheMenuAndDoesNotReopenItWhenEnabledAgain() {
        var enabled by mutableStateOf(true)
        var chosen: Int? = null
        compose.setContent { YearLevelField(null, { chosen = it }, enabled) }
        compose.onNodeWithText("Select Year Level").performClick()
        compose.onNodeWithText("3rd year").assertIsDisplayed()
        compose.runOnIdle { enabled = false }
        compose.onNodeWithText("3rd year").assertDoesNotExist()
        compose.onNodeWithText("Select Year Level").assertIsNotEnabled()
        compose.runOnIdle { enabled = true }
        compose.onNodeWithText("3rd year").assertDoesNotExist()
        compose.runOnIdle { assertNull(chosen) }
    }
}
