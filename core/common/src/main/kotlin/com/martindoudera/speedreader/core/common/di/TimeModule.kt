package com.martindoudera.speedreader.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlin.time.TimeSource

@Module
@InstallIn(SingletonComponent::class)
internal object TimeModule {
    @Provides
    fun providesTimeSource(): TimeSource = TimeSource.Monotonic
}
