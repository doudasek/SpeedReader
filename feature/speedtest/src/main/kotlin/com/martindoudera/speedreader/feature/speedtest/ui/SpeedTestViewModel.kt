package com.martindoudera.speedreader.feature.speedtest.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.martindoudera.speedreader.feature.speedtest.domain.model.ConnectionMode
import com.martindoudera.speedreader.feature.speedtest.domain.usecase.RunSpeedTestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import timber.log.Timber

private const val CONNECTION_MODE_KEY = "connectionMode"

/**
 * Runs speed tests for the screen. A running test lives in `viewModelScope`, so it survives configuration
 * changes; the chosen connection mode also survives process death, a running test does not.
 */
@HiltViewModel
class SpeedTestViewModel @Inject constructor(
    private val runSpeedTest: RunSpeedTestUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SpeedTestUiState(connectionMode = savedStateHandle[CONNECTION_MODE_KEY] ?: ConnectionMode.Parallel),
    )
    val uiState: StateFlow<SpeedTestUiState> = _uiState.asStateFlow()

    private var testJob: Job? = null

    fun onStartClick() {
        if (testJob?.isActive == true) {
            Timber.w("Start ignored, a speed test is already running")
            return
        }
        val mode = _uiState.value.connectionMode
        Timber.d("Start requested in %s mode", mode)
        testJob = runSpeedTest(mode)
            .onEach { progress -> showPhase(progress.toUiPhase()) }
            .launchIn(viewModelScope)
    }

    fun onStopClick() {
        Timber.d("Stop requested")
        testJob?.cancel()
        testJob = null
        showPhase(SpeedTestPhase.Idle)
    }

    /** Leaves a result or an error for the start screen; ignored while a test runs, where Stop is the way out. */
    fun onBackClick() {
        val phase = _uiState.value.phase
        if (phase !is SpeedTestPhase.Finished && phase !is SpeedTestPhase.Failed) {
            Timber.w("Back ignored in %s", phase::class.simpleName)
            return
        }
        showPhase(SpeedTestPhase.Idle)
    }

    fun onSingleConnectionChange(singleConnection: Boolean) {
        val mode = if (singleConnection) ConnectionMode.Single else ConnectionMode.Parallel
        Timber.d("Connection mode set to %s", mode)
        savedStateHandle[CONNECTION_MODE_KEY] = mode
        _uiState.update { it.copy(connectionMode = mode) }
    }

    private fun showPhase(phase: SpeedTestPhase) {
        val previous = _uiState.value.phase
        if (previous::class != phase::class) {
            Timber.d("Screen phase %s -> %s", previous::class.simpleName, phase::class.simpleName)
        }
        _uiState.update { it.copy(phase = phase) }
    }
}
