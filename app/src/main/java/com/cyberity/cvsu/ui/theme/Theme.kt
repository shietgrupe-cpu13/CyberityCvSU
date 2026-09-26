package com.cyberity.cvsu.ui.theme

import android.content.Context
import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cyberity.cvsu.AppBlue
import com.cyberity.cvsu.AppCard
import com.cyberity.cvsu.AppCyan
import com.cyberity.cvsu.AppDanger
import com.cyberity.cvsu.AppGray
import com.cyberity.cvsu.AppNavy
import com.cyberity.cvsu.AppOnBlue
import com.cyberity.cvsu.AppWhite

/**
 * App-wide Light / Dark mode.
 *
 * [mode] is "dark", "light" or "system" and is saved in the "app_settings"
 * SharedPreferences under "theme_mode". Because it is Compose state, every
 * screen that reads an App* color redraws as soon as it changes.
 */
object CyberityThemeState {
    const val PREF_KEY = "theme_mode"
    const val DARK = "dark"
    const val LIGHT = "light"
    const val SYSTEM = "system"

    var mode by mutableStateOf(DARK)
    var systemIsDark by mutableStateOf(true)

    val isDark: Boolean
        get() = when (mode) {
            LIGHT -> false
            SYSTEM -> systemIsDark
            else -> true
        }

    /** Restores the saved Light / Dark / System choice. Call before the first frame. */
    fun load(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        mode = prefs.getString(PREF_KEY, DARK) ?: DARK
        systemIsDark = (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}

@Composable
fun MyFirstTryTheme(
    content: @Composable () -> Unit
) {
    // Built from the Cyberity colors so default Material parts (dialogs, sheets,
    // switches, button text) match the app instead of the device wallpaper.
    val colorScheme = if (CyberityThemeState.isDark) {
        darkColorScheme(
            primary = AppBlue,
            onPrimary = AppOnBlue,
            secondary = AppCyan,
            onSecondary = AppNavy,
            background = AppNavy,
            onBackground = AppWhite,
            surface = AppCard,
            onSurface = AppWhite,
            surfaceVariant = AppCard,
            onSurfaceVariant = AppGray,
            surfaceContainerHigh = AppCard,
            outline = AppGray,
            error = AppDanger
        )
    } else {
        lightColorScheme(
            primary = AppBlue,
            onPrimary = AppOnBlue,
            secondary = AppCyan,
            onSecondary = AppOnBlue,
            background = AppNavy,
            onBackground = AppWhite,
            surface = AppCard,
            onSurface = AppWhite,
            surfaceVariant = AppNavy,
            onSurfaceVariant = AppGray,
            surfaceContainerHigh = AppCard,
            outline = AppGray,
            error = AppDanger
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
