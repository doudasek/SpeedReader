package com.martindoudera.speedreader.feature.speedtest.data.throughput

import com.martindoudera.speedreader.feature.speedtest.domain.model.ServerUrl

/** Uploads to a measurement server. Blocks the calling thread while a request is in flight. */
internal interface UploadDataSource {
    /**
     * Sends requests one after another until the caller is cancelled, reporting after each request
     * the bytes the server confirmed it received. Throws when a request fails.
     */
    suspend fun upload(server: ServerUrl, onBytes: (Long) -> Unit)
}
