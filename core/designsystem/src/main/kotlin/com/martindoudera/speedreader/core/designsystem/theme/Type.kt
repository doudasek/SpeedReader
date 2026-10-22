package com.martindoudera.speedreader.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private const val TABULAR_FIGURES = "tnum"

/**
 * Material 3 type scale on the system font. The styles that show measured numbers use tabular figures,
 * so a live value does not shift sideways as its digits change.
 */
internal val SpeedReaderTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 72.sp,
            lineHeight = 72.sp,
            letterSpacing = (-2).sp,
            fontFeatureSettings = TABULAR_FIGURES,
        ),
        headlineMedium = headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontFeatureSettings = TABULAR_FIGURES,
        ),
        headlineSmall = headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontFeatureSettings = TABULAR_FIGURES,
        ),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
        titleSmall = titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
        labelMedium = labelMedium.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontFeatureSettings = TABULAR_FIGURES,
        ),
        labelSmall = labelSmall.copy(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
    )
}
