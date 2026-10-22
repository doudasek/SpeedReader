package com.martindoudera.speedreader.feature.speedtest.domain.model

import kotlin.time.Duration

/** Data transferred so far, across all connections, at [elapsed] since the transfer started. */
data class TransferSample(val elapsed: Duration, val transferred: ByteCount)
