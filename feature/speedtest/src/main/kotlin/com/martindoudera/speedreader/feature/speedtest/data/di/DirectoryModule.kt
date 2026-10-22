package com.martindoudera.speedreader.feature.speedtest.data.di

import com.martindoudera.speedreader.feature.speedtest.data.directory.DirectoryTokenApi
import com.martindoudera.speedreader.feature.speedtest.data.directory.SpeedTestDirectoryApi
import com.martindoudera.speedreader.feature.speedtest.data.directory.TokenInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

internal val DIRECTORY_BASE_URL = "https://sp-dir.uwn.com/api/".toHttpUrl()

/** The token API runs on the plain client, everything else on a client with [TokenInterceptor]. */
@Module
@InstallIn(SingletonComponent::class)
internal object DirectoryModule {
    @Provides
    @Singleton
    fun providesDirectoryTokenApi(okHttpClient: OkHttpClient, json: Json): DirectoryTokenApi =
        retrofit(DIRECTORY_BASE_URL, okHttpClient, json).create()

    @Provides
    @Singleton
    fun providesSpeedTestDirectoryApi(okHttpClient: OkHttpClient, tokenInterceptor: TokenInterceptor, json: Json): SpeedTestDirectoryApi =
        retrofit(DIRECTORY_BASE_URL, okHttpClient.newBuilder().addInterceptor(tokenInterceptor).build(), json).create()

    fun retrofit(baseUrl: HttpUrl, okHttpClient: OkHttpClient, json: Json): Retrofit = Retrofit
        .Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}
