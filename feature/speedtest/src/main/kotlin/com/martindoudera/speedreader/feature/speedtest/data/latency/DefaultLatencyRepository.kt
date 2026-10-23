package com.martindoudera.speedreader.feature.speedtest.data.latency

import com.martindoudera.speedreader.core.common.di.Dispatcher
import com.martindoudera.speedreader.core.common.di.SpeedReaderDispatcher
import com.martindoudera.speedreader.core.common.error.Outcome
import com.martindoudera.speedreader.feature.speedtest.domain.SpeedTestError
import com.martindoudera.speedreader.feature.speedtest.domain.SpeedTestOutcome
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestServer
import com.martindoudera.speedreader.feature.speedtest.domain.repository.LatencyRepository
import java.io.IOException
import java.net.Inet6Address
import java.net.InetAddress
import java.net.UnknownHostException
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import timber.log.Timber

internal const val PING_PROBES = 3
internal const val LOST_PROBES_IN_A_ROW = 2
internal val PROBE_TIMEOUT = 500.milliseconds

/**
 * Pings a server up to [PING_PROBES] times, one probe after another, and reports the minimum
 * round trip: queueing only ever adds delay, so the minimum is the best estimate of the path itself.
 * A single lost probe is ordinary packet loss; [LOST_PROBES_IN_A_ROW] lost probes end the probing.
 *
 * The hostname is resolved first, so DNS time never counts as latency. The first address of each
 * family is tried in the resolver's order, so a server whose IPv6 path is broken is still reached
 * over IPv4, as OkHttp would reach it. A server whose name does not resolve or that answers no
 * probe on any address is [SpeedTestError.ServerUnreachable].
 */
internal class DefaultLatencyRepository @Inject constructor(
    private val pingDataSource: PingDataSource,
    private val dns: Dns,
    @param:Dispatcher(SpeedReaderDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) : LatencyRepository {
    override suspend fun measureLatency(server: SpeedTestServer): SpeedTestOutcome<Duration> = withContext(ioDispatcher) {
        for (address in resolve(server)) {
            val roundTrips = try {
                probe(address)
            } catch (e: IOException) {
                Timber.e(e, "Pinging %s failed", address.hostAddress)
                return@withContext Outcome.Failure(SpeedTestError.Unknown(e))
            }

            val latency = roundTrips.minOrNull()
            Timber.d("%s answered %d of up to %d probes, latency %s", address.hostAddress, roundTrips.size, PING_PROBES, latency)
            if (latency != null) return@withContext Outcome.Success(latency)
        }
        Timber.d("%s answered no probe", server.url.value)
        Outcome.Failure(SpeedTestError.ServerUnreachable)
    }

    private suspend fun probe(address: InetAddress): List<Duration> {
        val roundTrips = mutableListOf<Duration>()
        var lostInARow = 0
        repeat(PING_PROBES) {
            currentCoroutineContext().ensureActive()
            val roundTrip = pingDataSource.echo(address, PROBE_TIMEOUT)
            Timber.v("Echo from %s: %s", address.hostAddress, roundTrip)
            if (roundTrip != null) {
                roundTrips += roundTrip
                lostInARow = 0
            } else if (++lostInARow == LOST_PROBES_IN_A_ROW) {
                return roundTrips
            }
        }
        return roundTrips
    }

    private fun resolve(server: SpeedTestServer): List<InetAddress> {
        val host = server.url.value.toHttpUrlOrNull()?.host ?: return emptyList()
        return try {
            dns.lookup(host).distinctBy { it is Inet6Address }
        } catch (e: UnknownHostException) {
            Timber.w(e, "Cannot resolve %s", host)
            emptyList()
        }
    }
}
