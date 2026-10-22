package com.martindoudera.speedreader.core.designsystem.component

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.martindoudera.speedreader.core.designsystem.theme.SpeedReaderTheme

private const val PULSE_DURATION_MILLIS = 2600
private const val PULSE_GROWTH = 0.45f

/** The large round call-to-action on the brand gradient, with two rings pulsing out of it to invite a tap. */
@Composable
fun SrPulsingCircleButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val ringColor = MaterialTheme.colorScheme.primary
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(PULSE_DURATION_MILLIS, easing = LinearOutSlowInEasing), RepeatMode.Restart),
        label = "pulse",
    )

    Box(
        modifier = modifier
            .size(SpeedReaderTheme.dimens.startButton)
            .drawBehind {
                drawPulseRing(pulse, ringColor)
                drawPulseRing((pulse + 0.5f) % 1f, ringColor)
            }
            .shadow(elevation = 16.dp, shape = CircleShape, spotColor = ringColor)
            .background(Brush.linearGradient(SpeedReaderTheme.speedColors.brandGradient), CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = Color.White, style = MaterialTheme.typography.headlineSmall.copy(letterSpacing = 2.sp))
    }
}

private fun DrawScope.drawPulseRing(fraction: Float, color: Color) {
    drawCircle(
        color = color.copy(alpha = 0.45f * (1f - fraction)),
        radius = size.minDimension / 2 * (1f + PULSE_GROWTH * fraction),
        style = Stroke(width = 2.dp.toPx()),
    )
}

@ThemePreviews
@Composable
private fun PulsingCircleButtonPreview() {
    ComponentPreview {
        SrPulsingCircleButton(text = "START", onClick = {}, modifier = Modifier.padding(SpeedReaderTheme.spacing.xxxl))
    }
}
