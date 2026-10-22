package com.martindoudera.speedreader.core.designsystem.adaptive

import androidx.window.core.layout.WindowSizeClass

/**
 * Whether a screen should lay out two panes side by side rather than one column: from the expanded
 * width (840 dp), or from the medium width (600 dp) when the window is also short, as a phone in
 * landscape is.
 */
fun WindowSizeClass.prefersTwoPanes(): Boolean = isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ||
    (
        isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) &&
            !isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
        )
