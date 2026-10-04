package com.cyberity.cvsu

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cyberity.cvsu.ui.theme.MyFirstTryTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProfileCheckScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun pendingProfileExplainsLoadingAndAllowsSignOut() {
        var signOuts = 0
        compose.setContent {
            MyFirstTryTheme {
                ProfileCheckScreen(false, null, {}, { signOuts++ })
            }
        }
        compose.onNodeWithText("Checking your student profile").assertIsDisplayed()
        compose.onNodeWithText("Sign out").performClick()
        compose.runOnIdle { assertEquals(1, signOuts) }
    }

    @Test fun failedProfileAllowsRetry() {
        var retries = 0
        compose.setContent {
            MyFirstTryTheme {
                ProfileCheckScreen(true, null, { retries++ }, {})
            }
        }
        compose.onNodeWithText("Couldn't load your profile").assertIsDisplayed()
        compose.onNodeWithText("Try again").performClick()
        compose.runOnIdle { assertEquals(1, retries) }
    }

    @Test fun slowProfileExplainsConnectionAndStillAllowsSignOut() {
        var signOuts = 0
        compose.setContent {
            MyFirstTryTheme {
                ProfileCheckScreen(false, null, {}, { signOuts++ }, slow = true)
            }
        }
        compose.onNodeWithText("Still checking your student profile").assertIsDisplayed()
        compose.onNodeWithText("This is taking longer than usual.", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Sign out").performClick()
        compose.runOnIdle { assertEquals(1, signOuts) }
    }
}
