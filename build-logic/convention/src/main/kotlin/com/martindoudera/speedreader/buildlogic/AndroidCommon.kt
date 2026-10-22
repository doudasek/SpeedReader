package com.martindoudera.speedreader.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal const val COMPILE_SDK = 37
internal const val MIN_SDK = 26
internal const val TARGET_SDK = 37

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun CommonExtension.configureAndroidCommon() {
    compileSdk {
        version = release(COMPILE_SDK)
    }
    defaultConfig.minSdk = MIN_SDK
    compileOptions.sourceCompatibility = JavaVersion.VERSION_11
    compileOptions.targetCompatibility = JavaVersion.VERSION_11
}
