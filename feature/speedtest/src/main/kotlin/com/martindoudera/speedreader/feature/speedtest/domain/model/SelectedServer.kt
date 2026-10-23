package com.martindoudera.speedreader.feature.speedtest.domain.model

import kotlin.time.Duration

/**
 * The server the transfer tests run against, with its ICMP round-trip time. The latency is null on
 * a network that lets no ICMP echo through, where the closest server is used instead.
 */
data class SelectedServer(val server: SpeedTestServer, val latency: Duration?)
