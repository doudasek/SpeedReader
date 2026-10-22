package com.martindoudera.speedreader.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

private const val SHIMMER_DURATION_MILLIS = 1300

/** A rounded placeholder bar with a highlight sweeping across it, standing in for text that is still loading. */
@Composable
internal fun ShimmerLine(widthFraction: Float, modifier: Modifier = Modifier) {
    val base = MaterialTheme.colorScheme.surfaceContainer
    val highlight = MaterialTheme.colorScheme.surfaceContainerHigh
    val progress by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(SHIMMER_DURATION_MILLIS, easing = LinearEasing)),
        label = "shimmer",
    )
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(10.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .drawBehind {
                val start = (progress * 2f - 1f) * size.width
                drawRect(
                    Brush.linearGradient(listOf(base, highlight, base), start = Offset(start, 0f), end = Offset(start + size.width, 0f)),
                )
            },
    )
}
