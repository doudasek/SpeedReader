package com.martindoudera.speedreader.feature.speedtest.data.directory

import java.io.IOException
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Invocation
import timber.log.Timber

/**
 * Signs requests of methods annotated with [RequiresToken] and leaves all others untouched.
 *
 * Measurement servers answer an expired, unknown or missing token with `401 Unauthorized`, so a
 * request rejected with 401 is retried once with a fresh token. When no token can be issued,
 * the directory's HTTP status is returned as the response, so the caller sees it as an HTTP error.
 * Any other token failure leaves as an `IOException`, the only exception OkHttp lets an
 * interceptor throw without crashing an asynchronous call.
 */
internal class TokenInterceptor @Inject constructor(private val tokenDataSource: TokenDataSource) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val placement = request.tag(Invocation::class.java)?.method()?.getAnnotation(RequiresToken::class.java)?.placement
            ?: return chain.proceed(request)

        return try {
            chain.proceedSigned(request, placement)
        } catch (e: HttpException) {
            Timber.w(e, "Directory refused to issue a token")
            tokenUnavailable(request, e.code())
        }
    }

    private fun Interceptor.Chain.proceedSigned(request: Request, placement: TokenPlacement): Response {
        val token = issueToken()
        Timber.v("Signing %s with the directory token", request.url.encodedPath)
        val response = proceed(request.signedWith(token, placement))
        if (response.code != HTTP_UNAUTHORIZED) return response

        Timber.w("%s rejected the token, renewing it and retrying once", request.url.encodedPath)
        response.close()
        tokenDataSource.invalidate(token)
        return proceed(request.signedWith(issueToken(), placement))
    }

    /** Wraps only the token's own failures, so an exception from the request itself is not blamed on the token. */
    private fun issueToken(): DirectoryToken = try {
        tokenDataSource.token()
    } catch (e: HttpException) {
        throw e
    } catch (e: RuntimeException) {
        throw IOException("Directory token unavailable", e)
    }

    private fun Request.signedWith(token: DirectoryToken, placement: TokenPlacement): Request = when (placement) {
        TokenPlacement.Header -> newBuilder().header(TOKEN_HEADER, token.value).build()
        TokenPlacement.Query -> newBuilder().url(url.newBuilder().setQueryParameter(TOKEN_QUERY_PARAMETER, token.value).build()).build()
    }

    private fun tokenUnavailable(request: Request, status: Int): Response = Response
        .Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(status)
        .message("Directory token unavailable")
        .body("".toResponseBody())
        .build()
}
