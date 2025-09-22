package com.yapp.home.alarm.addedit

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmDay
import com.yapp.domain.model.AlarmSound
import com.yapp.domain.model.MissionType
import com.yapp.domain.model.toRepeatDays
import com.yapp.ui.base.UiState
import java.time.LocalTime

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
        val initialTime: LocalTime = LocalTime.of(1, 0),
        val currentTime: LocalTime = LocalTime.of(1, 0),
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
        val missionType: MissionType = MissionType.NONE,
        val missionCount: Int = 10,
    )

    data class AlarmSnoozeState(
        val isSnoozeEnabled: Boolean = true,
        val snoozeInterval: Int = 5,
        val snoozeCount: Int = 5,
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
        data class SetAlarmTime(val newTime: LocalTime) : Action()
        data object ToggleWeekdaysSelection : Action()
        data object ToggleWeekendsSelection : Action()
        data class ToggleSpecificDaySelection(val day: AlarmDay) : Action()
        data object ToggleHolidaySkipOption : Action()
        data class SaveMissionSetting(val type: MissionType, val count: Int) : Action()
        data class SaveSnoozeSetting(
            val enabled: Boolean,
            val interval: Int,
            val count: Int,
        ) : Action()
        data class SaveSoundSetting(
            val vibrationEnabled: Boolean,
            val soundEnabled: Boolean,
            val soundVolume: Int,
            val soundIndex: Int,
        ) : Action()
        data class ToggleVibrationEnabled(val enabled: Boolean) : Action()
        data class ToggleSoundEnabled(val enabled: Boolean) : Action()
        data class SetSoundVolume(val volume: Int) : Action()
        data class SetSoundIndex(val index: Int) : Action()
        data class ShowBottomSheet(val sheetType: BottomSheetType) : Action()
        data class NavigateToMissionPreview(val missionType: MissionType, val missionCount: Int) : Action()
        data object HideBottomSheet : Action()
    }

    sealed class BottomSheetType {
        data object MissionSetting : BottomSheetType()
        data object SnoozeSetting : BottomSheetType()
        data object SoundSetting : BottomSheetType()
    }

    sealed class SideEffect : com.yapp.ui.base.SideEffect {
        data object NavigateBack : SideEffect()

        data class NavigateToMissionPreview(
            val missionType: MissionType,
            val missionCount: Int,
        ) : SideEffect()

        data class ShowBottomSheet(
            val sheetType: BottomSheetType,
        ) : SideEffect()

        data object HideBottomSheet : SideEffect()

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
        hour = timeState.currentTime.hour,
        minute = timeState.currentTime.minute,
        repeatDays = daySelectionState.selectedDays.toRepeatDays(),
        isHolidayAlarmOff = holidayState.isDisableHolidayChecked,
        missionType = missionState.missionType,
        missionCount = missionState.missionCount,
        isSnoozeEnabled = snoozeState.isSnoozeEnabled,
        snoozeInterval = snoozeState.snoozeInterval,
        snoozeCount = snoozeState.snoozeCount,
        isVibrationEnabled = soundState.isVibrationEnabled,
        isSoundEnabled = soundState.isSoundEnabled,
        soundUri = soundState.sounds.getOrNull(soundState.soundIndex)?.uri.toString(),
        soundVolume = soundState.soundVolume,
        isAlarmActive = true,
    )
}
