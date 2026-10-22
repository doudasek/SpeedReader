package com.martindoudera.speedreader.konsist

import com.lemonappdev.konsist.api.ext.list.modifierprovider.withEnumModifier
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withSealedModifier
import com.lemonappdev.konsist.api.ext.list.withAnnotationNamed
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.ext.list.withParent
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class NamingTest {
    @Test
    fun `files in usecase packages end with UseCase`() {
        productionScope.files
            .withPackage("..usecase")
            .assertTrue { it.name.endsWith("UseCase") }
    }

    @Test
    fun `use cases have an injected constructor and a single operator invoke`() {
        productionScope.classes()
            .withNameEndingWith("UseCase")
            .assertTrue { useCase ->
                val publicFunctions = useCase.functions(includeNested = false).filter { it.hasPublicOrDefaultModifier }
                useCase.primaryConstructor?.hasAnnotationWithName("Inject") == true &&
                    publicFunctions.singleOrNull()?.let { it.name == "invoke" && it.hasOperatorModifier } == true
            }
    }

    @Test
    fun `view models are named ViewModel`() {
        productionScope.classes()
            .withParent { it.name == "ViewModel" }
            .assertTrue { it.name.endsWith("ViewModel") }
    }

    @Test
    fun `classes named ViewModel are Hilt view models`() {
        productionScope.classes()
            .withNameEndingWith("ViewModel")
            .assertTrue { viewModel ->
                viewModel.parents().any { it.name == "ViewModel" } && viewModel.hasAnnotationWithName("HiltViewModel")
            }
    }

    @Test
    fun `view models expose only StateFlow`() {
        productionScope.classes()
            .withNameEndingWith("ViewModel")
            .flatMap { it.properties(includeNested = false) }
            .filter { it.hasPublicOrDefaultModifier }
            .assertTrue { it.type?.name?.startsWith("StateFlow") == true }
    }

    @Test
    fun `event handlers are named in the present tense`() {
        productionScope.classes()
            .withNameEndingWith("ViewModel")
            .flatMap { it.functions(includeNested = false) }
            .filter { it.hasPublicOrDefaultModifier }
            .assertTrue { !PAST_TENSE_HANDLER.matches(it.name) }
    }

    @Test
    fun `dependencies are named after their type`() {
        productionScope.classes()
            .flatMap { it.primaryConstructor?.parameters.orEmpty() }
            .filter { parameter -> NAMED_BY_TYPE_SUFFIXES.any { parameter.type.name.endsWith(it) } }
            .assertTrue { it.name == it.type.name.removeSuffix("UseCase").replaceFirstChar(Char::lowercase) }
    }

    @Test
    fun `enum entries are PascalCase`() {
        projectScope.classes()
            .withEnumModifier()
            .flatMap { it.enumConstants }
            .assertTrue { PASCAL_CASE.matches(it.name) || it.name in ACRONYMS }
    }

    @Test
    fun `sealed subtypes are PascalCase`() {
        val sealedTypes = projectScope.classes().withSealedModifier() + projectScope.interfaces().withSealedModifier()
        sealedTypes
            .flatMap { it.classes(includeNested = false) + it.objects(includeNested = false) }
            .assertTrue { PASCAL_CASE.matches(it.name) }
    }

    @Test
    fun `hilt modules are internal, installed and live in di`() {
        productionScope.objects()
            .withAnnotationNamed("Module")
            .assertTrue {
                it.name.endsWith("Module") && it.resideInPackage("..di") && it.hasInternalModifier &&
                    it.hasAnnotationWithName("InstallIn")
            }
        productionScope.interfaces()
            .withAnnotationNamed("Module")
            .assertTrue {
                it.name.endsWith("Module") && it.resideInPackage("..di") && it.hasInternalModifier &&
                    it.hasAnnotationWithName("InstallIn")
            }
        productionScope.classes()
            .withAnnotationNamed("Module")
            .assertTrue {
                it.name.endsWith("Module") && it.resideInPackage("..di") && it.hasInternalModifier &&
                    it.hasAnnotationWithName("InstallIn")
            }
    }

    @Test
    fun `hilt bindings are named after the type they bind`() {
        productionScope.functions()
            .withAnnotationNamed("Binds")
            .assertTrue { it.name == "binds${it.returnType?.name}" }
        productionScope.functions()
            .withAnnotationNamed("Provides")
            .assertTrue { it.name.startsWith("provides") }
    }

    private companion object {
        val NAMED_BY_TYPE_SUFFIXES = listOf("UseCase", "Repository", "DataSource")

        /** Acronyms keep their established form, see notes on naming conventions. */
        val ACRONYMS = setOf("IO")
    }
}
