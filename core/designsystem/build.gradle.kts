plugins {
    id("speedreader.android.library")
    id("speedreader.android.compose")
}

android {
    namespace = "com.martindoudera.speedreader.core.designsystem"
    resourcePrefix = "core_designsystem_"
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material3.adaptive)
    api(libs.androidx.compose.material3.windowSizeClass)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
