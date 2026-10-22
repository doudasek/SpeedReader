package com.martindoudera.speedreader.core.network

import com.martindoudera.speedreader.core.common.error.NetworkError
import com.martindoudera.speedreader.core.common.error.Outcome
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.EOFException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class NetworkCallTest {
    @Test
    fun unknownHostBecomesNoConnection() {
        val error = UnknownHostException("sp-dir.uwn.com").toNetworkError()

        assertTrue(error is NetworkError.NoConnection)
    }

    @Test
    fun socketTimeoutBecomesTimeout() {
        val error = SocketTimeoutException("read timed out").toNetworkError()

        assertTrue(error is NetworkError.Timeout)
    }

    @Test
    fun handshakeFailureBecomesTls() {
        val error = SSLHandshakeException("trust anchor not found").toNetworkError()

        assertTrue(error is NetworkError.Tls)
    }

    @Test
    fun httpFailureKeepsTheStatusCode() {
        val unauthorized = HttpException(Response.error<Unit>(401, """{"message":"No token"}""".toResponseBody()))

        val error = unauthorized.toNetworkError()

        assertEquals(NetworkError.Http(401), error)
    }

    @Test
    fun serializationFailureBecomesMalformed() {
        val error = SerializationException("unexpected token").toNetworkError()

        assertTrue(error is NetworkError.Malformed)
    }

    @Test
    fun truncatedResponseIsTreatedAsConnectionLoss() {
        val error = EOFException("unexpected end of stream").toNetworkError()

        assertTrue(error is NetworkError.NoConnection)
    }

    @Test
    fun anythingElseBecomesUnknown() {
        val error = IllegalStateException("bug").toNetworkError()

        assertTrue(error is NetworkError.Unknown)
    }

    @Test
    fun successfulCallIsWrapped() {
        val outcome = runBlocking { safeApiCall { "payload" } }

        assertEquals(Outcome.Success("payload"), outcome)
    }

    @Test
    fun failingCallIsMappedToFailure() {
        val outcome = runBlocking { safeApiCall { throw UnknownHostException("nope") } }

        assertTrue(outcome is Outcome.Failure && outcome.error is NetworkError.NoConnection)
    }

    @Test
    fun cancellationIsRethrownRatherThanReportedAsFailure() {
        val thrown =
            runCatching {
                runBlocking { safeApiCall { throw CancellationException("stopped by the user") } }
            }.exceptionOrNull()

        assertTrue(thrown is CancellationException)
    }
}
