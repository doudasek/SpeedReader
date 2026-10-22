package com.martindoudera.speedreader.core.network

import com.martindoudera.speedreader.core.common.error.HttpStatusCode
import com.martindoudera.speedreader.core.common.error.NetworkError
import com.martindoudera.speedreader.core.common.error.NetworkOutcome
import com.martindoudera.speedreader.core.common.error.Outcome
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.PortUnreachableException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

suspend fun <T> safeApiCall(block: suspend () -> T): NetworkOutcome<T> = try {
    Outcome.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Outcome.Failure(e.toNetworkError())
}

private fun Throwable.toNetworkError(): NetworkError = when (this) {
    is HttpException -> NetworkError.Http(HttpStatusCode(code()))
    is SerializationException -> NetworkError.Malformed(this)
    is SSLException -> NetworkError.Tls(this)
    is SocketTimeoutException -> NetworkError.Timeout(this)
    is InterruptedIOException -> NetworkError.Timeout(this)
    is UnknownHostException, is ConnectException, is NoRouteToHostException, is PortUnreachableException, is IOException ->
        NetworkError.NoConnection(this)
    else -> NetworkError.Unknown(this)
}
