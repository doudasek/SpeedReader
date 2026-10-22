package com.martindoudera.speedreader.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private const val BLINK_DURATION_MILLIS = 600

/** A small dot that fades in and out, marking a value that is still being measured. */
@Composable
internal fun LiveDot(color: Color, modifier: Modifier = Modifier) {
    val alpha by rememberInfiniteTransition(label = "liveDot").animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(BLINK_DURATION_MILLIS), RepeatMode.Reverse),
        label = "liveDot",
    )
    Box(modifier.size(7.dp).alpha(alpha).background(color, CircleShape))
}
