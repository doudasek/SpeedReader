package com.martindoudera.speedreader.feature.speedtest.data.latency

import com.martindoudera.speedreader.core.common.di.Dispatcher
import com.martindoudera.speedreader.core.common.di.SpeedReaderDispatcher
import com.martindoudera.speedreader.core.common.error.Outcome
import com.martindoudera.speedreader.feature.speedtest.domain.SpeedTestError
import com.martindoudera.speedreader.feature.speedtest.domain.SpeedTestOutcome
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestServer
import com.martindoudera.speedreader.feature.speedtest.domain.repository.LatencyRepository
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import timber.log.Timber

internal const val PING_PROBES = 5
internal val PROBE_TIMEOUT = 1.seconds

/**
 * Pings a server's resolved address [PING_PROBES] times, one probe after another, and reports the
 * minimum round trip: queueing only ever adds delay, so the minimum is the best estimate of the
 * path itself.
 *
 * The hostname is resolved first, so DNS time never counts as latency. A server whose name does
 * not resolve or that answers no probe is [SpeedTestError.ServerUnreachable].
 */
internal class DefaultLatencyRepository @Inject constructor(
    private val pingDataSource: PingDataSource,
    private val dns: Dns,
    @param:Dispatcher(SpeedReaderDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) : LatencyRepository {
    override suspend fun measureLatency(server: SpeedTestServer): SpeedTestOutcome<Duration> = withContext(ioDispatcher) {
        val address = resolve(server) ?: return@withContext Outcome.Failure(SpeedTestError.ServerUnreachable)
        val roundTrips = try {
            List(PING_PROBES) {
                ensureActive()
                pingDataSource.echo(address, PROBE_TIMEOUT).also { Timber.v("Echo from %s: %s", address.hostAddress, it) }
            }.filterNotNull()
        } catch (e: IOException) {
            Timber.e(e, "Pinging %s failed", address.hostAddress)
            return@withContext Outcome.Failure(SpeedTestError.Unknown(e))
        }

        val latency = roundTrips.minOrNull()
        Timber.d("%s answered %d of %d probes, latency %s", server.url.value, roundTrips.size, PING_PROBES, latency)
        latency?.let { Outcome.Success(it) } ?: Outcome.Failure(SpeedTestError.ServerUnreachable)
    }

    private fun resolve(server: SpeedTestServer): InetAddress? {
        val host = server.url.value.toHttpUrlOrNull()?.host ?: return null
        return try {
            dns.lookup(host).first()
        } catch (e: UnknownHostException) {
            Timber.w(e, "Cannot resolve %s", host)
            null
        }
    }
}
