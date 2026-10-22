package com.martindoudera.speedreader.core.designsystem.adaptive

import androidx.window.core.layout.WindowSizeClass
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

class WindowSizeClassTest {
    @Test
    fun phoneInPortraitUsesOneColumn() {
        val window = WindowSizeClass.compute(412f, 915f)

        val twoPanes = window.prefersTwoPanes()

        assertFalse(twoPanes)
    }

    @Test
    fun phoneInLandscapeUsesTwoPanes() {
        val window = WindowSizeClass.compute(732f, 412f)

        val twoPanes = window.prefersTwoPanes()

        assertTrue(twoPanes)
    }

    @Test
    fun unfoldedFoldableInPortraitUsesOneColumn() {
        val window = WindowSizeClass.compute(673f, 841f)

        val twoPanes = window.prefersTwoPanes()

        assertFalse(twoPanes)
    }

    @Test
    fun tabletInLandscapeUsesTwoPanes() {
        val window = WindowSizeClass.compute(1280f, 800f)

        val twoPanes = window.prefersTwoPanes()

        assertTrue(twoPanes)
    }
}
