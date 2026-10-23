package com.martindoudera.speedreader.feature.speedtest.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.martindoudera.speedreader.core.common.domain.model.AppVersion
import com.martindoudera.speedreader.core.designsystem.R as DesignSystemR
import com.martindoudera.speedreader.core.designsystem.adaptive.prefersTwoPanes
import com.martindoudera.speedreader.core.designsystem.component.SrInfoChip
import com.martindoudera.speedreader.core.designsystem.component.SrMessage
import com.martindoudera.speedreader.core.designsystem.component.SrPulsingCircleButton
import com.martindoudera.speedreader.core.designsystem.component.SrTopBar
import com.martindoudera.speedreader.core.designsystem.theme.SpeedReaderTheme
import com.martindoudera.speedreader.feature.speedtest.R
import com.martindoudera.speedreader.feature.speedtest.ui.component.MeasuringStage
import com.martindoudera.speedreader.feature.speedtest.ui.component.MetricsRow
import com.martindoudera.speedreader.feature.speedtest.ui.component.SpeedTestFooter
import timber.log.Timber

@Composable
fun SpeedTestScreen(modifier: Modifier = Modifier, viewModel: SpeedTestViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val phase = uiState.phase

    BackHandler(enabled = phase is SpeedTestPhase.Finished || phase is SpeedTestPhase.Failed, onBack = viewModel::onBackClick)
    val running = phase is SpeedTestPhase.FindingServer || phase is SpeedTestPhase.Downloading || phase is SpeedTestPhase.Uploading

    SpeedTestContent(
        uiState = uiState,
        onStartClick = viewModel::onStartClick,
        onStopClick = viewModel::onStopClick,
        onBackClick = viewModel::onBackClick,
        onShareClick = context::shareText,
        onSingleConnectionChange = viewModel::onSingleConnectionChange,
        modifier = if (running) modifier.keepScreenOn() else modifier,
    )
}

/**
 * The whole screen as a function of [SpeedTestUiState]. Every phase uses the same slots — server,
 * stage, metrics, actions — and only their content changes, so the layout never jumps between phases.
 * Wide windows, and short ones such as a phone in landscape, put the stage beside the rest.
 */
@Composable
internal fun SpeedTestContent(
    uiState: SpeedTestUiState,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onBackClick: () -> Unit,
    onShareClick: (String) -> Unit,
    onSingleConnectionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = SpeedReaderTheme.spacing
    val twoPane = currentWindowAdaptiveInfoV2().windowSizeClass.prefersTwoPanes()
    val phase = uiState.phase
    val shareText = (phase as? SpeedTestPhase.Finished)?.let { shareText(it) }

    val chip = @Composable { chipModifier: Modifier -> ServerChip(uiState, chipModifier) }
    val stage = @Composable { stageModifier: Modifier -> Stage(phase, onStartClick, stageModifier) }
    val metrics = @Composable { metricsModifier: Modifier -> VisibleWhileTesting(phase, metricsModifier) { MetricsRow(phase, it) } }
    val footer = @Composable { footerModifier: Modifier ->
        SpeedTestFooter(
            phase = phase,
            connectionMode = uiState.connectionMode,
            onStopClick = onStopClick,
            onBackClick = onBackClick,
            onShareClick = { shareText?.let(onShareClick) },
            onSingleConnectionChange = onSingleConnectionChange,
            modifier = footerModifier,
        )
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = if (twoPane) spacing.xxl else spacing.lg)
                .padding(bottom = spacing.xl),
        ) {
            SrTopBar(title = stringResource(R.string.feature_speedtest_title)) {
                AppInfoLabel(uiState.appVersion, visible = phase is SpeedTestPhase.Idle)
            }
            if (twoPane) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(spacing.xxl)) {
                    stage(Modifier.weight(1f).fillMaxHeight())
                    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                        chip(Modifier)
                        metrics(Modifier)
                        footer(Modifier)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .widthIn(max = SpeedReaderTheme.dimens.contentMaxWidth)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    chip(Modifier.padding(top = spacing.xs))
                    stage(Modifier.weight(1f).fillMaxWidth())
                    metrics(Modifier)
                    footer(Modifier)
                }
            }
        }
    }
}

@Composable
private fun Stage(phase: SpeedTestPhase, onStartClick: () -> Unit, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = phase,
        contentKey = { it.stageKey() },
        transitionSpec = { (fadeIn(tween(400)) + scaleIn(initialScale = 0.94f)) togetherWith fadeOut(tween(200)) using null },
        contentAlignment = Alignment.Center,
        modifier = modifier,
        label = "stage",
    ) { target ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (target) {
                SpeedTestPhase.Idle -> SrPulsingCircleButton(
                    text = stringResource(R.string.feature_speedtest_start),
                    onClick = onStartClick,
                )
                SpeedTestPhase.FindingServer, is SpeedTestPhase.Downloading, is SpeedTestPhase.Uploading -> MeasuringStage(target)
                is SpeedTestPhase.Finished -> SrMessage(
                    illustration = painterResource(DesignSystemR.drawable.core_designsystem_illustration_test_complete),
                    title = stringResource(R.string.feature_speedtest_test_complete),
                )
                is SpeedTestPhase.Failed -> SrMessage(
                    illustration = painterResource(DesignSystemR.drawable.core_designsystem_illustration_disconnected),
                    title = stringResource(target.reason.titleRes()),
                    text = stringResource(target.reason.textRes()),
                )
            }
        }
    }
}

private enum class StageKey { Start, Measuring, Result, Error }

private fun SpeedTestPhase.stageKey(): StageKey = when (this) {
    SpeedTestPhase.Idle -> StageKey.Start
    SpeedTestPhase.FindingServer, is SpeedTestPhase.Downloading, is SpeedTestPhase.Uploading -> StageKey.Measuring
    is SpeedTestPhase.Finished -> StageKey.Result
    is SpeedTestPhase.Failed -> StageKey.Error
}

@Composable
private fun ServerChip(uiState: SpeedTestUiState, modifier: Modifier = Modifier) {
    val server = uiState.phase.server
    val connections = uiState.connectionMode.connections
    VisibleWhileTesting(uiState.phase, modifier) { visibleModifier ->
        SrInfoChip(
            icon = Icons.Rounded.Dns,
            title = server?.name.orEmpty(),
            subtitle = server?.location.orEmpty(),
            tag = pluralStringResource(R.plurals.feature_speedtest_connections, connections, connections),
            loading = uiState.phase is SpeedTestPhase.FindingServer,
            modifier = visibleModifier,
        )
    }
}

/** The app author and version in the top bar's corner, shown on the start screen only. */
@Composable
private fun AppInfoLabel(version: AppVersion, visible: Boolean, modifier: Modifier = Modifier) {
    val alpha by animateFloatAsState(if (visible) 1f else 0f, label = "appInfoLabel")
    Column(horizontalAlignment = Alignment.End, modifier = modifier.graphicsLayer { this.alpha = alpha }) {
        Text(
            text = stringResource(R.string.feature_speedtest_author),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.feature_speedtest_version, version.name),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Fades content out before a run and after an error while keeping its space, so the stage does not move. */
@Composable
private fun VisibleWhileTesting(phase: SpeedTestPhase, modifier: Modifier, content: @Composable (Modifier) -> Unit) {
    val visible = phase !is SpeedTestPhase.Idle && phase !is SpeedTestPhase.Failed
    val alpha by animateFloatAsState(if (visible) 1f else 0f, label = "visibleWhileTesting")
    content(modifier.graphicsLayer { this.alpha = alpha })
}

@Composable
private fun shareText(result: SpeedTestPhase.Finished): String {
    val server = result.server
    return stringResource(
        R.string.feature_speedtest_share_text,
        result.downloadSpeed.formatMegabits(SpeedReaderTheme.locale),
        result.uploadSpeed.formatMegabits(SpeedReaderTheme.locale),
        server.latency?.formatMilliseconds() ?: NO_VALUE,
        listOfNotNull(server.name, server.location).joinToString(", "),
    )
}

private fun Context.shareText(text: String) {
    Timber.d("Sharing the test result")
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    startActivity(Intent.createChooser(send, null))
}

private fun FailureReason.titleRes(): Int = when (this) {
    FailureReason.Offline -> R.string.feature_speedtest_error_offline_title
    FailureReason.NoServersAvailable -> R.string.feature_speedtest_error_no_servers_title
    FailureReason.ConnectionLost -> R.string.feature_speedtest_error_connection_lost_title
    FailureReason.Unexpected -> R.string.feature_speedtest_error_unexpected_title
}

private fun FailureReason.textRes(): Int = when (this) {
    FailureReason.Offline -> R.string.feature_speedtest_error_offline_text
    FailureReason.NoServersAvailable -> R.string.feature_speedtest_error_no_servers_text
    FailureReason.ConnectionLost -> R.string.feature_speedtest_error_connection_lost_text
    FailureReason.Unexpected -> R.string.feature_speedtest_error_unexpected_text
}
