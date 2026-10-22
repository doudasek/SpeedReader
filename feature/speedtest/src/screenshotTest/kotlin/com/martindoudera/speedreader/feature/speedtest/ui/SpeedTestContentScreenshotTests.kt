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
