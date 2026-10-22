package com.martindoudera.speedreader.feature.speedtest.domain.model

import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * A transfer rate in bits per second.
 *
 * [megabitsPerSecond] uses the decimal mega (10^6), as every speed test does. Turning it into text
 * is the UI's job.
 */
@JvmInline
value class Throughput(val bitsPerSecond: Double) {
    val megabitsPerSecond: Double
        get() = bitsPerSecond / 1_000_000

    companion object {
        val Zero = Throughput(0.0)

        fun of(transferred: ByteCount, duration: Duration): Throughput = if (duration.isPositive()) {
            Throughput(transferred.bytes * 8 / duration.toDouble(DurationUnit.SECONDS))
        } else {
            Zero
        }
    }
}
