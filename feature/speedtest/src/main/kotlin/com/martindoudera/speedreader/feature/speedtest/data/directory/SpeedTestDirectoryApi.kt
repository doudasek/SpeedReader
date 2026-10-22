package com.martindoudera.speedreader.feature.speedtest.data.directory

import kotlinx.serialization.json.JsonArray
import retrofit2.http.GET

/**
 * The Ubiquiti speed test directory (`sp-dir.uwn.com/api`).
 *
 * The server list comes back as a raw [JsonArray] so that one malformed entry can be dropped
 * without failing the whole response.
 */
internal interface SpeedTestDirectoryApi {
    @RequiresToken(TokenPlacement.Header)
    @GET("v2/servers?secured=only")
    suspend fun servers(): JsonArray
}
