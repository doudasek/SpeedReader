package com.martindoudera.speedreader.feature.speedtest.testing.directory

import com.martindoudera.speedreader.feature.speedtest.data.directory.SpeedTestDirectoryApi
import com.martindoudera.speedreader.feature.speedtest.data.directory.dto.IpLocationDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray

internal class FakeSpeedTestDirectoryApi : SpeedTestDirectoryApi {
    var serversJson = "[]"
    var serversFailure: Exception? = null
    var clientLocation = IpLocationDto(lat = 49.1953, lon = 16.6016, countryCode = "CZ")
    var clientLocationFailure: Exception? = null

    override suspend fun servers(): JsonArray {
        serversFailure?.let { throw it }
        return Json.parseToJsonElement(serversJson).jsonArray
    }

    override suspend fun clientLocation(): IpLocationDto {
        clientLocationFailure?.let { throw it }
        return clientLocation
    }
}
