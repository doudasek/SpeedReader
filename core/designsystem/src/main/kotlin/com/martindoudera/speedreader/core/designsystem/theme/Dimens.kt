package com.martindoudera.speedreader.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Fixed sizes of components and layout regions. */
@Immutable
data class Dimens(
    val topBarHeight: Dp = 56.dp,
    val logo: Dp = 28.dp,
    val iconSmall: Dp = 16.dp,
    val iconMedium: Dp = 20.dp,
    val iconLarge: Dp = 72.dp,
    val chipIconContainer: Dp = 40.dp,
    val startButton: Dp = 184.dp,
    val buttonHeight: Dp = 52.dp,
    val metricCardMinHeight: Dp = 96.dp,
    val progressBarWidth: Dp = 240.dp,
    val progressBarHeight: Dp = 8.dp,
    val illustration: Dp = 200.dp,
    val contentMaxWidth: Dp = 480.dp,
    val expandedSidePane: Dp = 380.dp,
)

val LocalDimens = staticCompositionLocalOf { Dimens() }
