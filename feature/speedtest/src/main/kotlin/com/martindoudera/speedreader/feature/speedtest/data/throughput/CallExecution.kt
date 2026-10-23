package com.martindoudera.speedreader.feature.speedtest.data.throughput

import java.io.IOException
import kotlin.random.Random
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

/**
 * Executes this call on the current thread and cancels it as soon as the calling coroutine is
 * cancelled. Cancelling the call aborts a read or write that is blocked on the socket, which
 * coroutine cancellation alone cannot do. The `IOException` that abort causes is reported as the
 * cancellation it is, not as a failure.
 *
 * The canceller starts undispatched, so it is waiting before the call blocks: a coroutine cancelled
 * while still queued never runs, and its `finally` would never cancel the call. A caller that is
 * already cancelled thus cancels the call before it is sent.
 */
internal suspend fun <T, R> Call<T>.executeCancellably(block: (Response<T>) -> R): R = coroutineScope {
    val call = this@executeCancellably
    val canceller = launch(start = CoroutineStart.UNDISPATCHED) {
        try {
            awaitCancellation()
        } finally {
            call.cancel()
        }
    }
    try {
        block(call.execute())
    } catch (e: IOException) {
        ensureActive()
        throw e
    } finally {
        canceller.cancel()
    }
}

private const val CACHE_BUSTER_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

/** Seven random characters, as the web client sends in `nc`. */
internal fun cacheBuster(): String = String(CharArray(7) { CACHE_BUSTER_CHARACTERS[Random.nextInt(CACHE_BUSTER_CHARACTERS.length)] })
