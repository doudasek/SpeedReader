package com.martindoudera.speedreader.feature.speedtest.testing.latency

import java.net.InetAddress
import java.net.UnknownHostException
import okhttp3.Dns

/** Resolves only the hosts in [addresses]. */
internal class FakeDns : Dns {
    var addresses: Map<String, InetAddress> = emptyMap()

    override fun lookup(hostname: String): List<InetAddress> = listOf(addresses[hostname] ?: throw UnknownHostException(hostname))
}
