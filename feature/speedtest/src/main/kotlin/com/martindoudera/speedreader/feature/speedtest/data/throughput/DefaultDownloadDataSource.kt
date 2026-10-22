package com.martindoudera.speedreader.feature.speedtest.data.throughput

import com.martindoudera.speedreader.feature.speedtest.domain.model.ServerUrl
import java.io.IOException
import javax.inject.Inject
import okio.Buffer
import retrofit2.HttpException

/**
 * Size asked for per download request. Far more than one connection moves in a 7.5 s phase, so a
 * single request normally lasts the whole phase; only the bytes actually read are transferred.
 */
internal const val DOWNLOAD_REQUEST_SIZE = 1_000_000_000L

private const val READ_SIZE = 64L * 1024

/** Reads the payload as it arrives and discards it; nothing is buffered beyond one read. */
internal class DefaultDownloadDataSource @Inject constructor(private val api: MeasurementApi) : DownloadDataSource {
    override suspend fun download(server: ServerUrl, onBytes: (Long) -> Unit) {
        api.download(server.endpoint("download"), DOWNLOAD_REQUEST_SIZE, cacheBuster()).executeCancellably { response ->
            if (!response.isSuccessful) throw HttpException(response)
            val body = response.body() ?: throw IOException("Empty download response")
            body.use {
                val source = it.source()
                val sink = Buffer()
                while (true) {
                    val read = source.read(sink, READ_SIZE)
                    if (read == -1L) break
                    sink.clear()
                    onBytes(read)
                }
            }
        }
    }
}

internal fun ServerUrl.endpoint(path: String): String = "${value.trimEnd('/')}/$path"
