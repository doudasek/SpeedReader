package com.martindoudera.speedreader.core.network

import com.martindoudera.speedreader.core.common.error.HttpStatusCode
import com.martindoudera.speedreader.core.common.error.NetworkError
import com.martindoudera.speedreader.core.common.error.Outcome
import java.io.EOFException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class NetworkCallTest {
    @Test
    fun successfulCallIsWrapped() = runTest {
        val outcome = safeApiCall { "payload" }

        assertEquals(Outcome.Success("payload"), outcome)
    }

    @Test
    fun unknownHostBecomesNoConnection() = runTest {
        val failure = UnknownHostException("sp-dir.uwn.com")

        val error = errorOf(failure)

        assertIs<NetworkError.NoConnection>(error)
    }

    @Test
    fun socketTimeoutBecomesTimeout() = runTest {
        val failure = SocketTimeoutException("read timed out")

        val error = errorOf(failure)

        assertIs<NetworkError.Timeout>(error)
    }

    @Test
    fun handshakeFailureBecomesTls() = runTest {
        val failure = SSLHandshakeException("trust anchor not found")

        val error = errorOf(failure)

        assertIs<NetworkError.Tls>(error)
    }

    @Test
    fun httpFailureKeepsTheStatusCode() = runTest {
        val failure = HttpException(Response.error<Unit>(401, """{"message":"No token"}""".toResponseBody()))

        val error = errorOf(failure)

        assertEquals(NetworkError.Http(HttpStatusCode(401)), error)
    }

    @Test
    fun serializationFailureBecomesMalformed() = runTest {
        val failure = SerializationException("unexpected token")

        val error = errorOf(failure)

        assertIs<NetworkError.Malformed>(error)
    }

    @Test
    fun truncatedResponseIsTreatedAsConnectionLoss() = runTest {
        val failure = EOFException("unexpected end of stream")

        val error = errorOf(failure)

        assertIs<NetworkError.NoConnection>(error)
    }

    @Test
    fun anythingElseBecomesUnknown() = runTest {
        val failure = IllegalStateException("bug")

        val error = errorOf(failure)

        assertIs<NetworkError.Unknown>(error)
    }

    @Test
    fun cancellationIsRethrownRatherThanReportedAsFailure() = runTest {
        val cancellation = CancellationException("stopped by the user")

        val result = runCatching { safeApiCall { throw cancellation } }

        assertEquals(cancellation, result.exceptionOrNull())
    }

    private suspend fun errorOf(failure: Exception): NetworkError {
        val outcome = safeApiCall { throw failure }
        return assertIs<Outcome.Failure<NetworkError>>(outcome).error
    }
}
