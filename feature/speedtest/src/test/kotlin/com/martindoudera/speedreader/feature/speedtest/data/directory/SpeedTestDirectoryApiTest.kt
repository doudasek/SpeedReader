package com.martindoudera.speedreader.feature.speedtest.data.directory

import com.martindoudera.speedreader.feature.speedtest.data.di.DIRECTORY_BASE_URL
import com.martindoudera.speedreader.feature.speedtest.data.di.DirectoryModule
import com.martindoudera.speedreader.feature.speedtest.testing.data.testJson
import com.martindoudera.speedreader.feature.speedtest.testing.directory.FakeTokenDataSource
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.create

class SpeedTestDirectoryApiTest {
    private val server = MockWebServer()
    private lateinit var api: SpeedTestDirectoryApi

    @Before
    fun setUp() {
        server.start()
        val client = OkHttpClient.Builder().addInterceptor(TokenInterceptor(FakeTokenDataSource())).build()
        api = DirectoryModule.retrofit(server.url("/api/"), client, testJson).create()
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun endpointsResolveUnderTheApiPath() {
        val tokens = DIRECTORY_BASE_URL.resolve("v1/tokens")

        assertEquals("https://sp-dir.uwn.com/api/v1/tokens", tokens.toString())
    }

    @Test
    fun serverListIsRequestedWithTheTokenHeaderAndHttpsOnly() = runTest {
        server.enqueue(MockResponse(body = """[{"url":"https://breclav.wifiman.me:81","latitude":48.7,"longitude":16.8}]"""))

        val servers = api.servers()

        val request = server.takeRequest()
        assertEquals(1, servers.size)
        assertEquals("GET", request.method)
        assertEquals("/api/v2/servers", request.url.encodedPath)
        assertEquals("only", request.url.queryParameter("secured"))
        assertEquals("token-1", request.headers[TOKEN_HEADER])
    }

    @Test
    fun captivePortalPageFailsToDecode() = runTest {
        server.enqueue(MockResponse(body = "<html><body>Sign in to Wi-Fi</body></html>"))

        assertFailsWith<SerializationException> { api.servers() }
    }
}
