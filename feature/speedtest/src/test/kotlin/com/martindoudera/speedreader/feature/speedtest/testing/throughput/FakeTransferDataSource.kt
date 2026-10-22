package com.martindoudera.speedreader.feature.speedtest.testing.throughput

import com.martindoudera.speedreader.feature.speedtest.data.throughput.DownloadDataSource
import com.martindoudera.speedreader.feature.speedtest.data.throughput.UploadDataSource
import com.martindoudera.speedreader.feature.speedtest.domain.model.ServerUrl
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

/**
 * Moves [bytesPerTick] every [tick] of virtual time per request. A request ends after
 * [bytesPerRequest] and, when [failure] is set, fails once it has run for [failAfter].
 *
 * The 40 ms tick never coincides with a 250 ms sample, so a test never depends on which of two
 * coroutines due at the same virtual instant runs first.
 */
internal class FakeTransferDataSource :
    DownloadDataSource,
    UploadDataSource {
    var bytesPerTick = 1_000L
    var tick = 40.milliseconds
    var bytesPerRequest = Long.MAX_VALUE
    var failure: Exception? = null
    var failAfter = Duration.ZERO

    val requestedServers = mutableListOf<ServerUrl>()
    var activeRequests = 0
        private set

    override suspend fun download(server: ServerUrl, onBytes: (Long) -> Unit) = transfer(server, onBytes)

    override suspend fun upload(server: ServerUrl, onBytes: (Long) -> Unit) = transfer(server, onBytes)

    private suspend fun transfer(server: ServerUrl, onBytes: (Long) -> Unit) {
        requestedServers += server
        activeRequests++
        try {
            var sent = 0L
            var running = Duration.ZERO
            while (sent < bytesPerRequest) {
                delay(tick)
                running += tick
                failure?.takeIf { running >= failAfter }?.let { throw it }
                onBytes(bytesPerTick)
                sent += bytesPerTick
            }
        } finally {
            activeRequests--
        }
    }
}
