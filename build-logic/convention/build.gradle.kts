plugins {
    `kotlin-dsl`
}

group = "com.martindoudera.speedreader.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kover.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "speedreader.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "speedreader.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "speedreader.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("hilt") {
            id = "speedreader.hilt"
            implementationClass = "HiltConventionPlugin"
        }
        register("kover") {
            id = "speedreader.kover"
            implementationClass = "KoverConventionPlugin"
        }
        register("androidScreenshot") {
            id = "speedreader.android.screenshot"
            implementationClass = "AndroidScreenshotConventionPlugin"
        }
    }
}
