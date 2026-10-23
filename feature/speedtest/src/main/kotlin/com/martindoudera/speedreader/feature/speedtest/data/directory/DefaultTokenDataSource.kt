package com.martindoudera.speedreader.feature.speedtest.data.directory

import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import timber.log.Timber

/**
 * Renew a token this long before its TTL runs out. Servers check the token only when a request
 * starts, so the margin only has to cover the time until the next request starts, not a whole
 * transfer.
 */
internal val TOKEN_RENEWAL_MARGIN = 15.seconds

/**
 * Caches the token and renews it [TOKEN_RENEWAL_MARGIN] before its TTL runs out, or halfway through
 * a TTL too short for that margin. Concurrent callers share one request.
 */
@Singleton
internal class DefaultTokenDataSource @Inject constructor(private val api: DirectoryTokenApi, private val timeSource: TimeSource) :
    TokenDataSource {
    private var cached: CachedToken? = null

    @Synchronized
    override fun token(): DirectoryToken {
        val current = cached?.takeUnless { it.renewAt.hasPassedNow() }
        if (current != null) Timber.v("Reusing the directory token")
        return (current ?: issue().also { cached = it }).token
    }

    @Synchronized
    override fun invalidate(token: DirectoryToken) {
        if (cached?.token == token) cached = null
    }

    private fun issue(): CachedToken {
        Timber.d("Requesting a directory token")
        val response = try {
            api.createToken().execute()
        } catch (e: SerializationException) {
            throw IOException("Malformed directory token", e)
        }
        if (!response.isSuccessful) throw HttpException(response)
        val body = response.body() ?: throw IOException("Empty directory token response")
        Timber.d("Directory token issued, valid for %d s", body.ttl)
        val ttl = body.ttl.seconds
        return CachedToken(DirectoryToken(body.token), timeSource.markNow() + ttl - minOf(TOKEN_RENEWAL_MARGIN, ttl / 2))
    }

    private class CachedToken(val token: DirectoryToken, val renewAt: TimeMark)
}
