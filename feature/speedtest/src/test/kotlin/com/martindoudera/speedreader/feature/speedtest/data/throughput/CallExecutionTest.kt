package com.martindoudera.speedreader.feature.speedtest.data.throughput

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.net.SocketException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.junit.Test
import retrofit2.Call

/** Simulates a specific network error state on the call itself: the connection reset during `execute()`. */
class CallExecutionTest {
    private val call = mockk<Call<String>> {
        every { cancel() } just Runs
    }

    @Test
    fun connectionResetWhileTheCallerIsActiveIsAFailureNotACancellation() = runTest {
        every { call.execute() } throws SocketException("Connection reset")

        val failure = assertFailsWith<SocketException> { call.executeCancellably { it.body() } }

        assertEquals("Connection reset", failure.message)
    }
}
