package com.martindoudera.speedreader.feature.speedtest.data.throughput

import com.martindoudera.speedreader.core.common.di.Dispatcher
import com.martindoudera.speedreader.core.common.di.SpeedReaderDispatcher
import com.martindoudera.speedreader.core.common.error.NetworkError
import com.martindoudera.speedreader.core.common.error.NetworkOutcome
import com.martindoudera.speedreader.core.common.error.Outcome
import com.martindoudera.speedreader.core.common.error.onFailure
import com.martindoudera.speedreader.core.network.safeApiCall
import com.martindoudera.speedreader.feature.speedtest.domain.model.ByteCount
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestServer
import com.martindoudera.speedreader.feature.speedtest.domain.model.TransferSample
import com.martindoudera.speedreader.feature.speedtest.domain.repository.ThroughputRepository
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber

/** How often the transferred total is reported; 7.5 s is a whole number of these. */
internal val TRANSFER_SAMPLE_INTERVAL = 250.milliseconds

/**
 * Runs `connections` parallel streams against one server, all adding into one byte counter, and
 * reports the counter every [TRANSFER_SAMPLE_INTERVAL] until the collector stops.
 *
 * A stream whose request ends is restarted. A stream that fails stops, and the others carry on, so
 * one dropped connection costs only its share of the speed. Only when every stream has failed is
 * the last failure emitted as the final element. Elapsed time runs
 * from the moment the streams start, so connection setup counts, as it does for the user.
 */
internal class DefaultThroughputRepository @Inject constructor(
    private val downloadDataSource: DownloadDataSource,
    private val uploadDataSource: UploadDataSource,
    private val timeSource: TimeSource,
    @param:Dispatcher(SpeedReaderDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) : ThroughputRepository {
    override fun download(server: SpeedTestServer, connections: Int): Flow<NetworkOutcome<TransferSample>> =
        transfer("Download", connections) { onBytes -> downloadDataSource.download(server.url, onBytes) }

    override fun upload(server: SpeedTestServer, connections: Int): Flow<NetworkOutcome<TransferSample>> =
        transfer("Upload", connections) { onBytes -> uploadDataSource.upload(server.url, onBytes) }

    private fun transfer(
        direction: String,
        connections: Int,
        request: suspend (onBytes: (Long) -> Unit) -> Unit,
    ): Flow<NetworkOutcome<TransferSample>> = channelFlow {
        val transferred = AtomicLong()
        val failed = AtomicInteger()
        val failure = AtomicReference<NetworkError?>()
        val startedAt = timeSource.markNow()
        Timber.d("%s started on %d connections", direction, connections)

        repeat(connections) { stream ->
            launch {
                safeApiCall {
                    while (true) {
                        request { bytes -> transferred.addAndGet(bytes) }
                        Timber.v("%s stream %d: request ended, starting another", direction, stream)
                    }
                }.onFailure { error ->
                    Timber.w("%s stream %d failed: %s", direction, stream, error)
                    if (failed.incrementAndGet() == connections) {
                        Timber.e("%s failed on every stream", direction)
                        failure.set(error)
                    }
                }
            }
        }
        try {
            while (failure.get() == null) {
                delay(TRANSFER_SAMPLE_INTERVAL)
                send(Outcome.Success(TransferSample(startedAt.elapsedNow(), ByteCount(transferred.get()))))
            }
            send(Outcome.Failure(checkNotNull(failure.get())))
        } finally {
            Timber.d("%s ended after %s, %d bytes", direction, startedAt.elapsedNow(), transferred.get())
        }
    }.flowOn(ioDispatcher)
}
