package com.cyberity.cvsu

import androidx.compose.runtime.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Rule
import org.junit.Test

class YearLevelFieldTest {
    @get:Rule val compose = createComposeRule()
    @Test fun existingStudentsCanSelectARealYearWithoutDefaultingToFirstYear() {
        compose.setContent {
            var year by remember { mutableStateOf<Int?>(null) }
            YearLevelField(year, { year = it })
        }
        compose.onNodeWithText("Select year level").assertIsDisplayed().performClick()
        compose.onNodeWithText("3rd year").performClick()
        compose.onNodeWithText("3rd year").assertIsDisplayed()
    }
}
