package com.martindoudera.speedreader.feature.speedtest.domain.repository

import com.martindoudera.speedreader.feature.speedtest.domain.SpeedTestOutcome
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestServer
import kotlin.time.Duration

/** Measures the round-trip time to a server with real ICMP echo requests. */
interface LatencyRepository {
    suspend fun measureLatency(server: SpeedTestServer): SpeedTestOutcome<Duration>
}
