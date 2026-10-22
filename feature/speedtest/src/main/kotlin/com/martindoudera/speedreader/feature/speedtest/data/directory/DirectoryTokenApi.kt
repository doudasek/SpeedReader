package com.martindoudera.speedreader.feature.speedtest.data.directory

import com.martindoudera.speedreader.feature.speedtest.data.directory.dto.TokenDto
import retrofit2.Call
import retrofit2.http.POST

/**
 * Issues directory tokens: an empty `POST v1/tokens` answers `{"token": "<uuid>", "ttl": 80}`.
 *
 * Verified with curl: `ttl` is in seconds, since a token still worked at 78 s and was refused at
 * 82 s. Only the measurement servers check the token, and only when a request starts.
 * Blocking, because it is called from inside an OkHttp interceptor.
 */
internal interface DirectoryTokenApi {
    @POST("v1/tokens")
    fun createToken(): Call<TokenDto>
}
