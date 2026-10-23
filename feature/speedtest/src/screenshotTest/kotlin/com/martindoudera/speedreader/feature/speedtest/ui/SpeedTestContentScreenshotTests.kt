package com.martindoudera.speedreader.feature.speedtest.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.android.tools.screenshot.PreviewTest
import com.martindoudera.speedreader.core.designsystem.component.ThemePreviews

@PreviewTest
@ThemePreviews
@Composable
private fun SpeedTestContentScreenshot(@PreviewParameter(SpeedTestPhaseProvider::class) phase: SpeedTestPhase) = ContentPreview(phase)

@PreviewTest
@Preview(name = "Tablet", device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
private fun SpeedTestContentTabletScreenshot(@PreviewParameter(SpeedTestPhaseProvider::class) phase: SpeedTestPhase) = ContentPreview(phase)

@PreviewTest
@Preview(name = "Phone landscape", device = "spec:width=891dp,height=411dp,dpi=420")
@Composable
private fun SpeedTestContentLandscapeScreenshot(@PreviewParameter(SpeedTestPhaseProvider::class) phase: SpeedTestPhase) =
    ContentPreview(phase)

@PreviewTest
@Preview(name = "Foldable", device = "spec:width=673dp,height=841dp,dpi=420")
@Composable
private fun SpeedTestContentFoldableScreenshot(@PreviewParameter(SpeedTestPhaseProvider::class) phase: SpeedTestPhase) =
    ContentPreview(phase)

@PreviewTest
@Preview(name = "Large font", device = "spec:width=411dp,height=891dp,dpi=420", fontScale = 2f)
@Composable
private fun SpeedTestContentLargeFontScreenshot(@PreviewParameter(SpeedTestPhaseProvider::class) phase: SpeedTestPhase) =
    ContentPreview(phase)
