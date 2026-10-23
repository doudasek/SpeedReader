package com.martindoudera.speedreader.feature.speedtest.ui

import androidx.lifecycle.SavedStateHandle
import com.martindoudera.speedreader.core.common.domain.model.AppVersion
import com.martindoudera.speedreader.core.common.error.NetworkError
import com.martindoudera.speedreader.core.common.error.Outcome
import com.martindoudera.speedreader.feature.speedtest.domain.model.ConnectionMode
import com.martindoudera.speedreader.feature.speedtest.domain.model.ServerUrl
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestServer
import com.martindoudera.speedreader.feature.speedtest.domain.usecase.PHASE_DURATION
import com.martindoudera.speedreader.feature.speedtest.domain.usecase.RunSpeedTestUseCase
import com.martindoudera.speedreader.feature.speedtest.domain.usecase.SelectClosestServersUseCase
import com.martindoudera.speedreader.feature.speedtest.testing.MainDispatcherRule
import com.martindoudera.speedreader.feature.speedtest.testing.data.geoPoint
import com.martindoudera.speedreader.feature.speedtest.testing.data.steadyTransfer
import com.martindoudera.speedreader.feature.speedtest.testing.data.transferSample
import com.martindoudera.speedreader.feature.speedtest.testing.repository.FakeLatencyRepository
import com.martindoudera.speedreader.feature.speedtest.testing.repository.FakeLocationRepository
import com.martindoudera.speedreader.feature.speedtest.testing.repository.FakeServerRepository
import com.martindoudera.speedreader.feature.speedtest.testing.repository.FakeThroughputRepository
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SpeedTestViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val serverRepository = FakeServerRepository()
    private val locationRepository = FakeLocationRepository()
    private val latencyRepository = FakeLatencyRepository()
    private val throughputRepository = FakeThroughputRepository()

    @Before
    fun givenAHealthyServer() {
        serverRepository.servers = Outcome.Success(listOf(SERVER))
        locationRepository.location = Outcome.Success(geoPoint(49.0, 16.0))
        latencyRepository.latencies = mapOf(SERVER.url to 9.milliseconds)
        throughputRepository.downloadSamples = steadyTransfer(bytesPerTick = 2_500_000, until = PHASE_DURATION)
        throughputRepository.uploadSamples = steadyTransfer(bytesPerTick = 250_000, until = PHASE_DURATION)
    }

    @Test
    fun startsIdleWithParallelConnections() {
        val viewModel = createViewModel()

        assertEquals(SpeedTestUiState(APP_VERSION, ConnectionMode.Parallel, SpeedTestPhase.Idle), viewModel.uiState.value)
    }

    @Test
    fun keepsTheChosenConnectionModeAcrossProcessDeath() {
        val savedState = SavedStateHandle()
        createViewModel(savedState).onSingleConnectionChange(true)

        val restored = createViewModel(savedState)

        assertEquals(ConnectionMode.Single, restored.uiState.value.connectionMode)
    }

    @Test
    fun showsFindingServerUntilTheMeasurementStarts() {
        throughputRepository.downloadSamples = flow { awaitCancellation() }
        val viewModel = createViewModel()

        viewModel.onStartClick()

        assertEquals(SpeedTestPhase.FindingServer, viewModel.uiState.value.phase)
    }

    @Test
    fun showsTheCurrentAndTheAverageSpeedWhileDownloading() {
        throughputRepository.downloadSamples = flow {
            emit(transferSample(tick = 1, totalBytes = 250_000))
            emit(transferSample(tick = 8, totalBytes = 1_250_000))
            emit(transferSample(tick = 12, totalBytes = 5_250_000))
            awaitCancellation()
        }
        val viewModel = createViewModel()

        viewModel.onStartClick()

        val downloading = assertIs<SpeedTestPhase.Downloading>(viewModel.uiState.value.phase)
        assertEquals(32_000_000.0, downloading.currentSpeed.bitsPerSecond, 1e-6)
        assertEquals(14_000_000.0, downloading.averageSpeed.bitsPerSecond, 1e-6)
    }

    @Test
    fun labelsTheServerWithItsProviderAndLocation() {
        throughputRepository.downloadSamples = flow {
            emit(transferSample(tick = 1, totalBytes = 1_000))
            awaitCancellation()
        }
        val viewModel = createViewModel()

        viewModel.onStartClick()

        val downloading = assertIs<SpeedTestPhase.Downloading>(viewModel.uiState.value.phase)
        assertEquals(ServerUiModel(name = "CETIN a.s.", location = "Prague, CZ", latency = 9.milliseconds), downloading.server)
    }

    @Test
    fun namesAServerWithoutProviderByItsHost() {
        val anonymous = SERVER.copy(provider = null, city = null, countryCode = null)
        serverRepository.servers = Outcome.Success(listOf(anonymous))
        val viewModel = createViewModel()

        viewModel.onStartClick()

        val finished = assertIs<SpeedTestPhase.Finished>(viewModel.uiState.value.phase)
        assertEquals(ServerUiModel(name = "speed.example.net", location = null, latency = 9.milliseconds), finished.server)
    }

    @Test
    fun keepsTheDownloadAverageWhileUploading() {
        throughputRepository.uploadSamples = flow {
            emit(transferSample(tick = 1, totalBytes = 250_000))
            awaitCancellation()
        }
        val viewModel = createViewModel()

        viewModel.onStartClick()

        val uploading = assertIs<SpeedTestPhase.Uploading>(viewModel.uiState.value.phase)
        assertEquals(80_000_000.0, uploading.downloadSpeed.bitsPerSecond, 1e-6)
        assertEquals(8_000_000.0, uploading.averageSpeed.bitsPerSecond, 1e-6)
    }

    @Test
    fun showsBothAveragesWhenTheTestFinishes() {
        val viewModel = createViewModel()

        viewModel.onStartClick()

        val finished = assertIs<SpeedTestPhase.Finished>(viewModel.uiState.value.phase)
        assertEquals(80_000_000.0, finished.downloadSpeed.bitsPerSecond, 1e-6)
        assertEquals(8_000_000.0, finished.uploadSpeed.bitsPerSecond, 1e-6)
    }

    @Test
    fun stopReturnsToIdle() {
        throughputRepository.downloadSamples = flow { awaitCancellation() }
        val viewModel = createViewModel()
        viewModel.onStartClick()

        viewModel.onStopClick()

        assertEquals(SpeedTestPhase.Idle, viewModel.uiState.value.phase)
    }

    @Test
    fun startIsIgnoredWhileATestIsRunning() {
        throughputRepository.downloadSamples = flow { awaitCancellation() }
        val viewModel = createViewModel()
        viewModel.onStartClick()

        viewModel.onStartClick()

        assertEquals(listOf(6), throughputRepository.requestedConnections)
    }

    @Test
    fun runsWithASingleConnectionWhenChosen() {
        val viewModel = createViewModel()
        viewModel.onSingleConnectionChange(true)

        viewModel.onStartClick()

        assertEquals(listOf(1, 1), throughputRepository.requestedConnections)
    }

    @Test
    fun backFromTheResultReturnsToIdle() {
        val viewModel = createViewModel()
        viewModel.onStartClick()

        viewModel.onBackClick()

        assertEquals(SpeedTestPhase.Idle, viewModel.uiState.value.phase)
    }

    @Test
    fun backIsIgnoredWhileATestIsRunning() {
        throughputRepository.downloadSamples = flow { awaitCancellation() }
        val viewModel = createViewModel()
        viewModel.onStartClick()

        viewModel.onBackClick()

        assertEquals(SpeedTestPhase.FindingServer, viewModel.uiState.value.phase)
    }

    @Test
    fun explainsAMissingConnectionAsOffline() {
        serverRepository.servers = Outcome.Failure(NetworkError.NoConnection(null))
        val viewModel = createViewModel()

        viewModel.onStartClick()

        assertEquals(SpeedTestPhase.Failed(FailureReason.Offline), viewModel.uiState.value.phase)
    }

    @Test
    fun showsNoLatencyWhenNoServerAnswersPing() {
        latencyRepository.latencies = emptyMap()
        val viewModel = createViewModel()

        viewModel.onStartClick()

        val finished = assertIs<SpeedTestPhase.Finished>(viewModel.uiState.value.phase)
        assertEquals(ServerUiModel(name = "CETIN a.s.", location = "Prague, CZ", latency = null), finished.server)
    }

    @Test
    fun explainsATimedOutTransferAsConnectionLost() {
        throughputRepository.downloadSamples = flow {
            emit(transferSample(tick = 1, totalBytes = 1_000))
            emit(Outcome.Failure(NetworkError.Timeout(null)))
        }
        val viewModel = createViewModel()

        viewModel.onStartClick()

        assertEquals(SpeedTestPhase.Failed(FailureReason.ConnectionLost), viewModel.uiState.value.phase)
    }

    @Test
    fun explainsAnUnexpectedExceptionAsUnexpected() {
        throughputRepository.downloadSamples = flow { throw IllegalStateException("Boom") }
        val viewModel = createViewModel()

        viewModel.onStartClick()

        assertEquals(SpeedTestPhase.Failed(FailureReason.Unexpected), viewModel.uiState.value.phase)
    }

    @Test
    fun startsAgainAfterAnUnexpectedException() {
        throughputRepository.downloadSamples = flow { throw IllegalStateException("Boom") }
        val viewModel = createViewModel()
        viewModel.onStartClick()
        throughputRepository.downloadSamples = flow { awaitCancellation() }

        viewModel.onStartClick()

        assertEquals(SpeedTestPhase.FindingServer, viewModel.uiState.value.phase)
    }

    private fun createViewModel(savedState: SavedStateHandle = SavedStateHandle()) = SpeedTestViewModel(
        RunSpeedTestUseCase(
            serverRepository,
            locationRepository,
            latencyRepository,
            throughputRepository,
            SelectClosestServersUseCase(locationRepository),
        ),
        savedState,
        APP_VERSION,
    )

    private companion object {
        val APP_VERSION = AppVersion("0.0.1")

        val SERVER = SpeedTestServer(
            url = ServerUrl("https://speed.example.net:8080"),
            location = geoPoint(49.5, 16.0),
            provider = "CETIN a.s.",
            city = "Prague",
            countryCode = "CZ",
        )
    }
}
