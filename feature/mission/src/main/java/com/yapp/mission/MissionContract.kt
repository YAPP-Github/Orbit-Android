package com.yapp.mission

import com.yapp.domain.model.MissionType

sealed class MissionContract {

    data class State(
        val missionType: MissionType = MissionType.TAP,
        val isMissionTypeLoading: Boolean = true,
        val missionCount: Int = 10,
        val currentCount: Int = 0,
        val isMissionCompleted: Boolean = false,
        val playWhenClick: Boolean = false,
        val showFinalAnimation: Boolean = false,
        val isFlipped: Boolean = false,
        val rotationY: Float = 0f,
        val rotationZ: Float = 0f,
        val showExitDialog: Boolean = false,
        val errorMessage: String? = null,
    ) : com.yapp.ui.base.UiState

    sealed class Action {
        data object ShakeCard : Action()
        data object ClickCard : Action()
        data object ShowExitDialog : Action()
        data object HideExitDialog : Action()
        data object RetryPostFortune : Action()
    }

    sealed class SideEffect : com.yapp.ui.base.SideEffect {
        data object NavigateToFortune : SideEffect()
        data object NavigateToHome : SideEffect()
        data object NavigateBack : SideEffect()
    }
}
