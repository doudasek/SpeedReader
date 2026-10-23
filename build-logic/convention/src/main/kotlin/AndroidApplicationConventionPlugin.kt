import com.android.build.api.dsl.ApplicationExtension
import com.martindoudera.speedreader.buildlogic.TARGET_SDK
import com.martindoudera.speedreader.buildlogic.configureAndroidCommon
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("speedreader.kover")

            extensions.configure<ApplicationExtension> {
                configureAndroidCommon()
                defaultConfig.targetSdk = TARGET_SDK
            }
        }
    }
}
