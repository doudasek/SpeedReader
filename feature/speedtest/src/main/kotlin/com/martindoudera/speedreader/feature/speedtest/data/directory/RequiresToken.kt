package com.martindoudera.speedreader.feature.speedtest.data.directory

/** Marks a Retrofit method whose request [TokenInterceptor] signs with the directory token. */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class RequiresToken(val placement: TokenPlacement)

/** The directory reads the token from a header, the measurement servers from a query parameter. */
internal enum class TokenPlacement {
    Header,
    Query,
}

internal const val TOKEN_HEADER = "x-test-token"
internal const val TOKEN_QUERY_PARAMETER = "token"
