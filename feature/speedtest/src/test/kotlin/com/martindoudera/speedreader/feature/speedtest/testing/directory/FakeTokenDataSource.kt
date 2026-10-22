package com.martindoudera.speedreader.feature.speedtest.testing.directory

import com.martindoudera.speedreader.feature.speedtest.data.directory.DirectoryToken
import com.martindoudera.speedreader.feature.speedtest.data.directory.TokenDataSource

/** Issues `token-1`, `token-2`, … and a new one only after the current one is invalidated or cleared. */
internal class FakeTokenDataSource : TokenDataSource {
    var failure: Exception? = null
    var renewalFailure: Exception? = null

    var issuedTokens = 0
        private set
    var clears = 0
        private set
    val invalidated = mutableListOf<DirectoryToken>()

    private var current: DirectoryToken? = null

    override fun token(): DirectoryToken {
        failure?.let { throw it }
        current?.let { return it }
        if (issuedTokens > 0) renewalFailure?.let { throw it }
        return DirectoryToken("token-${++issuedTokens}").also { current = it }
    }

    override fun invalidate(token: DirectoryToken) {
        invalidated += token
        if (current == token) current = null
    }

    override fun clear() {
        clears++
        current = null
    }
}
