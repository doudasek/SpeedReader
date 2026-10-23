package com.martindoudera.speedreader.feature.speedtest.ui

import com.martindoudera.speedreader.feature.speedtest.domain.model.Throughput
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import org.junit.Test

class SpeedTestFormatTest {
    @Test
    fun speedsBelowOneHundredKeepOneDecimal() {
        val speed = Throughput(41_549_000.0)

        val text = speed.formatMegabits(Locale.US)

        assertEquals("41.5", text)
    }

    @Test
    fun speedsFromOneHundredAreWholeNumbers() {
        val speed = Throughput(236_800_000.0)

        val text = speed.formatMegabits(Locale.US)

        assertEquals("237", text)
    }

    @Test
    fun speedsThatRoundToOneHundredAreWholeNumbers() {
        val speed = Throughput(99_960_000.0)

        val text = speed.formatMegabits(Locale.US)

        assertEquals("100", text)
    }

    @Test
    fun theDecimalSeparatorFollowsTheLocale() {
        val speed = Throughput(41_549_000.0)

        val text = speed.formatMegabits(Locale.forLanguageTag("cs-CZ"))

        assertEquals("41,5", text)
    }

    @Test
    fun remainingTimeRoundsUpToWholeSeconds() {
        val remaining = 10.5.seconds

        val seconds = remaining.wholeSecondsLeft()

        assertEquals(11, seconds)
    }

    @Test
    fun latencyIsShownInWholeMilliseconds() {
        val latency = 9.7.milliseconds

        val text = latency.formatMilliseconds()

        assertEquals("9", text)
    }

    @Test
    fun subMillisecondLatencyIsNotShownAsZero() {
        val latency = 0.4.milliseconds

        val text = latency.formatMilliseconds()

        assertEquals("<1", text)
    }
}
