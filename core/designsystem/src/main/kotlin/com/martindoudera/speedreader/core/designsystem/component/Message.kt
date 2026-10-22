package com.martindoudera.speedreader.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.martindoudera.speedreader.core.designsystem.R
import com.martindoudera.speedreader.core.designsystem.theme.SpeedReaderTheme

/** A centred illustration with a title and an optional explanation, for outcomes such as a finished test or an error. */
@Composable
fun SrMessage(illustration: Painter, title: String, modifier: Modifier = Modifier, text: String? = null) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painter = illustration, contentDescription = null, modifier = Modifier.width(SpeedReaderTheme.dimens.illustration))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = SpeedReaderTheme.spacing.lg),
        )
        if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = SpeedReaderTheme.spacing.xs).widthIn(max = 280.dp),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun MessagePreview() {
    ComponentPreview {
        SrMessage(
            illustration = painterResource(R.drawable.core_designsystem_illustration_disconnected),
            title = "You're offline",
            text = "Check your Wi-Fi or mobile data and try again.",
        )
    }
}
