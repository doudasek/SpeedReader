package com.martindoudera.speedreader.feature.speedtest.data.directory

import com.martindoudera.speedreader.feature.speedtest.data.di.DirectoryModule
import com.martindoudera.speedreader.feature.speedtest.testing.data.testJson
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource
import kotlinx.serialization.SerializationException
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.create

class DefaultTokenDataSourceTest {
    private val server = MockWebServer()
    private val timeSource = TestTimeSource()
    private lateinit var dataSource: DefaultTokenDataSource

    @Before
    fun setUp() {
        server.start()
        val api = DirectoryModule.retrofit(server.url("/api/"), OkHttpClient(), testJson).create<DirectoryTokenApi>()
        dataSource = DefaultTokenDataSource(api, timeSource)
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun tokenIsRequestedWithAnEmptyPost() {
        enqueueTokens("6fdd5148")

        val token = dataSource.token()

        val request = server.takeRequest()
        assertEquals(DirectoryToken("6fdd5148"), token)
        assertEquals("POST", request.method)
        assertEquals("/api/v1/tokens", request.url.encodedPath)
        assertEquals(0, request.bodySize)
    }

    @Test
    fun tokenIsReusedWhileItIsValid() {
        enqueueTokens("first", "second")
        dataSource.token()
        timeSource += 60.seconds

        val token = dataSource.token()

        assertEquals(DirectoryToken("first"), token)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun tokenIsRenewedBeforeTheDirectoryExpiresIt() {
        enqueueTokens("first", "second")
        dataSource.token()
        timeSource += 80.seconds - TOKEN_RENEWAL_MARGIN

        val token = dataSource.token()

        assertEquals(DirectoryToken("second"), token)
    }

    @Test
    fun clearedTokenIsReplacedByAFreshOne() {
        enqueueTokens("first", "second")
        dataSource.token()

        dataSource.clear()

        assertEquals(DirectoryToken("second"), dataSource.token())
    }

    @Test
    fun invalidatedTokenIsReplaced() {
        enqueueTokens("first", "second")
        val rejected = dataSource.token()

        dataSource.invalidate(rejected)

        assertEquals(DirectoryToken("second"), dataSource.token())
    }

    @Test
    fun invalidatingAnOlderTokenKeepsTheCurrentOne() {
        enqueueTokens("first", "second", "third")
        val stale = dataSource.token()
        dataSource.clear()
        dataSource.token()

        dataSource.invalidate(stale)

        assertEquals(DirectoryToken("second"), dataSource.token())
    }

    @Test
    fun refusalCarriesTheDirectoryStatus() {
        server.enqueue(MockResponse(code = 503))

        val failure = assertFailsWith<HttpException> { dataSource.token() }

        assertEquals(503, failure.code())
    }

    @Test
    fun malformedTokenIsAnIoFailure() {
        server.enqueue(MockResponse(body = "<html>Sign in to Wi-Fi</html>"))

        assertFailsWith<IOException> { dataSource.token() }
    }

    @Test
    fun concurrentCallersShareOneRequest() {
        server.enqueue(MockResponse.Builder().body(tokenJson("shared")).headersDelay(200, TimeUnit.MILLISECONDS).build())
        enqueueTokens("second", "third", "fourth", "fifth", "sixth")
        val executor = Executors.newFixedThreadPool(6)

        val tokens = executor.invokeAll(List(6) { Callable { dataSource.token() } }).map { it.get() }

        executor.shutdown()
        assertEquals(setOf(DirectoryToken("shared")), tokens.toSet())
        assertEquals(1, server.requestCount)
    }

    private fun enqueueTokens(vararg tokens: String) = tokens.forEach { server.enqueue(MockResponse(body = tokenJson(it))) }

    private fun tokenJson(token: String) = """{"token":"$token","ttl":80}"""
}
