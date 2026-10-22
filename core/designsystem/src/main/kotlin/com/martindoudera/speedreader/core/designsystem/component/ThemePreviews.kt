package com.martindoudera.speedreader.core.designsystem.component

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.martindoudera.speedreader.core.designsystem.theme.SpeedReaderTheme

/** Renders a preview in both the light and the dark theme. */
@Preview(name = "Light", uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES)
annotation class ThemePreviews

@Composable
internal fun ComponentPreview(content: @Composable () -> Unit) {
    SpeedReaderTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(Modifier.padding(SpeedReaderTheme.spacing.lg)) { content() }
        }
    }
}
