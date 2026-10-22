package com.martindoudera.speedreader.feature.speedtest.domain.usecase

import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestServer
import com.martindoudera.speedreader.feature.speedtest.testing.data.geoPoint
import com.martindoudera.speedreader.feature.speedtest.testing.data.testServer
import com.martindoudera.speedreader.feature.speedtest.testing.repository.FakeLocationRepository
import kotlin.random.Random
import kotlin.test.assertEquals
import org.junit.Test

class SelectClosestServersUseCaseTest {
    private val selectClosestServers = SelectClosestServersUseCase(FakeLocationRepository())

    @Test
    fun picksTheClosestServersInOrderOfDistance() {
        val closest = selectClosestServers(directory, ORIGIN)

        assertEquals(listOf(NEAREST, SECOND, CO_LOCATED_A, CO_LOCATED_B, FIFTH), closest)
    }

    @Test
    fun coLocatedServersAreOrderedByUrl() {
        val coLocated = listOf(CO_LOCATED_B, CO_LOCATED_A)

        val ordered = selectClosestServers(coLocated, ORIGIN)

        assertEquals(listOf(CO_LOCATED_A, CO_LOCATED_B), ordered)
    }

    @Test
    fun inputOrderDoesNotChangeTheSelection() {
        val shuffledDirectories = List(20) { seed -> directory.shuffled(Random(seed)) }

        val selections = shuffledDirectories.map { selectClosestServers(it, ORIGIN) }

        assertEquals(setOf(selectClosestServers(directory, ORIGIN)), selections.toSet())
    }

    @Test
    fun withoutLocationKeepsTheDirectoryOrder() {
        val closest = selectClosestServers(directory, from = null)

        assertEquals(directory.take(5), closest)
    }

    @Test
    fun returnsEveryServerWhenThereAreFewerThanRequested() {
        val smallDirectory = directory.take(3)

        val closest = selectClosestServers(smallDirectory, ORIGIN)

        assertEquals(3, closest.size)
    }

    @Test
    fun emptyDirectoryGivesNoServers() {
        val closest = selectClosestServers(emptyList(), ORIGIN)

        assertEquals(emptyList<SpeedTestServer>(), closest)
    }

    private companion object {
        val ORIGIN = geoPoint(49.0, 16.0)

        val NEAREST = testServer("https://nearest.example", 49.1, 16.0)
        val SECOND = testServer("https://second.example", 49.3, 16.0)
        val CO_LOCATED_A = testServer("https://co-located-a.example", 49.5, 16.0)
        val CO_LOCATED_B = testServer("https://co-located-b.example", 49.5, 16.0)
        val FIFTH = testServer("https://fifth.example", 49.7, 16.0)
        val SIXTH = testServer("https://sixth.example", 49.9, 16.0)
        val FARTHEST = testServer("https://farthest.example", 50.5, 16.0)

        val directory = listOf(FARTHEST, CO_LOCATED_B, SECOND, SIXTH, NEAREST, CO_LOCATED_A, FIFTH)
    }
}
