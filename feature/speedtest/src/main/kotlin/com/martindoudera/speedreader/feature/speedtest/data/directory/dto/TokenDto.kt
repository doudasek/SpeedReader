package com.martindoudera.speedreader.feature.speedtest.data.directory.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class TokenDto(val token: String, val ttl: Long)
