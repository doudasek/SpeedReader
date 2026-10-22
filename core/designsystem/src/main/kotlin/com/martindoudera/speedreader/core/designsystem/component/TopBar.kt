package com.martindoudera.speedreader.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.martindoudera.speedreader.core.designsystem.R
import com.martindoudera.speedreader.core.designsystem.theme.SpeedReaderTheme

/** The app's top bar: logo and app name, no actions. */
@Composable
fun SrTopBar(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().height(SpeedReaderTheme.dimens.topBarHeight),
        horizontalArrangement = Arrangement.spacedBy(SpeedReaderTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painterResource(R.drawable.core_designsystem_logo),
            contentDescription = null,
            modifier = Modifier.size(SpeedReaderTheme.dimens.logo),
        )
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
}

@ThemePreviews
@Composable
private fun TopBarPreview() {
    ComponentPreview {
        SrTopBar(title = "SpeedReader")
    }
}
