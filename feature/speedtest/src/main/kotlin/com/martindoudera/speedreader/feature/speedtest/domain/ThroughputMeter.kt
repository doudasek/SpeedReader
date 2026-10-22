package com.martindoudera.speedreader.feature.speedtest.domain

import com.martindoudera.speedreader.feature.speedtest.domain.model.ByteCount
import com.martindoudera.speedreader.feature.speedtest.domain.model.Throughput
import com.martindoudera.speedreader.feature.speedtest.domain.model.TransferSample
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Turns cumulative transfer samples of one test phase into speeds.
 *
 * The current speed covers roughly the last [window], so the live readout follows real changes
 * without jumping on every sample. The average is total bytes over total elapsed time, untrimmed.
 */
internal class ThroughputMeter(private val window: Duration = 1.seconds) {
    private val samples = ArrayDeque(listOf(TransferSample(Duration.ZERO, ByteCount.Zero)))

    val transferred: ByteCount
        get() = samples.last().transferred

    fun record(sample: TransferSample): Throughput {
        samples.addLast(sample)
        while (samples.size > 2 && sample.elapsed - samples[1].elapsed >= window) {
            samples.removeFirst()
        }
        val oldest = samples.first()
        return Throughput.of(sample.transferred - oldest.transferred, sample.elapsed - oldest.elapsed)
    }

    fun average(): Throughput = samples.last().let { Throughput.of(it.transferred, it.elapsed) }
}
