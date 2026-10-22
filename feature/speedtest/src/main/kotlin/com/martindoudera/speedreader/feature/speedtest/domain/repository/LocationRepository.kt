package com.martindoudera.speedreader.feature.speedtest.domain.repository

import com.martindoudera.speedreader.core.common.error.NetworkOutcome
import com.martindoudera.speedreader.feature.speedtest.domain.model.Distance
import com.martindoudera.speedreader.feature.speedtest.domain.model.GeoPoint

/**
 * Locates the phone by the geolocation of its public IP address, as reported by the Ubiquiti
 * directory, and measures distances between positions.
 */
interface LocationRepository {
    suspend fun currentLocation(): NetworkOutcome<GeoPoint>

    fun distance(from: GeoPoint, to: GeoPoint): Distance
}
