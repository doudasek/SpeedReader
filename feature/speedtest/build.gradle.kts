plugins {
    id("speedreader.android.library")
    id("speedreader.android.compose")
    id("speedreader.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.martindoudera.speedreader.feature.speedtest"
    resourcePrefix = "feature_speedtest_"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.kotlinx.coroutines.test)
}
