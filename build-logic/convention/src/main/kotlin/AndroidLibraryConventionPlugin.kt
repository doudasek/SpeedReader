import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.martindoudera.speedreader.buildlogic.configureAndroidCommon
import com.martindoudera.speedreader.buildlogic.configureJacoco
import com.martindoudera.speedreader.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")

            extensions.configure<LibraryExtension> {
                configureAndroidCommon()
            }
            configureJacoco(
                commonExtension = extensions.getByType<LibraryExtension>(),
                androidComponentsExtension = extensions.getByType<LibraryAndroidComponentsExtension>(),
            )

            dependencies {
                add("testImplementation", libs.findLibrary("junit").get())
                add("testImplementation", libs.findLibrary("kotlin-test").get())
            }
        }
    }
}
