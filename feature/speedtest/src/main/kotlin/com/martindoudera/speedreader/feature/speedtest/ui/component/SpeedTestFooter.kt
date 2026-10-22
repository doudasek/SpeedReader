package com.martindoudera.speedreader.feature.speedtest.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.martindoudera.speedreader.core.designsystem.component.SrCheckboxRow
import com.martindoudera.speedreader.core.designsystem.component.SrOutlinedButton
import com.martindoudera.speedreader.core.designsystem.component.SrPrimaryButton
import com.martindoudera.speedreader.core.designsystem.theme.SpeedReaderTheme
import com.martindoudera.speedreader.feature.speedtest.R
import com.martindoudera.speedreader.feature.speedtest.domain.model.ConnectionMode
import com.martindoudera.speedreader.feature.speedtest.ui.SpeedTestPhase

/**
 * The actions under the metrics: the connection checkbox before a run, Stop during it, Back and Share
 * with a result and Back alone after an error. Its minimum height fits two buttons, so swapping
 * actions does not shift the rest of the screen.
 */
@Composable
internal fun SpeedTestFooter(
    phase: SpeedTestPhase,
    connectionMode: ConnectionMode,
    onStopClick: () -> Unit,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onSingleConnectionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth().heightIn(min = SpeedReaderTheme.dimens.buttonHeight * 2),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedContent(targetState = phase.footerActions(), transitionSpec = {
            fadeIn() togetherWith fadeOut()
        }, label = "footer") { actions ->
            when (actions) {
                FooterActions.ConnectionMode -> SrCheckboxRow(
                    checked = connectionMode == ConnectionMode.Single,
                    onCheckedChange = onSingleConnectionChange,
                    title = stringResource(R.string.feature_speedtest_single_connection_title),
                    description = stringResource(
                        R.string.feature_speedtest_single_connection_description,
                        ConnectionMode.Parallel.connections,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                FooterActions.Stop -> SrOutlinedButton(
                    text = stringResource(R.string.feature_speedtest_stop),
                    onClick = onStopClick,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.Stop,
                )
                FooterActions.BackAndShare -> Row(horizontalArrangement = Arrangement.spacedBy(SpeedReaderTheme.spacing.md)) {
                    SrOutlinedButton(
                        text = stringResource(R.string.feature_speedtest_back),
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    )
                    SrPrimaryButton(
                        text = stringResource(R.string.feature_speedtest_share),
                        onClick = onShareClick,
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Share,
                    )
                }
                FooterActions.Back -> SrOutlinedButton(
                    text = stringResource(R.string.feature_speedtest_back),
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                )
            }
        }
    }
}

private enum class FooterActions { ConnectionMode, Stop, BackAndShare, Back }

private fun SpeedTestPhase.footerActions(): FooterActions = when (this) {
    SpeedTestPhase.Idle -> FooterActions.ConnectionMode
    SpeedTestPhase.FindingServer, is SpeedTestPhase.Downloading, is SpeedTestPhase.Uploading -> FooterActions.Stop
    is SpeedTestPhase.Finished -> FooterActions.BackAndShare
    is SpeedTestPhase.Failed -> FooterActions.Back
}
