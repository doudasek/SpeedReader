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
    private val ipv6Address = InetAddress.getByName("2001:db8::10")

    @Before
    fun setUp() {
        dns.addresses = mapOf("breclav.wifiman.me" to listOf(address))
    }

    @Test
    fun latencyIsTheFastestProbe() = runTest(dispatcher) {
        pingDataSource.roundTrips = listOf(30, 22, 25).map { it.milliseconds }

        val outcome = repository.measureLatency(server)

        assertEquals(Outcome.Success(22.milliseconds), outcome)
    }

    @Test
    fun oneLostProbeDoesNotEndProbing() = runTest(dispatcher) {
        pingDataSource.roundTrips = listOf(null, 30.milliseconds, 10.milliseconds)

        val outcome = repository.measureLatency(server)

        assertEquals(Outcome.Success(10.milliseconds), outcome)
        assertEquals(PING_PROBES, pingDataSource.pinged.size)
    }

    @Test
    fun answersBeforeTwoLostProbesInARowCount() = runTest(dispatcher) {
        pingDataSource.roundTrips = listOf(30.milliseconds, null, null)

        val outcome = repository.measureLatency(server)

        assertEquals(Outcome.Success(30.milliseconds), outcome)
    }

    @Test
    fun everyProbeGoesToTheResolvedAddressWithTheProbeTimeout() = runTest(dispatcher) {
        pingDataSource.roundTrips = List(PING_PROBES) { 20.milliseconds }

        repository.measureLatency(server)

        assertEquals(List(PING_PROBES) { address }, pingDataSource.pinged)
        assertEquals(List(PING_PROBES) { PROBE_TIMEOUT }, pingDataSource.timeouts)
    }

    @Test
    fun silentServerIsUnreachableAfterTwoLostProbesInARow() = runTest(dispatcher) {
        pingDataSource.roundTrips = emptyList()

        val outcome = repository.measureLatency(server)

        assertEquals(Outcome.Failure(SpeedTestError.ServerUnreachable), outcome)
        assertEquals(LOST_PROBES_IN_A_ROW, pingDataSource.pinged.size)
    }

    @Test
    fun silentAddressFallsBackToTheOtherFamily() = runTest(dispatcher) {
        dns.addresses = mapOf("breclav.wifiman.me" to listOf(ipv6Address, address))
        pingDataSource.roundTrips = listOf(null, null, 20.milliseconds, 25.milliseconds, 22.milliseconds)

        val outcome = repository.measureLatency(server)

        assertEquals(Outcome.Success(20.milliseconds), outcome)
        assertEquals(listOf(ipv6Address, ipv6Address, address, address, address), pingDataSource.pinged)
    }

    @Test
    fun answeringAddressIsTheOnlyOnePinged() = runTest(dispatcher) {
        dns.addresses = mapOf("breclav.wifiman.me" to listOf(ipv6Address, address))
        pingDataSource.roundTrips = List(PING_PROBES) { 20.milliseconds }

        repository.measureLatency(server)

        assertEquals(List(PING_PROBES) { ipv6Address }, pingDataSource.pinged)
    }

    @Test
    fun onlyTheFirstAddressOfEachFamilyIsPinged() = runTest(dispatcher) {
        dns.addresses = mapOf("breclav.wifiman.me" to listOf(address, InetAddress.getByName("192.0.2.11")))

        val outcome = repository.measureLatency(server)

        assertEquals(Outcome.Failure(SpeedTestError.ServerUnreachable), outcome)
        assertEquals(listOf(address, address), pingDataSource.pinged)
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
