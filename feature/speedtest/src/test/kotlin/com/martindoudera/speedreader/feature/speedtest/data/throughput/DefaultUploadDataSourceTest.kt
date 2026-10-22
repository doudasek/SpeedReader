package com.martindoudera.speedreader.feature.speedtest.data.throughput

import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okio.Buffer
import okio.ForwardingSink
import okio.buffer
import org.junit.After
import org.junit.Before
import org.junit.Test

class DefaultUploadDataSourceTest {
    private val server = MockWebServer()
    private lateinit var dataSource: DefaultUploadDataSource

    @Before
    fun setUp() {
        server.start()
        dataSource = DefaultUploadDataSource(measurementApi(server))
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun uploadStreamsUntilCancelledAsAChunkedPostWithToken() = runTest {
        server.enqueue(MockResponse(body = "0"))
        val sent = AtomicLong()

        withContext(Dispatchers.IO) {
            val upload = launch { dataSource.upload(server.serverUrl()) { sent.addAndGet(it) } }
            while (sent.get() < 1_000_000) yield()

            withTimeout(2.seconds) { upload.cancelAndJoin() }
        }

        val request = checkNotNull(server.takeRequest(2, TimeUnit.SECONDS))
        assertEquals("POST", request.method)
        assertEquals("/upload", request.url.encodedPath)
        assertEquals("token-1", request.url.queryParameter("token"))
        assertEquals("chunked", request.headers["Transfer-Encoding"])
        assertEquals("text/plain", request.headers["Accept"])
    }

    @Test
    fun bodyReportsExactlyWhatTheSinkAccepted() {
        val reported = AtomicLong()
        val accepted = AtomicLong()
        val sink = object : ForwardingSink(Buffer()) {
            override fun write(source: Buffer, byteCount: Long) {
                if (accepted.get() >= 1_000_000) throw IOException("connection closed")
                accepted.addAndGet(byteCount)
                super.write(source, byteCount)
            }
        }.buffer()

        assertFailsWith<IOException> { EndlessUploadBody { reported.addAndGet(it) }.writeTo(sink) }

        assertTrue(reported.get() in 1_000_000..accepted.get())
    }

    @Test
    fun bodyIsChunkedAndCannotBeReplayed() {
        val body = EndlessUploadBody {}

        assertEquals(-1, body.contentLength())
        assertTrue(body.isOneShot())
    }
}
