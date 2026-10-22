package com.martindoudera.speedreader.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.martindoudera.speedreader.core.designsystem.theme.SpeedReaderTheme

/**
 * A full-width pill with an icon, a title, a subtitle and an optional trailing tag.
 * While `loading` is true the texts are replaced by shimmering placeholders of the same height.
 */
@Composable
fun SrInfoChip(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    tag: String? = null,
    loading: Boolean = false,
) {
    val spacing = SpeedReaderTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .padding(start = spacing.sm, top = spacing.sm, bottom = spacing.sm, end = spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(SpeedReaderTheme.dimens.chipIconContainer)
                .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(SpeedReaderTheme.dimens.iconMedium),
            )
        }
        if (loading) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                ShimmerLine(widthFraction = 1f)
                ShimmerLine(widthFraction = 0.55f)
            }
        } else {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            if (tag != null) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                        .padding(horizontal = spacing.sm, vertical = spacing.xs),
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun InfoChipPreview() {
    ComponentPreview {
        Column(verticalArrangement = Arrangement.spacedBy(SpeedReaderTheme.spacing.md)) {
            SrInfoChip(icon = Icons.Rounded.Dns, title = "CETIN a.s.", subtitle = "Prague, CZ", tag = "6 connections")
            SrInfoChip(icon = Icons.Rounded.Dns, title = "", subtitle = "", loading = true)
        }
    }
}
