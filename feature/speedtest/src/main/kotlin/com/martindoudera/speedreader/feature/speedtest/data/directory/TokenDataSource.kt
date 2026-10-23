package com.martindoudera.speedreader.feature.speedtest.data.directory

/**
 * Holds the directory token shared by all requests.
 *
 * Blocking, because [TokenInterceptor] calls it on OkHttp threads. [token] throws `HttpException`
 * when the directory refuses to issue a token, and `IOException` when it cannot be reached or its
 * answer is malformed.
 */
internal interface TokenDataSource {
    fun token(): DirectoryToken

    fun invalidate(token: DirectoryToken)
}
