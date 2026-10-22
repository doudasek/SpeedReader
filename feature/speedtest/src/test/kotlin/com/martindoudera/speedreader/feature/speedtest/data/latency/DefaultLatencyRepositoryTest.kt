package com.martindoudera.speedreader.feature.speedtest.data.latency

import com.martindoudera.speedreader.core.common.error.Outcome
import com.martindoudera.speedreader.feature.speedtest.domain.SpeedTestError
import com.martindoudera.speedreader.feature.speedtest.testing.data.testServer
import com.martindoudera.speedreader.feature.speedtest.testing.latency.FakeDns
import com.martindoudera.speedreader.feature.speedtest.testing.latency.FakePingDataSource
import java.io.IOException
import java.net.InetAddress
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DefaultLatencyRepositoryTest {
    private val pingDataSource = FakePingDataSource()
    private val dns = FakeDns()
    private val dispatcher = StandardTestDispatcher()
    private val repository = DefaultLatencyRepository(pingDataSource, dns, dispatcher)
    private val server = testServer("https://breclav.wifiman.me:81", 48.7569, 16.8854)
    private val address = InetAddress.getByName("192.0.2.10")

    @Before
    fun setUp() {
        dns.addresses = mapOf("breclav.wifiman.me" to address)
    }

    @Test
    fun latencyIsTheFastestProbe() = runTest(dispatcher) {
        pingDataSource.roundTrips = listOf(30, 22, 25, 40, 28).map { it.milliseconds }

        val outcome = repository.measureLatency(server)

        assertEquals(Outcome.Success(22.milliseconds), outcome)
    }

    @Test
    fun lostProbesAreIgnored() = runTest(dispatcher) {
        pingDataSource.roundTrips = listOf(null, 30.milliseconds, null, 25.milliseconds, null)

        val outcome = repository.measureLatency(server)

        assertEquals(Outcome.Success(25.milliseconds), outcome)
    }

    @Test
    fun everyProbeGoesToTheResolvedAddressWithTheProbeTimeout() = runTest(dispatcher) {
        pingDataSource.roundTrips = List(PING_PROBES) { 20.milliseconds }

        repository.measureLatency(server)

        assertEquals(List(PING_PROBES) { address }, pingDataSource.pinged)
        assertEquals(List(PING_PROBES) { PROBE_TIMEOUT }, pingDataSource.timeouts)
    }

    @Test
    fun silentServerIsUnreachable() = runTest(dispatcher) {
        pingDataSource.roundTrips = emptyList()

        val outcome = repository.measureLatency(server)

        assertEquals(Outcome.Failure(SpeedTestError.ServerUnreachable), outcome)
    }

    @Test
    fun unresolvableHostIsUnreachableWithoutAnyProbe() = runTest(dispatcher) {
        dns.addresses = emptyMap()

        val outcome = repository.measureLatency(server)

        assertEquals(Outcome.Failure(SpeedTestError.ServerUnreachable), outcome)
        assertEquals(emptyList(), pingDataSource.pinged)
    }

    @Test
    fun malformedServerUrlIsUnreachable() = runTest(dispatcher) {
        val broken = testServer("not a url", 48.7569, 16.8854)

        val outcome = repository.measureLatency(broken)

        assertEquals(Outcome.Failure(SpeedTestError.ServerUnreachable), outcome)
    }

    @Test
    fun pingFailureIsUnknownRatherThanUnreachable() = runTest(dispatcher) {
        pingDataSource.failure = IOException("Network is unreachable")

        val outcome = repository.measureLatency(server)

        assertIs<SpeedTestError.Unknown>(assertIs<Outcome.Failure<SpeedTestError>>(outcome).error)
    }
}
