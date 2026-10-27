package com.cyberity.cvsu

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cyberity.cvsu.ui.theme.MyFirstTryTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class UpdateAndNameScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun cancelAllowsReopeningAndDownloadRequiresItsOwnClick() {
        var downloads = 0
        var cancels = 0
        compose.setContent {
            var open by remember { mutableStateOf(false) }
            MyFirstTryTheme {
                Button(onClick = { open = true }) { Text("Check for Updates") }
                if (open) UpdatePrompt("v9.0.0", { downloads++; open = false }, { cancels++; open = false })
            }
        }
        repeat(2) {
            compose.onNodeWithText("Check for Updates").performClick()
            compose.onNodeWithText("Update Available").assertIsDisplayed()
            compose.onNodeWithText("Cancel").performClick()
            compose.onNodeWithText("Update Available").assertDoesNotExist()
        }
        compose.runOnIdle { assertEquals(0, downloads); assertEquals(2, cancels) }
        compose.onNodeWithText("Check for Updates").performClick()
        compose.onNodeWithText("Download").performClick()
        compose.runOnIdle { assertEquals(1, downloads) }
    }

    @Test fun currentReleaseCanBeCheckedRepeatedly() {
        compose.setContent { MyFirstTryTheme { CheckForUpdates() } }
        repeat(2) {
            compose.onNodeWithText("Check for Updates").performClick()
            compose.waitUntil(timeoutMillis = 35000) {
                compose.onAllNodesWithText("You’re up to date.").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("You’re up to date.").assertIsDisplayed()
        }
    }

    @Test fun firstAndLastNameWorkWithMiddleNameBlank() {
        var first = ""; var middle = ""; var last = ""
        compose.setContent {
            var f by remember { mutableStateOf("") }; var m by remember { mutableStateOf("") }; var l by remember { mutableStateOf("") }
            MyFirstTryTheme { StudentNameFields(f, { f = it; first = it }, m, { m = it; middle = it }, l, { l = it; last = it }) }
        }
        compose.onNodeWithText("First Name").performTextInput("Ana")
        compose.onNodeWithText("Last Name").performTextInput("Cruz")
        compose.runOnIdle {
            assertNull(StudentIdRules.validateNameParts(first, middle, last))
            assertEquals("Ana Cruz", StudentIdRules.composeFullName(first, middle, last))
        }
    }
}
