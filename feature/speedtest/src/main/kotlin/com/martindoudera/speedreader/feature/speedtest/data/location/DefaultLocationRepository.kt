package com.martindoudera.speedreader.feature.speedtest.data.location

import android.location.Location
import com.martindoudera.speedreader.core.common.error.NetworkOutcome
import com.martindoudera.speedreader.core.common.error.onFailure
import com.martindoudera.speedreader.core.network.safeApiCall
import com.martindoudera.speedreader.feature.speedtest.data.directory.SpeedTestDirectoryApi
import com.martindoudera.speedreader.feature.speedtest.data.directory.dto.IpLocationDto
import com.martindoudera.speedreader.feature.speedtest.domain.model.Distance
import com.martindoudera.speedreader.feature.speedtest.domain.model.GeoPoint
import com.martindoudera.speedreader.feature.speedtest.domain.model.Latitude
import com.martindoudera.speedreader.feature.speedtest.domain.model.Longitude
import com.martindoudera.speedreader.feature.speedtest.domain.repository.LocationRepository
import javax.inject.Inject
import kotlinx.serialization.SerializationException
import timber.log.Timber

/**
 * Locates the phone by the directory's geolocation of its public IP; no device location and no
 * permission. Distances come from [Location.distanceBetween], on the WGS 84 ellipsoid.
 */
internal class DefaultLocationRepository @Inject constructor(private val api: SpeedTestDirectoryApi) : LocationRepository {
    override suspend fun currentLocation(): NetworkOutcome<GeoPoint> {
        Timber.d("Requesting the IP location")
        return safeApiCall { api.clientLocation().toGeoPoint() }
            .onFailure { error -> Timber.w("IP location unavailable: %s", error) }
    }

    override fun distance(from: GeoPoint, to: GeoPoint): Distance {
        val results = FloatArray(1)
        Location.distanceBetween(from.latitude.degrees, from.longitude.degrees, to.latitude.degrees, to.longitude.degrees, results)
        return Distance(results[0].toDouble())
    }

    private fun IpLocationDto.toGeoPoint(): GeoPoint = try {
        if (lat == 0.0 && lon == 0.0) throw SerializationException("IP not located, (0, 0) is a placeholder")
        GeoPoint(Latitude(lat), Longitude(lon)).also { Timber.d("Located by IP in %s", countryCode) }
    } catch (e: IllegalArgumentException) {
        throw SerializationException("IP location out of range", e)
    }
}
