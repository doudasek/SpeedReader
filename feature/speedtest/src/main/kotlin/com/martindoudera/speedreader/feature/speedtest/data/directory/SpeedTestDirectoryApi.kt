package com.martindoudera.speedreader.feature.speedtest.data.directory

import com.martindoudera.speedreader.feature.speedtest.data.directory.dto.IpLocationDto
import kotlinx.serialization.json.JsonArray
import retrofit2.http.GET

/**
 * The Ubiquiti speed test directory (`sp-dir.uwn.com/api`).
 *
 * The API is undocumented. Everything below comes from the wifiman.com web client and was checked
 * with curl in October 2026:
 * - The server list is already filtered by the client's IP, so it holds about ten nearby servers,
 *   not a world list.
 * - `secured=only` limits it to HTTPS servers. Their ports vary (80, 81, 88, 800, …), never 443.
 * - The directory no longer enforces the token on the server list: it answers 200 without the
 *   header. The web client still sends it, and so do we. An unknown query parameter is rejected
 *   with 400.
 * - `v1/ip` needs no token. Its coordinates are an IP geolocation, so city-level at best.
 *
 * The server list comes back as a raw [JsonArray] so that one malformed entry can be dropped
 * without failing the whole response.
 */
internal interface SpeedTestDirectoryApi {
    @RequiresToken(TokenPlacement.Header)
    @GET("v2/servers?secured=only")
    suspend fun servers(): JsonArray

    @GET("v1/ip")
    suspend fun clientLocation(): IpLocationDto
}
