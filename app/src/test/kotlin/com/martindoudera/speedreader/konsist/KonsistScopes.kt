package com.martindoudera.speedreader.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.container.KoScope

internal const val BASE_PACKAGE = "com.martindoudera.speedreader"

internal val PASCAL_CASE = Regex("^[A-Z][A-Za-z0-9]*$")

/** `onClicked`, `onStartClicked`: handlers name the event in the present tense, `onStartClick`. */
internal val PAST_TENSE_HANDLER = Regex("^on\\w*(Clicked|Changed|Selected|Checked|Pressed|Dismissed|Tapped)$")

/** Parsing the project takes a few seconds, so every rule shares one scope per source-set kind. */
internal val projectScope: KoScope by lazy { Konsist.scopeFromProject() }

internal val productionScope: KoScope by lazy { Konsist.scopeFromProduction() }

internal val testScope: KoScope by lazy { Konsist.scopeFromTest() }

/** Root packages of every feature, such as `com.martindoudera.speedreader.feature.speedtest`. */
internal val featurePackages: List<String> by lazy {
    productionScope.files
        .mapNotNull { it.packagee?.name }
        .filter { it.startsWith("$BASE_PACKAGE.feature.") }
        .map { it.split('.').take(5).joinToString(".") }
        .distinct()
}
