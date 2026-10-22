package com.martindoudera.speedreader.feature.speedtest.data.throughput

import com.martindoudera.speedreader.feature.speedtest.domain.model.ServerUrl

/** Streams one upload request to a measurement server. Blocks the calling thread while writing. */
internal interface UploadDataSource {
    /**
     * Writes until the caller is cancelled, reporting every chunk of bytes handed to the socket.
     * Returns only if the server ends the request on its own, and throws when the request fails.
     */
    suspend fun upload(server: ServerUrl, onBytes: (Long) -> Unit)
}
