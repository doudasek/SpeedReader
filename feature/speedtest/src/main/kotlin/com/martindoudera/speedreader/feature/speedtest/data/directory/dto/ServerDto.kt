package com.martindoudera.speedreader.feature.speedtest.data.directory.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class ServerDto(
    val url: String,
    val latitude: Double,
    val longitude: Double,
    val provider: String? = null,
    val city: String? = null,
    val countryCode: String? = null,
)
