package com.martindoudera.speedreader.feature.speedtest.data.throughput

import com.martindoudera.speedreader.feature.speedtest.data.directory.RequiresToken
import com.martindoudera.speedreader.feature.speedtest.data.directory.TokenPlacement
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * The measurement servers' payload endpoints, on the host the directory listed.
 *
 * Verified with curl in October 2026, against what the wifiman.com web client sends:
 * - `GET /download?size=<bytes>` streams exactly `size` bytes (at least 200 000, up to at least
 *   10 GB) as `application/octet-stream`. The server ends a request after roughly 27 to 40 s with
 *   status 200 and a body shorter than its `Content-Length`.
 * - `POST /upload` accepts any body, chunked too, and answers with the number of bytes it received
 *   (`text/plain` when asked for it). It ends a request after about 20 s with `503 Timeouted`.
 * - `nc` is a cache buster. The token goes into the query, as the web client sends it.
 * - The servers speak HTTP/1.1 only.
 *
 * Blocking calls, because the payload is read and written on the calling thread.
 */
internal interface MeasurementApi {
    @RequiresToken(TokenPlacement.Query)
    @Streaming
    @GET
    fun download(@Url url: String, @Query("size") size: Long, @Query("nc") cacheBuster: String): Call<ResponseBody>

    @RequiresToken(TokenPlacement.Query)
    @Headers("Accept: text/plain")
    @POST
    fun upload(@Url url: String, @Query("nc") cacheBuster: String, @Body body: RequestBody): Call<ResponseBody>
}
