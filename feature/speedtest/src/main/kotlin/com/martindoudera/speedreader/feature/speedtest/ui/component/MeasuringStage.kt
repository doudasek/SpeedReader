package com.martindoudera.speedreader.feature.speedtest.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.martindoudera.speedreader.core.designsystem.component.MIN_FITTED_TEXT_SIZE
import com.martindoudera.speedreader.core.designsystem.component.ProgressSegment
import com.martindoudera.speedreader.core.designsystem.component.SrIndeterminateProgressBar
import com.martindoudera.speedreader.core.designsystem.component.SrSegmentedProgressBar
import com.martindoudera.speedreader.core.designsystem.component.SrStatusPill
import com.martindoudera.speedreader.core.designsystem.theme.SpeedReaderTheme
import com.martindoudera.speedreader.feature.speedtest.R
import com.martindoudera.speedreader.feature.speedtest.domain.model.Throughput
import com.martindoudera.speedreader.feature.speedtest.ui.SpeedTestPhase
import com.martindoudera.speedreader.feature.speedtest.ui.formatMegabits
import com.martindoudera.speedreader.feature.speedtest.ui.wholeSecondsLeft
import kotlin.time.Duration

/**
 * The centre of the screen while a test runs: the current speed above a bar split into the download
 * and upload halves of the test. While the server is being found the same slots show a globe and an
 * indeterminate bar, so nothing moves when measuring starts.
 */
@Composable
internal fun MeasuringStage(phase: SpeedTestPhase, modifier: Modifier = Modifier) {
    val colors = SpeedReaderTheme.speedColors
    val spacing = SpeedReaderTheme.spacing
    val reading = phase.toReading()
    val findingAlpha by animateFloatAsState(if (phase is SpeedTestPhase.FindingServer) 1f else 0f, label = "finding")

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Column(Modifier.graphicsLayer { alpha = findingAlpha }, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.Public,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(SpeedReaderTheme.dimens.iconLarge),
                )
                Text(
                    text = stringResource(R.string.feature_speedtest_finding_server),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = spacing.md),
                )
            }
            Column(Modifier.graphicsLayer { alpha = 1f - findingAlpha }, horizontalAlignment = Alignment.CenterHorizontally) {
                SrStatusPill(
                    icon = if (reading.uploading) Icons.Rounded.Upload else Icons.Rounded.Download,
                    text = stringResource(
                        if (reading.uploading) R.string.feature_speedtest_upload else R.string.feature_speedtest_download,
                    ),
                    color = if (reading.uploading) colors.upload else colors.download,
                )
                Text(
                    text = reading.currentSpeed.formatMegabits(SpeedReaderTheme.locale),
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.padding(top = spacing.md),
                )
                Text(
                    text = stringResource(R.string.feature_speedtest_unit_mbps),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Column(Modifier.padding(top = spacing.xl).width(SpeedReaderTheme.dimens.progressBarWidth)) {
            if (phase is SpeedTestPhase.FindingServer) {
                SrIndeterminateProgressBar(Modifier.fillMaxWidth())
            } else {
                val doubled = reading.progress * 2
                SrSegmentedProgressBar(
                    segments = listOf(
                        ProgressSegment(doubled.coerceAtMost(1f), colors.download),
                        ProgressSegment((doubled - 1f).coerceAtLeast(0f), colors.upload),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = spacing.xs + spacing.xxs).graphicsLayer { alpha = 1f - findingAlpha },
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                TimelineLabel(stringResource(R.string.feature_speedtest_download), TextAlign.Start)
                TimelineLabel(stringResource(R.string.feature_speedtest_remaining, reading.remaining.wholeSecondsLeft()), TextAlign.Center)
                TimelineLabel(stringResource(R.string.feature_speedtest_upload), TextAlign.End)
            }
        }
    }
}

/** One of three equal slots under the bar; shrinks rather than running into its neighbour at large font scales. */
@Composable
private fun RowScope.TimelineLabel(text: String, align: TextAlign) {
    val style = MaterialTheme.typography.labelMedium
    Text(
        text = text,
        style = style,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = align,
        softWrap = false,
        maxLines = 1,
        autoSize = TextAutoSize.StepBased(
            minFontSize = with(LocalDensity.current) {
                MIN_FITTED_TEXT_SIZE.toSp()
            },
            maxFontSize = style.fontSize,
        ),
        modifier = Modifier.weight(1f),
    )
}

private data class Reading(val uploading: Boolean, val currentSpeed: Throughput, val progress: Float, val remaining: Duration)

private fun SpeedTestPhase.toReading(): Reading = when (this) {
    is SpeedTestPhase.Downloading -> Reading(uploading = false, currentSpeed, progress.fraction, remaining)
    is SpeedTestPhase.Uploading -> Reading(uploading = true, currentSpeed, progress.fraction, remaining)
    else -> Reading(uploading = false, Throughput.Zero, 0f, Duration.ZERO)
}
