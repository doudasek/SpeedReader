package com.martindoudera.speedreader.di

import com.martindoudera.speedreader.BuildConfig
import com.martindoudera.speedreader.core.common.domain.model.AppVersion
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object AppVersionModule {
    @Provides
    fun providesAppVersion(): AppVersion = AppVersion(BuildConfig.VERSION_NAME)
}
