package com.martindoudera.speedreader.feature.speedtest.data.server

import com.martindoudera.speedreader.core.common.error.NetworkOutcome
import com.martindoudera.speedreader.core.common.error.map
import com.martindoudera.speedreader.core.common.error.onFailure
import com.martindoudera.speedreader.core.network.safeApiCall
import com.martindoudera.speedreader.feature.speedtest.data.directory.SpeedTestDirectoryApi
import com.martindoudera.speedreader.feature.speedtest.data.directory.dto.ServerDto
import com.martindoudera.speedreader.feature.speedtest.domain.model.GeoPoint
import com.martindoudera.speedreader.feature.speedtest.domain.model.Latitude
import com.martindoudera.speedreader.feature.speedtest.domain.model.Longitude
import com.martindoudera.speedreader.feature.speedtest.domain.model.ServerUrl
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestServer
import com.martindoudera.speedreader.feature.speedtest.domain.repository.ServerRepository
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import timber.log.Timber

/**
 * Loads the HTTPS servers the directory offers for this client.
 *
 * Entries are decoded one by one, so a malformed entry or one with impossible coordinates is
 * dropped instead of failing the whole list.
 */
internal class DefaultServerRepository @Inject constructor(private val api: SpeedTestDirectoryApi, private val json: Json) :
    ServerRepository {
    override suspend fun servers(): NetworkOutcome<List<SpeedTestServer>> {
        Timber.d("Loading the server list")
        return safeApiCall { api.servers() }
            .map { entries -> entries.decodeServers() }
            .onFailure { error -> Timber.e("Loading the server list failed: %s", error) }
    }

    private fun JsonArray.decodeServers(): List<SpeedTestServer> {
        val servers = mapNotNull { entry -> entry.toServerOrNull() }
        Timber.i("Directory offered %d servers, %d unusable", servers.size, size - servers.size)
        return servers
    }

    private fun JsonElement.toServerOrNull(): SpeedTestServer? = try {
        json.decodeFromJsonElement(ServerDto.serializer(), this).toSpeedTestServer().also { server ->
            Timber.v("Server %s by %s in %s", server.url.value, server.provider, server.countryCode)
        }
    } catch (e: IllegalArgumentException) {
        Timber.w(e, "Dropped an unusable server entry")
        null
    }

    private fun ServerDto.toSpeedTestServer() = SpeedTestServer(
        url = ServerUrl(url),
        location = GeoPoint(Latitude(latitude), Longitude(longitude)),
        provider = provider,
        city = city,
        countryCode = countryCode,
    )
}
