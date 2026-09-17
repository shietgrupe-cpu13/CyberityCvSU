package com.cyberity.cvsu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ===========================================================================
// XP INDICATOR
// ===========================================================================
// Shown on the Learn header and inside a running level. During a level the
// value is what the student would earn if they finished right now, so opening
// a hint shows the cost land immediately rather than as a surprise at the end.

private val XpGain = Color(0xFF27E0A8)
private val XpLoss = Color(0xFFFF5C7A)

/**
 * A lightning bolt, drawn here rather than pulled from the extended icon set
 * so the app doesn't take a dependency on it for one glyph.
 */
val XpBolt: ImageVector by lazy {
    ImageVector.Builder(
        name = "XpBolt",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = PathData {
            moveTo(13f, 2f)
            lineTo(4f, 14f)
            lineTo(11f, 14f)
            lineTo(10f, 22f)
            lineTo(20f, 10f)
            lineTo(13f, 10f)
            close()
        },
        fill = SolidColor(Color.Black)  // replaced by Icon's tint
    ).build()
}

/**
 * XP with a counter that rolls to each new value, a pulse in the direction of
 * the change, and the change itself shown briefly beside it — so a deduction
 * reads as "-30" rather than just a number that quietly got smaller.
 */
@Composable
fun XpIndicator(
    xp: Int,
    modifier: Modifier = Modifier,
    iconSize: Dp = 18.dp,
    fontSize: TextUnit = 15.sp,
    showDelta: Boolean = true
) {
    val rolling by animateIntAsState(targetValue = xp, animationSpec = tween(450), label = "xpValue")
    val scale = remember { Animatable(1f) }

    var previous by remember { mutableIntStateOf(xp) }
    var direction by remember { mutableIntStateOf(0) }
    var deltaLabel by remember { mutableStateOf("") }
    var deltaVisible by remember { mutableStateOf(false) }

    val tint by animateColorAsState(
        targetValue = when (direction) {
            1 -> XpGain
            -1 -> XpLoss
            else -> AppCyan
        },
        animationSpec = tween(220),
        label = "xpTint"
    )

    LaunchedEffect(xp) {
        val delta = xp - previous
        previous = xp
        if (delta == 0) return@LaunchedEffect

        direction = if (delta > 0) 1 else -1
        deltaLabel = if (delta > 0) "+$delta" else "$delta"
        deltaVisible = showDelta

        if (delta > 0) {
            scale.animateTo(1.28f, tween(140))
            scale.animateTo(1f, tween(220))
        } else {
            scale.animateTo(0.78f, tween(140))
            scale.animateTo(1f, tween(240))
        }

        delay(750)
        deltaVisible = false
        direction = 0
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = XpBolt,
            contentDescription = "XP",
            tint = tint,
            modifier = Modifier.size(iconSize).scale(scale.value)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = rolling.toString(),
            color = AppWhite,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            modifier = Modifier.scale(scale.value)
        )
        AnimatedVisibility(visible = deltaVisible, enter = fadeIn(), exit = fadeOut()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(5.dp))
                Text(
                    text = deltaLabel,
                    color = if (deltaLabel.startsWith("+")) XpGain else XpLoss,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize * 0.8f
                )
            }
        }
    }
}
