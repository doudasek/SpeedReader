package com.martindoudera.speedreader.feature.speedtest.data.throughput

import com.martindoudera.speedreader.feature.speedtest.domain.model.ServerUrl
import javax.inject.Inject
import kotlin.random.Random
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okio.BufferedSink
import retrofit2.HttpException

/**
 * Sends an endless chunked body until the caller is cancelled. One request lasts the whole phase,
 * well inside the server's limit of about 20 s per upload request.
 *
 * Bytes are counted when the socket accepts them, as curl and most speed tests do. That runs
 * ahead of what the server has received by what sits in the kernel's send buffer, roughly one
 * round trip of data per connection.
 */
internal class DefaultUploadDataSource @Inject constructor(private val api: MeasurementApi) : UploadDataSource {
    override suspend fun upload(server: ServerUrl, onBytes: (Long) -> Unit) {
        api.upload(server.endpoint("upload"), cacheBuster(), EndlessUploadBody(onBytes)).executeCancellably { response ->
            response.body()?.close()
            if (!response.isSuccessful) throw HttpException(response)
        }
    }
}

/** Random bytes, so that nothing on the path can compress them, repeated without end. */
internal class EndlessUploadBody(private val onBytes: (Long) -> Unit) : RequestBody() {
    override fun contentType(): MediaType = OCTET_STREAM

    override fun contentLength(): Long = -1

    override fun isOneShot(): Boolean = true

    override fun writeTo(sink: BufferedSink) {
        while (true) {
            sink.write(PAYLOAD)
            sink.flush()
            onBytes(PAYLOAD.size.toLong())
        }
    }

    private companion object {
        val OCTET_STREAM = "application/octet-stream".toMediaType()
        val PAYLOAD: ByteArray = Random.nextBytes(64 * 1024)
    }
}
