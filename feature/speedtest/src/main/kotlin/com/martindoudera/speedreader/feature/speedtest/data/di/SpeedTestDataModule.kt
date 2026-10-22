package com.martindoudera.speedreader.feature.speedtest.data.di

import com.martindoudera.speedreader.feature.speedtest.data.directory.DefaultTokenDataSource
import com.martindoudera.speedreader.feature.speedtest.data.directory.TokenDataSource
import com.martindoudera.speedreader.feature.speedtest.data.latency.DefaultLatencyRepository
import com.martindoudera.speedreader.feature.speedtest.data.latency.DefaultPingDataSource
import com.martindoudera.speedreader.feature.speedtest.data.latency.PingDataSource
import com.martindoudera.speedreader.feature.speedtest.data.location.DefaultLocationRepository
import com.martindoudera.speedreader.feature.speedtest.data.server.DefaultServerRepository
import com.martindoudera.speedreader.feature.speedtest.data.throughput.DefaultDownloadDataSource
import com.martindoudera.speedreader.feature.speedtest.data.throughput.DefaultThroughputRepository
import com.martindoudera.speedreader.feature.speedtest.data.throughput.DefaultUploadDataSource
import com.martindoudera.speedreader.feature.speedtest.data.throughput.DownloadDataSource
import com.martindoudera.speedreader.feature.speedtest.data.throughput.UploadDataSource
import com.martindoudera.speedreader.feature.speedtest.domain.repository.LatencyRepository
import com.martindoudera.speedreader.feature.speedtest.domain.repository.LocationRepository
import com.martindoudera.speedreader.feature.speedtest.domain.repository.ServerRepository
import com.martindoudera.speedreader.feature.speedtest.domain.repository.ThroughputRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Dns

@Module
@InstallIn(SingletonComponent::class)
internal interface SpeedTestDataModule {
    @Binds
    fun bindsServerRepository(repository: DefaultServerRepository): ServerRepository

    @Binds
    fun bindsLocationRepository(repository: DefaultLocationRepository): LocationRepository

    @Binds
    fun bindsLatencyRepository(repository: DefaultLatencyRepository): LatencyRepository

    @Binds
    fun bindsThroughputRepository(repository: DefaultThroughputRepository): ThroughputRepository

    @Binds
    fun bindsTokenDataSource(dataSource: DefaultTokenDataSource): TokenDataSource

    @Binds
    fun bindsPingDataSource(dataSource: DefaultPingDataSource): PingDataSource

    @Binds
    fun bindsDownloadDataSource(dataSource: DefaultDownloadDataSource): DownloadDataSource

    @Binds
    fun bindsUploadDataSource(dataSource: DefaultUploadDataSource): UploadDataSource

    companion object {
        @Provides
        fun providesDns(): Dns = Dns.SYSTEM
    }
}
