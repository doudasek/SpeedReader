package com.martindoudera.speedreader.feature.speedtest.ui

import com.martindoudera.speedreader.feature.speedtest.domain.model.Throughput
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit

internal const val NO_VALUE = "—"

/** Megabits per second, with one decimal below 100 and whole numbers from there on, e.g. "41.5" and "236". */
internal fun Throughput.formatMegabits(locale: Locale = Locale.getDefault()): String {
    val megabits = megabitsPerSecond
    return if (megabits >= 100) megabits.roundToInt().toString() else String.format(locale, "%.1f", megabits)
}

internal fun Duration.formatMilliseconds(): String = inWholeMilliseconds.toString()

internal fun Duration.wholeSecondsLeft(): Int = ceil(toDouble(DurationUnit.SECONDS)).toInt()
