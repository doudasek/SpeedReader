package com.martindoudera.speedreader.feature.speedtest.data.latency

import java.net.InetAddress
import kotlin.time.Duration

/** Sends one ICMP echo request and waits for its reply. Blocking. */
internal interface PingDataSource {
    /**
     * Returns the round-trip time, or null when no reply arrived within [timeout]. Throws
     * `IOException` when the address cannot be pinged at all.
     */
    fun echo(address: InetAddress, timeout: Duration): Duration?
}
