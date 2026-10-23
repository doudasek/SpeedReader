package com.martindoudera.speedreader.feature.speedtest.ui

import com.martindoudera.speedreader.core.common.error.NetworkError
import com.martindoudera.speedreader.feature.speedtest.domain.SpeedTestError
import com.martindoudera.speedreader.feature.speedtest.domain.model.SelectedServer
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestProgress
import java.net.URI

internal fun SpeedTestProgress.toUiPhase(): SpeedTestPhase = when (this) {
    SpeedTestProgress.FindingServer -> SpeedTestPhase.FindingServer
    is SpeedTestProgress.Downloading -> SpeedTestPhase.Downloading(server.toUiModel(), progress, remaining, currentSpeed, averageSpeed)
    is SpeedTestProgress.Uploading ->
        SpeedTestPhase.Uploading(server.toUiModel(), downloadSpeed, progress, remaining, currentSpeed, averageSpeed)
    is SpeedTestProgress.Finished -> SpeedTestPhase.Finished(server.toUiModel(), downloadSpeed, uploadSpeed)
    is SpeedTestProgress.Failed -> SpeedTestPhase.Failed(error.toFailureReason())
}

private fun SelectedServer.toUiModel(): ServerUiModel {
    val name = server.provider?.takeIf { it.isNotBlank() }
        ?: runCatching { URI(server.url.value).host }.getOrNull()
        ?: server.url.value
    val location = listOfNotNull(server.city, server.countryCode).filter { it.isNotBlank() }.joinToString(", ").ifEmpty { null }
    return ServerUiModel(name, location, latency)
}

private fun SpeedTestError.toFailureReason(): FailureReason = when (this) {
    is SpeedTestError.Network -> when (error) {
        is NetworkError.NoConnection -> FailureReason.Offline
        is NetworkError.Timeout -> FailureReason.ConnectionLost
        else -> FailureReason.Unexpected
    }
    SpeedTestError.NoServersAvailable -> FailureReason.NoServersAvailable
    SpeedTestError.MeasurementFailed -> FailureReason.ConnectionLost
    SpeedTestError.ServerUnreachable, is SpeedTestError.Unknown -> FailureReason.Unexpected
}
