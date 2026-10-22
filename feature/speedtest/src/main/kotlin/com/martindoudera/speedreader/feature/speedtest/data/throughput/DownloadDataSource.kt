package com.martindoudera.speedreader.feature.speedtest.data.throughput

import com.martindoudera.speedreader.feature.speedtest.domain.model.ServerUrl

/** Streams one download request from a measurement server. Blocks the calling thread while reading. */
internal interface DownloadDataSource {
    /**
     * Reports every chunk of bytes as it arrives and returns when the server ends the response.
     * Throws when the request fails, and stops promptly when the caller is cancelled.
     */
    suspend fun download(server: ServerUrl, onBytes: (Long) -> Unit)
}
