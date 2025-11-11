package com.yapp.home.alarm.addedit

import android.util.Log
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.yapp.analytics.AnalyticsEvent
import com.yapp.analytics.AnalyticsHelper
import com.yapp.common.util.ResourceProvider
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmDay
import com.yapp.domain.model.AlarmSound
import com.yapp.domain.model.MissionType
import com.yapp.domain.model.copyFrom
import com.yapp.domain.model.toAlarmDayNames
import com.yapp.domain.model.toAlarmDays
import com.yapp.domain.model.toRepeatDays
import com.yapp.domain.usecase.AlarmUseCase
import com.yapp.home.util.AlarmDateTimeFormatter
import com.yapp.media.haptic.HapticFeedbackManager
import com.yapp.media.haptic.HapticType
import dagger.hilt.android.lifecycle.HiltViewModel
import feature.home.R
import kotlinx.coroutines.flow.first
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AlarmAddEditViewModel @Inject constructor(
    private val analyticsHelper: AnalyticsHelper,
    private val alarmUseCase: AlarmUseCase,
    private val resourceProvider: ResourceProvider,
    private val alarmDateTimeFormatter: AlarmDateTimeFormatter,
    private val hapticFeedbackManager: HapticFeedbackManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), ContainerHost<AlarmAddEditContract.State, AlarmAddEditContract.SideEffect> {

    override val container: Container<AlarmAddEditContract.State, AlarmAddEditContract.SideEffect> = container(initialState = AlarmAddEditContract.State()) {
        intent {
            reduce { state.copy(mode = if (alarmId == -1L) AlarmAddEditContract.EditMode.ADD else AlarmAddEditContract.EditMode.EDIT) }
            initializeAlarmScreen()
        }
    }

    private val alarmId: Long = savedStateHandle.get<Long>("alarmId") ?: -1

    private fun initializeAlarmScreen() = intent {
        alarmUseCase.getAlarmSounds().onSuccess { sounds ->
            if (alarmId == -1L) {
                setupNewAlarmScreen(sounds)
            } else {
                loadExistingAlarm(sounds)
            }
        }.onFailure {
            Log.e("AlarmAddEditViewModel", "Failed to load alarm sounds", it)
        }
    }

    private fun setupNewAlarmScreen(sounds: List<AlarmSound>) = intent {
        val defaultSoundIndex = sounds.indexOfFirst { it.title == "Homecoming" }.takeIf { it >= 0 } ?: 0
        val defaultSound = sounds[defaultSoundIndex]

        alarmUseCase.initializeSoundPlayer(defaultSound.uri)

        val now = LocalTime.now()

        reduce {
            state.copy(
                initialLoading = false,
                timeState = state.timeState.copy(
                    initialTime = now,
                    currentTime = now,
                    alarmMessage = getAlarmMessage(now, emptySet()),
                ),
                soundState = state.soundState.copy(sounds = sounds, soundIndex = defaultSoundIndex),
            )
        }
    }

    private fun loadExistingAlarm(sounds: List<AlarmSound>) = intent {
        alarmUseCase.getAlarm(alarmId).onSuccess { alarm ->
            val repeatDays = alarm.repeatDays.toAlarmDays()
            val selectedSoundIndex = sounds.indexOfFirst { it.uri == alarm.soundUri }
            val selectedSound = sounds.getOrNull(selectedSoundIndex) ?: sounds.first()

            alarmUseCase.initializeSoundPlayer(selectedSound.uri)

            reduce {
                state.copy(
                    initialLoading = false,
                    timeState = state.timeState.copy(
                        initialTime = LocalTime.of(alarm.hour, alarm.minute),
                        currentTime = LocalTime.of(alarm.hour, alarm.minute),
                        alarmMessage = getAlarmMessage(
                            LocalTime.of(alarm.hour, alarm.minute),
                            repeatDays,
                        ),
                    ),
                    daySelectionState = setupDaySelectionState(repeatDays, state),
                    holidayState = state.holidayState.copy(
                        isDisableHolidayEnabled = repeatDays.isNotEmpty(),
                        isDisableHolidayChecked = alarm.isHolidayAlarmOff,
                    ),
                    missionState = setUpMissionState(alarm, state),
                    snoozeState = setupSnoozeState(alarm, state),
                    soundState = state.soundState.copy(
                        isVibrationEnabled = alarm.isVibrationEnabled,
                        isSoundEnabled = alarm.isSoundEnabled,
                        soundVolume = alarm.soundVolume,
                        sounds = sounds,
                        soundIndex = selectedSoundIndex,
                    ),
                )
            }
        }
    }

    private fun setupDaySelectionState(
        repeatDays: Set<AlarmDay>,
        currentState: AlarmAddEditContract.State,
    ): AlarmAddEditContract.AlarmDaySelectionState {
        return currentState.daySelectionState.copy(
            selectedDays = repeatDays,
            isWeekdaysChecked = repeatDays.containsAll(setOf(AlarmDay.MON, AlarmDay.TUE, AlarmDay.WED, AlarmDay.THU, AlarmDay.FRI)),
            isWeekendsChecked = repeatDays.containsAll(setOf(AlarmDay.SAT, AlarmDay.SUN)),
        )
    }

    private fun setUpMissionState(
        alarm: Alarm,
        currentState: AlarmAddEditContract.State,
    ): AlarmAddEditContract.AlarmMissionState {
        return currentState.missionState.copy(
            missionType = alarm.missionType,
            missionCount = alarm.missionCount,
        )
    }

    private fun setupSnoozeState(
        alarm: Alarm,
        currentState: AlarmAddEditContract.State,
    ): AlarmAddEditContract.AlarmSnoozeState {
        return currentState.snoozeState.copy(
            isSnoozeEnabled = alarm.isSnoozeEnabled,
            snoozeInterval = alarm.snoozeInterval,
            snoozeCount = alarm.snoozeCount,
        )
    }

    override fun onCleared() {
        super.onCleared()
        alarmUseCase.releaseSoundPlayer()
    }

    fun processAction(action: AlarmAddEditContract.Action) {
        when (action) {
            is AlarmAddEditContract.Action.CheckUnsavedChangesBeforeExit -> checkUnsavedChangesBeforeExit()
            is AlarmAddEditContract.Action.NavigateBack -> navigateBack()
            is AlarmAddEditContract.Action.SaveAlarm -> saveAlarm()
            is AlarmAddEditContract.Action.ShowDeleteDialog -> showDeleteDialog()
            is AlarmAddEditContract.Action.HideDeleteDialog -> hideDeleteDialog()
            is AlarmAddEditContract.Action.ShowUnsavedChangesDialog -> showUnsavedChangesDialog()
            is AlarmAddEditContract.Action.HideUnsavedChangesDialog -> hideUnsavedChangesDialog()
            is AlarmAddEditContract.Action.DeleteAlarm -> deleteAlarm()
            is AlarmAddEditContract.Action.SetAlarmTime -> setAlarmTime(action.newTime)
            is AlarmAddEditContract.Action.ToggleWeekdaysSelection -> toggleWeekdaysSelection()
            is AlarmAddEditContract.Action.ToggleWeekendsSelection -> toggleWeekendsSelection()
            is AlarmAddEditContract.Action.ToggleSpecificDaySelection -> toggleSpecificDaySelection(action.day)
            is AlarmAddEditContract.Action.ToggleHolidaySkipOption -> toggleHolidaySkipOption()
            is AlarmAddEditContract.Action.SaveMissionSetting -> saveMissionSetting(action.type, action.count)
            is AlarmAddEditContract.Action.SaveSnoozeSetting -> saveSnoozeSetting(
                action.enabled,
                action.interval,
                action.count,
            )
            is AlarmAddEditContract.Action.SaveSoundSetting -> saveSoundSetting(
                action.vibrationEnabled,
                action.soundEnabled,
                action.soundVolume,
                action.soundIndex,
            )
            is AlarmAddEditContract.Action.ToggleVibrationEnabled -> toggleVibrationEnabled(action.enabled)
            is AlarmAddEditContract.Action.ToggleSoundEnabled -> toggleSoundEnabled(action.enabled)
            is AlarmAddEditContract.Action.SetSoundVolume -> setSoundVolume(action.volume)
            is AlarmAddEditContract.Action.SetSoundIndex -> setSoundIndex(action.index)
            is AlarmAddEditContract.Action.NavigateToMissionPreview -> navigateToMissionPreview(action.missionType, action.missionCount)
            is AlarmAddEditContract.Action.ShowBottomSheet -> showBottomSheet(action.sheetType)
            is AlarmAddEditContract.Action.HideBottomSheet -> hideBottomSheet()
        }
    }

    private fun checkUnsavedChangesBeforeExit() = intent {
        if (state.mode == AlarmAddEditContract.EditMode.ADD) {
            navigateBack()
        } else {
            val updatedAlarm = state.toAlarm()
            alarmUseCase.getAlarm(alarmId).onSuccess { existingAlarm ->
                if (updatedAlarm.copy(id = alarmId) != existingAlarm) {
                    showUnsavedChangesDialog()
                } else {
                    postSideEffect(AlarmAddEditContract.SideEffect.NavigateBack)
                }
            }
        }
    }

    private fun navigateBack() = intent {
        postSideEffect(AlarmAddEditContract.SideEffect.NavigateBack)
    }

    private fun navigateToMissionPreview(
        missionType: MissionType,
        missionCount: Int,
    ) = intent {
        val newTimeState = state.timeState.copy(
            initialTime = state.timeState.currentTime,
        )
        reduce {
            state.copy(
                timeState = newTimeState,
            )
        }
        postSideEffect(AlarmAddEditContract.SideEffect.NavigateToMissionPreview(missionType, missionCount))
    }

    private fun saveAlarm() = intent {
        val newAlarm = state.toAlarm()

        when (state.mode) {
            AlarmAddEditContract.EditMode.EDIT -> updateExistingAlarm(newAlarm)
            AlarmAddEditContract.EditMode.ADD -> checkAndCreateAlarm(newAlarm)
        }
    }

    private fun updateExistingAlarm(alarm: Alarm) = intent {
        val updatedAlarm = alarm.copy(id = alarmId)

        alarmUseCase.getAlarm(alarmId).onSuccess { oldAlarm ->
            alarmUseCase.unScheduleAlarm(oldAlarm)
        }

        alarmUseCase.updateAlarm(updatedAlarm)
            .onSuccess {
                alarmUseCase.scheduleAlarm(updatedAlarm)
                postSideEffect(AlarmAddEditContract.SideEffect.UpdateAlarm(it.id))
            }
            .onFailure {
                Log.e("AlarmAddEditViewModel", "Failed to update alarm", it)
            }
    }

    private suspend fun checkAndCreateAlarm(newAlarm: Alarm) {
        val timeMatchedAlarms = alarmUseCase.getAlarmsByTime(newAlarm.hour, newAlarm.minute)
            .first()

        when {
            timeMatchedAlarms.any { it.copy(id = 0) == newAlarm.copy(id = 0) } -> {
                showAlarmAlreadySetWarning()
            }

            timeMatchedAlarms.isNotEmpty() -> {
                val existingAlarm = timeMatchedAlarms.first()
                val updatedAlarm = existingAlarm.copyFrom(newAlarm).copy(id = existingAlarm.id)
                updateExistingAlarm(updatedAlarm)
            }

            else -> {
                createNewAlarm(newAlarm)
            }
        }
    }

    private fun showAlarmAlreadySetWarning() = intent {
        postSideEffect(
            AlarmAddEditContract.SideEffect.ShowSnackBar(
                message = resourceProvider.getString(R.string.alarm_already_set),
                iconRes = resourceProvider.getDrawable(core.designsystem.R.drawable.ic_alert),
                bottomPadding = 78.dp,
                onDismiss = { },
                onAction = { },
            ),
        )
    }

    private fun createNewAlarm(alarm: Alarm) = intent {
        alarmUseCase.insertAlarm(alarm)
            .onSuccess {
                analyticsHelper.logEvent(
                    AnalyticsEvent(
                        type = "alarm_create",
                        properties = mapOf(
                            AnalyticsEvent.AlarmPropertiesKeys.ALARM_ID to "${it.id}",
                            AnalyticsEvent.AlarmPropertiesKeys.REPEAT_DAYS to it.repeatDays.toAlarmDayNames(),
                            AnalyticsEvent.AlarmPropertiesKeys.SNOOZE_OPTION to listOf(it.snoozeInterval, it.snoozeCount),
                        ),
                    ),
                )
                alarmUseCase.scheduleAlarm(it)
                postSideEffect(AlarmAddEditContract.SideEffect.SaveAlarm(it.id))
            }
            .onFailure {
                Log.e("AlarmAddEditViewModel", "Failed to insert alarm", it)
            }
    }

    private fun setAlarmTime(newTime: LocalTime) = intent {
        val newTimeState = state.timeState.copy(
            currentTime = newTime,
            alarmMessage = getAlarmMessage(newTime, state.daySelectionState.selectedDays),
        )

        hapticFeedbackManager.performHapticFeedback(HapticType.LIGHT_TICK)

        reduce {
            state.copy(timeState = newTimeState)
        }
    }

    private fun showDeleteDialog() = intent {
        reduce { state.copy(isDeleteDialogVisible = true) }
    }

    private fun hideDeleteDialog() = intent {
        reduce { state.copy(isDeleteDialogVisible = false) }
    }

    private fun showUnsavedChangesDialog() = intent {
        reduce { state.copy(isUnsavedChangesDialogVisible = true) }
    }

    private fun hideUnsavedChangesDialog() = intent {
        reduce { state.copy(isUnsavedChangesDialogVisible = false) }
    }

    private fun deleteAlarm() = intent {
        postSideEffect(AlarmAddEditContract.SideEffect.DeleteAlarm(alarmId))
    }

    private fun toggleWeekdaysSelection() = intent {
        val weekdays = setOf(AlarmDay.MON, AlarmDay.TUE, AlarmDay.WED, AlarmDay.THU, AlarmDay.FRI)
        val isChecked = !state.daySelectionState.isWeekdaysChecked
        val updatedDays = if (isChecked) {
            state.daySelectionState.selectedDays + weekdays
        } else {
            state.daySelectionState.selectedDays - weekdays
        }
        val newDayState = state.daySelectionState.copy(
            isWeekdaysChecked = isChecked,
            selectedDays = updatedDays,
        )
        reduce {
            state.copy(
                timeState = state.timeState.copy(
                    alarmMessage = getAlarmMessage(state.timeState.currentTime, newDayState.selectedDays),
                ),
                daySelectionState = newDayState,
                holidayState = state.holidayState.copy(
                    isDisableHolidayEnabled = newDayState.selectedDays.isNotEmpty(),
                    isDisableHolidayChecked = if (newDayState.selectedDays.isEmpty()) false else state.holidayState.isDisableHolidayChecked,
                ),
            )
        }
    }

    private fun toggleWeekendsSelection() = intent {
        val weekends = setOf(AlarmDay.SAT, AlarmDay.SUN)
        val isChecked = !state.daySelectionState.isWeekendsChecked
        val updatedDays = if (isChecked) {
            state.daySelectionState.selectedDays + weekends
        } else {
            state.daySelectionState.selectedDays - weekends
        }
        val newDayState = state.daySelectionState.copy(
            isWeekendsChecked = isChecked,
            selectedDays = updatedDays,
        )
        reduce {
            state.copy(
                timeState = state.timeState.copy(
                    alarmMessage = getAlarmMessage(state.timeState.currentTime, newDayState.selectedDays),
                ),
                daySelectionState = newDayState,
                holidayState = state.holidayState.copy(
                    isDisableHolidayEnabled = newDayState.selectedDays.isNotEmpty(),
                    isDisableHolidayChecked = if (newDayState.selectedDays.isEmpty()) false else state.holidayState.isDisableHolidayChecked,
                ),
            )
        }
    }

    private fun toggleSpecificDaySelection(day: AlarmDay) = intent {
        val updatedDays = if (day in state.daySelectionState.selectedDays) {
            state.daySelectionState.selectedDays - day
        } else {
            state.daySelectionState.selectedDays + day
        }
        val weekdays = setOf(AlarmDay.MON, AlarmDay.TUE, AlarmDay.WED, AlarmDay.THU, AlarmDay.FRI)
        val weekends = setOf(AlarmDay.SAT, AlarmDay.SUN)

        val newDayState = state.daySelectionState.copy(
            selectedDays = updatedDays,
            isWeekdaysChecked = updatedDays.containsAll(weekdays),
            isWeekendsChecked = updatedDays.containsAll(weekends),
        )
        reduce {
            state.copy(
                timeState = state.timeState.copy(
                    alarmMessage = getAlarmMessage(state.timeState.currentTime, newDayState.selectedDays),
                ),
                daySelectionState = newDayState,
                holidayState = state.holidayState.copy(
                    isDisableHolidayEnabled = newDayState.selectedDays.isNotEmpty(),
                    isDisableHolidayChecked = if (newDayState.selectedDays.isEmpty()) false else state.holidayState.isDisableHolidayChecked,
                ),
            )
        }
    }

    private fun toggleHolidaySkipOption() = intent {
        val newHolidayState = state.holidayState.copy(
            isDisableHolidayChecked = !state.holidayState.isDisableHolidayChecked,
        )

        reduce {
            state.copy(holidayState = newHolidayState)
        }

        if (newHolidayState.isDisableHolidayChecked) {
            postSideEffect(
                AlarmAddEditContract.SideEffect.ShowSnackBar(
                    message = resourceProvider.getString(R.string.alarm_disabled_warning),
                    label = resourceProvider.getString(R.string.alarm_delete_dialog_btn_cancel),
                    iconRes = resourceProvider.getDrawable(core.designsystem.R.drawable.ic_check_green),
                    bottomPadding = 78.dp,
                    onDismiss = { },
                    onAction = {
                        intent {
                            reduce {
                                state.copy(
                                    holidayState = state.holidayState.copy(
                                        isDisableHolidayChecked = false,
                                    ),
                                )
                            }
                        }
                    },
                ),
            )
        }
    }

    private fun saveMissionSetting(type: MissionType, count: Int) = intent {
        val newMissionState = state.missionState.copy(
            missionType = type,
            missionCount = count,
        )
        reduce {
            state.copy(missionState = newMissionState)
        }
    }

    private fun saveSnoozeSetting(
        isSnoozeEnabled: Boolean,
        snoozeInterval: Int,
        snoozeCount: Int,
    ) = intent {
        val newSnoozeState = state.snoozeState.copy(
            isSnoozeEnabled = isSnoozeEnabled,
            snoozeInterval = snoozeInterval,
            snoozeCount = snoozeCount,
        )

        reduce {
            state.copy(snoozeState = newSnoozeState)
        }
    }

    private fun saveSoundSetting(
        vibrationEnabled: Boolean,
        soundEnabled: Boolean,
        soundVolume: Int,
        soundIndex: Int,
    ) = intent {
        val newSoundState = state.soundState.copy(
            isVibrationEnabled = vibrationEnabled,
            isSoundEnabled = soundEnabled,
            soundVolume = soundVolume,
            soundIndex = soundIndex,
        )

        reduce {
            state.copy(soundState = newSoundState)
        }
    }

    private fun toggleVibrationEnabled(enabled: Boolean) = intent {
        if (enabled) {
            hapticFeedbackManager.performHapticFeedback(HapticType.SUCCESS)
        }
    }

    private fun toggleSoundEnabled(enabled: Boolean) = intent {
        if (!enabled) {
            alarmUseCase.stopAlarmSound()
        }
    }

    private fun setSoundVolume(volume: Int) = intent {
        alarmUseCase.updateAlarmVolume(volume)
    }

    private fun setSoundIndex(index: Int) = intent {
        val selectedSound = state.soundState.sounds[index]
        alarmUseCase.initializeSoundPlayer(selectedSound.uri)
        alarmUseCase.playAlarmSound(state.soundState.soundVolume)
    }

    private fun showBottomSheet(sheetType: AlarmAddEditContract.BottomSheetType) = intent {
        postSideEffect(AlarmAddEditContract.SideEffect.ShowBottomSheet(sheetType))
    }

    private fun hideBottomSheet() = intent {
        postSideEffect(AlarmAddEditContract.SideEffect.HideBottomSheet)
    }

    private fun getAlarmMessage(currentTime: LocalTime, selectedDays: Set<AlarmDay>): String {
        val repeatDays = selectedDays.toRepeatDays()
        val nextOccurrence = alarmDateTimeFormatter.calculateNextOccurrence(
            hour = currentTime.hour,
            minute = currentTime.minute,
            repeatDays = repeatDays,
            now = LocalDateTime.now(),
        )

        return alarmDateTimeFormatter.formatTimeDifference(
            baseTime = LocalDateTime.now(),
            futureTime = nextOccurrence,
            formats = AlarmDateTimeFormatter.TimeDifferenceFormats(
                daysHoursMinutesFormat = resourceProvider.getString(R.string.alarm_remaining_time_days_hours),
                hoursMinutesFormat = resourceProvider.getString(R.string.alarm_remaining_time_hours_minutes),
                minutesFormat = resourceProvider.getString(R.string.alarm_remaining_time_minutes_only),
                soonFormat = resourceProvider.getString(R.string.alarm_remaining_time_soon),
            ),
        )
    }
}
