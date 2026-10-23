plugins {
    id("speedreader.android.application")
    id("speedreader.android.compose")
    id("speedreader.hilt")
}

// Semantic version; minor and patch stay below 100 so that versionCode keeps growing.
val versionMajor = 0
val versionMinor = 0
val versionPatch = 1

android {
    namespace = "com.martindoudera.speedreader"

    defaultConfig {
        applicationId = "com.martindoudera.speedreader"
        versionCode = versionMajor * 10_000 + versionMinor * 100 + versionPatch
        versionName = "$versionMajor.$versionMinor.$versionPatch"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            optimization {
                enable = true
            }
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:speedtest"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.timber)
    testImplementation(libs.junit)
    testImplementation(libs.konsist)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
