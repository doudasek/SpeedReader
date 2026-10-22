package com.martindoudera.speedreader.feature.speedtest.data.throughput

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import kotlinx.serialization.SerializationException
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class DefaultUploadDataSourceTest {
    private val server = MockWebServer()
    private lateinit var dataSource: DefaultUploadDataSource

    @Before
    fun setUp() {
        server.start()
        dataSource = DefaultUploadDataSource(measurementApi(server), TestTimeSource())
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun uploadReportsWhatTheServerConfirmsNotWhatWasSent() = runTest {
        server.enqueue(MockResponse(body = "4000"))
        server.enqueue(MockResponse(body = "7000\n"))
        val reported = CopyOnWriteArrayList<Long>()

        withContext(Dispatchers.IO) {
            val upload = launch { dataSource.upload(server.serverUrl()) { reported.add(it) } }
            while (reported.size < 2) yield()

            withTimeout(2.seconds) { upload.cancelAndJoin() }
        }

        assertEquals(listOf(4000L, 7000L), reported)
    }

    @Test
    fun firstRequestIsASmallSizedPostWithToken() = runTest {
        server.enqueue(MockResponse(body = "0"))

        withContext(Dispatchers.IO) {
            val upload = launch { dataSource.upload(server.serverUrl()) {} }
            val request = checkNotNull(server.takeRequest(2, TimeUnit.SECONDS))
            withTimeout(2.seconds) { upload.cancelAndJoin() }

            assertEquals("POST", request.method)
            assertEquals("/upload", request.url.encodedPath)
            assertEquals("token-1", request.url.queryParameter("token"))
            assertEquals("text/plain", request.headers["Accept"])
            assertEquals(UPLOAD_FIRST_REQUEST_SIZE.toString(), request.headers["Content-Length"])
            assertEquals(UPLOAD_FIRST_REQUEST_SIZE, request.bodySize)
        }
    }

    @Test
    fun failedRequestEndsTheUploadWithItsStatus() = runTest {
        server.enqueue(MockResponse(code = 503, body = "Timeouted"))

        val error = withContext(Dispatchers.IO) {
            assertFailsWith<HttpException> { dataSource.upload(server.serverUrl()) {} }
        }

        assertEquals(503, error.code())
    }

    @Test
    fun unreadableAnswerEndsTheUpload() = runTest {
        server.enqueue(MockResponse(body = "Server is running"))

        withContext(Dispatchers.IO) {
            assertFailsWith<SerializationException> { dataSource.upload(server.serverUrl()) {} }
        }
    }

    @Test
    fun nextSizeLastsTheTargetDurationAtThePreviousSpeed() {
        val size = nextUploadSize(received = 1_000_000, elapsed = UPLOAD_REQUEST_DURATION * 4)

        assertEquals(250_000, size)
    }

    @Test
    fun nextSizeStaysWithinBounds() {
        assertEquals(UPLOAD_FIRST_REQUEST_SIZE, nextUploadSize(received = 0, elapsed = 1.seconds))
        assertEquals(UPLOAD_MAX_REQUEST_SIZE, nextUploadSize(received = 50_000_000, elapsed = 100.milliseconds))
        assertEquals(UPLOAD_MAX_REQUEST_SIZE, nextUploadSize(received = 10_000, elapsed = Duration.ZERO))
    }

    @Test
    fun bodyWritesExactlyItsDeclaredSize() {
        val body = UploadBody(200_000)
        val sink = Buffer()

        body.writeTo(sink)

        assertEquals(200_000, body.contentLength())
        assertEquals(200_000, sink.size)
    }
}
