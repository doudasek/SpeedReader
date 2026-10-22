package com.martindoudera.speedreader.feature.speedtest.domain.model

/** A point on Earth (WGS 84). */
data class GeoPoint(val latitude: Latitude, val longitude: Longitude)

/** Latitude in decimal degrees, from -90 (south pole) to 90 (north pole). */
@JvmInline
value class Latitude(val degrees: Double) {
    init {
        require(degrees in -90.0..90.0) { "Latitude out of range: $degrees" }
    }
}

/** Longitude in decimal degrees, from -180 to 180. */
@JvmInline
value class Longitude(val degrees: Double) {
    init {
        require(degrees in -180.0..180.0) { "Longitude out of range: $degrees" }
    }
}
