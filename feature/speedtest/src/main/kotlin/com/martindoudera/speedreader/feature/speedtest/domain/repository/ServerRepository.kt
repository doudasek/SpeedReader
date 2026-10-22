package com.martindoudera.speedreader.feature.speedtest.domain.repository

import com.martindoudera.speedreader.core.common.error.NetworkOutcome
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestServer

/**
 * Provides the measurement servers the Ubiquiti directory offers for this client.
 *
 * An empty list is a success; whether that ends the run is the use case's decision.
 */
interface ServerRepository {
    suspend fun servers(): NetworkOutcome<List<SpeedTestServer>>
}
