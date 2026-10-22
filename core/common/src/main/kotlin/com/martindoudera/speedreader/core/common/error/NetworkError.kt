package com.martindoudera.speedreader.core.common.error

sealed interface NetworkError {
    data class NoConnection(
        val cause: Throwable?,
    ) : NetworkError

    data class Timeout(
        val cause: Throwable?,
    ) : NetworkError

    data class Tls(
        val cause: Throwable?,
    ) : NetworkError

    data class Http(
        val statusCode: Int,
    ) : NetworkError

    data class Malformed(
        val cause: Throwable?,
    ) : NetworkError

    data class Unknown(
        val cause: Throwable?,
    ) : NetworkError
}

typealias NetworkOutcome<T> = Outcome<T, NetworkError>
