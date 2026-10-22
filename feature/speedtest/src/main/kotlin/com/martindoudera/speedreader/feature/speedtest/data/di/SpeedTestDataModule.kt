package com.martindoudera.speedreader.feature.speedtest.data.di

import com.martindoudera.speedreader.feature.speedtest.data.directory.DefaultTokenDataSource
import com.martindoudera.speedreader.feature.speedtest.data.directory.TokenDataSource
import com.martindoudera.speedreader.feature.speedtest.data.server.DefaultServerRepository
import com.martindoudera.speedreader.feature.speedtest.domain.repository.ServerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface SpeedTestDataModule {
    @Binds
    fun bindsServerRepository(repository: DefaultServerRepository): ServerRepository

    @Binds
    fun bindsTokenDataSource(dataSource: DefaultTokenDataSource): TokenDataSource
}
