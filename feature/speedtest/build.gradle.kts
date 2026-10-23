plugins {
    id("speedreader.android.library")
    id("speedreader.android.compose")
    id("speedreader.android.screenshot")
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
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.timber)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver3)
}
