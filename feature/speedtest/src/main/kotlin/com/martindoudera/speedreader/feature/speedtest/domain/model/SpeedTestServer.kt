package com.martindoudera.speedreader.feature.speedtest.domain.model

/**
 * A measurement server from the Ubiquiti directory.
 *
 * [location] is where the server is hosted. The [city] and [countryCode] labels come from the
 * directory as well and sometimes contradict it, so ranking uses [location] only.
 */
data class SpeedTestServer(val url: ServerUrl, val location: GeoPoint, val provider: String?, val city: String?, val countryCode: String?)
