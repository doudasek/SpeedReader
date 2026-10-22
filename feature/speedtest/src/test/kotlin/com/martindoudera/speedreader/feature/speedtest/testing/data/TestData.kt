package com.martindoudera.speedreader.feature.speedtest.testing.data

import com.martindoudera.speedreader.core.common.error.NetworkOutcome
import com.martindoudera.speedreader.core.common.error.Outcome
import com.martindoudera.speedreader.feature.speedtest.domain.model.ByteCount
import com.martindoudera.speedreader.feature.speedtest.domain.model.GeoPoint
import com.martindoudera.speedreader.feature.speedtest.domain.model.Latitude
import com.martindoudera.speedreader.feature.speedtest.domain.model.Longitude
import com.martindoudera.speedreader.feature.speedtest.domain.model.ServerUrl
import com.martindoudera.speedreader.feature.speedtest.domain.model.SpeedTestServer
import com.martindoudera.speedreader.feature.speedtest.domain.model.TransferSample
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

val SAMPLE_INTERVAL = 250.milliseconds

fun geoPoint(latitude: Double, longitude: Double) = GeoPoint(Latitude(latitude), Longitude(longitude))

fun testServer(url: String, latitude: Double, longitude: Double) =
    SpeedTestServer(ServerUrl(url), geoPoint(latitude, longitude), provider = null, city = null, countryCode = null)

fun transferSample(tick: Int, totalBytes: Long): NetworkOutcome<TransferSample> =
    Outcome.Success(TransferSample(SAMPLE_INTERVAL * tick, ByteCount(totalBytes)))

/** A transfer moving [bytesPerTick] every [SAMPLE_INTERVAL] until [until]. */
fun steadyTransfer(bytesPerTick: Long, until: Duration): Flow<NetworkOutcome<TransferSample>> {
    val ticks = (until / SAMPLE_INTERVAL).toInt()
    return (1..ticks).map { tick -> transferSample(tick, bytesPerTick * tick) }.asFlow()
}

val testJson = Json { ignoreUnknownKeys = true }

fun httpError(code: Int) = HttpException(Response.error<Unit>(code, "".toResponseBody()))
