package com.martindoudera.speedreader.core.network.di

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class OkHttpClientConfigurationTest {
    private val client = NetworkModule.providesOkHttpClient()

    @Test
    fun timeoutsAreTenSeconds() {
        val expected = TimeUnit.SECONDS.toMillis(10).toInt()

        assertEquals(expected, client.connectTimeoutMillis)
        assertEquals(expected, client.readTimeoutMillis)
        assertEquals(expected, client.writeTimeoutMillis)
    }

    @Test
    fun noCallTimeoutIsSetSoDerivedClientsCanRunLongTransfers() {
        assertEquals(0, client.callTimeoutMillis)
    }
}
