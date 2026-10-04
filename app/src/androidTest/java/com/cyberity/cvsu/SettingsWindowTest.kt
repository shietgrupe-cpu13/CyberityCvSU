package com.cyberity.cvsu

import android.view.inspector.WindowInspector
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.test.filters.SdkSuppress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Security must change content without closing/reopening the window over Learn. */
@SdkSuppress(minSdkVersion = 29)
class SettingsWindowTest {
    @get:Rule val compose = createComposeRule()

    @Test fun enteringAndLeavingSecurityKeepsTheSettingsWindow() {
        compose.setContent { SettingsDialog(onDismiss = {}) }
        compose.waitForIdle()
        val before = compose.runOnIdle { WindowInspector.getGlobalWindowViews().toSet() }
        compose.onNodeWithText("Security").performClick()
        compose.onNodeWithText("Protect your account").assertExists()
        compose.onAllNodesWithText("Planned").assertCountEquals(0)
        compose.runOnIdle { assertEquals(before, WindowInspector.getGlobalWindowViews().toSet()) }
        compose.onNodeWithContentDescription("Back to settings").performClick()
        compose.onNodeWithText("Make Cyberity yours").assertExists()
        compose.runOnIdle { assertEquals(before, WindowInspector.getGlobalWindowViews().toSet()) }
    }

    @Test fun cancellingVerificationNeverRunsTheSensitiveAction() {
        var verified = false
        var cancelled = false
        compose.setContent {
            AccountVerificationDialog("Verify account", "Confirm before continuing.",
                onVerified = { verified = true }, onCancel = { cancelled = true }, onBusyChanged = {})
        }
        compose.onNodeWithText("Verify and continue").assertIsNotEnabled()
        compose.onNodeWithText("Cancel").performClick()
        compose.runOnIdle { assertTrue(cancelled); assertFalse(verified) }
    }

    @Test fun privacyAndHelpUseTheSameWindowAsTheDashboard() {
        compose.setContent { SettingsDialog(onDismiss = {}) }
        compose.waitForIdle()
        val before = compose.runOnIdle { WindowInspector.getGlobalWindowViews().toSet() }
        compose.onNodeWithText("Privacy").performScrollTo().performClick()
        compose.onNodeWithText("Your information").assertExists()
        compose.runOnIdle { assertEquals(before, WindowInspector.getGlobalWindowViews().toSet()) }
        compose.waitUntil(timeoutMillis = 15_000) {
            !compose.onNodeWithContentDescription("Back to settings").fetchSemanticsNode()
                .config.contains(SemanticsProperties.Disabled)
        }
        compose.onNodeWithContentDescription("Back to settings").performClick()
        compose.onNodeWithText("Help & About").performScrollTo().performClick()
        compose.onNodeWithText("Help topics").assertExists()
        compose.runOnIdle { assertEquals(before, WindowInspector.getGlobalWindowViews().toSet()) }
        compose.onNodeWithContentDescription("Back to settings").performClick()
        compose.onNodeWithText("Make Cyberity yours").assertExists()
    }
}
