package com.martindoudera.speedreader.feature.speedtest.domain.model

import kotlin.time.Duration

/** The server the transfer tests run against, with its ICMP round-trip time. */
data class SelectedServer(val server: SpeedTestServer, val latency: Duration)
