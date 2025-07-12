package com.yapp.mission

import android.app.Application
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.yapp.alarm.pendingIntent.interaction.createAlarmDismissIntent
import com.yapp.analytics.AnalyticsEvent
import com.yapp.analytics.AnalyticsHelper
import com.yapp.domain.model.MissionType
import com.yapp.domain.repository.FortuneRepository
import com.yapp.domain.repository.UserInfoRepository
import com.yapp.domain.usecase.GetMissionTypeUseCase
import com.yapp.media.haptic.HapticFeedbackManager
import com.yapp.media.haptic.HapticType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class MissionViewModel @Inject constructor(
    private val analyticsHelper: AnalyticsHelper,
    private val hapticFeedbackManager: HapticFeedbackManager,
    private val fortuneRepository: FortuneRepository,
    private val userInfoRepository: UserInfoRepository,
    private val getMissionTypeUseCase: GetMissionTypeUseCase,
    private val app: Application,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), ContainerHost<MissionContract.State, MissionContract.SideEffect> {

    override val container: Container<MissionContract.State, MissionContract.SideEffect> = container(
        initialState = MissionContract.State(),
    ) {
        savedStateHandle.get<String>("notificationId")?.toLong()?.let {
            sendAlarmDismissIntent(it)
        }
        loadRemoteMissionType()
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

    private fun loadRemoteMissionType() = intent {
        val missionType = getMissionTypeUseCase.execute()
        reduce {
            state.copy(
                missionType = missionType,
                isMissionTypeLoading = false,
            )
        }
    }

    private fun showExitDialog() = intent {
        reduce { state.copy(showExitDialog = true) }
    }

    private fun hideExitDialog() = intent {
        reduce { state.copy(showExitDialog = false) }
    }

    private fun handleShake() = intent {
        if (state.missionType != MissionType.SHAKE) return@intent

        val currentCount = state.shakeCount
        if (currentCount < 9) {
            performHapticSuccess()
            reduce { state.copy(shakeCount = currentCount + 1) }
        } else if (!state.isFlipped) {
            completeMission(type = "shake")
            reduce {
                state.copy(
                    isMissionCompleted = true,
                    shakeCount = 10,
                    isFlipped = true,
                )
            }
            delay(500)
        }
    }

    private fun handleClick() = intent {
        if (state.missionType != MissionType.TAP) return@intent

        val currentCount = state.clickCount
        if (currentCount < 9) {
            performHapticSuccess()
            reduce { state.copy(clickCount = currentCount + 1, playWhenClick = true) }
            delay(500)
            reduce { state.copy(playWhenClick = false) }
        } else {
            completeMission("click")
            reduce {
                state.copy(
                    isMissionCompleted = true,
                    clickCount = 10,
                    showFinalAnimation = true,
                )
            }
            delay(500)
        }
    }

    private fun postFortune() = intent {
        val userId = userInfoRepository.userIdFlow.firstOrNull() ?: return@intent
        val result = runCatching {
            withContext(Dispatchers.IO) {
                fortuneRepository.postFortune(userId)
            }
        }

        result.onSuccess {
            val data = it.getOrThrow()
            fortuneRepository.saveFortuneId(data.id)
            fortuneRepository.saveFortuneScore(data.avgFortuneScore)

            postSideEffect(MissionContract.SideEffect.NavigateToFortune)
        }.onFailure { error ->
            Log.e("MissionViewModel", "운세 데이터 요청 실패: ${error.message}")
            reduce { state.copy(errorMessage = error.message) }
        }
    }

    private fun retryPostFortune() = intent {
        val userId = userInfoRepository.userIdFlow.firstOrNull() ?: return@intent
        val result = runCatching {
            withContext(Dispatchers.IO) {
                fortuneRepository.postFortune(userId)
            }
        }

        result.onSuccess {
            val data = it.getOrThrow()
            fortuneRepository.saveFortuneId(data.id)
            fortuneRepository.saveFortuneScore(data.avgFortuneScore)

            postSideEffect(MissionContract.SideEffect.NavigateToFortune)
        }.onFailure {
            Log.e("MissionViewModel", "운세 재요청 실패: ${it.message}")
            navigateToHome()
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

    private fun navigateToHome() = intent {
        postSideEffect(MissionContract.SideEffect.NavigateToHome)
    }

    private fun sendAlarmDismissIntent(id: Long) {
        val alarmDismissIntent = createAlarmDismissIntent(
            context = app,
            notificationId = id,
        )
        app.sendBroadcast(alarmDismissIntent)
    }
}
