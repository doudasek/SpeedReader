package com.martindoudera.speedreader.feature.speedtest.domain.usecase

import com.martindoudera.speedreader.core.common.error.NetworkError
import com.martindoudera.speedreader.core.common.error.NetworkOutcome
import com.martindoudera.speedreader.core.common.error.Outcome
import com.martindoudera.speedreader.feature.speedtest.domain.model.Distance
import com.martindoudera.speedreader.feature.speedtest.domain.model.GeoPoint
import com.martindoudera.speedreader.feature.speedtest.domain.model.Latitude
import com.martindoudera.speedreader.feature.speedtest.domain.model.Longitude
import com.martindoudera.speedreader.feature.speedtest.domain.model.ServerUrl
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestServer
import com.martindoudera.speedreader.feature.speedtest.domain.repository.LocationRepository
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Test

class SelectClosestServersUseCaseTest {
    private val selectClosestServers = SelectClosestServersUseCase(FixedDistanceLocationRepository)

    @Test
    fun picksTheClosestServersInOrderOfDistance() {
        val closest = selectClosestServers(directory, ORIGIN)

        assertEquals(listOf(NEAREST, SECOND, CO_LOCATED_A, CO_LOCATED_B, FIFTH), closest.map { it.url })
    }

    @Test
    fun coLocatedServersAreOrderedByUrl() {
        val coLocated = directory.filter { it.url == CO_LOCATED_A || it.url == CO_LOCATED_B }

        val ordered = selectClosestServers(coLocated.reversed(), ORIGIN)

        assertEquals(listOf(CO_LOCATED_A, CO_LOCATED_B), ordered.map { it.url })
    }

    @Test
    fun inputOrderDoesNotChangeTheSelection() {
        val expected = selectClosestServers(directory, ORIGIN)

        repeat(20) { seed ->
            assertEquals(expected, selectClosestServers(directory.shuffled(Random(seed)), ORIGIN))
        }
    }

    @Test
    fun withoutLocationKeepsTheDirectoryOrder() {
        val closest = selectClosestServers(directory, from = null)

        assertEquals(directory.take(5), closest)
    }

    @Test
    fun returnsEveryServerWhenThereAreFewerThanRequested() {
        val closest = selectClosestServers(directory.take(3), ORIGIN)

        assertEquals(3, closest.size)
    }

    @Test
    fun emptyDirectoryGivesNoServers() {
        assertEquals(emptyList<SpeedTestServer>(), selectClosestServers(emptyList(), ORIGIN))
    }

    private object FixedDistanceLocationRepository : LocationRepository {
        override suspend fun currentLocation(): NetworkOutcome<GeoPoint> = Outcome.Failure(NetworkError.Unknown(null))

        override fun distance(from: GeoPoint, to: GeoPoint): Distance = distances.getValue(to)
    }

    private companion object {
        fun point(latitude: Double, longitude: Double) = GeoPoint(Latitude(latitude), Longitude(longitude))

        val ORIGIN = point(49.0, 16.0)

        val NEAREST = ServerUrl("https://nearest.example")
        val SECOND = ServerUrl("https://second.example")
        val CO_LOCATED_A = ServerUrl("https://co-located-a.example")
        val CO_LOCATED_B = ServerUrl("https://co-located-b.example")
        val FIFTH = ServerUrl("https://fifth.example")
        val SIXTH = ServerUrl("https://sixth.example")
        val FARTHEST = ServerUrl("https://farthest.example")

        val shared = point(48.2, 16.4)

        val distances = mapOf(
            point(48.8, 16.9) to Distance(53_000.0),
            point(49.2, 17.7) to Distance(77_000.0),
            shared to Distance(111_000.0),
            point(48.2, 16.3) to Distance(112_000.0),
            point(49.5, 18.1) to Distance(115_000.0),
            point(50.0, 16.6) to Distance(160_000.0),
        )

        val directory = listOf(
            SpeedTestServer(FARTHEST, point(50.0, 16.6), null, null, null),
            SpeedTestServer(CO_LOCATED_B, shared, null, null, null),
            SpeedTestServer(SECOND, point(49.2, 17.7), null, null, null),
            SpeedTestServer(SIXTH, point(49.5, 18.1), null, null, null),
            SpeedTestServer(NEAREST, point(48.8, 16.9), null, null, null),
            SpeedTestServer(CO_LOCATED_A, shared, null, null, null),
            SpeedTestServer(FIFTH, point(48.2, 16.3), null, null, null),
        )
    }
}
