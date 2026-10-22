package com.martindoudera.speedreader.feature.speedtest.data.directory

/**
 * Holds the directory token shared by all requests.
 *
 * Blocking, because [TokenInterceptor] calls it on OkHttp threads. [token] throws `IOException`
 * when the directory cannot be reached and `HttpException` when it refuses to issue a token.
 */
internal interface TokenDataSource {
    fun token(): DirectoryToken

    fun invalidate(token: DirectoryToken)

    fun clear()
}
