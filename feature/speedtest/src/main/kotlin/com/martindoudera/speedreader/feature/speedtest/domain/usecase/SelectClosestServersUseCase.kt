package com.martindoudera.speedreader.feature.speedtest.domain.usecase

import com.martindoudera.speedreader.feature.speedtest.domain.model.GeoPoint
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestServer
import com.martindoudera.speedreader.feature.speedtest.domain.repository.LocationRepository
import javax.inject.Inject
import timber.log.Timber

const val CLOSEST_SERVER_COUNT = 5

/**
 * Picks the `count` servers closest to `from`, or the first `count` in directory order when the
 * location is unknown.
 *
 * Several servers share exact coordinates, so equal distances are ordered by URL to keep the
 * selection stable between runs.
 */
class SelectClosestServersUseCase @Inject constructor(private val locationRepository: LocationRepository) {
    operator fun invoke(servers: List<SpeedTestServer>, from: GeoPoint?, count: Int = CLOSEST_SERVER_COUNT): List<SpeedTestServer> {
        if (from == null) {
            Timber.w("Location unknown, using the first %d servers in directory order", count)
            return servers.take(count)
        }
        val closest = servers
            .map { server -> server to locationRepository.distance(from, server.location) }
            .sortedWith(compareBy({ (_, distance) -> distance }, { (server, _) -> server.url }))
            .take(count)
        Timber.d(
            "Closest servers: %s",
            closest.joinToString { (server, distance) ->
                "${server.provider} ${distance.meters.toInt() / 1000} km"
            },
        )
        return closest.map { (server, _) -> server }
    }
}
