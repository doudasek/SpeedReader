package com.martindoudera.speedreader.feature.speedtest.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** Replaces `Dispatchers.Main` for the duration of a test, so `viewModelScope` runs on the test dispatcher. */
class MainDispatcherRule(private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(testDispatcher)

    override fun finished(description: Description) = Dispatchers.resetMain()
}
