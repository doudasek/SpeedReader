package com.martindoudera.speedreader.feature.speedtest.data.throughput

import com.martindoudera.speedreader.feature.speedtest.data.di.DirectoryModule
import com.martindoudera.speedreader.feature.speedtest.data.directory.TokenInterceptor
import com.martindoudera.speedreader.feature.speedtest.domain.model.ServerUrl
import com.martindoudera.speedreader.feature.speedtest.testing.data.testJson
import com.martindoudera.speedreader.feature.speedtest.testing.directory.FakeTokenDataSource
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import retrofit2.create

/** A [MeasurementApi] wired like the app's, talking to [server]. */
internal fun measurementApi(server: MockWebServer): MeasurementApi {
    val client = OkHttpClient.Builder().addInterceptor(TokenInterceptor(FakeTokenDataSource())).build()
    return DirectoryModule.retrofit(server.url("/"), client, testJson).create()
}

internal fun MockWebServer.serverUrl() = ServerUrl(url("/").toString().trimEnd('/'))
