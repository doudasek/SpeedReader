package com.martindoudera.speedreader.feature.speedtest.ui

import com.martindoudera.speedreader.feature.speedtest.domain.model.Throughput
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

internal const val NO_VALUE = "—"

/**
 * Megabits per second, with one decimal below 100 and whole numbers from there on, e.g. "41.5" and
 * "236". The threshold applies after rounding, so 99.96 reads "100", not "100.0".
 */
internal fun Throughput.formatMegabits(locale: Locale): String {
    val tenths = (megabitsPerSecond * 10).roundToLong()
    return if (tenths >= 1_000) megabitsPerSecond.roundToLong().toString() else String.format(locale, "%.1f", tenths / 10.0)
}

/** Whole milliseconds; a round trip under one millisecond reads "<1" rather than "0". */
internal fun Duration.formatMilliseconds(): String = if (this < 1.milliseconds) "<1" else inWholeMilliseconds.toString()

internal fun Duration.wholeSecondsLeft(): Int = ceil(toDouble(DurationUnit.SECONDS)).toInt()
