package com.martindoudera.speedreader.feature.speedtest.testing.latency

import java.net.InetAddress
import java.net.UnknownHostException
import okhttp3.Dns

/** Resolves only the hosts in [addresses], to their addresses in the given order. */
internal class FakeDns : Dns {
    var addresses: Map<String, List<InetAddress>> = emptyMap()

    override fun lookup(hostname: String): List<InetAddress> = addresses[hostname] ?: throw UnknownHostException(hostname)
}
