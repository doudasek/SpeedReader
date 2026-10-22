package com.martindoudera.speedreader.feature.speedtest.domain

import com.martindoudera.speedreader.feature.speedtest.domain.model.ByteCount
import com.martindoudera.speedreader.feature.speedtest.domain.model.Throughput
import com.martindoudera.speedreader.feature.speedtest.domain.model.TransferSample
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.assertEquals
import org.junit.Test

class ThroughputMeterTest {
    @Test
    fun firstSampleIsMeasuredFromTheStart() {
        val meter = ThroughputMeter()

        val speed = meter.record(TransferSample(200.milliseconds, ByteCount(2_500_000)))

        assertEquals(100_000_000.0, speed.bitsPerSecond, 1e-6)
    }

    @Test
    fun currentSpeedFollowsTheLastSecondOnly() {
        val meter = ThroughputMeter()
        steadyRate(meter, bytesPerTick = 2_500_000, ticks = 10)

        val speed = steadyRate(meter, bytesPerTick = 250_000, ticks = 5, startTick = 10)

        assertEquals(10_000_000.0, speed.bitsPerSecond, 1e-6)
    }

    @Test
    fun averageIsTotalBytesOverTotalTimeIncludingTheSlowPart() {
        val meter = ThroughputMeter()
        steadyRate(meter, bytesPerTick = 2_500_000, ticks = 10)
        steadyRate(meter, bytesPerTick = 250_000, ticks = 10, startTick = 10)

        assertEquals(55_000_000.0, meter.average().bitsPerSecond, 1e-6)
    }

    @Test
    fun stalledTransferReadsZero() {
        val meter = ThroughputMeter()
        steadyRate(meter, bytesPerTick = 2_500_000, ticks = 5)

        val speed = steadyRate(meter, bytesPerTick = 0, ticks = 5, startTick = 5)

        assertEquals(Throughput.Zero, speed)
    }

    @Test
    fun sampleWithoutElapsedTimeDoesNotDivideByZero() {
        val meter = ThroughputMeter()

        val speed = meter.record(TransferSample(Duration.ZERO, ByteCount(1_000)))

        assertEquals(Throughput.Zero, speed)
    }

    @Test
    fun nothingRecordedAveragesToZero() {
        val meter = ThroughputMeter()

        assertEquals(Throughput.Zero, meter.average())
        assertEquals(ByteCount.Zero, meter.transferred)
    }

    @Test
    fun throughputConvertsBytesToBits() {
        assertEquals(8.0, Throughput.of(ByteCount(1), 1.seconds).bitsPerSecond, 0.0)
    }

    private fun steadyRate(meter: ThroughputMeter, bytesPerTick: Long, ticks: Int, startTick: Int = 0): Throughput {
        var speed = Throughput.Zero
        var total = meter.transferred
        for (tick in startTick + 1..startTick + ticks) {
            total += ByteCount(bytesPerTick)
            speed = meter.record(TransferSample(TICK * tick, total))
        }
        return speed
    }

    private companion object {
        val TICK = 200.milliseconds
    }
}
