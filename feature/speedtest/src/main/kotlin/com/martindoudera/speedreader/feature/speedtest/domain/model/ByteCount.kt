package com.martindoudera.speedreader.feature.speedtest.domain.model

/** An amount of data in bytes. */
@JvmInline
value class ByteCount(val bytes: Long) : Comparable<ByteCount> {
    operator fun plus(other: ByteCount): ByteCount = ByteCount(bytes + other.bytes)

    operator fun minus(other: ByteCount): ByteCount = ByteCount(bytes - other.bytes)

    override fun compareTo(other: ByteCount): Int = bytes.compareTo(other.bytes)

    companion object {
        val Zero = ByteCount(0)
    }
}
