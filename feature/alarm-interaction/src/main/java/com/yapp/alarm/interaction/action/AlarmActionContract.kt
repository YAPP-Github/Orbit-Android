package com.yapp.alarm.interaction.action

import com.yapp.domain.model.Alarm
import com.yapp.ui.base.UiState

class AlarmActionContract {

    data class State(
        val initialLoading: Boolean = true,
        val isAm: Boolean = true,
        val hour: Int = 0,
        val minute: Int = 0,
        val todayDate: String = "",
        val snoozeEnabled: Boolean = true,
        val snoozeInterval: Int = 5,
        val snoozeCount: Int = 5,
        val isFirstMission: Boolean? = null,
    ) : UiState

    sealed class Action {
        data object Snooze : Action()
        data object Dismiss : Action()
    }

    sealed class SideEffect : com.yapp.ui.base.SideEffect {
        data class NavigateToAlarmSnooze(val alarm: Alarm) : SideEffect()
    }
}
