package com.martindoudera.speedreader.konsist

import com.lemonappdev.konsist.api.declaration.KoObjectDeclaration
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withParent
import com.lemonappdev.konsist.api.provider.KoDeclarationProvider
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class DeclarationTest {
    @Test
    fun `companion object is the last declaration`() {
        projectScope.objects()
            .filter { it.hasCompanionModifier }
            .assertTrue { companion ->
                val container = companion.containingDeclaration as? KoDeclarationProvider
                val last = container?.declarations(includeNested = false, includeLocal = false)?.lastOrNull()
                last is KoObjectDeclaration && last.hasCompanionModifier
            }
    }

    @Test
    fun `ui state classes are data classes`() {
        productionScope.classes()
            .withNameEndingWith("UiState")
            .assertTrue { it.hasDataModifier && it.resideInPackage("..ui..") }
    }

    @Test
    fun `dtos are internal serializable data classes`() {
        productionScope.classes()
            .withNameEndingWith("Dto")
            .assertTrue { it.hasDataModifier && it.hasInternalModifier && it.hasAnnotationWithName("Serializable") }
    }

    @Test
    fun `logging goes through Timber without hand-written tags`() {
        productionScope.files.assertFalse { file ->
            file.hasImport { it.name == "android.util.Log" } || file.text.contains("println(") || file.text.contains("Timber.tag(")
        }
    }

    @Test
    fun `production code does not block on coroutines`() {
        productionScope.files.assertFalse { file -> file.hasImport { it.name == "kotlinx.coroutines.runBlocking" } }
    }

    @Test
    fun `project types are imported, not fully qualified`() {
        projectScope.files.assertFalse { file ->
            file.text.lines()
                .map { it.trim() }
                .filterNot {
                    it.startsWith("import ") || it.startsWith("package ") || it.startsWith("*") || it.startsWith("//") ||
                        it.startsWith("/*")
                }
                .any { it.contains("$BASE_PACKAGE.") }
        }
    }

    @Test
    fun `test classes end with Test`() {
        testScope.classes()
            .filter { testClass -> testClass.functions(includeNested = false).any { it.hasAnnotationWithName("Test") } }
            .assertTrue { it.name.endsWith("Test") }
    }

    @Test
    fun `view model tests replace the main dispatcher`() {
        testScope.classes()
            .withNameEndingWith("ViewModelTest")
            .assertTrue { test ->
                test.properties(includeNested = false).any {
                    it.text.contains("MainDispatcherRule()") &&
                        it.text.contains("@get:Rule")
                }
            }
    }

    @Test
    fun `fakes are named Fake and shared from the testing package`() {
        testScope.classes()
            .withParent { parent -> FAKED_SUFFIXES.any { parent.name.endsWith(it) } }
            .assertTrue { it.name.startsWith("Fake") && it.resideInPackage("..testing..") }
    }

    private companion object {
        val FAKED_SUFFIXES = listOf("Repository", "DataSource", "Api")
    }
}
