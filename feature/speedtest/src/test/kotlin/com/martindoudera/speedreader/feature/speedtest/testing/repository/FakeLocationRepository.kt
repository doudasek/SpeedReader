package com.martindoudera.speedreader.feature.speedtest.testing.repository

import com.martindoudera.speedreader.core.common.error.NetworkError
import com.martindoudera.speedreader.core.common.error.NetworkOutcome
import com.martindoudera.speedreader.core.common.error.Outcome
import com.martindoudera.speedreader.feature.speedtest.domain.model.Distance
import com.martindoudera.speedreader.feature.speedtest.domain.model.GeoPoint
import com.martindoudera.speedreader.feature.speedtest.domain.repository.LocationRepository
import kotlin.math.hypot

/** Distances are flat-plane approximations (1° = 111 km), enough to order points in tests. */
class FakeLocationRepository : LocationRepository {
    var location: NetworkOutcome<GeoPoint> = Outcome.Failure(NetworkError.NoConnection(null))

    override suspend fun currentLocation(): NetworkOutcome<GeoPoint> = location

    override fun distance(from: GeoPoint, to: GeoPoint): Distance {
        val degrees = hypot(to.latitude.degrees - from.latitude.degrees, to.longitude.degrees - from.longitude.degrees)
        return Distance(degrees * 111_000)
    }
}
