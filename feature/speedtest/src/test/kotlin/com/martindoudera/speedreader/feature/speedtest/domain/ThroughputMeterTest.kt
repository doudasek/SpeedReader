package com.martindoudera.speedreader.feature.speedtest.domain

import com.martindoudera.speedreader.feature.speedtest.domain.model.ByteCount
import com.martindoudera.speedreader.feature.speedtest.domain.model.Throughput
import com.martindoudera.speedreader.feature.speedtest.domain.model.TransferSample
import com.martindoudera.speedreader.feature.speedtest.testing.data.SAMPLE_INTERVAL
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import org.junit.Test

class ThroughputMeterTest {
    private val meter = ThroughputMeter()

    @Test
    fun firstSampleIsMeasuredFromTheStart() {
        val sample = TransferSample(200.milliseconds, ByteCount(2_500_000))

        val speed = meter.record(sample)

        assertEquals(100_000_000.0, speed.bitsPerSecond, 1e-6)
    }

    @Test
    fun currentSpeedFollowsTheLastSecondOnly() {
        recordSteadyRate(bytesPerTick = 2_500_000, ticks = 10)

        val speed = recordSteadyRate(bytesPerTick = 250_000, ticks = 5)

        assertEquals(8_000_000.0, speed.bitsPerSecond, 1e-6)
    }

    @Test
    fun averageIsTotalDataOverTotalTimeIncludingTheSlowPart() {
        recordSteadyRate(bytesPerTick = 2_500_000, ticks = 10)
        recordSteadyRate(bytesPerTick = 250_000, ticks = 10)

        val average = meter.average()

        assertEquals(44_000_000.0, average.bitsPerSecond, 1e-6)
    }

    @Test
    fun stalledTransferReadsZero() {
        recordSteadyRate(bytesPerTick = 2_500_000, ticks = 5)

        val speed = recordSteadyRate(bytesPerTick = 0, ticks = 5)

        assertEquals(Throughput.Zero, speed)
    }

    @Test
    fun sampleWithoutElapsedTimeDoesNotDivideByZero() {
        val sample = TransferSample(Duration.ZERO, ByteCount(1_000))

        val speed = meter.record(sample)

        assertEquals(Throughput.Zero, speed)
    }

    @Test
    fun nothingRecordedAveragesToZero() {
        val average = meter.average()

        assertEquals(Throughput.Zero, average)
        assertEquals(ByteCount.Zero, meter.transferred)
    }

    @Test
    fun throughputConvertsBytesToBits() {
        val throughput = Throughput.of(ByteCount(1), 1.seconds)

        assertEquals(8.0, throughput.bitsPerSecond)
    }

    private var ticksRecorded = 0

    private fun recordSteadyRate(bytesPerTick: Long, ticks: Int): Throughput {
        var speed = Throughput.Zero
        repeat(ticks) {
            ticksRecorded++
            val sample = TransferSample(SAMPLE_INTERVAL * ticksRecorded, meter.transferred + ByteCount(bytesPerTick))
            speed = meter.record(sample)
        }
        return speed
    }
}
