package com.martindoudera.speedreader.core.common.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: SpeedReaderDispatcher)

enum class SpeedReaderDispatcher {
    Default,
    IO,
}
