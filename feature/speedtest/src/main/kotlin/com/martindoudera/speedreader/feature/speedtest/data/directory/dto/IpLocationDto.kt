package com.martindoudera.speedreader.feature.speedtest.data.directory.dto

import kotlinx.serialization.Serializable

/**
 * The directory's geolocation of the client's public IP, city-level at best.
 *
 * Only the coordinates are used, plus the country code for logs. The response also carries the
 * IP, ISP and timezone, which are skipped: none of them may be logged.
 */
@Serializable
internal data class IpLocationDto(val lat: Double, val lon: Double, val countryCode: String? = null)
