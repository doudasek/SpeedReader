package com.martindoudera.speedreader.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.martindoudera.speedreader.core.designsystem.theme.SpeedReaderTheme

/** One part of a [SrSegmentedProgressBar]; `progress` runs from 0 to 1 within the segment. */
@Immutable
data class ProgressSegment(val progress: Float, val color: Color)

/** A determinate progress bar split into equally wide segments, each filling in its own colour. */
@Composable
fun SrSegmentedProgressBar(segments: List<ProgressSegment>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.height(SpeedReaderTheme.dimens.progressBarHeight),
        horizontalArrangement = Arrangement.spacedBy(SpeedReaderTheme.spacing.xs),
    ) {
        segments.forEach { segment ->
            LinearProgressIndicator(
                progress = { segment.progress },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                color = segment.color,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
        }
    }
}

/** The same bar while the length of the work is unknown. */
@Composable
fun SrIndeterminateProgressBar(modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        modifier = modifier.height(SpeedReaderTheme.dimens.progressBarHeight),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
    )
}

@ThemePreviews
@Composable
private fun ProgressBarPreview() {
    ComponentPreview {
        val colors = SpeedReaderTheme.speedColors
        val width = Modifier.width(SpeedReaderTheme.dimens.progressBarWidth)
        Column(verticalArrangement = Arrangement.spacedBy(SpeedReaderTheme.spacing.lg)) {
            SrSegmentedProgressBar(listOf(ProgressSegment(1f, colors.download), ProgressSegment(0.4f, colors.upload)), width)
            SrIndeterminateProgressBar(width)
        }
    }
}
