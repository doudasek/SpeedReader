package com.martindoudera.speedreader.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.martindoudera.speedreader.core.designsystem.theme.SpeedReaderTheme

/** The filled pill button for the main action of a screen state. */
@Composable
fun SrPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = SpeedReaderTheme.dimens.buttonHeight),
        contentPadding = if (icon != null) ButtonDefaults.ButtonWithIconContentPadding else ButtonDefaults.ContentPadding,
    ) {
        ButtonContent(text, icon)
    }
}

/** The outlined pill button for secondary actions such as Stop or Back. */
@Composable
fun SrOutlinedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = SpeedReaderTheme.dimens.buttonHeight),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        contentPadding = if (icon != null) ButtonDefaults.ButtonWithIconContentPadding else ButtonDefaults.ContentPadding,
    ) {
        ButtonContent(text, icon)
    }
}

@Composable
private fun RowScope.ButtonContent(text: String, icon: ImageVector?) {
    if (icon != null) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
    }
    Text(text, style = MaterialTheme.typography.labelLarge)
}

@ThemePreviews
@Composable
private fun ButtonsPreview() {
    ComponentPreview {
        Column {
            SrPrimaryButton(text = "Share", onClick = {}, icon = Icons.Rounded.Share)
            SrOutlinedButton(text = "Stop", onClick = {}, icon = Icons.Rounded.Stop)
        }
    }
}
