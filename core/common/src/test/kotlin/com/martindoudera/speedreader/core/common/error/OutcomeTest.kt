package com.martindoudera.speedreader.core.common.error

import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class OutcomeTest {
    @Test
    fun onSuccessSeesTheValueAndPassesTheOutcomeOn() {
        val success: Outcome<Int, String> = Outcome.Success(42)
        var seen: Int? = null

        val result = success.onSuccess { seen = it }

        assertEquals(42, seen)
        assertEquals(success, result)
    }

    @Test
    fun onSuccessIsSkippedOnFailure() {
        val failure: Outcome<Int, String> = Outcome.Failure("offline")
        var seen: Int? = null

        val result = failure.onSuccess { seen = it }

        assertNull(seen)
        assertEquals(failure, result)
    }

    @Test
    fun onFailureSeesTheErrorAndPassesTheOutcomeOn() {
        val failure: Outcome<Int, String> = Outcome.Failure("offline")
        var seen: String? = null

        val result = failure.onFailure { seen = it }

        assertEquals("offline", seen)
        assertEquals(failure, result)
    }

    @Test
    fun onFailureIsSkippedOnSuccess() {
        val success: Outcome<Int, String> = Outcome.Success(42)
        var seen: String? = null

        val result = success.onFailure { seen = it }

        assertNull(seen)
        assertEquals(success, result)
    }
}
