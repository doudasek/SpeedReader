package com.martindoudera.speedreader.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.martindoudera.speedreader.core.designsystem.theme.SpeedReaderTheme

/** A whole-row checkbox with a title and a one-line explanation; tapping anywhere on the card toggles it. */
@Composable
fun SrCheckboxRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit, title: String, description: String, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(SpeedReaderTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = null, modifier = Modifier.padding(SpeedReaderTheme.spacing.md))
        Spacer(Modifier.width(SpeedReaderTheme.spacing.xs))
        Column(Modifier.weight(1f).padding(vertical = SpeedReaderTheme.spacing.sm)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(SpeedReaderTheme.spacing.lg))
    }
}

@ThemePreviews
@Composable
private fun CheckboxRowPreview() {
    ComponentPreview {
        SrCheckboxRow(
            checked = true,
            onCheckedChange = {},
            title = "Single connection only",
            description = "Measure one transfer instead of 6 in parallel",
        )
    }
}
