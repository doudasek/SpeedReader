package com.martindoudera.speedreader.feature.speedtest.ui

import com.martindoudera.speedreader.core.common.domain.model.AppVersion
import com.martindoudera.speedreader.feature.speedtest.domain.model.ConnectionMode
import com.martindoudera.speedreader.feature.speedtest.domain.model.Progress
import com.martindoudera.speedreader.feature.speedtest.domain.model.Throughput
import kotlin.time.Duration

/** Everything the speed test screen shows. `connectionMode` is the user's choice for the next run. */
data class SpeedTestUiState(
    val appVersion: AppVersion,
    val connectionMode: ConnectionMode = ConnectionMode.Parallel,
    val phase: SpeedTestPhase = SpeedTestPhase.Idle,
)

/** What the screen is doing: the domain's `SpeedTestProgress` plus `Idle` before and between runs. */
sealed interface SpeedTestPhase {
    data object Idle : SpeedTestPhase

    data object FindingServer : SpeedTestPhase

    data class Downloading(
        val server: ServerUiModel,
        val progress: Progress,
        val remaining: Duration,
        val currentSpeed: Throughput,
        val averageSpeed: Throughput,
    ) : SpeedTestPhase

    data class Uploading(
        val server: ServerUiModel,
        val downloadSpeed: Throughput,
        val progress: Progress,
        val remaining: Duration,
        val currentSpeed: Throughput,
        val averageSpeed: Throughput,
    ) : SpeedTestPhase

    data class Finished(val server: ServerUiModel, val downloadSpeed: Throughput, val uploadSpeed: Throughput) : SpeedTestPhase

    data class Failed(val reason: FailureReason) : SpeedTestPhase
}

internal val SpeedTestPhase.server: ServerUiModel?
    get() = when (this) {
        is SpeedTestPhase.Downloading -> server
        is SpeedTestPhase.Uploading -> server
        is SpeedTestPhase.Finished -> server
        SpeedTestPhase.Idle, SpeedTestPhase.FindingServer, is SpeedTestPhase.Failed -> null
    }

/** The selected server as the screen labels it; `location` is null when the directory names no city or country. */
data class ServerUiModel(val name: String, val location: String?, val latency: Duration?)

/** Why a run failed, at the granularity the screen explains it to the user. */
enum class FailureReason { Offline, NoServersAvailable, ConnectionLost, Unexpected }
