package com.martindoudera.speedreader.feature.speedtest.data.directory

import com.martindoudera.speedreader.feature.speedtest.data.di.DirectoryModule
import com.martindoudera.speedreader.feature.speedtest.testing.data.httpError
import com.martindoudera.speedreader.feature.speedtest.testing.data.testJson
import com.martindoudera.speedreader.feature.speedtest.testing.directory.FakeTokenDataSource
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.create
import retrofit2.http.GET

class TokenInterceptorTest {
    private val server = MockWebServer()
    private val tokenDataSource = FakeTokenDataSource()
    private lateinit var api: SignedApi

    @Before
    fun setUp() {
        server.start()
        val client = OkHttpClient.Builder().addInterceptor(TokenInterceptor(tokenDataSource)).build()
        api = DirectoryModule.retrofit(server.url("/"), client, testJson).create()
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun headerPlacementSendsTheTokenAsHeader() = runTest {
        server.enqueue(MockResponse())

        api.header()

        val request = server.takeRequest()
        assertEquals("token-1", request.headers[TOKEN_HEADER])
        assertNull(request.url.queryParameter(TOKEN_QUERY_PARAMETER))
    }

    @Test
    fun queryPlacementAddsTheTokenNextToExistingParameters() = runTest {
        server.enqueue(MockResponse())

        api.query()

        val request = server.takeRequest()
        assertEquals("token-1", request.url.queryParameter(TOKEN_QUERY_PARAMETER))
        assertEquals("1000", request.url.queryParameter("size"))
        assertNull(request.headers[TOKEN_HEADER])
    }

    @Test
    fun unannotatedRequestIsLeftAlone() = runTest {
        server.enqueue(MockResponse())

        api.open()

        val request = server.takeRequest()
        assertNull(request.headers[TOKEN_HEADER])
        assertEquals(0, tokenDataSource.issuedTokens)
    }

    @Test
    fun rejectedTokenIsRenewedAndTheRequestRetriedOnce() = runTest {
        server.enqueue(MockResponse(code = 401))
        server.enqueue(MockResponse())

        api.header()

        assertEquals("token-1", server.takeRequest().headers[TOKEN_HEADER])
        assertEquals("token-2", server.takeRequest().headers[TOKEN_HEADER])
        assertEquals(listOf(DirectoryToken("token-1")), tokenDataSource.invalidated)
    }

    @Test
    fun secondRejectionReachesTheCaller() = runTest {
        server.enqueue(MockResponse(code = 401))
        server.enqueue(MockResponse(code = 401))

        val failure = assertFailsWith<HttpException> { api.header() }

        assertEquals(401, failure.code())
        assertEquals(2, server.requestCount)
    }

    @Test
    fun otherHttpFailuresAreNotRetried() = runTest {
        server.enqueue(MockResponse(code = 503))

        assertFailsWith<HttpException> { api.header() }

        assertEquals(1, server.requestCount)
        assertEquals(emptyList(), tokenDataSource.invalidated)
    }

    @Test
    fun refusedTokenIsReportedWithTheDirectoryStatus() = runTest {
        tokenDataSource.failure = httpError(503)

        val failure = assertFailsWith<HttpException> { api.header() }

        assertEquals(503, failure.code())
        assertEquals(0, server.requestCount)
    }

    @Test
    fun refusedRenewalAfterRejectionIsReportedWithTheDirectoryStatus() = runTest {
        tokenDataSource.renewalFailure = httpError(503)
        server.enqueue(MockResponse(code = 401))

        val failure = assertFailsWith<HttpException> { api.header() }

        assertEquals(503, failure.code())
        assertEquals(1, server.requestCount)
    }

    @Test
    fun malformedTokenIsAnIoFailure() = runTest {
        tokenDataSource.failure = SerializationException("unexpected token")

        assertFailsWith<IOException> { api.header() }

        assertEquals(0, server.requestCount)
    }

    @Test
    fun unreachableDirectoryIsAnIoFailure() = runTest {
        tokenDataSource.failure = IOException("sp-dir.uwn.com unreachable")

        assertFailsWith<IOException> { api.header() }

        assertEquals(0, server.requestCount)
    }

    private interface SignedApi {
        @RequiresToken(TokenPlacement.Header)
        @GET("header")
        suspend fun header()

        @RequiresToken(TokenPlacement.Query)
        @GET("download?size=1000")
        suspend fun query()

        @GET("open")
        suspend fun open()
    }
}
