package com.martindoudera.speedreader.feature.speedtest.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.NetworkPing
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.martindoudera.speedreader.core.designsystem.component.MetricStatus
import com.martindoudera.speedreader.core.designsystem.component.SrMetricCard
import com.martindoudera.speedreader.core.designsystem.theme.SpeedReaderTheme
import com.martindoudera.speedreader.feature.speedtest.R
import com.martindoudera.speedreader.feature.speedtest.domain.model.Throughput
import com.martindoudera.speedreader.feature.speedtest.ui.NO_VALUE
import com.martindoudera.speedreader.feature.speedtest.ui.SpeedTestPhase
import com.martindoudera.speedreader.feature.speedtest.ui.formatMegabits
import com.martindoudera.speedreader.feature.speedtest.ui.formatMilliseconds
import com.martindoudera.speedreader.feature.speedtest.ui.server

/** Ping, download and upload side by side; download and upload show their average, live while measured. */
@Composable
internal fun MetricsRow(phase: SpeedTestPhase, modifier: Modifier = Modifier) {
    val colors = SpeedReaderTheme.speedColors
    val finished = phase is SpeedTestPhase.Finished
    val server = phase.server
    val download = phase.downloadMetric()
    val upload = phase.uploadMetric()
    val cardModifier = Modifier.fillMaxHeight()

    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(SpeedReaderTheme.spacing.sm),
    ) {
        SrMetricCard(
            icon = Icons.Rounded.NetworkPing,
            label = stringResource(R.string.feature_speedtest_ping),
            value = server?.latency?.formatMilliseconds() ?: NO_VALUE,
            unit = stringResource(R.string.feature_speedtest_unit_ms),
            accent = colors.highlight,
            modifier = cardModifier.weight(1f),
            caption = server?.latency?.let { stringResource(R.string.feature_speedtest_caption_icmp) },
            emphasized = finished,
        )
        SrMetricCard(
            icon = Icons.Rounded.Download,
            label = stringResource(R.string.feature_speedtest_download),
            value = download.speed?.formatMegabits(SpeedReaderTheme.locale) ?: NO_VALUE,
            unit = stringResource(R.string.feature_speedtest_unit_mbps),
            accent = colors.download,
            modifier = cardModifier.weight(1f),
            status = download.status,
            caption = download.caption(),
            emphasized = finished,
        )
        SrMetricCard(
            icon = Icons.Rounded.Upload,
            label = stringResource(R.string.feature_speedtest_upload),
            value = upload.speed?.formatMegabits(SpeedReaderTheme.locale) ?: NO_VALUE,
            unit = stringResource(R.string.feature_speedtest_unit_mbps),
            accent = colors.upload,
            modifier = cardModifier.weight(1f),
            status = upload.status,
            caption = if (phase is SpeedTestPhase.Downloading) {
                stringResource(
                    R.string.feature_speedtest_caption_up_next,
                )
            } else {
                upload.caption()
            },
            emphasized = finished,
        )
    }
}

private data class SpeedMetric(val speed: Throughput?, val status: MetricStatus)

@Composable
private fun SpeedMetric.caption(): String? = when (status) {
    MetricStatus.Live -> stringResource(R.string.feature_speedtest_caption_live_average)
    MetricStatus.Done -> stringResource(R.string.feature_speedtest_caption_average)
    MetricStatus.Idle -> null
}

private fun SpeedTestPhase.downloadMetric(): SpeedMetric = when (this) {
    is SpeedTestPhase.Downloading -> SpeedMetric(averageSpeed, MetricStatus.Live)
    is SpeedTestPhase.Uploading -> SpeedMetric(downloadSpeed, MetricStatus.Done)
    is SpeedTestPhase.Finished -> SpeedMetric(downloadSpeed, MetricStatus.Done)
    else -> SpeedMetric(null, MetricStatus.Idle)
}

private fun SpeedTestPhase.uploadMetric(): SpeedMetric = when (this) {
    is SpeedTestPhase.Uploading -> SpeedMetric(averageSpeed, MetricStatus.Live)
    is SpeedTestPhase.Finished -> SpeedMetric(uploadSpeed, MetricStatus.Done)
    else -> SpeedMetric(null, MetricStatus.Idle)
}
