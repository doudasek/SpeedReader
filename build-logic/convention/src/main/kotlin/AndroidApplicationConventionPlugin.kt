import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.martindoudera.speedreader.buildlogic.TARGET_SDK
import com.martindoudera.speedreader.buildlogic.configureAndroidCommon
import com.martindoudera.speedreader.buildlogic.configureJacoco
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")

            extensions.configure<ApplicationExtension> {
                configureAndroidCommon()
                defaultConfig.targetSdk = TARGET_SDK
            }
            configureJacoco(
                commonExtension = extensions.getByType<ApplicationExtension>(),
                androidComponentsExtension = extensions.getByType<ApplicationAndroidComponentsExtension>(),
            )
        }
    }
}
