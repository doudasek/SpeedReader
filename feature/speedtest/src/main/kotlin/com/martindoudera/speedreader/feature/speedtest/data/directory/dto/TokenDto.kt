package com.martindoudera.speedreader.feature.speedtest.data.directory.dto

import kotlinx.serialization.Serializable

/** [ttl] is the token's lifetime in seconds; every response so far said 80. */
@Serializable
internal data class TokenDto(val token: String, val ttl: Long)
