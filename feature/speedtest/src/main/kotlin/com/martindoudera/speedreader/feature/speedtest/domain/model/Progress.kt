package com.martindoudera.speedreader.feature.speedtest.domain.model

/** How much of something is done, from 0 (nothing) to 1 (all of it). */
@JvmInline
value class Progress(val fraction: Float) {
    init {
        require(fraction in 0f..1f) { "Progress out of range: $fraction" }
    }
}
