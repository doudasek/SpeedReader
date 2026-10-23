import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

private const val COVERAGE_VARIANT = "coverage"

private val coverageExclusions = listOf(
    // Hilt and Dagger
    "*_Hilt*",
    "*.Hilt_*",
    "*.HiltWrapper_*",
    "*_Factory",
    "*_Factory$*",
    "*_Provide*Factory*",
    "*_MembersInjector",
    "*_GeneratedInjector",
    "*_ComponentTreeDeps",
    "hilt_aggregated_deps.*",
    "dagger.hilt.internal.*",
    // kotlinx.serialization
    "*$\$serializer",
    // Compose UI and Android entry points: the screenshot tests render them on layoutlib, which Kover cannot measure
    "*ComposableSingletons*",
    "*Previews*",
    "*.designsystem.component.*",
    "*.designsystem.theme.*",
    "*.ui.component.*",
    "*.ui.*Screen*",
    "*Activity",
    "*Activity$*",
    "*Application",
)

/** Kover code coverage of the unit tests, with generated code and the screenshot-tested UI left out. */
class KoverConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlinx.kover")

            extensions.configure<KoverProjectExtension> {
                // The root project merges the same-named variant of every module, so the debug variant needs a custom name.
                currentProject.createVariant(COVERAGE_VARIANT) {
                    add("debug", optional = true)
                }
                reports.filters.excludes {
                    androidGeneratedClasses()
                    annotatedBy("dagger.Module")
                    classes(coverageExclusions)
                }
            }
        }
    }
}
