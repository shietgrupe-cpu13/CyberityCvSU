package com.cyberity.cvsu

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.cyberity.cvsu.ui.theme.MyFirstTryTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class StudentNameAndLayoutTest {
    @get:Rule val compose = createComposeRule()

    @Test fun narrowSettingsKeepDescriptionsBelowReadableTitlesAtLargeTextSizes() {
        var scale by mutableFloatStateOf(1f)
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                MyFirstTryTheme {
                    Surface(color = AppCard, modifier = Modifier.width(280.dp)) {
                        Column {
                            SectionRow(Icons.Default.Badge, "Student Information", "Profile and Year Level") {}
                            SectionRow(Icons.Default.WorkspacePremium, "Completion Certificate", "Preview and Download") {}
                        }
                    }
                }
            }
        }
        for (fontScale in listOf(1f, 1.5f, 2f)) {
            compose.runOnIdle { scale = fontScale }
            compose.waitForIdle()
            val directory = InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)!!
            File(directory, "settings-name-layout-$fontScale.png").outputStream().use {
                compose.onRoot().captureToImage().asAndroidBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            for ((title, subtitle) in listOf("Student Information" to "Profile and Year Level",
                "Completion Certificate" to "Preview and Download")) {
                val name = compose.onNodeWithText(title, useUnmergedTree = true)
                val detail = compose.onNodeWithText(subtitle, useUnmergedTree = true)
                name.assertIsDisplayed(); detail.assertIsDisplayed()
                assertTrue(name.fetchSemanticsNode().boundsInRoot.bottom <= detail.fetchSemanticsNode().boundsInRoot.top)
                val results = mutableListOf<TextLayoutResult>()
                name.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                val layout = results.single()
                // Compose rounds its measured width to integer pixels. Fractional
                // paragraph widths can report overflow even when all glyphs fit.
                assertFalse("$title at $fontScale has clipped height", layout.didOverflowHeight)
                for (line in 0 until layout.lineCount) assertTrue("$title at $fontScale has clipped text",
                    layout.getLineLeft(line) >= -1f && layout.getLineRight(line) <= layout.size.width + 1f)
            }
        }
    }

    @Test fun nameWarningRequiresExplicitConfirmationAndCanBeCanceled() {
        var starts = 0
        var canceled = 0
        compose.setContent { MyFirstTryTheme {
            ConfirmStudentNameDialog("Ana Maria Cruz", false, null, { starts++ }, { canceled++ })
        } }
        compose.onNodeWithText("Ana Maria Cruz").assertIsDisplayed()
        compose.onNodeWithText("Not Yet").performClick()
        compose.runOnIdle { assertEquals(0, starts); assertEquals(1, canceled) }
        compose.onNodeWithText("Confirm and Start").performClick()
        compose.runOnIdle { assertEquals(1, starts) }
    }

    @Test fun confirmingStartDisablesRepeatedTapsWhileSaving() {
        compose.setContent { MyFirstTryTheme {
            ConfirmStudentNameDialog("Ana Maria Cruz", true, null, { error("Must not start twice") }, { error("Must not cancel a committing start") })
        } }
        compose.onNodeWithText("Starting…").assertIsNotEnabled()
        compose.onNodeWithText("Not Yet").assertIsNotEnabled()
    }
}
