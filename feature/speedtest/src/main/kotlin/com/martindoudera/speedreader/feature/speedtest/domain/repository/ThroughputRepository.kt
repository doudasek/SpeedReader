package com.martindoudera.speedreader.feature.speedtest.domain.repository

import com.martindoudera.speedreader.core.common.error.NetworkOutcome
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestServer
import com.martindoudera.speedreader.feature.speedtest.domain.model.TransferSample
import kotlinx.coroutines.flow.Flow

/**
 * Transfers data to and from a server over `connections` parallel streams and reports progress.
 *
 * Both flows emit cumulative samples at a steady cadence, also when no bytes arrived, until the
 * collector cancels them; a failure is always the last element. Turning samples into speed is the
 * domain's job, not the repository's.
 */
interface ThroughputRepository {
    fun download(server: SpeedTestServer, connections: Int): Flow<NetworkOutcome<TransferSample>>

    fun upload(server: SpeedTestServer, connections: Int): Flow<NetworkOutcome<TransferSample>>
}
