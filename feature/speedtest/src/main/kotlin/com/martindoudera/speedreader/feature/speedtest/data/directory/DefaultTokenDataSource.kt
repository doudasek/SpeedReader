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

/** Renew a token this long before the directory would expire it, so it cannot expire mid-request. */
internal val TOKEN_RENEWAL_MARGIN = 15.seconds

/** Caches the token and renews it [TOKEN_RENEWAL_MARGIN] before its TTL runs out. Concurrent callers share one request. */
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

    @Synchronized
    override fun clear() {
        Timber.d("Dropping the cached directory token")
        cached = null
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
        return CachedToken(DirectoryToken(body.token), timeSource.markNow() + body.ttl.seconds - TOKEN_RENEWAL_MARGIN)
    }

    private class CachedToken(val token: DirectoryToken, val renewAt: TimeMark)
}
