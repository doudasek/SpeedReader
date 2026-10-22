package com.martindoudera.speedreader.feature.speedtest.data.directory.dto

import kotlinx.serialization.Serializable

/**
 * One entry of the directory's server list.
 *
 * `url`, `latitude` and `longitude` were present in every entry seen. `provider`, `city` and
 * `countryCode` can be missing (one server had no city), and the city labels sometimes
 * contradict the coordinates. Fields we do not use (`providerUrl`, `country`, `speedMbps`,
 * which is the server's link capacity rather than a measurement) are skipped.
 */
@Serializable
internal data class ServerDto(
    val url: String,
    val latitude: Double,
    val longitude: Double,
    val provider: String? = null,
    val city: String? = null,
    val countryCode: String? = null,
)
