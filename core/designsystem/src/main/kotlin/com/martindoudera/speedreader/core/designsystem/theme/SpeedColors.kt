package com.martindoudera.speedreader.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colours Material 3 has no slot for: download and upload each keep one colour across the whole UI,
 * and [brandGradient] is the same in both themes so white text on it stays readable.
 */
@Immutable
data class SpeedColors(
    val download: Color = Color.Unspecified,
    val upload: Color = Color.Unspecified,
    val highlight: Color = Color.Unspecified,
    val brandGradient: List<Color> = emptyList(),
)

val LocalSpeedColors = staticCompositionLocalOf { SpeedColors() }
