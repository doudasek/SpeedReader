package com.martindoudera.speedreader.buildlogic

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.api.variant.SourceDirectories
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

private val coverageExclusions = listOf(
    // Android
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    // Hilt and Dagger
    "**/*_Hilt*.class",
    "**/Hilt_*.class",
    "**/HiltWrapper_*.class",
    "**/*_Factory.class",
    "**/*_Factory\$*.class",
    "**/*_Provide*Factory*.class",
    "**/*_MembersInjector.class",
    "**/*_GeneratedInjector.class",
    "**/*_ComponentTreeDeps.class",
    "**/hilt_aggregated_deps/**",
    "**/dagger/hilt/internal/**",
    // kotlinx.serialization
    "**/*\$\$serializer.class",
    // Compose UI and Android entry points: the screenshot tests render them on layoutlib, which JaCoCo cannot measure
    "**/ComposableSingletons*.class",
    "**/*Previews*.class",
    "**/designsystem/component/**",
    "**/designsystem/theme/**",
    "**/ui/component/**",
    "**/ui/*Screen*.class",
    "**/*Activity.class",
    "**/*Activity\$*.class",
    "**/*Application.class",
)

/**
 * Registers `jacoco{Variant}Report` for the debug variant: runs its unit tests and writes an XML and HTML
 * report to `build/reports/jacoco/`, inspired by Now in Android's `configureJacoco`.
 */
internal fun Project.configureJacoco(commonExtension: CommonExtension, androidComponentsExtension: AndroidComponentsExtension<*, *, *>) {
    apply<JacocoPlugin>()

    val jacocoVersion = libs.findVersion("jacoco").get().toString()
    configure<JacocoPluginExtension> {
        toolVersion = jacocoVersion
    }
    commonExtension.testCoverage.jacocoVersion = jacocoVersion

    // Only debug, otherwise the release build would be made debuggable as well.
    commonExtension.buildTypes.named("debug") {
        enableUnitTestCoverage = true
    }

    androidComponentsExtension.onVariants(androidComponentsExtension.selector().withBuildType("debug")) { variant ->
        val variantName = variant.name.replaceFirstChar(Char::uppercase)
        // A local reference, so that the lazy class directories do not capture the Project for the configuration cache.
        val objects = objects
        val allJars: ListProperty<RegularFile> = objects.listProperty(RegularFile::class.java)
        val allDirectories: ListProperty<Directory> = objects.listProperty(Directory::class.java)

        val reportTask = tasks.register("jacoco${variantName}Report", JacocoReport::class) {
            group = "verification"
            description = "Generates the code coverage report of the $variantName unit tests."
            dependsOn("test${variantName}UnitTest")

            classDirectories.setFrom(
                allJars,
                allDirectories.map { directories ->
                    directories.map { directory -> objects.fileTree().setDir(directory).exclude(coverageExclusions) }
                },
            )

            fun SourceDirectories.Flat?.toFilePaths(): Provider<List<String>> = this
                ?.all
                ?.map { directories -> directories.map { it.asFile.path } }
                ?: provider { emptyList() }
            sourceDirectories.setFrom(
                files(variant.sources.java.toFilePaths(), variant.sources.kotlin.toFilePaths()),
            )

            executionData.setFrom(
                layout.buildDirectory.dir("outputs/unit_test_code_coverage/${variant.name}UnitTest")
                    .map { directory -> directory.asFileTree.matching { include("**/*.exec") } },
            )

            reports {
                xml.required = true
                html.required = true
            }
        }

        variant.artifacts.forScope(ScopedArtifacts.Scope.PROJECT)
            .use(reportTask)
            .toGet(ScopedArtifact.CLASSES, { allJars }, { allDirectories })
    }
}
