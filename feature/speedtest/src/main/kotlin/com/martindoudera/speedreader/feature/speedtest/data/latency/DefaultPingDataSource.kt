package com.martindoudera.speedreader.feature.speedtest.data.latency

import java.net.InetAddress
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.TimeSource
import timber.log.Timber

/**
 * Pings with [InetAddress.isReachable], timed around the call.
 *
 * On Android, unlike the JVM, `isReachable` sends a real ICMP echo request over an unprivileged
 * ping socket (`SOCK_DGRAM` + `IPPROTO_ICMP`), with no root and no permission beyond `INTERNET`.
 * This has been so since Android 8.0 (API 26, AOSP libcore `Inet6AddressImpl`, b/31926888).
 * With a timeout of at most one second it sends exactly one request.
 *
 * Only when ICMP gets no answer does it fall back to a TCP connection to port 7, which would
 * measure something else. A positive answer that took longer than [timeout] can only come from
 * that fallback, so it is discarded as a lost probe. The measured time also includes opening the
 * socket, a fraction of a millisecond.
 */
internal class DefaultPingDataSource @Inject constructor(private val timeSource: TimeSource) : PingDataSource {
    override fun echo(address: InetAddress, timeout: Duration): Duration? {
        val sentAt = timeSource.markNow()
        val reachable = address.isReachable(timeout.inWholeMilliseconds.toInt())
        val roundTrip = sentAt.elapsedNow()
        if (reachable && roundTrip > timeout) {
            Timber.v("Discarding a TCP fallback answer from %s after %s", address.hostAddress, roundTrip)
            return null
        }
        return roundTrip.takeIf { reachable }
    }
}
