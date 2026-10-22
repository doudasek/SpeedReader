package com.martindoudera.speedreader.konsist

import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import com.lemonappdev.konsist.api.ext.list.withAnnotationNamed
import com.lemonappdev.konsist.api.ext.list.withParent
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class ComposeTest {
    @Test
    fun `previews are private and end with Preview`() {
        projectScope.functions()
            .filter { it.isPreview() && !it.hasAnnotationWithName("PreviewTest") }
            .assertTrue { it.name.endsWith("Preview") && it.hasPrivateModifier }
    }

    @Test
    fun `multipreview annotations end with Previews`() {
        projectScope.classes()
            .filter { it.hasAnnotationModifier && it.hasAnnotationWithName("Preview") }
            .assertTrue { it.name.endsWith("Previews") }
    }

    @Test
    fun `preview parameter providers end with Provider`() {
        projectScope.classes()
            .withParent { it.name.startsWith("PreviewParameterProvider") }
            .assertTrue { it.name.endsWith("Provider") }
    }

    @Test
    fun `screenshot tests end with Screenshot and live in ScreenshotTests files`() {
        projectScope.functions()
            .withAnnotationNamed("PreviewTest")
            .assertTrue { screenshot ->
                screenshot.name.endsWith("Screenshot") &&
                    screenshot.containingFile.name.endsWith("ScreenshotTests") &&
                    screenshot.containingFile.path.contains("/src/screenshotTest/")
            }
    }

    @Test
    fun `composables are not suffixed Component`() {
        composables().assertFalse { it.name.endsWith("Component") }
    }

    @Test
    fun `composables emitting UI are PascalCase`() {
        composables()
            .filterNot { it.hasReturnType() }
            .assertTrue { it.name.first().isUpperCase() }
    }

    @Test
    fun `public design system composables are prefixed Sr`() {
        composables()
            .filter { it.resideInPackage("$BASE_PACKAGE.core.designsystem.component..") && it.hasPublicOrDefaultModifier }
            .assertTrue { it.name.startsWith("Sr") }
    }

    @Test
    fun `composables emitting UI take a modifier as their first optional parameter`() {
        uiComposables().assertTrue { composable ->
            val firstOptional = composable.parameters.firstOrNull { it.defaultValue != null }
            firstOptional?.name == "modifier" && firstOptional.type.name == "Modifier" && firstOptional.defaultValue == "Modifier"
        }
    }

    @Test
    fun `composable parameters list required before optional ones, except a trailing content slot`() {
        composables().assertTrue { composable ->
            val parameters = composable.parameters.let { all ->
                if (all.lastOrNull()?.type?.text?.startsWith("@Composable") == true) all.dropLast(1) else all
            }
            parameters.dropWhile { it.defaultValue == null }.all { it.defaultValue != null }
        }
    }

    @Test
    fun `composable event parameters are named in the present tense`() {
        composables()
            .flatMap { it.parameters }
            .filter { it.name.startsWith("on") }
            .assertFalse { PAST_TENSE_HANDLER.matches(it.name) }
    }

    @Test
    fun `only screens receive a view model`() {
        composables()
            .filter { composable -> composable.parameters.any { it.type.name.endsWith("ViewModel") } }
            .assertTrue { it.name.endsWith("Screen") }
    }

    private fun composables() = productionScope.functions()
        .withAnnotationNamed("Composable")
        .filterNot { it.isPreview() }

    /** Public and internal composables that draw something; previews, their helpers and the theme only wrap content. */
    private fun uiComposables() = composables().filter { composable ->
        !composable.hasReturnType() &&
            !composable.hasPrivateModifier &&
            !composable.name.endsWith("Preview") &&
            !composable.resideInPackage("..designsystem.theme..")
    }

    private fun KoFunctionDeclaration.isPreview() = annotations.any { it.name in previewAnnotations }

    private companion object {
        /** `@Preview` itself and every multipreview annotation built on it, such as `@ThemePreviews`. */
        val previewAnnotations: Set<String> by lazy {
            projectScope.classes()
                .filter { it.hasAnnotationModifier && it.hasAnnotationWithName("Preview") }
                .map { it.name }
                .toSet() + "Preview"
        }
    }
}
