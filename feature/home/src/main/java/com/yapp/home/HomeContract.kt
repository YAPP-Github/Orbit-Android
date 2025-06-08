package com.yapp.home

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yapp.domain.model.Alarm
import com.yapp.ui.base.SideEffect
import com.yapp.ui.base.UiState

sealed class HomeContract {

    data class State(
        val initialLoading: Boolean = true,
        val alarms: List<Alarm> = emptyList(),
        val lastAddedAlarmIndex: Int? = null,
        val dropdownMenuExpanded: Boolean = false,
        val sortDropDownMenuExpanded: Boolean = false,
        val selectedAlarmIds: Set<Long> = emptySet(),
        val isSelectionMode: Boolean = false,
        val isDeleteDialogVisible: Boolean = false,
        val isNoActivatedAlarmDialogVisible: Boolean = false,
        val isNoDailyFortuneDialogVisible: Boolean = false,
        val hasNewFortune: Boolean = false,
        val isToolTipVisible: Boolean = false,
        val pendingAlarmToggle: Pair<Long, Boolean>? = null,
        val lastFortuneScore: Int = -1,
        val deliveryTime: String = "받을 수 있는 운세가 없어요",
        val name: String = "",
        val activeItemMenu: Long? = null,
        val activeItemMenuPosition: Pair<Float, Float>? = null,
        val sortOrder: AlarmSortOrder = AlarmSortOrder.DEFAULT,
    ) : UiState {
        val isAllSelected: Boolean
            get() = alarms.isNotEmpty() && selectedAlarmIds.size == alarms.size

        val hasActivatedAlarm: Boolean
            get() = alarms.any { it.isAlarmActive }

        val sortedAlarms: List<Alarm>
            get() = when (sortOrder) {
                AlarmSortOrder.DEFAULT -> alarms
                AlarmSortOrder.ACTIVATION -> alarms.sortedByDescending { it.isAlarmActive }
            }
    }

    sealed class Action {
        data object NavigateToAlarmCreation : Action()
        data object ToggleMultiSelectionMode : Action()
        data object ShowDropDownMenu : Action()
        data object ShowSortDropDownMenu : Action()
        data object HideDropDownMenu : Action()
        data class ToggleAlarmSelection(val alarmId: Long) : Action()
        data class ToggleAlarmActivation(val alarmId: Long) : Action()
        data class SwipeToDeleteAlarm(val id: Long) : Action()
        data object ToggleAllAlarmSelection : Action()
        data object ShowDeleteDialog : Action()
        data object HideDeleteDialog : Action()
        data object ShowNoActivatedAlarmDialog : Action()
        data object HideNoActivatedAlarmDialog : Action()
        data object ShowNoDailyFortuneDialog : Action()
        data object HideNoDailyFortuneDialog : Action()
        data object HideToolTip : Action()
        data object RollbackPendingAlarmToggle : Action()
        data object ConfirmDeletion : Action()
        data class DeleteSingleAlarm(val alarmId: Long) : Action()
        data object LoadMoreAlarms : Action()
        data object ResetLastAddedAlarmIndex : Action()
        data class EditAlarm(val alarmId: Long) : Action()
        data object ShowDailyFortune : Action()
        data object NavigateToSetting : Action()
        data class ShowItemMenu(val alarmId: Long, val x: Float, val y: Float) : Action()
        data object HideItemMenu : Action()
        data class SetSortOrder(val sortOrder: AlarmSortOrder) : Action()
    }

    sealed class SideEffect : com.yapp.ui.base.SideEffect {
        data object NavigateToAddAlarm : SideEffect()

        data class NavigateToEditAlarm(val alarmId: Long) : SideEffect()

        data object NavigateToFortune : SideEffect()

        data object NavigateToSetting : SideEffect()

        data class ShowSnackBar(
            val message: String,
            val label: String = "",
            val iconRes: Int,
            val bottomPadding: Dp = 12.dp,
            val durationMillis: Long = 2000,
            val onDismiss: () -> Unit,
            val onAction: () -> Unit,
        ) : SideEffect()
    }

    enum class AlarmSortOrder {
        DEFAULT,
        ACTIVATION,
    }
}
