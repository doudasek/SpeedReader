package com.martindoudera.speedreader.feature.speedtest.domain.usecase

import com.martindoudera.speedreader.core.common.error.HttpStatusCode
import com.martindoudera.speedreader.core.common.error.NetworkError
import com.martindoudera.speedreader.core.common.error.Outcome
import com.martindoudera.speedreader.feature.speedtest.domain.SpeedTestError
import com.martindoudera.speedreader.feature.speedtest.domain.model.ConnectionMode
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestProgress.Downloading
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestProgress.Failed
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestProgress.FindingServer
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestProgress.Finished
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestProgress.Uploading
import com.martindoudera.speedreader.feature.speedtest.testing.data.geoPoint
import com.martindoudera.speedreader.feature.speedtest.testing.data.steadyTransfer
import com.martindoudera.speedreader.feature.speedtest.testing.data.testServer
import com.martindoudera.speedreader.feature.speedtest.testing.data.transferSample
import com.martindoudera.speedreader.feature.speedtest.testing.repository.FakeLatencyRepository
import com.martindoudera.speedreader.feature.speedtest.testing.repository.FakeLocationRepository
import com.martindoudera.speedreader.feature.speedtest.testing.repository.FakeServerRepository
import com.martindoudera.speedreader.feature.speedtest.testing.repository.FakeThroughputRepository
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RunSpeedTestUseCaseTest {
    private val serverRepository = FakeServerRepository()
    private val locationRepository = FakeLocationRepository()
    private val latencyRepository = FakeLatencyRepository()
    private val throughputRepository = FakeThroughputRepository()

    private val runSpeedTest = RunSpeedTestUseCase(
        serverRepository,
        locationRepository,
        latencyRepository,
        throughputRepository,
        SelectClosestServersUseCase(locationRepository),
    )

    @Before
    fun givenAHealthyDirectoryAndServers() {
        serverRepository.servers = Outcome.Success(DIRECTORY)
        locationRepository.location = Outcome.Success(ORIGIN)
        latencyRepository.latencies = DIRECTORY.associate { it.url to 20.milliseconds }
        throughputRepository.downloadSamples = steadyTransfer(bytesPerTick = 1_000, until = PHASE_DURATION)
        throughputRepository.uploadSamples = steadyTransfer(bytesPerTick = 1_000, until = PHASE_DURATION)
    }

    @Test
    fun passesThroughEveryPhaseInOrder() = runTest {
        val states = runSpeedTest(ConnectionMode.Parallel).toList()

        val phases = states.map { it::class }.distinct()
        assertEquals(listOf(FindingServer::class, Downloading::class, Uploading::class, Finished::class), phases)
    }

    @Test
    fun finishesWithTheAverageOfEachPhase() = runTest {
        throughputRepository.downloadSamples = steadyTransfer(bytesPerTick = 2_500_000, until = PHASE_DURATION)
        throughputRepository.uploadSamples = steadyTransfer(bytesPerTick = 250_000, until = PHASE_DURATION)

        val states = runSpeedTest(ConnectionMode.Parallel).toList()

        val finished = assertIs<Finished>(states.last())
        assertEquals(80_000_000.0, finished.downloadSpeed.bitsPerSecond, 1e-6)
        assertEquals(8_000_000.0, finished.uploadSpeed.bitsPerSecond, 1e-6)
    }

    @Test
    fun uploadPhaseCarriesTheDownloadAverage() = runTest {
        throughputRepository.downloadSamples = steadyTransfer(bytesPerTick = 2_500_000, until = PHASE_DURATION)

        val states = runSpeedTest(ConnectionMode.Parallel).toList()

        val uploading = states.filterIsInstance<Uploading>()
        assertTrue(uploading.isNotEmpty())
        assertTrue(uploading.all { it.downloadSpeed.bitsPerSecond == 80_000_000.0 })
    }

    @Test
    fun measuringStatesCarryTheRunningAverageOfTheirPhase() = runTest {
        throughputRepository.downloadSamples =
            flowOf(transferSample(tick = 1, totalBytes = 250_000), transferSample(tick = 2, totalBytes = 1_000_000))

        val states = runSpeedTest(ConnectionMode.Parallel).toList()

        val downloading = states.filterIsInstance<Downloading>()
        assertEquals(8_000_000.0, downloading[0].averageSpeed.bitsPerSecond, 1e-6)
        assertEquals(16_000_000.0, downloading[1].averageSpeed.bitsPerSecond, 1e-6)
    }

    @Test
    fun eachPhaseStopsAtHalfTheTestEvenIfTheServerKeepsSending() = runTest {
        throughputRepository.downloadSamples = steadyTransfer(bytesPerTick = 1_000, until = 20.seconds)

        val states = runSpeedTest(ConnectionMode.Parallel).toList()

        assertEquals(PHASE_DURATION, throughputRepository.lastDownloadSampleConsumed)
        assertEquals(30, states.filterIsInstance<Downloading>().size)
    }

    @Test
    fun progressAndRemainingTimeSpanTheWholeTest() = runTest {
        val states = runSpeedTest(ConnectionMode.Parallel).toList()

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
    fun progressNeverPassesTheEndOfItsPhase() = runTest {
        throughputRepository.downloadSamples = steadyTransfer(bytesPerTick = 1_000, until = 20.seconds)
        throughputRepository.uploadSamples =
            flowOf(transferSample(tick = 1, totalBytes = 1_000), transferSample(tick = 40, totalBytes = 2_000))

        val states = runSpeedTest(ConnectionMode.Parallel).toList()

        assertEquals(0.5f, states.filterIsInstance<Downloading>().maxOf { it.progress.fraction })
        assertEquals(1f, states.filterIsInstance<Uploading>().last().progress.fraction)
        assertEquals(Duration.ZERO, states.filterIsInstance<Uploading>().last().remaining)
    }

    @Test
    fun picksTheReachableServerWithTheLowestLatency() = runTest {
        latencyRepository.latencies = mapOf(NEAR.url to 30.milliseconds, MIDDLE.url to 12.milliseconds, FAR.url to 8.milliseconds)

        val states = runSpeedTest(ConnectionMode.Parallel).toList()

        val finished = assertIs<Finished>(states.last())
        assertEquals(FAR, finished.server.server)
        assertEquals(8.milliseconds, finished.server.latency)
    }

    @Test
    fun prefersTheClosestServerWhenLatenciesTie() = runTest {
        val states = runSpeedTest(ConnectionMode.Parallel).toList()

        assertEquals(NEAR, assertIs<Finished>(states.last()).server.server)
    }

    @Test
    fun skipsServersThatDoNotAnswer() = runTest {
        latencyRepository.latencies = mapOf(MIDDLE.url to 20.milliseconds)

        val states = runSpeedTest(ConnectionMode.Parallel).toList()

        assertEquals(MIDDLE, assertIs<Finished>(states.last()).server.server)
    }

    @Test
    fun failsWhenNoServerAnswers() = runTest {
        latencyRepository.latencies = emptyMap()

        val states = runSpeedTest(ConnectionMode.Parallel).toList()

        assertEquals(listOf(FindingServer, Failed(SpeedTestError.NoServerReachable)), states)
    }

    @Test
    fun failsWithTheNetworkErrorWhenTheServerListCannotBeLoaded() = runTest {
        val error = NetworkError.Http(HttpStatusCode(503))
        serverRepository.servers = Outcome.Failure(error)

        val states = runSpeedTest(ConnectionMode.Parallel).toList()

        assertEquals(listOf(FindingServer, Failed(SpeedTestError.Network(error))), states)
    }

    @Test
    fun failsWhenTheDirectoryOffersNoServers() = runTest {
        serverRepository.servers = Outcome.Success(emptyList())

        val states = runSpeedTest(ConnectionMode.Parallel).toList()

        assertEquals(listOf(FindingServer, Failed(SpeedTestError.NoServersAvailable)), states)
    }

    @Test
    fun withoutLocationUsesTheDirectoryOrder() = runTest {
        locationRepository.location = Outcome.Failure(NetworkError.Timeout(null))

        val states = runSpeedTest(ConnectionMode.Parallel).toList()

        assertEquals(FAR, assertIs<Finished>(states.last()).server.server)
    }

    @Test
    fun downloadFailureEndsTheRunWithTheNetworkError() = runTest {
        val error = NetworkError.NoConnection(null)
        throughputRepository.downloadSamples = flowOf(transferSample(tick = 1, totalBytes = 1_000), Outcome.Failure(error))

        val states = runSpeedTest(ConnectionMode.Parallel).toList()

        assertEquals(Failed(SpeedTestError.Network(error)), states.last())
        assertTrue(states.none { it is Uploading })
    }

    @Test
    fun unexpectedExceptionEndsTheRunAsUnknownInsteadOfThrowing() = runTest {
        val exception = IllegalStateException("Boom")
        throughputRepository.downloadSamples = flow { throw exception }

        val states = runSpeedTest(ConnectionMode.Parallel).toList()

        assertEquals(Failed(SpeedTestError.Unknown(exception)), states.last())
    }

    @Test
    fun stoppingTheRunIsNotReportedAsAFailure() = runTest {
        throughputRepository.downloadSamples = flow { awaitCancellation() }

        val states = runSpeedTest(ConnectionMode.Parallel).take(1).toList()

        assertEquals(listOf(FindingServer), states)
    }

    @Test
    fun uploadWithoutASingleByteIsAMeasurementFailure() = runTest {
        throughputRepository.uploadSamples = steadyTransfer(bytesPerTick = 0, until = PHASE_DURATION)

        val states = runSpeedTest(ConnectionMode.Parallel).toList()

        assertEquals(Failed(SpeedTestError.MeasurementFailed), states.last())
    }

    @Test
    fun singleModeUsesOneConnectionInEachDirection() = runTest {
        runSpeedTest(ConnectionMode.Single).toList()

        assertEquals(listOf(1, 1), throughputRepository.requestedConnections)
    }

    @Test
    fun parallelModeUsesSixConnectionsInEachDirection() = runTest {
        runSpeedTest(ConnectionMode.Parallel).toList()

        assertEquals(listOf(6, 6), throughputRepository.requestedConnections)
    }

    private companion object {
        val ORIGIN = geoPoint(49.0, 16.0)

        val FAR = testServer("https://far.example", 52.0, 16.0)
        val NEAR = testServer("https://near.example", 49.5, 16.0)
        val MIDDLE = testServer("https://middle.example", 50.0, 16.0)

        val DIRECTORY = listOf(FAR, NEAR, MIDDLE)
    }
}
