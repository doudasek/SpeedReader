package com.martindoudera.speedreader.konsist

import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.ext.list.withParent
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * The build graph keeps `:core:*` away from `:feature:*`; these rules guard what Gradle cannot see, the
 * `data` → `domain` ← `ui` layering inside a feature module.
 */
class ArchitectureTest {
    @Test
    fun `core modules depend only on core common`() {
        productionScope.assertArchitecture {
            val common = Layer("Common", "$BASE_PACKAGE.core.common..")
            val network = Layer("Network", "$BASE_PACKAGE.core.network..")
            val designSystem = Layer("DesignSystem", "$BASE_PACKAGE.core.designsystem..")
            val feature = Layer("Feature", "$BASE_PACKAGE.feature..")

            common.dependsOnNothing()
            network.dependsOn(common)
            designSystem.dependsOnNothing()
            feature.dependsOn(common, network, designSystem)
        }
    }

    @Test
    fun `feature layers depend only on the domain`() {
        featurePackages.forEach { feature ->
            productionScope.assertArchitecture {
                val domain = Layer("Domain", "$feature.domain..")
                val data = Layer("Data", "$feature.data..")
                val ui = Layer("Ui", "$feature.ui..")

                domain.dependsOnNothing()
                data.dependsOn(domain)
                ui.dependsOn(domain)
            }
        }
    }

    @Test
    fun `domain stays plain Kotlin`() {
        productionScope.files
            .withPackage("$BASE_PACKAGE.feature..domain..")
            .assertFalse { it.importsAnyOf(FORBIDDEN_IN_DOMAIN) }
    }

    @Test
    fun `ui knows nothing about the network stack`() {
        productionScope.files
            .withPackage("$BASE_PACKAGE.feature..ui..")
            .assertFalse { it.importsAnyOf(NETWORK_STACK + "$BASE_PACKAGE.core.network.") }
    }

    @Test
    fun `data knows nothing about Compose`() {
        productionScope.files
            .withPackage("$BASE_PACKAGE.feature..data..", "$BASE_PACKAGE.core.network..", "$BASE_PACKAGE.core.common..")
            .assertFalse { it.importsAnyOf(listOf("androidx.compose.", "$BASE_PACKAGE.core.designsystem.")) }
    }

    @Test
    fun `repository interfaces live in domain repository`() {
        productionScope.interfaces()
            .withNameEndingWith("Repository")
            .assertTrue { it.resideInPackage("..domain.repository") }
    }

    @Test
    fun `repository implementations are internal Default classes in data`() {
        productionScope.classes()
            .withParent { it.name.endsWith("Repository") }
            .assertTrue { repository ->
                val contract = repository.parents().first { it.name.endsWith("Repository") }
                repository.name == "Default${contract.name}" &&
                    repository.resideInPackage("..data..") &&
                    repository.hasInternalModifier
            }
    }

    @Test
    fun `data sources are internal interfaces in data`() {
        productionScope.interfaces()
            .withNameEndingWith("DataSource")
            .assertTrue { it.resideInPackage("..data..") && it.hasInternalModifier }
    }

    @Test
    fun `data source implementations are internal Default classes`() {
        productionScope.classes()
            .withParent { it.name.endsWith("DataSource") }
            .assertTrue { dataSource ->
                val contract = dataSource.parents().first { it.name.endsWith("DataSource") }
                dataSource.name == "Default${contract.name}" && dataSource.hasInternalModifier
            }
    }

    @Test
    fun `use cases live in domain usecase`() {
        productionScope.classes()
            .withNameEndingWith("UseCase")
            .assertTrue { it.resideInPackage("..domain.usecase") }
    }

    @Test
    fun `view models live in ui`() {
        productionScope.classes()
            .withNameEndingWith("ViewModel")
            .assertTrue { it.resideInPackage("..ui..") }
    }

    @Test
    fun `dtos and retrofit apis live in data`() {
        productionScope.classes()
            .withNameEndingWith("Dto")
            .assertTrue { it.resideInPackage("..data..dto") }
        productionScope.interfaces()
            .withNameEndingWith("Api")
            .assertTrue { it.resideInPackage("..data..") && it.hasInternalModifier }
    }

    private fun KoFileDeclaration.importsAnyOf(prefixes: List<String>) = hasImport { import -> prefixes.any { import.name.startsWith(it) } }

    private companion object {
        val NETWORK_STACK = listOf("okhttp3.", "retrofit2.", "kotlinx.serialization.")

        val FORBIDDEN_IN_DOMAIN = NETWORK_STACK + listOf(
            "android.",
            "androidx.",
            "dagger.",
            "$BASE_PACKAGE.core.network.",
            "$BASE_PACKAGE.core.designsystem.",
        )
    }
}
