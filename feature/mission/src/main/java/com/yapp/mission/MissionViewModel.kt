package com.yapp.mission

import android.app.Application
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yapp.alarm.pendingIntent.interaction.createAlarmDismissIntent
import com.yapp.analytics.AnalyticsEvent
import com.yapp.analytics.AnalyticsHelper
import com.yapp.datastore.UserPreferences
import com.yapp.domain.model.MissionType
import com.yapp.domain.repository.FortuneRepository
import com.yapp.domain.usecase.GetMissionTypeUseCase
import com.yapp.media.haptic.HapticFeedbackManager
import com.yapp.media.haptic.HapticType
import com.yapp.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MissionViewModel @Inject constructor(
    private val analyticsHelper: AnalyticsHelper,
    private val hapticFeedbackManager: HapticFeedbackManager,
    private val fortuneRepository: FortuneRepository,
    private val userPreferences: UserPreferences,
    private val getMissionTypeUseCase: GetMissionTypeUseCase,
    private val app: Application,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<MissionContract.State, MissionContract.SideEffect>(
    MissionContract.State(),
) {
    init {
        savedStateHandle.get<String>("notificationId")?.toLong()?.let {
            sendAlarmDismissIntent(it)
        }
        loadRemoteMissionType()
    }

    private fun loadRemoteMissionType() {
        viewModelScope.launch {
            val missionType = getMissionTypeUseCase.execute()
            updateState {
                copy(
                    missionType = missionType,
                    isMissionTypeLoading = false,
                )
            }
        }
    }

    fun processAction(action: MissionContract.Action) {
        when (action) {
            is MissionContract.Action.ShakeCard -> handleShake()
            is MissionContract.Action.ClickCard -> handleClick()
            is MissionContract.Action.ShowExitDialog -> showExitDialog()
            is MissionContract.Action.HideExitDialog -> hideExitDialog()
            is MissionContract.Action.RetryPostFortune -> retryPostFortune()
        }
    }

    private fun showExitDialog() {
        updateState { copy(showExitDialog = true) }
    }

    private fun hideExitDialog() {
        updateState { copy(showExitDialog = false) }
    }

    private fun handleShake() = viewModelScope.launch {
        if (currentState.missionType !is MissionType.Shake) return@launch

        val currentCount = currentState.shakeCount
        if (currentCount < 9) {
            performHapticSuccess()
            updateState { copy(shakeCount = currentCount + 1) }
        } else if (!currentState.isFlipped) {
            completeMission(type = "shake")
            updateState {
                copy(
                    isMissionCompleted = true,
                    shakeCount = 10,
                    isFlipped = true,
                )
            }
            delay(500)
        }
    }

    private fun handleClick() = viewModelScope.launch {
        if (currentState.missionType !is MissionType.Click) return@launch

        val currentCount = currentState.clickCount
        if (currentCount < 9) {
            performHapticSuccess()
            logMissionSuccess("click")
            updateState { copy(clickCount = currentCount + 1, playWhenClick = true) }
            delay(500)
            updateState { copy(playWhenClick = false) }
        } else {
            updateState {
                copy(
                    clickCount = 10,
                    showFinalAnimation = true,
                )
            }
            postFortune()
            delay(500)
            updateState { copy(isMissionCompleted = true) }
        }
    }

    private fun postFortune() {
        viewModelScope.launch {
            val userId = userPreferences.userIdFlow.firstOrNull() ?: return@launch
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    fortuneRepository.postFortune(userId)
                }
            }

            result.onSuccess {
                val data = it.getOrThrow()
                userPreferences.saveFortuneId(data.id)
                userPreferences.saveFortuneScore(data.avgFortuneScore)

                emitSideEffect(MissionContract.SideEffect.NavigateToFortune)
            }.onFailure { error ->
                Log.e("MissionViewModel", "운세 데이터 요청 실패: ${error.message}")
                updateState { copy(errorMessage = error.message) }
            }
        }
    }

    private fun retryPostFortune() {
        viewModelScope.launch {
            val userId = userPreferences.userIdFlow.firstOrNull() ?: return@launch
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    fortuneRepository.postFortune(userId)
                }
            }

            result.onSuccess {
                val data = it.getOrThrow()
                userPreferences.saveFortuneId(data.id)
                userPreferences.saveFortuneScore(data.avgFortuneScore)

                emitSideEffect(MissionContract.SideEffect.NavigateToFortune)
            }.onFailure {
                Log.e("MissionViewModel", "운세 재요청 실패: ${it.message}")
                navigateToHome()
            }
        }
    }

    private fun completeMission(type: String) {
        performHapticSuccess()
        logMissionSuccess(type)
        postFortune()
    }

    private fun performHapticSuccess() {
        hapticFeedbackManager.performHapticFeedback(HapticType.SUCCESS)
    }

    private fun logMissionSuccess(type: String) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                type = "mission_success",
                properties = mapOf(
                    AnalyticsEvent.MissionPropertiesKeys.MISSION_TYPE to type,
                ),
            ),
        )
    }

    private fun navigateToHome() {
        emitSideEffect(MissionContract.SideEffect.NavigateToFortune)
    }

    private fun sendAlarmDismissIntent(id: Long) {
        val alarmDismissIntent = createAlarmDismissIntent(
            context = app,
            notificationId = id,
        )
        app.sendBroadcast(alarmDismissIntent)
    }
}
