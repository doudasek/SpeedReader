plugins {
    id("speedreader.android.library")
    id("speedreader.hilt")
}

android {
    namespace = "com.martindoudera.speedreader.core.common"
}

dependencies {
    api(libs.kotlinx.coroutines.android)
    api(libs.timber)
}
