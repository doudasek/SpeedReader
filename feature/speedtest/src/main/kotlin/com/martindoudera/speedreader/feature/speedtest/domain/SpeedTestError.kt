package com.martindoudera.speedreader.feature.speedtest.domain

import com.martindoudera.speedreader.core.common.error.NetworkError
import com.martindoudera.speedreader.core.common.error.Outcome

/**
 * Everything that can go wrong in a speed test run, as the domain sees it.
 *
 * [ServerUnreachable] is recoverable: the use case skips that server, so it never ends a run on its own.
 */
sealed interface SpeedTestError {
    data class Network(val error: NetworkError) : SpeedTestError

    data object NoServersAvailable : SpeedTestError

    data object ServerUnreachable : SpeedTestError

    data object MeasurementFailed : SpeedTestError

    data class Unknown(val cause: Throwable?) : SpeedTestError
}

typealias SpeedTestOutcome<T> = Outcome<T, SpeedTestError>
