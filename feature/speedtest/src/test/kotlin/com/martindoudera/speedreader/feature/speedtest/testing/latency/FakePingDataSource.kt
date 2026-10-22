package com.martindoudera.speedreader.feature.speedtest.testing.latency

import com.martindoudera.speedreader.feature.speedtest.data.latency.PingDataSource
import java.io.IOException
import java.net.InetAddress
import kotlin.time.Duration

/** Answers probe n with `roundTrips[n]`; a null entry or a missing one is a lost probe. */
internal class FakePingDataSource : PingDataSource {
    var roundTrips: List<Duration?> = emptyList()
    var failure: IOException? = null

    val pinged = mutableListOf<InetAddress>()
    val timeouts = mutableListOf<Duration>()

    override fun echo(address: InetAddress, timeout: Duration): Duration? {
        failure?.let { throw it }
        pinged += address
        timeouts += timeout
        return roundTrips.getOrNull(pinged.size - 1)
    }
}
