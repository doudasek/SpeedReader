plugins {
    id("speedreader.android.library")
    id("speedreader.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.martindoudera.speedreader.core.network"
}

dependencies {
    api(libs.kotlinx.serialization.json)
    api(libs.okhttp)
    api(libs.retrofit)
    api(libs.retrofit.converter.kotlinx.serialization)
    implementation(project(":core:common"))
    testImplementation(libs.kotlinx.coroutines.test)
}
