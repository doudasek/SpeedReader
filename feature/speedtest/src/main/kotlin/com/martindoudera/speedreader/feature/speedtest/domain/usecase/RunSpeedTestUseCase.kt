package com.martindoudera.speedreader.feature.speedtest.domain.usecase

import com.martindoudera.speedreader.core.common.error.NetworkOutcome
import com.martindoudera.speedreader.core.common.error.Outcome
import com.martindoudera.speedreader.core.common.error.onFailure
import com.martindoudera.speedreader.core.common.error.valueOrNull
import com.martindoudera.speedreader.feature.speedtest.domain.SpeedTestError
import com.martindoudera.speedreader.feature.speedtest.domain.SpeedTestOutcome
import com.martindoudera.speedreader.feature.speedtest.domain.ThroughputMeter
import com.martindoudera.speedreader.feature.speedtest.domain.model.ByteCount
import com.martindoudera.speedreader.feature.speedtest.domain.model.ConnectionMode
import com.martindoudera.speedreader.feature.speedtest.domain.model.Progress
import com.martindoudera.speedreader.feature.speedtest.domain.model.SelectedServer
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestProgress
import com.martindoudera.speedreader.feature.speedtest.domain.model.Throughput
import com.martindoudera.speedreader.feature.speedtest.domain.model.TransferSample
import com.martindoudera.speedreader.feature.speedtest.domain.repository.LatencyRepository
import com.martindoudera.speedreader.feature.speedtest.domain.repository.LocationRepository
import com.martindoudera.speedreader.feature.speedtest.domain.repository.ServerRepository
import com.martindoudera.speedreader.feature.speedtest.domain.repository.ThroughputRepository
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.transformWhile
import timber.log.Timber

val TEST_DURATION = 15.seconds
val PHASE_DURATION = TEST_DURATION / 2

/**
 * Runs a whole speed test: finds the best server, then measures download and upload for
 * [PHASE_DURATION] each, so both together take [TEST_DURATION]. Emits [SpeedTestProgress] along the way.
 *
 * The flow never throws; it ends with [SpeedTestProgress.Finished] or [SpeedTestProgress.Failed].
 * Cancelling the collection stops the run.
 */
class RunSpeedTestUseCase @Inject constructor(
    private val serverRepository: ServerRepository,
    private val locationRepository: LocationRepository,
    private val latencyRepository: LatencyRepository,
    private val throughputRepository: ThroughputRepository,
    private val selectClosestServers: SelectClosestServersUseCase,
) {
    operator fun invoke(mode: ConnectionMode): Flow<SpeedTestProgress> = flow {
        Timber.d("Speed test started in %s mode, %d connections", mode, mode.connections)
        emit(SpeedTestProgress.FindingServer)
        val server = valueOrFail(findServer()) ?: return@flow

        val downloadSamples = throughputRepository.download(server.server, mode.connections)
        val downloadOutcome = measure(downloadSamples, startsAt = Duration.ZERO) { progress, remaining, speed ->
            SpeedTestProgress.Downloading(server, progress, remaining, speed)
        }
        val download = valueOrFail(downloadOutcome) ?: return@flow
        Timber.d("Download average %.1f Mbit/s", download.megabitsPerSecond)

        val uploadSamples = throughputRepository.upload(server.server, mode.connections)
        val uploadOutcome = measure(uploadSamples, startsAt = PHASE_DURATION) { progress, remaining, speed ->
            SpeedTestProgress.Uploading(server, download, progress, remaining, speed)
        }
        val upload = valueOrFail(uploadOutcome) ?: return@flow

        Timber.i("Speed test finished: download %.1f Mbit/s, upload %.1f Mbit/s", download.megabitsPerSecond, upload.megabitsPerSecond)
        emit(SpeedTestProgress.Finished(server, download, upload))
    }.onCompletion { cause ->
        if (cause is CancellationException) Timber.d("Speed test stopped before it finished")
    }

    private suspend fun findServer(): SpeedTestOutcome<SelectedServer> = coroutineScope {
        val location = async { locationRepository.currentLocation().valueOrNull() }
        val servers = when (val fetched = serverRepository.servers()) {
            is Outcome.Failure -> {
                location.cancel()
                return@coroutineScope Outcome.Failure(SpeedTestError.Network(fetched.error))
            }
            is Outcome.Success -> fetched.value
        }
        if (servers.isEmpty()) {
            location.cancel()
            return@coroutineScope Outcome.Failure(SpeedTestError.NoServersAvailable)
        }

        val fastest = selectClosestServers(servers, location.await())
            .map { candidate -> async { candidate to latencyRepository.measureLatency(candidate).valueOrNull() } }
            .awaitAll()
            .mapNotNull { (candidate, latency) -> latency?.let { candidate to it } }
            .minByOrNull { (_, latency) -> latency }
            ?: return@coroutineScope Outcome.Failure(SpeedTestError.NoServerReachable)
        Timber.d("Selected %s (%s), latency %s", fastest.first.url.value, fastest.first.provider, fastest.second)
        Outcome.Success(SelectedServer(fastest.first, fastest.second))
    }

    private suspend fun FlowCollector<SpeedTestProgress>.measure(
        samples: Flow<NetworkOutcome<TransferSample>>,
        startsAt: Duration,
        progressOf: (progress: Progress, remaining: Duration, currentSpeed: Throughput) -> SpeedTestProgress,
    ): SpeedTestOutcome<Throughput> {
        val meter = ThroughputMeter()
        var failure: SpeedTestError? = null
        samples
            .transformWhile { outcome ->
                when (outcome) {
                    is Outcome.Failure -> {
                        failure = SpeedTestError.Network(outcome.error)
                        false
                    }
                    is Outcome.Success -> {
                        emit(outcome.value)
                        outcome.value.elapsed < PHASE_DURATION
                    }
                }
            }.collect { sample ->
                val currentSpeed = meter.record(sample)
                Timber.v("%s: %d B, current %.1f Mbit/s", sample.elapsed, sample.transferred.bytes, currentSpeed.megabitsPerSecond)
                val testElapsed = startsAt + sample.elapsed.coerceAtMost(PHASE_DURATION)
                emit(progressOf(Progress((testElapsed / TEST_DURATION).toFloat()), TEST_DURATION - testElapsed, currentSpeed))
            }

        failure?.let { return Outcome.Failure(it) }
        if (meter.transferred == ByteCount.Zero) return Outcome.Failure(SpeedTestError.MeasurementFailed)
        return Outcome.Success(meter.average())
    }

    private suspend fun <T> FlowCollector<SpeedTestProgress>.valueOrFail(outcome: SpeedTestOutcome<T>): T? = outcome
        .onFailure { error ->
            Timber.e("Speed test failed: %s", error)
            emit(SpeedTestProgress.Failed(error))
        }.valueOrNull()
}
