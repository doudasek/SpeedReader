package com.martindoudera.speedreader.feature.speedtest.domain.model

/** How many parallel connections the download and upload tests open against the selected server. */
enum class ConnectionMode(val connections: Int) {
    Single(1),
    Parallel(4),
}
