package com.martindoudera.speedreader.feature.speedtest.domain.model

import com.martindoudera.speedreader.feature.speedtest.domain.SpeedTestError
import kotlin.time.Duration

/**
 * The states a speed test run passes through, in order. [Finished] and [Failed] are terminal;
 * stopping the run is cancellation, not a state of its own.
 *
 * While measuring, `progress` is the elapsed share of the whole test (download and upload together)
 * and `remaining` is the time left until it finishes. Finding the server comes before
 * that clock starts and has no progress of its own, since its length cannot be known in advance.
 */
sealed interface SpeedTestProgress {
    data object FindingServer : SpeedTestProgress

    data class Downloading(val server: SelectedServer, val progress: Progress, val remaining: Duration, val currentSpeed: Throughput) :
        SpeedTestProgress

    data class Uploading(
        val server: SelectedServer,
        val downloadSpeed: Throughput,
        val progress: Progress,
        val remaining: Duration,
        val currentSpeed: Throughput,
    ) : SpeedTestProgress

    data class Finished(val server: SelectedServer, val downloadSpeed: Throughput, val uploadSpeed: Throughput) : SpeedTestProgress

    data class Failed(val error: SpeedTestError) : SpeedTestProgress
}
