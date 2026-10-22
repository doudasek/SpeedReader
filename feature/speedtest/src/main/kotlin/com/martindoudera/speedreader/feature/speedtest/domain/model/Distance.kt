package com.martindoudera.speedreader.feature.speedtest.domain.model

/** A distance in metres. */
@JvmInline
value class Distance(val meters: Double) : Comparable<Distance> {
    override fun compareTo(other: Distance): Int = meters.compareTo(other.meters)
}
