package com.martindoudera.speedreader.feature.speedtest.domain.usecase

import com.martindoudera.speedreader.core.common.error.HttpStatusCode
import com.martindoudera.speedreader.core.common.error.NetworkError
import com.martindoudera.speedreader.core.common.error.NetworkOutcome
import com.martindoudera.speedreader.core.common.error.Outcome
import com.martindoudera.speedreader.feature.speedtest.domain.SpeedTestError
import com.martindoudera.speedreader.feature.speedtest.domain.SpeedTestOutcome
import com.martindoudera.speedreader.feature.speedtest.domain.model.ByteCount
import com.martindoudera.speedreader.feature.speedtest.domain.model.ConnectionMode
import com.martindoudera.speedreader.feature.speedtest.domain.model.Distance
import com.martindoudera.speedreader.feature.speedtest.domain.model.GeoPoint
import com.martindoudera.speedreader.feature.speedtest.domain.model.Latitude
import com.martindoudera.speedreader.feature.speedtest.domain.model.Longitude
import com.martindoudera.speedreader.feature.speedtest.domain.model.ServerUrl
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestProgress
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestProgress.Downloading
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestProgress.Failed
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestProgress.FindingServer
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestProgress.Finished
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestProgress.Uploading
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestServer
import com.martindoudera.speedreader.feature.speedtest.domain.model.TransferSample
import com.martindoudera.speedreader.feature.speedtest.domain.repository.LatencyRepository
import com.martindoudera.speedreader.feature.speedtest.domain.repository.LocationRepository
import com.martindoudera.speedreader.feature.speedtest.domain.repository.ServerRepository
import com.martindoudera.speedreader.feature.speedtest.domain.repository.ThroughputRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RunSpeedTestUseCaseTest {
    private val servers = FakeServerRepository()
    private val location = FakeLocationRepository()
    private val latency = FakeLatencyRepository()
    private val throughput = FakeThroughputRepository()

    private val runSpeedTest = RunSpeedTestUseCase(servers, location, latency, throughput, SelectClosestServersUseCase(location))

    @Test
    fun passesThroughEveryPhaseInOrder() {
        val states = run()

        assertEquals(FindingServer, states.first())
        val phases = states.map { it::class }.distinct()
        assertEquals(listOf(FindingServer::class, Downloading::class, Uploading::class, Finished::class), phases)
    }

    @Test
    fun finishesWithTheAverageOfEachPhase() {
        throughput.download = steady(bytesPerTick = 2_500_000)
        throughput.upload = steady(bytesPerTick = 250_000)

        val finished = run().last() as Finished

        assertEquals(80_000_000.0, finished.downloadSpeed.bitsPerSecond, 1e-6)
        assertEquals(8_000_000.0, finished.uploadSpeed.bitsPerSecond, 1e-6)
    }

    @Test
    fun uploadPhaseCarriesTheDownloadAverage() {
        throughput.download = steady(bytesPerTick = 2_500_000)

        val uploading = run().filterIsInstance<Uploading>()

        assertTrue(uploading.all { it.downloadSpeed.bitsPerSecond == 80_000_000.0 })
    }

    @Test
    fun downloadAndUploadShareTheFifteenSeconds() {
        assertEquals(15.seconds, TEST_DURATION)
        assertEquals(7.5.seconds, PHASE_DURATION)
    }

    @Test
    fun eachPhaseStopsAtHalfTheTestEvenIfTheServerKeepsSending() {
        throughput.download = steady(bytesPerTick = 1_000, until = 20.seconds)

        val downloading = run().filterIsInstance<Downloading>()

        assertEquals(PHASE_DURATION, throughput.lastDownloadSampleTaken)
        assertEquals(30, downloading.size)
    }

    @Test
    fun progressAndRemainingTimeSpanTheWholeTest() {
        val states = run()
        val downloading = states.filterIsInstance<Downloading>()
        val uploading = states.filterIsInstance<Uploading>()

        assertEquals(1f / 60, downloading.first().progress.fraction, 1e-6f)
        assertEquals(14.75.seconds, downloading.first().remaining)
        assertEquals(0.5f, downloading.last().progress.fraction)
        assertEquals(7.5.seconds, downloading.last().remaining)
        assertEquals(31f / 60, uploading.first().progress.fraction, 1e-6f)
        assertEquals(1f, uploading.last().progress.fraction)
        assertEquals(Duration.ZERO, uploading.last().remaining)
    }

    @Test
    fun progressNeverPassesTheEndOfItsPhase() {
        throughput.download = steady(bytesPerTick = 1_000, until = 20.seconds)
        throughput.upload = flowOf(sample(1, 1_000), sample(40, 2_000))

        val states = run()

        assertEquals(0.5f, states.filterIsInstance<Downloading>().maxOf { it.progress.fraction })
        assertEquals(1f, states.filterIsInstance<Uploading>().last().progress.fraction)
        assertEquals(Duration.ZERO, states.filterIsInstance<Uploading>().last().remaining)
    }

    @Test
    fun picksTheReachableServerWithTheLowestLatency() {
        latency.latencies = mapOf(NEAR to 30.milliseconds, MIDDLE to 12.milliseconds, FAR to 8.milliseconds)

        val finished = run().last() as Finished

        assertEquals(FAR, finished.server.server.url)
        assertEquals(8.milliseconds, finished.server.latency)
    }

    @Test
    fun skipsServersThatDoNotAnswer() {
        latency.latencies = mapOf(NEAR to null, MIDDLE to 20.milliseconds, FAR to null)

        val finished = run().last() as Finished

        assertEquals(MIDDLE, finished.server.server.url)
    }

    @Test
    fun failsWhenNoServerAnswers() {
        latency.latencies = mapOf(NEAR to null, MIDDLE to null, FAR to null)

        assertEquals(listOf(FindingServer, Failed(SpeedTestError.NoServerReachable)), run())
    }

    @Test
    fun failsWithTheNetworkErrorWhenTheServerListCannotBeLoaded() {
        val error = NetworkError.Http(HttpStatusCode(503))
        servers.result = Outcome.Failure(error)

        assertEquals(listOf(FindingServer, Failed(SpeedTestError.Network(error))), run())
    }

    @Test
    fun failsWhenTheDirectoryOffersNoServers() {
        servers.result = Outcome.Success(emptyList())

        assertEquals(listOf(FindingServer, Failed(SpeedTestError.NoServersAvailable)), run())
    }

    @Test
    fun withoutLocationUsesTheDirectoryOrder() {
        location.result = Outcome.Failure(NetworkError.Timeout(null))
        latency.latencies = mapOf(NEAR to 10.milliseconds, MIDDLE to 10.milliseconds, FAR to 10.milliseconds)

        val finished = run().last() as Finished

        assertEquals(FAR, finished.server.server.url)
    }

    @Test
    fun downloadFailureEndsTheRunWithTheNetworkError() {
        val error = NetworkError.NoConnection(null)
        throughput.download = flowOf(sample(1, 1_000), Outcome.Failure(error))

        val states = run()

        assertEquals(Failed(SpeedTestError.Network(error)), states.last())
        assertTrue(states.none { it is Uploading })
    }

    @Test
    fun uploadWithoutASingleByteIsAMeasurementFailure() {
        throughput.upload = steady(bytesPerTick = 0)

        assertEquals(Failed(SpeedTestError.MeasurementFailed), run().last())
    }

    @Test
    fun connectionModeDecidesTheNumberOfConnections() {
        run(ConnectionMode.Single)
        assertEquals(listOf(1, 1), throughput.connectionsRequested)

        throughput.connectionsRequested.clear()
        run(ConnectionMode.Parallel)
        assertEquals(listOf(4, 4), throughput.connectionsRequested)
    }

    private fun run(mode: ConnectionMode = ConnectionMode.Parallel): List<SpeedTestProgress> = runBlocking { runSpeedTest(mode).toList() }

    private class FakeServerRepository : ServerRepository {
        var result: NetworkOutcome<List<SpeedTestServer>> = Outcome.Success(DIRECTORY)

        override suspend fun servers() = result
    }

    private class FakeLocationRepository : LocationRepository {
        var result: NetworkOutcome<GeoPoint> = Outcome.Success(ORIGIN)

        override suspend fun currentLocation() = result

        override fun distance(from: GeoPoint, to: GeoPoint): Distance = Distance(to.latitude.degrees - from.latitude.degrees)
    }

    private class FakeLatencyRepository : LatencyRepository {
        var latencies: Map<ServerUrl, Duration?> = mapOf(NEAR to 20.milliseconds, MIDDLE to 20.milliseconds, FAR to 20.milliseconds)

        override suspend fun measureLatency(server: SpeedTestServer): SpeedTestOutcome<Duration> =
            latencies.getValue(server.url)?.let { Outcome.Success(it) } ?: Outcome.Failure(SpeedTestError.ServerUnreachable)
    }

    private class FakeThroughputRepository : ThroughputRepository {
        var download: Flow<NetworkOutcome<TransferSample>> = steady(bytesPerTick = 1_000)
        var upload: Flow<NetworkOutcome<TransferSample>> = steady(bytesPerTick = 1_000)
        val connectionsRequested = mutableListOf<Int>()
        var lastDownloadSampleTaken = Duration.ZERO

        override fun download(server: SpeedTestServer, connections: Int): Flow<NetworkOutcome<TransferSample>> {
            connectionsRequested += connections
            return download.onEach { if (it is Outcome.Success) lastDownloadSampleTaken = it.value.elapsed }
        }

        override fun upload(server: SpeedTestServer, connections: Int): Flow<NetworkOutcome<TransferSample>> {
            connectionsRequested += connections
            return upload
        }
    }

    private companion object {
        val TICK = 250.milliseconds
        fun point(latitude: Double, longitude: Double) = GeoPoint(Latitude(latitude), Longitude(longitude))

        val ORIGIN = point(49.0, 16.0)

        val NEAR = ServerUrl("https://near.example")
        val MIDDLE = ServerUrl("https://middle.example")
        val FAR = ServerUrl("https://far.example")

        val DIRECTORY = listOf(
            SpeedTestServer(FAR, point(52.0, 16.0), null, null, null),
            SpeedTestServer(NEAR, point(49.5, 16.0), null, null, null),
            SpeedTestServer(MIDDLE, point(50.0, 16.0), null, null, null),
        )

        fun sample(tick: Int, totalBytes: Long): NetworkOutcome<TransferSample> =
            Outcome.Success(TransferSample(TICK * tick, ByteCount(totalBytes)))

        fun steady(bytesPerTick: Long, until: Duration = PHASE_DURATION): Flow<NetworkOutcome<TransferSample>> {
            val ticks = (until / TICK).toInt()
            return flowOf(*Array(ticks) { index -> sample(index + 1, bytesPerTick * (index + 1)) })
        }
    }
}
