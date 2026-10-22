package com.martindoudera.speedreader.core.network.di

import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import org.junit.Test

class OkHttpClientConfigurationTest {
    @Test
    fun timeoutsAreTenSeconds() {
        val expected = TimeUnit.SECONDS.toMillis(10).toInt()

        val client = NetworkModule.providesOkHttpClient()

        assertEquals(expected, client.connectTimeoutMillis)
        assertEquals(expected, client.readTimeoutMillis)
        assertEquals(expected, client.writeTimeoutMillis)
    }

    @Test
    fun noCallTimeoutIsSetSoDerivedClientsCanRunLongTransfers() {
        val client = NetworkModule.providesOkHttpClient()

        assertEquals(0, client.callTimeoutMillis)
    }
}
