package com.martindoudera.speedreader.feature.speedtest.data.di

import com.martindoudera.speedreader.feature.speedtest.data.directory.TokenInterceptor
import com.martindoudera.speedreader.feature.speedtest.data.throughput.MeasurementApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.create

/**
 * The measurement client pins HTTP/1.1, so N parallel requests are N TCP connections. The servers
 * speak only HTTP/1.1 today; over HTTP/2 OkHttp would multiplex all of them onto one connection.
 * Calls carry absolute URLs, so the base URL is a placeholder Retrofit requires.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object MeasurementModule {
    @Provides
    @Singleton
    fun providesMeasurementApi(okHttpClient: OkHttpClient, tokenInterceptor: TokenInterceptor, json: Json): MeasurementApi {
        val client = okHttpClient.newBuilder().protocols(listOf(Protocol.HTTP_1_1)).addInterceptor(tokenInterceptor).build()
        return DirectoryModule.retrofit(DIRECTORY_BASE_URL, client, json).create()
    }
}
