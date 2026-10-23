package com.martindoudera.speedreader.feature.speedtest.data.location

import com.martindoudera.speedreader.core.common.error.HttpStatusCode
import com.martindoudera.speedreader.core.common.error.NetworkError
import com.martindoudera.speedreader.core.common.error.Outcome
import com.martindoudera.speedreader.feature.speedtest.data.directory.dto.IpLocationDto
import com.martindoudera.speedreader.feature.speedtest.testing.data.geoPoint
import com.martindoudera.speedreader.feature.speedtest.testing.data.httpError
import com.martindoudera.speedreader.feature.speedtest.testing.directory.FakeSpeedTestDirectoryApi
import java.net.UnknownHostException
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultLocationRepositoryTest {
    private val api = FakeSpeedTestDirectoryApi()
    private val repository = DefaultLocationRepository(api)

    @Test
    fun ipLocationBecomesTheCurrentLocation() = runTest {
        api.clientLocation = IpLocationDto(lat = 50.0755, lon = 14.4378, countryCode = "CZ")

        val outcome = repository.currentLocation()

        assertEquals(Outcome.Success(geoPoint(50.0755, 14.4378)), outcome)
    }

    @Test
    fun missingCountryDoesNotMatter() = runTest {
        api.clientLocation = IpLocationDto(lat = 50.0755, lon = 14.4378)

        val outcome = repository.currentLocation()

        assertEquals(Outcome.Success(geoPoint(50.0755, 14.4378)), outcome)
    }

    @Test
    fun impossibleCoordinatesAreMalformed() = runTest {
        api.clientLocation = IpLocationDto(lat = 120.0, lon = 14.4378)

        val outcome = repository.currentLocation()

        assertIs<NetworkError.Malformed>(outcome.error())
    }

    @Test
    fun zeroCoordinatesAreAnUnlocatedIpRatherThanALocation() = runTest {
        api.clientLocation = IpLocationDto(lat = 0.0, lon = 0.0)

        val outcome = repository.currentLocation()

        assertIs<NetworkError.Malformed>(outcome.error())
    }

    @Test
    fun unreachableDirectoryIsNoConnection() = runTest {
        api.clientLocationFailure = UnknownHostException("sp-dir.uwn.com")

        val outcome = repository.currentLocation()

        assertIs<NetworkError.NoConnection>(outcome.error())
    }

    @Test
    fun httpFailureKeepsTheStatusCode() = runTest {
        api.clientLocationFailure = httpError(502)

        val outcome = repository.currentLocation()

        assertEquals(NetworkError.Http(HttpStatusCode(502)), outcome.error())
    }

    private fun <T> Outcome<T, NetworkError>.error() = assertIs<Outcome.Failure<NetworkError>>(this).error
}
