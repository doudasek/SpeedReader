package com.martindoudera.speedreader.feature.speedtest.data.throughput

import com.martindoudera.speedreader.core.common.error.HttpStatusCode
import com.martindoudera.speedreader.core.common.error.NetworkError
import com.martindoudera.speedreader.core.common.error.Outcome
import com.martindoudera.speedreader.feature.speedtest.domain.model.ByteCount
import com.martindoudera.speedreader.feature.speedtest.domain.model.TransferSample
import com.martindoudera.speedreader.feature.speedtest.testing.data.httpError
import com.martindoudera.speedreader.feature.speedtest.testing.data.testServer
import com.martindoudera.speedreader.feature.speedtest.testing.throughput.FakeTransferDataSource
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultThroughputRepositoryTest {
    private val downloads = FakeTransferDataSource()
    private val uploads = FakeTransferDataSource()
    private val dispatcher = StandardTestDispatcher()
    private val repository = DefaultThroughputRepository(downloads, uploads, dispatcher.scheduler.timeSource, dispatcher)
    private val server = testServer("https://breclav.wifiman.me:81", 48.7569, 16.8854)

    @Test
    fun downloadReportsTheRunningTotalEverySampleInterval() = runTest(dispatcher) {
        val samples = repository.download(server, connections = 1).take(3).toList()

        assertEquals(listOf(sample(250, 6_000), sample(500, 12_000), sample(750, 18_000)), samples)
    }

    @Test
    fun allConnectionsAddIntoOneTotal() = runTest(dispatcher) {
        val samples = repository.download(server, connections = 6).take(1).toList()

        assertEquals(listOf(sample(250, 36_000)), samples)
        assertEquals(List(6) { server.url }, downloads.requestedServers)
    }

    @Test
    fun samplesKeepComingWhenNothingMoves() = runTest(dispatcher) {
        downloads.bytesPerTick = 0

        val samples = repository.download(server, connections = 2).take(3).toList()

        assertEquals(listOf(sample(250, 0), sample(500, 0), sample(750, 0)), samples)
    }

    @Test
    fun requestThatEndsIsReplacedByANewOne() = runTest(dispatcher) {
        downloads.bytesPerRequest = 2_000

        val samples = repository.download(server, connections = 1).take(2).toList()

        assertEquals(listOf(sample(250, 6_000), sample(500, 12_000)), samples)
        assertEquals(7, downloads.requestedServers.size)
    }

    @Test
    fun failingStreamEndsTheTransferWithItsError() = runTest(dispatcher) {
        downloads.failure = httpError(503)
        downloads.failAfter = 300.milliseconds

        val samples = repository.download(server, connections = 2).toList()

        assertEquals(sample(250, 12_000), samples.first())
        assertEquals(Outcome.Failure(NetworkError.Http(HttpStatusCode(503))), samples.last())
        assertEquals(0, downloads.activeRequests)
    }

    @Test
    fun stoppingTheCollectionCancelsEveryStream() = runTest(dispatcher) {
        repository.download(server, connections = 6).take(2).toList()

        assertEquals(0, downloads.activeRequests)
    }

    @Test
    fun uploadRunsOnTheUploadDataSource() = runTest(dispatcher) {
        val samples = repository.upload(server, connections = 2).take(1).toList()

        assertEquals(listOf(sample(250, 12_000)), samples)
        assertEquals(List(2) { server.url }, uploads.requestedServers)
        assertEquals(emptyList(), downloads.requestedServers)
    }

    private fun sample(elapsedMillis: Int, bytes: Long) = Outcome.Success(TransferSample(elapsedMillis.milliseconds, ByteCount(bytes)))
}
