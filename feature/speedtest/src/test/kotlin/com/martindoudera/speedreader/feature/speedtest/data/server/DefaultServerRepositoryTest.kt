package com.martindoudera.speedreader.feature.speedtest.data.server

import com.martindoudera.speedreader.core.common.error.HttpStatusCode
import com.martindoudera.speedreader.core.common.error.NetworkError
import com.martindoudera.speedreader.core.common.error.Outcome
import com.martindoudera.speedreader.feature.speedtest.domain.model.GeoPoint
import com.martindoudera.speedreader.feature.speedtest.domain.model.Latitude
import com.martindoudera.speedreader.feature.speedtest.domain.model.Longitude
import com.martindoudera.speedreader.feature.speedtest.domain.model.ServerUrl
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestServer
import com.martindoudera.speedreader.feature.speedtest.testing.data.httpError
import com.martindoudera.speedreader.feature.speedtest.testing.data.testJson
import com.martindoudera.speedreader.feature.speedtest.testing.directory.FakeSpeedTestDirectoryApi
import com.martindoudera.speedreader.feature.speedtest.testing.directory.FakeTokenDataSource
import java.net.UnknownHostException
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultServerRepositoryTest {
    private val api = FakeSpeedTestDirectoryApi()
    private val tokenDataSource = FakeTokenDataSource()
    private val repository = DefaultServerRepository(api, tokenDataSource, testJson)

    @Test
    fun serverIsMappedToTheDomainModel() = runTest {
        api.serversJson = """[$BRECLAV]"""

        val outcome = repository.servers()

        val breclav = SpeedTestServer(
            url = ServerUrl("https://breclav.wifiman.me:81"),
            location = GeoPoint(Latitude(48.7569), Longitude(16.8854)),
            provider = "MyOffice s.r.o.",
            city = "Břeclav",
            countryCode = "CZ",
        )
        assertEquals(Outcome.Success(listOf(breclav)), outcome)
    }

    @Test
    fun missingLabelsBecomeNull() = runTest {
        api.serversJson = """[{"url": "https://graz.wifiman.me:80", "latitude": 48.2, "longitude": 16.37}]"""

        val server = repository.servers().servers().single()

        assertEquals(null, server.provider)
        assertEquals(null, server.city)
        assertEquals(null, server.countryCode)
    }

    @Test
    fun malformedEntryIsDroppedAndTheRestKept() = runTest {
        api.serversJson = """[$BRECLAV, {"url": "https://broken.wifiman.me", "latitude": "north"}]"""

        val servers = repository.servers().servers()

        assertEquals(listOf(ServerUrl("https://breclav.wifiman.me:81")), servers.map { it.url })
    }

    @Test
    fun entryWithImpossibleCoordinatesIsDropped() = runTest {
        api.serversJson = """[$BRECLAV, {"url": "https://nowhere.wifiman.me", "latitude": 91.0, "longitude": 0.0}]"""

        val servers = repository.servers().servers()

        assertEquals(listOf(ServerUrl("https://breclav.wifiman.me:81")), servers.map { it.url })
    }

    @Test
    fun emptyListIsSuccess() = runTest {
        api.serversJson = "[]"

        val outcome = repository.servers()

        assertEquals(Outcome.Success(emptyList()), outcome)
    }

    @Test
    fun everyLoadStartsWithAFreshToken() = runTest {
        repository.servers()

        repository.servers()

        assertEquals(2, tokenDataSource.clears)
    }

    @Test
    fun httpFailureKeepsTheStatusCode() = runTest {
        api.serversFailure = httpError(503)

        val outcome = repository.servers()

        assertEquals(Outcome.Failure(NetworkError.Http(HttpStatusCode(503))), outcome)
    }

    @Test
    fun unreachableDirectoryIsNoConnection() = runTest {
        api.serversFailure = UnknownHostException("sp-dir.uwn.com")

        val outcome = repository.servers()

        assertIs<NetworkError.NoConnection>(assertIs<Outcome.Failure<NetworkError>>(outcome).error)
    }

    private fun Outcome<List<SpeedTestServer>, NetworkError>.servers() = assertIs<Outcome.Success<List<SpeedTestServer>>>(this).value

    private companion object {
        const val BRECLAV = """{
            "url": "https://breclav.wifiman.me:81",
            "latitude": 48.7569,
            "longitude": 16.8854,
            "provider": "MyOffice s.r.o.",
            "providerUrl": "https://www.myoffice.cz",
            "city": "Břeclav",
            "country": "Czechia",
            "countryCode": "CZ",
            "speedMbps": 1000
        }"""
    }
}
