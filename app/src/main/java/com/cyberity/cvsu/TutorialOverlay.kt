package com.cyberity.cvsu

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun TutorialSpotlightOverlay(
    targetBounds: Rect?,
    title: String,
    description: String,
    stepNumber: Int,
    totalSteps: Int = 9,
    onTargetTapped: () -> Unit,
    onSkipTutorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var overlayBoundsInWindow by remember { mutableStateOf<Rect?>(null) }

    val localTargetBounds = remember(targetBounds, overlayBoundsInWindow) {
        val tb = targetBounds
        val ob = overlayBoundsInWindow
        if (tb != null && ob != null) {
            tb.translate(-ob.left, -ob.top)
        } else tb
    }

    val transition = rememberInfiniteTransition(label = "handBounce")
    val handBounce by transition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "handBounceOffset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                overlayBoundsInWindow = coords.boundsInWindow()
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onTargetTapped()
            }
    ) {
        // Spotlight Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.99f)
        ) {
            // Darkened overlay
            drawRect(color = Color.Black.copy(alpha = 0.82f))

            // Cutout spotlight using localTargetBounds
            localTargetBounds?.let { rect ->
                val paddingPx = 12.dp.toPx()
                val left = rect.left - paddingPx
                val top = rect.top - paddingPx
                val right = rect.right + paddingPx
                val bottom = rect.bottom + paddingPx
                val width = (right - left).coerceAtLeast(10f)
                val height = (bottom - top).coerceAtLeast(10f)

                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    blendMode = BlendMode.Clear
                )

                drawRoundRect(
                    color = AppCyan,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    style = Stroke(width = 3.5.dp.toPx())
                )
            }
        }

        // Animated Hand Pointer
        localTargetBounds?.let { rect ->
            val handX = with(density) { (rect.center.x - 20.dp.toPx()).roundToInt() }
            val handY = with(density) { (rect.bottom + (10 + handBounce).dp.toPx()).roundToInt() }

            Box(
                modifier = Modifier
                    .offset { IntOffset(handX, handY) }
                    .size(42.dp)
                    .background(AppCyan.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.TouchApp,
                    contentDescription = "Tap here",
                    tint = AppCyan,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Tooltip Card
        val isTargetInBottomHalf = localTargetBounds?.let { it.center.y > 1000 } ?: false
        val cardAlignment = if (isTargetInBottomHalf) Alignment.TopCenter else Alignment.BottomCenter

        Card(
            modifier = Modifier
                .align(cardAlignment)
                .padding(horizontal = 20.dp, vertical = 28.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppCard),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STEP $stepNumber OF $totalSteps",
                        color = AppCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    TextButton(onClick = onSkipTutorial) {
                        Text("Skip Tutorial", color = AppGray, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = title,
                    color = AppWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = description,
                    color = AppGray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onTargetTapped,
                    colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (stepNumber == totalSteps) "Start Learning" else "Tap Here / Next Step",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
