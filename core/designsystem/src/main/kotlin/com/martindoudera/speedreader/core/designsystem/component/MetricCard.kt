package com.martindoudera.speedreader.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.NetworkPing
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.martindoudera.speedreader.core.designsystem.theme.SpeedReaderTheme

/** Where a [SrMetricCard]'s value stands: not measured yet, being measured, or final. */
enum class MetricStatus { Idle, Live, Done }

/**
 * A card with one measured value: an icon and label on top, the value with its unit, and a caption.
 * A `Live` card is tinted with `accent` and its caption gets a blinking dot; a `Done` caption gets a check mark.
 */
@Composable
fun SrMetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    accent: Color,
    modifier: Modifier = Modifier,
    status: MetricStatus = MetricStatus.Idle,
    caption: String? = null,
    emphasized: Boolean = false,
) {
    val spacing = SpeedReaderTheme.spacing
    val surface = MaterialTheme.colorScheme.surface
    val live = status == MetricStatus.Live
    val container by animateColorAsState(if (live) accent.copy(alpha = 0.1f).compositeOver(surface) else surface, label = "container")
    val border by animateColorAsState(if (live) accent.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outlineVariant, label = "border")
    val shape = MaterialTheme.shapes.medium

    Column(
        modifier = modifier
            .defaultMinSize(minHeight = SpeedReaderTheme.dimens.metricCardMinHeight)
            .clip(shape)
            .background(container)
            .border(1.dp, border, shape)
            .padding(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.xxs),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.xs + spacing.xxs)) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(SpeedReaderTheme.dimens.iconSmall))
            FittedText(label, MaterialTheme.typography.labelMedium, MaterialTheme.colorScheme.onSurfaceVariant)
        }
        ValueWithUnit(
            value = value,
            unit = unit,
            valueStyle = if (emphasized) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = spacing.xs),
        )
        Row(
            modifier = Modifier.heightIn(min = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            when (status) {
                MetricStatus.Live -> LiveDot(accent)
                MetricStatus.Done -> Icon(Icons.Rounded.Check, contentDescription = null, tint = accent, modifier = Modifier.size(13.dp))
                MetricStatus.Idle -> Unit
            }
            if (caption != null) {
                FittedText(caption, MaterialTheme.typography.labelSmall, MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * The value and its smaller unit as one line of text, so that when a large font scale makes them
 * too wide for the card they shrink together instead of the unit being cut off.
 */
@Composable
private fun ValueWithUnit(value: String, unit: String, valueStyle: TextStyle, modifier: Modifier = Modifier) {
    val unitStyle = MaterialTheme.typography.labelSmall
    val unitSpan = SpanStyle(
        fontSize = (unitStyle.fontSize.value / valueStyle.fontSize.value).em,
        fontWeight = unitStyle.fontWeight,
        letterSpacing = unitStyle.letterSpacing,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val text = buildAnnotatedString {
        append(value)
        withStyle(unitSpan) {
            append(UNIT_SEPARATOR)
            append(unit)
        }
    }
    FittedText(text, valueStyle, modifier = modifier)
}

/** One line of text in [style] that shrinks to fit the width rather than wrapping or being cut off. */
@Composable
private fun FittedText(text: AnnotatedString, style: TextStyle, color: Color = Color.Unspecified, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = style,
        color = color,
        modifier = modifier,
        softWrap = false,
        maxLines = 1,
        autoSize = TextAutoSize.StepBased(minFontSize = MIN_FITTED_FONT_SIZE, maxFontSize = style.fontSize),
    )
}

@Composable
private fun FittedText(text: String, style: TextStyle, color: Color = Color.Unspecified, modifier: Modifier = Modifier) =
    FittedText(AnnotatedString(text), style, color, modifier)

/** A three-per-em space: at the unit's size, about as wide as the gap the separate texts used to have. */
private const val UNIT_SEPARATOR = "\u2004"
private val MIN_FITTED_FONT_SIZE = 8.sp

@ThemePreviews
@Composable
private fun MetricCardPreview() {
    ComponentPreview {
        val colors = SpeedReaderTheme.speedColors
        Row(horizontalArrangement = Arrangement.spacedBy(SpeedReaderTheme.spacing.sm)) {
            SrMetricCard(Icons.Rounded.NetworkPing, "Ping", "9", "ms", colors.highlight, Modifier.weight(1f), caption = "ICMP")
            SrMetricCard(
                Icons.Rounded.Download,
                "Download",
                "223",
                "Mbps",
                colors.download,
                Modifier.weight(1f),
                MetricStatus.Done,
                "average",
            )
            SrMetricCard(Icons.Rounded.Upload, "Upload", "39.4", "Mbps", colors.upload, Modifier.weight(1f), MetricStatus.Live, "live avg")
        }
    }
}
