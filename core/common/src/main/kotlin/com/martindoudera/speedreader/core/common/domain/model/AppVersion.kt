package com.martindoudera.speedreader.core.common.domain.model

/**
 * The installed app's version name, e.g. `0.0.1` or `0.0.1-debug`. A data class rather than a value class,
 * because Dagger cannot inject value classes.
 */
data class AppVersion(val name: String)
