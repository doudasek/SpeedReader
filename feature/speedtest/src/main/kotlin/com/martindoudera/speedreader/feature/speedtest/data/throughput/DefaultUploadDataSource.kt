package com.martindoudera.speedreader.feature.speedtest.data.throughput

import com.martindoudera.speedreader.feature.speedtest.domain.model.ServerUrl
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.serialization.SerializationException
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okio.BufferedSink
import retrofit2.HttpException
import timber.log.Timber

internal const val UPLOAD_FIRST_REQUEST_SIZE = 10_000L
internal const val UPLOAD_MAX_REQUEST_SIZE = 10_000_000L

/** How long one upload request should take at the speed the previous one reached. */
internal val UPLOAD_REQUEST_DURATION = 500.milliseconds

/**
 * Sends the payload as a series of fixed-size requests and counts only the bytes the server says
 * it received. Bytes the socket accepts are no measure: Android lets each connection queue up to
 * 4 MB in the kernel, which on a slow uplink is seconds of data that has not left the phone.
 *
 * Each request is sized to last [UPLOAD_REQUEST_DURATION] at the previous one's speed, as the
 * wifiman web client does, so a slow link confirms its first bytes quickly and a fast one is not
 * slowed down by request overhead. The request still in flight when the phase ends is not counted.
 */
internal class DefaultUploadDataSource @Inject constructor(private val api: MeasurementApi, private val timeSource: TimeSource) :
    UploadDataSource {
    override suspend fun upload(server: ServerUrl, onBytes: (Long) -> Unit) {
        var size = UPLOAD_FIRST_REQUEST_SIZE
        while (true) {
            val startedAt = timeSource.markNow()
            val received = send(server, size)
            val elapsed = startedAt.elapsedNow()
            Timber.v("Upload of %d B: server received %d B in %s", size, received, elapsed)
            onBytes(received)
            size = nextUploadSize(received, elapsed)
        }
    }

    private suspend fun send(server: ServerUrl, size: Long): Long =
        api.upload(server.endpoint("upload"), cacheBuster(), UploadBody(size)).executeCancellably { response ->
            if (!response.isSuccessful) {
                response.body()?.close()
                throw HttpException(response)
            }
            val answer = response.body()?.use { it.string() }
            answer?.trim()?.toLongOrNull() ?: throw SerializationException("Unexpected upload answer: $answer")
        }
}

internal fun nextUploadSize(received: Long, elapsed: Duration): Long =
    (received * (UPLOAD_REQUEST_DURATION / elapsed)).toLong().coerceIn(UPLOAD_FIRST_REQUEST_SIZE, UPLOAD_MAX_REQUEST_SIZE)

/** [size] random bytes, so that nothing on the path can compress them. */
internal class UploadBody(private val size: Long) : RequestBody() {
    override fun contentType(): MediaType = OCTET_STREAM

    override fun contentLength(): Long = size

    override fun writeTo(sink: BufferedSink) {
        var remaining = size
        while (remaining > 0) {
            val chunk = minOf(remaining, PAYLOAD.size.toLong()).toInt()
            sink.write(PAYLOAD, 0, chunk)
            remaining -= chunk
        }
    }

    private companion object {
        val OCTET_STREAM = "application/octet-stream".toMediaType()
        val PAYLOAD: ByteArray = Random.nextBytes(64 * 1024)
    }
}
