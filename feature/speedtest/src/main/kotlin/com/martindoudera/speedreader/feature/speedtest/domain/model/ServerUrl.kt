package com.martindoudera.speedreader.feature.speedtest.domain.model

/** Base URL of a measurement server, as the directory reports it. Also the server's identity. */
@JvmInline
value class ServerUrl(val value: String) : Comparable<ServerUrl> {
    override fun compareTo(other: ServerUrl): Int = value.compareTo(other.value)
}
