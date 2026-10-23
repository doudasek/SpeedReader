package com.martindoudera.speedreader.feature.speedtest.data.throughput

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class DefaultDownloadDataSourceTest {
    private val server = MockWebServer()
    private lateinit var dataSource: DefaultDownloadDataSource

    @Before
    fun setUp() {
        server.start()
        dataSource = DefaultDownloadDataSource(measurementApi(server))
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun everyByteOfTheResponseIsReported() = runTest {
        server.enqueue(MockResponse.Builder().body(Buffer().write(ByteArray(300_000))).build())
        var received = 0L

        dataSource.download(server.serverUrl()) { received += it }

        assertEquals(300_000, received)
    }

    @Test
    fun requestAsksForALargePayloadWithCacheBusterAndToken() = runTest {
        server.enqueue(MockResponse.Builder().body(Buffer().write(ByteArray(10))).build())

        dataSource.download(server.serverUrl()) {}

        val url = server.takeRequest().url
        assertEquals("/download", url.encodedPath)
        assertEquals(DOWNLOAD_REQUEST_SIZE.toString(), url.queryParameter("size"))
        assertEquals(7, url.queryParameter("nc")?.length)
        assertEquals("token-1", url.queryParameter("token"))
    }

    @Test
    fun serverErrorThrowsWithItsStatus() = runTest {
        server.enqueue(MockResponse(code = 503))

        val failure = assertFailsWith<HttpException> { dataSource.download(server.serverUrl()) {} }

        assertEquals(503, failure.code())
    }

    @Test
    fun cancellationAbortsAReadBlockedOnTheSocket() = runTest {
        server.enqueue(
            MockResponse.Builder().body(Buffer().write(ByteArray(10_000_000))).throttleBody(1_024, 1, TimeUnit.SECONDS).build(),
        )
        val received = AtomicLong()

        withContext(Dispatchers.IO) {
            val download = launch { dataSource.download(server.serverUrl()) { received.addAndGet(it) } }
            while (received.get() == 0L) yield()

            withTimeout(2.seconds) { download.cancelAndJoin() }
        }

        assertTrue(received.get() < 10_000_000)
    }

    @Test
    fun cancelledCallerSendsNoRequest() = runTest {
        server.enqueue(MockResponse.Builder().body(Buffer().write(ByteArray(2_048))).throttleBody(1_024, 1, TimeUnit.SECONDS).build())
        val received = AtomicLong()

        withContext(Dispatchers.IO) {
            launch {
                cancel()
                dataSource.download(server.serverUrl()) { received.addAndGet(it) }
            }.join()
        }

        assertEquals(0, server.requestCount)
        assertEquals(0, received.get())
    }
}
