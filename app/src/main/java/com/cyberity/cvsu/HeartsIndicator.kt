package com.cyberity.cvsu

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ===========================================================================
// HEARTS BAR
// ===========================================================================
// Shown inside a running level so a mistake reads as a cost the moment it
// happens, rather than something noticed later on the path screen.

/** Same heart color the Learn header uses. */
private val HeartFilled: Color get() = AppHeart

@Composable
fun HeartsRow(
    hearts: Int,
    modifier: Modifier = Modifier,
    heartSize: Dp = 16.dp
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(MAX_HEARTS) { index ->
            if (index > 0) Spacer(Modifier.width(3.dp))
            HeartIcon(filled = index < hearts, size = heartSize)
        }
    }
}

/**
 * One heart, animated on change only.
 *
 * Losing it snaps outward then collapses into the empty outline; gaining it
 * pops in past full size and settles. Both are short enough to register
 * without pulling attention off the simulation. The first composition is
 * skipped so entering a level doesn't animate hearts the student never lost.
 */
@Composable
private fun HeartIcon(filled: Boolean, size: Dp) {
    val scale = remember { Animatable(1f) }
    val isFirstPass = remember { mutableStateOf(true) }

    LaunchedEffect(filled) {
        if (isFirstPass.value) {
            isFirstPass.value = false
            return@LaunchedEffect
        }
        if (filled) {
            scale.snapTo(0.6f)
            scale.animateTo(1.25f, tween(160))
            scale.animateTo(1f, tween(140))
        } else {
            scale.animateTo(1.3f, tween(120))
            scale.animateTo(0.85f, tween(180))
            scale.animateTo(1f, tween(120))
        }
    }

    Icon(
        imageVector = if (filled) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
        contentDescription = null,
        tint = if (filled) HeartFilled else AppGray.copy(alpha = 0.5f),
        modifier = Modifier.size(size).scale(scale.value)
    )
}
