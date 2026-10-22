package com.martindoudera.speedreader.core.network.di

import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.junit.Test

enum class TestKind { Unknown, Residential, DataCentre }

@Serializable
data class ForwardCompatibleDto(
    val url: String,
    val kind: TestKind = TestKind.Unknown,
    val label: String = "fallback",
    val note: String? = null,
)

@Serializable
data class StrictDto(val url: String, val kind: TestKind)

class JsonConfigurationTest {
    private val json = NetworkModule.providesJson()

    @Test
    fun unknownKeysAreIgnored() {
        val withNewField = """{"url":"https://a","fieldAddedLater":42}"""

        val decoded = json.decodeFromString<ForwardCompatibleDto>(withNewField)

        assertEquals("https://a", decoded.url)
    }

    @Test
    fun unknownNestedObjectsAndArraysAreIgnored() {
        val withNewBlock = """{"url":"https://a","meta":{"added":1,"nested":[1,2,{"deep":null}]},"tags":["x","y"]}"""

        val decoded = json.decodeFromString<ForwardCompatibleDto>(withNewBlock)

        assertEquals("https://a", decoded.url)
    }

    @Test
    fun oneMalformedEntryFailsTheWholeList() {
        val list = """[{"url":"https://a"},{"url":42}]"""

        val result = runCatching { json.decodeFromString<List<ForwardCompatibleDto>>(list) }

        assertIs<SerializationException>(result.exceptionOrNull())
    }

    @Test
    fun unknownEnumValueFallsBackToDeclaredDefault() {
        val withNewEnumValue = """{"url":"https://a","kind":"Satellite"}"""

        val decoded = json.decodeFromString<ForwardCompatibleDto>(withNewEnumValue)

        assertEquals(TestKind.Unknown, decoded.kind)
    }

    @Test
    fun unknownEnumValueFailsWhenThePropertyHasNoDefault() {
        val withNewEnumValue = """{"url":"https://a","kind":"Satellite"}"""

        val result = runCatching { json.decodeFromString<StrictDto>(withNewEnumValue) }

        assertIs<SerializationException>(result.exceptionOrNull())
    }

    @Test
    fun explicitNullFallsBackToDefaultOnNonNullableProperty() {
        val withNullLabel = """{"url":"https://a","label":null}"""

        val decoded = json.decodeFromString<ForwardCompatibleDto>(withNullLabel)

        assertEquals("fallback", decoded.label)
    }

    @Test
    fun missingNullablePropertyDecodesToNull() {
        val withoutNote = """{"url":"https://a"}"""

        val decoded = json.decodeFromString<ForwardCompatibleDto>(withoutNote)

        assertNull(decoded.note)
    }

    @Test
    fun missingRequiredPropertyFails() {
        val withoutUrl = """{"kind":"Residential"}"""

        val result = runCatching { json.decodeFromString<ForwardCompatibleDto>(withoutUrl) }

        assertIs<MissingFieldException>(result.exceptionOrNull())
    }
}
