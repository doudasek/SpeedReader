package com.martindoudera.speedreader.feature.speedtest.testing.repository

import com.martindoudera.speedreader.core.common.error.NetworkOutcome
import com.martindoudera.speedreader.core.common.error.Outcome
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestServer
import com.martindoudera.speedreader.feature.speedtest.domain.model.TransferSample
import com.martindoudera.speedreader.feature.speedtest.domain.repository.ThroughputRepository
import kotlin.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onEach

/** Replays the configured samples and records what the caller asked for and consumed. */
class FakeThroughputRepository : ThroughputRepository {
    var downloadSamples: Flow<NetworkOutcome<TransferSample>> = emptyFlow()
    var uploadSamples: Flow<NetworkOutcome<TransferSample>> = emptyFlow()

    val requestedConnections = mutableListOf<Int>()
    var lastDownloadSampleConsumed: Duration? = null
        private set

    override fun download(server: SpeedTestServer, connections: Int): Flow<NetworkOutcome<TransferSample>> {
        requestedConnections += connections
        return downloadSamples.onEach { if (it is Outcome.Success) lastDownloadSampleConsumed = it.value.elapsed }
    }

    override fun upload(server: SpeedTestServer, connections: Int): Flow<NetworkOutcome<TransferSample>> {
        requestedConnections += connections
        return uploadSamples
    }
}
