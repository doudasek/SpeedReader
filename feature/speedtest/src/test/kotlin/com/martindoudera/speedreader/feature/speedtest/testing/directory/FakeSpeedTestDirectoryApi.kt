package com.martindoudera.speedreader.feature.speedtest.testing.directory

import com.martindoudera.speedreader.feature.speedtest.data.directory.SpeedTestDirectoryApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray

internal class FakeSpeedTestDirectoryApi : SpeedTestDirectoryApi {
    var serversJson = "[]"
    var serversFailure: Exception? = null

    override suspend fun servers(): JsonArray {
        serversFailure?.let { throw it }
        return Json.parseToJsonElement(serversJson).jsonArray
    }
}
