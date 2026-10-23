package com.martindoudera.speedreader.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

private val LightColorScheme = lightColorScheme(
    primary = Indigo50,
    onPrimary = Color.White,
    secondary = Teal50,
    onSecondary = Color.White,
    tertiary = Violet60,
    onTertiary = Color.White,
    error = Red50,
    onError = Color.White,
    errorContainer = Red95,
    onErrorContainer = Red50,
    background = Slate98,
    onBackground = Slate10,
    surface = Color.White,
    onSurface = Slate10,
    surfaceVariant = Slate94,
    onSurfaceVariant = Slate40,
    surfaceContainer = Slate94,
    surfaceContainerHigh = Slate90,
    outline = Slate40,
    outlineVariant = Slate85,
    inverseSurface = Slate10,
    inverseOnSurface = Slate98,
)

private val DarkColorScheme = darkColorScheme(
    primary = Indigo80,
    onPrimary = Slate5,
    secondary = Teal80,
    onSecondary = Slate5,
    tertiary = Violet80,
    onTertiary = Slate5,
    error = Red70,
    onError = Slate5,
    errorContainer = Red20,
    onErrorContainer = Red70,
    background = Slate5,
    onBackground = Slate95,
    surface = Slate12,
    onSurface = Slate95,
    surfaceVariant = Slate15,
    onSurfaceVariant = Slate70,
    surfaceContainer = Slate15,
    surfaceContainerHigh = Slate20,
    outline = Slate70,
    outlineVariant = Slate25,
    inverseSurface = Slate95,
    inverseOnSurface = Slate5,
)

private val BrandGradient = listOf(Teal50, Indigo50, Violet60)

private val LightSpeedColors = SpeedColors(download = Teal50, upload = Violet60, highlight = Amber60, brandGradient = BrandGradient)

private val DarkSpeedColors = SpeedColors(download = Teal80, upload = Violet80, highlight = Amber80, brandGradient = BrandGradient)

private val DefaultSpacing = Spacing()

private val DefaultDimens = Dimens()

/**
 * The app theme: Material 3 with the SpeedReader palette, plus [SpeedColors], [Spacing] and [Dimens].
 *
 * Dynamic colour is deliberately not supported, because download and upload are recognised by colour
 * throughout the UI and a wallpaper-derived palette would erase that.
 */
@Composable
fun SpeedReaderTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalSpeedColors provides if (darkTheme) DarkSpeedColors else LightSpeedColors,
        LocalSpacing provides DefaultSpacing,
        LocalDimens provides DefaultDimens,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = SpeedReaderTypography,
            shapes = SpeedReaderShapes,
            content = content,
        )
    }
}

/** Access to the theme values Material 3 does not cover, alongside `MaterialTheme`. */
object SpeedReaderTheme {
    val speedColors: SpeedColors
        @Composable @ReadOnlyComposable
        get() = LocalSpeedColors.current

    val spacing: Spacing
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current

    val dimens: Dimens
        @Composable @ReadOnlyComposable
        get() = LocalDimens.current

    /** The locale the UI is shown in; numbers and dates must be formatted in it, not in the JVM default. */
    val locale: Locale
        @Composable @ReadOnlyComposable
        get() = LocalConfiguration.current.locales[0]
}
