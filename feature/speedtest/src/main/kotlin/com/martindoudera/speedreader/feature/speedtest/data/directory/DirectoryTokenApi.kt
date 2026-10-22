package com.martindoudera.speedreader.feature.speedtest.data.directory

import com.martindoudera.speedreader.feature.speedtest.data.directory.dto.TokenDto
import retrofit2.Call
import retrofit2.http.POST

/** Issues directory tokens. Blocking, because it is called from inside an OkHttp interceptor. */
internal interface DirectoryTokenApi {
    @POST("v1/tokens")
    fun createToken(): Call<TokenDto>
}
