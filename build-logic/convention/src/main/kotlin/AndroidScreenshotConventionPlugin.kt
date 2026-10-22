import com.android.build.api.dsl.CommonExtension
import com.martindoudera.speedreader.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** Compose Preview Screenshot Testing: `@PreviewTest` previews in `src/screenshotTest` rendered on the JVM by layoutlib. */
class AndroidScreenshotConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.compose.screenshot")

            val android = extensions.getByName("android") as CommonExtension
            android.experimentalProperties["android.experimental.enableScreenshotTest"] = true

            dependencies {
                add("screenshotTestImplementation", libs.findLibrary("screenshot-validation-api").get())
                add("screenshotTestImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
            }
        }
    }
}
