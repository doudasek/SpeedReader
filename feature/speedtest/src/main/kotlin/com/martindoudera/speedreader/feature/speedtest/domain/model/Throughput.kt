package com.martindoudera.speedreader.feature.speedtest.domain.model

import kotlin.time.Duration
import kotlin.time.DurationUnit

/** A transfer rate in bits per second. Formatting it for people (Mbit/s) is the UI's job. */
@JvmInline
value class Throughput(val bitsPerSecond: Double) {
    companion object {
        val Zero = Throughput(0.0)

        fun of(transferred: ByteCount, duration: Duration): Throughput = if (duration.isPositive()) {
            Throughput(transferred.bytes * 8 / duration.toDouble(DurationUnit.SECONDS))
        } else {
            Zero
        }
    }
}
