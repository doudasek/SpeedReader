package com.martindoudera.speedreader.feature.speedtest.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.martindoudera.speedreader.core.common.domain.model.AppVersion
import com.martindoudera.speedreader.core.designsystem.component.ThemePreviews
import com.martindoudera.speedreader.core.designsystem.theme.SpeedReaderTheme
import com.martindoudera.speedreader.feature.speedtest.domain.model.Progress
import com.martindoudera.speedreader.feature.speedtest.domain.model.Throughput
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val PreviewServer = ServerUiModel(name = "CETIN a.s.", location = "Prague, CZ", latency = 9.milliseconds)

internal class SpeedTestPhaseProvider : PreviewParameterProvider<SpeedTestPhase> {
    override val values = sequenceOf(
        SpeedTestPhase.Idle,
        SpeedTestPhase.FindingServer,
        SpeedTestPhase.Downloading(PreviewServer, Progress(0.3f), 10.5.seconds, Throughput(218_000_000.0), Throughput(212_400_000.0)),
        SpeedTestPhase.Uploading(
            server = PreviewServer,
            downloadSpeed = Throughput(223_100_000.0),
            progress = Progress(0.78f),
            remaining = 3.3.seconds,
            currentSpeed = Throughput(51_200_000.0),
            averageSpeed = Throughput(39_400_000.0),
        ),
        SpeedTestPhase.Finished(PreviewServer, Throughput(223_100_000.0), Throughput(41_500_000.0)),
        SpeedTestPhase.Failed(FailureReason.Offline),
    )
}

@ThemePreviews
@Composable
private fun SpeedTestContentPreview(@PreviewParameter(SpeedTestPhaseProvider::class) phase: SpeedTestPhase) {
    ContentPreview(phase)
}

@Preview(name = "Tablet", device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
private fun SpeedTestContentTabletPreview(@PreviewParameter(SpeedTestPhaseProvider::class) phase: SpeedTestPhase) {
    ContentPreview(phase)
}

@Composable
internal fun ContentPreview(phase: SpeedTestPhase) {
    SpeedReaderTheme {
        SpeedTestContent(
            uiState = SpeedTestUiState(appVersion = AppVersion("0.0.1"), phase = phase),
            onStartClick = {},
            onStopClick = {},
            onBackClick = {},
            onShareClick = {},
            onSingleConnectionChange = {},
        )
    }
}
