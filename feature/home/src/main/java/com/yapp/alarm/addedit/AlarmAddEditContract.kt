package com.yapp.alarm.addedit

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmDay
import com.yapp.domain.model.AlarmSound
import com.yapp.domain.model.MissionType
import com.yapp.domain.model.toRepeatDays
import com.yapp.ui.base.UiState

sealed class AlarmAddEditContract {

    data class State(
        val mode: EditMode = EditMode.ADD,
        val initialLoading: Boolean = true,
        val timeState: AlarmTimeState = AlarmTimeState(),
        val daySelectionState: AlarmDaySelectionState = AlarmDaySelectionState(),
        val holidayState: AlarmHolidayState = AlarmHolidayState(),
        val missionState: AlarmMissionState = AlarmMissionState(),
        val snoozeState: AlarmSnoozeState = AlarmSnoozeState(),
        val soundState: AlarmSoundState = AlarmSoundState(),
        val bottomSheetState: BottomSheetType? = null,
        val isDeleteDialogVisible: Boolean = false,
        val isUnsavedChangesDialogVisible: Boolean = false,
    ) : UiState

    data class AlarmTimeState(
        val initialAmPm: String = "오전",
        val initialHour: String = "1",
        val initialMinute: String = "00",
        val currentAmPm: String = "오전",
        val currentHour: Int = 1,
        val currentMinute: Int = 0,
        val alarmMessage: String = "",
    )

    data class AlarmDaySelectionState(
        val days: Set<AlarmDay> = enumValues<AlarmDay>().toSet(),
        val isWeekdaysChecked: Boolean = false,
        val isWeekendsChecked: Boolean = false,
        val selectedDays: Set<AlarmDay> = setOf(),
    )

    data class AlarmHolidayState(
        val isDisableHolidayEnabled: Boolean = false,
        val isDisableHolidayChecked: Boolean = false,
    )

    data class AlarmMissionState(
        val isMissionEnabled: Boolean = false,
        val missionType: MissionType = MissionType.TAP,
    )

    data class AlarmSnoozeState(
        val isSnoozeEnabled: Boolean = true,
        val snoozeIntervalIndex: Int = 2,
        val snoozeCountIndex: Int = 2,
        val snoozeIntervals: List<String> = listOf("1분", "3분", "5분", "10분", "15분"),
        val snoozeCounts: List<String> = listOf("1회", "3회", "5회", "10회", "무한"),
    )

    data class AlarmSoundState(
        val isVibrationEnabled: Boolean = true,
        val isSoundEnabled: Boolean = true,
        val soundVolume: Int = 70,
        val soundIndex: Int = 0,
        val sounds: List<AlarmSound> = emptyList(),
    )

    enum class EditMode {
        ADD, EDIT
    }

    sealed class Action {
        data object CheckUnsavedChangesBeforeExit : Action()
        data object NavigateBack : Action()
        data object SaveAlarm : Action()
        data object ShowDeleteDialog : Action()
        data object HideDeleteDialog : Action()
        data object ShowUnsavedChangesDialog : Action()
        data object HideUnsavedChangesDialog : Action()
        data object DeleteAlarm : Action()
        data class SetAlarmTime(val amPm: String, val hour: Int, val minute: Int) : Action()
        data object ToggleWeekdaysSelection : Action()
        data object ToggleWeekendsSelection : Action()
        data class ToggleSpecificDaySelection(val day: AlarmDay) : Action()
        data object ToggleHolidaySkipOption : Action()
        data object ToggleSnoozeOption : Action()
        data class SetSnoozeInterval(val index: Int) : Action()
        data class SetSnoozeRepeatCount(val index: Int) : Action()
        data object ToggleVibrationOption : Action()
        data object ToggleSoundOption : Action()
        data class AdjustSoundVolume(val volume: Int) : Action()
        data class SelectAlarmSound(val index: Int) : Action()
        data class ToggleBottomSheet(val sheetType: BottomSheetType) : Action()
    }

    sealed class BottomSheetType {
        data object SnoozeSetting : BottomSheetType()
        data object SoundSetting : BottomSheetType()
    }

    sealed class SideEffect : com.yapp.ui.base.SideEffect {
        data object NavigateBack : SideEffect()

        data class SaveAlarm(val id: Long) : SideEffect()

        data class UpdateAlarm(val id: Long) : SideEffect()

        data class DeleteAlarm(val id: Long) : SideEffect()

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
}

internal fun AlarmAddEditContract.State.toAlarm(id: Long = 0): Alarm {
    return Alarm(
        id = id,
        isAm = timeState.currentAmPm == "오전",
        hour = timeState.currentHour,
        minute = timeState.currentMinute,
        repeatDays = daySelectionState.selectedDays.toRepeatDays(),
        isHolidayAlarmOff = holidayState.isDisableHolidayChecked,
        isSnoozeEnabled = snoozeState.isSnoozeEnabled,
        snoozeInterval = snoozeState.snoozeIntervals.getOrNull(snoozeState.snoozeIntervalIndex)
            ?.filter { it.isDigit() }
            ?.toIntOrNull()
            ?: 5,
        snoozeCount = snoozeState.snoozeCounts.getOrNull(snoozeState.snoozeCountIndex)
            ?.let { if (it == "무한") -1 else it.filter { char -> char.isDigit() }.toIntOrNull() ?: 1 }
            ?: 1,
        isVibrationEnabled = soundState.isVibrationEnabled,
        isSoundEnabled = soundState.isSoundEnabled,
        soundUri = soundState.sounds.getOrNull(soundState.soundIndex)?.uri.toString(),
        soundVolume = soundState.soundVolume,
        isAlarmActive = true,
    )
}
