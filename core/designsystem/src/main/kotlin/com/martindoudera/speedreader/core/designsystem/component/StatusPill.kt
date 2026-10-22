package com.martindoudera.speedreader.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.martindoudera.speedreader.core.designsystem.theme.SpeedReaderTheme

/** A small tinted label with an icon, naming what is happening right now; changes of `color` animate. */
@Composable
fun SrStatusPill(icon: ImageVector, text: String, color: Color, modifier: Modifier = Modifier) {
    val spacing = SpeedReaderTheme.spacing
    val tint by animateColorAsState(color, label = "statusPill")
    Row(
        modifier = modifier
            .background(tint.copy(alpha = 0.15f), CircleShape)
            .padding(start = spacing.sm, end = spacing.md, top = spacing.xs, bottom = spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(spacing.xs + spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(SpeedReaderTheme.dimens.iconSmall))
        Text(text, style = MaterialTheme.typography.labelMedium, color = tint)
    }
}

@ThemePreviews
@Composable
private fun StatusPillPreview() {
    ComponentPreview {
        SrStatusPill(icon = Icons.Rounded.Download, text = "Download", color = SpeedReaderTheme.speedColors.download)
    }
}
