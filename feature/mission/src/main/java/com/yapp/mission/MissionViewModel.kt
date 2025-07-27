package com.yapp.mission

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.yapp.alarm.pendingIntent.interaction.createAlarmDismissIntent
import com.yapp.analytics.AnalyticsEvent
import com.yapp.analytics.AnalyticsHelper
import com.yapp.domain.MissionMode
import com.yapp.domain.model.MissionType
import com.yapp.domain.repository.FortuneRepository
import com.yapp.domain.repository.UserInfoRepository
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
    private val app: Application,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), ContainerHost<MissionContract.State, MissionContract.SideEffect> {

    override val container: Container<MissionContract.State, MissionContract.SideEffect> = container(
        initialState = MissionContract.State(),
    ) {
        savedStateHandle.get<String>("notificationId")?.toLong()?.let {
            sendAlarmDismissIntent(it)
        }
        loadMissionInfo(
            missionTypeRaw = savedStateHandle.get<String>("missionType"),
            missionCountRaw = savedStateHandle.get<String>("missionCount"),
            missionModeRaw = savedStateHandle.get<String>("missionMode"),
        )
    }

    fun processAction(action: MissionContract.Action) {
        when (action) {
            is MissionContract.Action.NavigateBack -> navigateBack()
            is MissionContract.Action.ShakeCard -> handleMissionProgress(MissionType.SHAKE)
            is MissionContract.Action.ClickCard -> handleMissionProgress(MissionType.TAP)
            is MissionContract.Action.ShowExitDialog -> showExitDialog()
            is MissionContract.Action.HideExitDialog -> hideExitDialog()
            is MissionContract.Action.RetryPostFortune -> retryPostFortune()
        }
    }

    private fun loadMissionInfo(
        missionTypeRaw: String?,
        missionCountRaw: String?,
        missionModeRaw: String?,
    ) = intent {
        val missionType = missionTypeRaw?.toIntOrNull() ?: MissionType.TAP.value
        val missionCount = missionCountRaw?.toIntOrNull() ?: 10
        val missionMode = MissionMode.fromRaw(missionModeRaw)

        reduce {
            state.copy(
                missionMode = missionMode,
                missionType = MissionType.fromInt(missionType),
                missionCount = missionCount,
                isMissionTypeLoading = false,
            )
        }
    }

    private fun navigateBack() = intent {
        postSideEffect(MissionContract.SideEffect.NavigateBack)
    }

    private fun showExitDialog() = intent {
        reduce { state.copy(showExitDialog = true) }
    }

    private fun hideExitDialog() = intent {
        reduce { state.copy(showExitDialog = false) }
    }

    private fun handleMissionProgress(missionType: MissionType) = intent {
        val isLast = state.currentCount >= state.missionCount - 1
        val nextCount = state.currentCount + 1

        performHapticSuccess()

        if (isLast) {
            completeMission(type = missionType.name.lowercase())
            reduce {
                state.copy(
                    isMissionCompleted = true,
                    currentCount = state.missionCount,
                    showFinalAnimation = true,
                )
            }
            delay(500)
        } else {
            val transientState = if (missionType == MissionType.TAP) {
                state.copy(currentCount = nextCount, playWhenClick = true)
            } else {
                state.copy(currentCount = nextCount)
            }

            reduce { transientState }

            if (missionType == MissionType.TAP) {
                delay(500)
                reduce { state.copy(playWhenClick = false) }
            }
        }
    }

    private fun postFortune(isRetry: Boolean = false) = intent {
        val userId = userInfoRepository.userIdFlow.firstOrNull() ?: return@intent

        val result = withContext(Dispatchers.IO) {
            fortuneRepository.postFortune(userId)
        }

        result.onSuccess { data ->
            fortuneRepository.saveFortuneId(data.id)
            fortuneRepository.saveFortuneScore(data.avgFortuneScore)

            postSideEffect(MissionContract.SideEffect.NavigateToFortune)
        }.onFailure { error ->
            if (isRetry) {
                navigateToHome()
            } else {
                reduce { state.copy(errorMessage = error.message) }
            }
        }
    }

    fun retryPostFortune() {
        postFortune(isRetry = true)
    }

    private fun completeMission(type: String) = intent {
        performHapticSuccess()
        logMissionSuccess(type)
        if (state.missionMode == MissionMode.REAL) {
            postFortune()
        } else {
            postSideEffect(MissionContract.SideEffect.NavigateBack)
        }
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
