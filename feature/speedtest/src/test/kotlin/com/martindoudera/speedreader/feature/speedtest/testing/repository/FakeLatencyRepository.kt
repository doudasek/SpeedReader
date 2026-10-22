package com.martindoudera.speedreader.feature.speedtest.testing.repository

import com.martindoudera.speedreader.core.common.error.Outcome
import com.martindoudera.speedreader.feature.speedtest.domain.SpeedTestError
import com.martindoudera.speedreader.feature.speedtest.domain.SpeedTestOutcome
import com.martindoudera.speedreader.feature.speedtest.domain.model.ServerUrl
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestServer
import com.martindoudera.speedreader.feature.speedtest.domain.repository.LatencyRepository
import kotlin.time.Duration

/** Servers without a configured latency do not answer. */
class FakeLatencyRepository : LatencyRepository {
    var latencies: Map<ServerUrl, Duration> = emptyMap()

    override suspend fun measureLatency(server: SpeedTestServer): SpeedTestOutcome<Duration> =
        latencies[server.url]?.let { Outcome.Success(it) } ?: Outcome.Failure(SpeedTestError.ServerUnreachable)
}
