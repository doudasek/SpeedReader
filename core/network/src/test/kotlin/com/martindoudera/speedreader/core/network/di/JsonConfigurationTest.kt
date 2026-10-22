package com.martindoudera.speedreader.core.network.di

import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
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
        val decoded = json.decodeFromString<ForwardCompatibleDto>("""{"url":"https://a","fieldAddedLater":42}""")

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

        assertThrows(SerializationException::class.java) {
            json.decodeFromString<List<ForwardCompatibleDto>>(list)
        }
    }

    @Test
    fun unknownEnumValueFallsBackToDeclaredDefault() {
        val decoded = json.decodeFromString<ForwardCompatibleDto>("""{"url":"https://a","kind":"Satellite"}""")

        assertEquals(TestKind.Unknown, decoded.kind)
    }

    @Test
    fun unknownEnumValueFailsWhenThePropertyHasNoDefault() {
        assertThrows(SerializationException::class.java) {
            json.decodeFromString<StrictDto>("""{"url":"https://a","kind":"Satellite"}""")
        }
    }

    @Test
    fun explicitNullFallsBackToDefaultOnNonNullableProperty() {
        val decoded = json.decodeFromString<ForwardCompatibleDto>("""{"url":"https://a","label":null}""")

        assertEquals("fallback", decoded.label)
    }

    @Test
    fun missingNullablePropertyDecodesToNull() {
        val decoded = json.decodeFromString<ForwardCompatibleDto>("""{"url":"https://a"}""")

        assertNull(decoded.note)
    }

    @Test
    fun missingRequiredPropertyFails() {
        assertThrows(MissingFieldException::class.java) {
            json.decodeFromString<ForwardCompatibleDto>("""{"kind":"Residential"}""")
        }
    }
}
