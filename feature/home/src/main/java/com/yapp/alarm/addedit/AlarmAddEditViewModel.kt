package com.yapp.alarm.addedit

import android.util.Log
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yapp.analytics.AnalyticsEvent
import com.yapp.analytics.AnalyticsHelper
import com.yapp.common.util.ResourceProvider
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmDay
import com.yapp.domain.model.AlarmSound
import com.yapp.domain.model.copyFrom
import com.yapp.domain.model.toAlarmDayNames
import com.yapp.domain.model.toAlarmDays
import com.yapp.domain.model.toDayOfWeek
import com.yapp.domain.scheduler.AlarmScheduler
import com.yapp.domain.usecase.AlarmUseCase
import com.yapp.media.haptic.HapticFeedbackManager
import com.yapp.media.haptic.HapticType
import com.yapp.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import feature.home.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AlarmAddEditViewModel @Inject constructor(
    private val analyticsHelper: AnalyticsHelper,
    private val alarmUseCase: AlarmUseCase,
    private val resourceProvider: ResourceProvider,
    private val hapticFeedbackManager: HapticFeedbackManager,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<AlarmAddEditContract.State, AlarmAddEditContract.SideEffect>(
    initialState = AlarmAddEditContract.State(),
) {
    private val alarmId: Long = savedStateHandle.get<Long>("alarmId") ?: -1

    init {
        updateState { copy(mode = if (alarmId == -1L) AlarmAddEditContract.EditMode.ADD else AlarmAddEditContract.EditMode.EDIT) }
        initializeAlarmScreen()
    }

    private fun initializeAlarmScreen() = viewModelScope.launch {
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

    private fun setupNewAlarmScreen(sounds: List<AlarmSound>) {
        val defaultSoundIndex = sounds.indexOfFirst { it.title == "Homecoming" }.takeIf { it >= 0 } ?: 0
        val defaultSound = sounds[defaultSoundIndex]

        alarmUseCase.initializeSoundPlayer(defaultSound.uri)

        val now = LocalTime.now()
        val initialAmPm = if (now.hour < 12) "오전" else "오후"
        val initialHour = if (now.hour == 0 || now.hour == 12) 12 else now.hour % 12
        val initialMinute = now.minute

        updateState {
            copy(
                initialLoading = false,
                timeState = timeState.copy(
                    initialAmPm = initialAmPm,
                    initialHour = "$initialHour",
                    initialMinute = initialMinute.toString().padStart(2, '0'),
                    currentAmPm = initialAmPm,
                    currentHour = initialHour,
                    currentMinute = initialMinute,
                    alarmMessage = getAlarmMessage(initialAmPm, initialHour, initialMinute, emptySet()),
                ),
                soundState = soundState.copy(sounds = sounds, soundIndex = defaultSoundIndex),
            )
        }
    }

    private suspend fun loadExistingAlarm(sounds: List<AlarmSound>) {
        alarmUseCase.getAlarm(alarmId).onSuccess { alarm ->
            val repeatDays = alarm.repeatDays.toAlarmDays()
            val isAM = alarm.isAm
            val hour = alarm.hour
            val selectedSoundIndex = sounds.indexOfFirst { it.uri.toString() == alarm.soundUri }
            val selectedSound = sounds.getOrNull(selectedSoundIndex) ?: sounds.first()

            alarmUseCase.initializeSoundPlayer(selectedSound.uri)

            updateState {
                copy(
                    initialLoading = false,
                    timeState = timeState.copy(
                        initialAmPm = if (isAM) "오전" else "오후",
                        initialHour = "$hour",
                        initialMinute = alarm.minute.toString().padStart(2, '0'),
                        currentAmPm = if (isAM) "오전" else "오후",
                        currentHour = hour,
                        currentMinute = alarm.minute,
                        alarmMessage = getAlarmMessage(if (isAM) "오전" else "오후", hour, alarm.minute, repeatDays),
                    ),
                    daySelectionState = setupDaySelectionState(repeatDays),
                    holidayState = holidayState.copy(
                        isDisableHolidayEnabled = repeatDays.isNotEmpty(),
                        isDisableHolidayChecked = alarm.isHolidayAlarmOff,
                    ),
                    snoozeState = setupSnoozeState(alarm),
                    soundState = soundState.copy(
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

    private fun setupDaySelectionState(repeatDays: Set<AlarmDay>) = currentState.daySelectionState.copy(
        selectedDays = repeatDays,
        isWeekdaysChecked = repeatDays.containsAll(setOf(AlarmDay.MON, AlarmDay.TUE, AlarmDay.WED, AlarmDay.THU, AlarmDay.FRI)),
        isWeekendsChecked = repeatDays.containsAll(setOf(AlarmDay.SAT, AlarmDay.SUN)),
    )

    private fun setupSnoozeState(alarm: Alarm) = currentState.snoozeState.copy(
        isSnoozeEnabled = alarm.isSnoozeEnabled,
        snoozeIntervalIndex = findSnoozeIndex(alarm.snoozeInterval, currentState.snoozeState.snoozeIntervals),
        snoozeCountIndex = findSnoozeIndex(alarm.snoozeCount, currentState.snoozeState.snoozeCounts),
    )

    private fun findSnoozeIndex(value: Int, list: List<String>): Int {
        return list.indexOfFirst {
            it == "무한" && value == -1 || it.filter { char -> char.isDigit() }.toIntOrNull() == value
        }.takeIf { it >= 0 } ?: 0
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
            is AlarmAddEditContract.Action.SetAlarmTime -> setAlarmTime(action.amPm, action.hour, action.minute)
            is AlarmAddEditContract.Action.ToggleWeekdaysSelection -> toggleWeekdaysSelection()
            is AlarmAddEditContract.Action.ToggleWeekendsSelection -> toggleWeekendsSelection()
            is AlarmAddEditContract.Action.ToggleSpecificDaySelection -> toggleSpecificDaySelection(action.day)
            is AlarmAddEditContract.Action.ToggleHolidaySkipOption -> toggleHolidaySkipOption()
            is AlarmAddEditContract.Action.ToggleSnoozeOption -> toggleSnoozeOption()
            is AlarmAddEditContract.Action.SetSnoozeInterval -> setSnoozeInterval(action.index)
            is AlarmAddEditContract.Action.SetSnoozeRepeatCount -> setSnoozeRepeatCount(action.index)
            is AlarmAddEditContract.Action.ToggleVibrationOption -> toggleVibrationOption()
            is AlarmAddEditContract.Action.ToggleSoundOption -> toggleSoundOption()
            is AlarmAddEditContract.Action.AdjustSoundVolume -> adjustSoundVolume(action.volume)
            is AlarmAddEditContract.Action.SelectAlarmSound -> selectAlarmSound(action.index)
            is AlarmAddEditContract.Action.ToggleBottomSheet -> toggleBottomSheet(action.sheetType)
        }
    }

    private fun checkUnsavedChangesBeforeExit() {
        if (currentState.mode == AlarmAddEditContract.EditMode.ADD) {
            navigateBack()
        } else {
            val updatedAlarm = currentState.toAlarm()
            viewModelScope.launch {
                alarmUseCase.getAlarm(alarmId).onSuccess { existingAlarm ->
                    if (updatedAlarm.copy(id = alarmId) != existingAlarm) {
                        showUnsavedChangesDialog()
                    } else {
                        emitSideEffect(AlarmAddEditContract.SideEffect.NavigateBack)
                    }
                }
            }
        }
    }

    private fun navigateBack() {
        emitSideEffect(AlarmAddEditContract.SideEffect.NavigateBack)
    }

    private fun saveAlarm() {
        val newAlarm = currentState.toAlarm()

        viewModelScope.launch {
            when (currentState.mode) {
                AlarmAddEditContract.EditMode.EDIT -> updateExistingAlarm(newAlarm)
                AlarmAddEditContract.EditMode.ADD -> checkAndCreateAlarm(newAlarm)
            }
        }
    }

    private suspend fun updateExistingAlarm(alarm: Alarm) {
        val updatedAlarm = alarm.copy(id = alarmId)

        alarmUseCase.getAlarm(alarmId).onSuccess { oldAlarm ->
            alarmScheduler.unScheduleAlarm(oldAlarm)
        }

        alarmUseCase.updateAlarm(updatedAlarm)
            .onSuccess {
                alarmScheduler.scheduleAlarm(updatedAlarm)
                emitSideEffect(AlarmAddEditContract.SideEffect.UpdateAlarm(it.id))
            }
            .onFailure {
                Log.e("AlarmAddEditViewModel", "Failed to update alarm", it)
            }
    }

    private suspend fun checkAndCreateAlarm(newAlarm: Alarm) {
        val timeMatchedAlarms = alarmUseCase.getAlarmsByTime(newAlarm.hour, newAlarm.minute, newAlarm.isAm)
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

    private fun showAlarmAlreadySetWarning() {
        emitSideEffect(
            AlarmAddEditContract.SideEffect.ShowSnackBar(
                message = resourceProvider.getString(R.string.alarm_already_set),
                iconRes = resourceProvider.getDrawable(core.designsystem.R.drawable.ic_alert),
                bottomPadding = 78.dp,
                onDismiss = { },
                onAction = { },
            ),
        )
    }

    private suspend fun createNewAlarm(alarm: Alarm) {
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
                alarmScheduler.scheduleAlarm(it)
                emitSideEffect(AlarmAddEditContract.SideEffect.SaveAlarm(it.id))
            }
            .onFailure {
                Log.e("AlarmAddEditViewModel", "Failed to insert alarm", it)
            }
    }

    private fun setAlarmTime(amPm: String, hour: Int, minute: Int) {
        val newTimeState = currentState.timeState.copy(
            currentAmPm = amPm,
            currentHour = hour,
            currentMinute = minute,
            alarmMessage = getAlarmMessage(amPm, hour, minute, currentState.daySelectionState.selectedDays),
        )

        hapticFeedbackManager.performHapticFeedback(HapticType.LIGHT_TICK)

        updateState {
            copy(timeState = newTimeState)
        }
    }

    private fun showDeleteDialog() {
        updateState { copy(isDeleteDialogVisible = true) }
    }

    private fun hideDeleteDialog() {
        updateState { copy(isDeleteDialogVisible = false) }
    }

    private fun showUnsavedChangesDialog() {
        updateState { copy(isUnsavedChangesDialogVisible = true) }
    }

    private fun hideUnsavedChangesDialog() {
        updateState { copy(isUnsavedChangesDialogVisible = false) }
    }

    private fun deleteAlarm() {
        emitSideEffect(AlarmAddEditContract.SideEffect.DeleteAlarm(alarmId))
    }

    private fun toggleWeekdaysSelection() {
        val weekdays = setOf(AlarmDay.MON, AlarmDay.TUE, AlarmDay.WED, AlarmDay.THU, AlarmDay.FRI)
        val isChecked = !currentState.daySelectionState.isWeekdaysChecked
        val updatedDays = if (isChecked) {
            currentState.daySelectionState.selectedDays + weekdays
        } else {
            currentState.daySelectionState.selectedDays - weekdays
        }
        val newDayState = currentState.daySelectionState.copy(
            isWeekdaysChecked = isChecked,
            selectedDays = updatedDays,
        )
        updateState {
            copy(
                timeState = timeState.copy(
                    alarmMessage = getAlarmMessage(timeState.currentAmPm, timeState.currentHour, timeState.currentMinute, newDayState.selectedDays),
                ),
                daySelectionState = newDayState,
                holidayState = holidayState.copy(
                    isDisableHolidayEnabled = newDayState.selectedDays.isNotEmpty(),
                    isDisableHolidayChecked = if (newDayState.selectedDays.isEmpty()) false else holidayState.isDisableHolidayChecked,
                ),
            )
        }
    }

    private fun toggleWeekendsSelection() {
        val weekends = setOf(AlarmDay.SAT, AlarmDay.SUN)
        val isChecked = !currentState.daySelectionState.isWeekendsChecked
        val updatedDays = if (isChecked) {
            currentState.daySelectionState.selectedDays + weekends
        } else {
            currentState.daySelectionState.selectedDays - weekends
        }
        val newDayState = currentState.daySelectionState.copy(
            isWeekendsChecked = isChecked,
            selectedDays = updatedDays,
        )
        updateState {
            copy(
                timeState = timeState.copy(
                    alarmMessage = getAlarmMessage(timeState.currentAmPm, timeState.currentHour, timeState.currentMinute, newDayState.selectedDays),
                ),
                daySelectionState = newDayState,
                holidayState = holidayState.copy(
                    isDisableHolidayEnabled = newDayState.selectedDays.isNotEmpty(),
                    isDisableHolidayChecked = if (newDayState.selectedDays.isEmpty()) false else holidayState.isDisableHolidayChecked,
                ),
            )
        }
    }

    private fun toggleSpecificDaySelection(day: AlarmDay) {
        val updatedDays = if (day in currentState.daySelectionState.selectedDays) {
            currentState.daySelectionState.selectedDays - day
        } else {
            currentState.daySelectionState.selectedDays + day
        }
        val weekdays = setOf(AlarmDay.MON, AlarmDay.TUE, AlarmDay.WED, AlarmDay.THU, AlarmDay.FRI)
        val weekends = setOf(AlarmDay.SAT, AlarmDay.SUN)

        val newDayState = currentState.daySelectionState.copy(
            selectedDays = updatedDays,
            isWeekdaysChecked = updatedDays.containsAll(weekdays),
            isWeekendsChecked = updatedDays.containsAll(weekends),
        )
        updateState {
            copy(
                timeState = timeState.copy(
                    alarmMessage = getAlarmMessage(timeState.currentAmPm, timeState.currentHour, timeState.currentMinute, newDayState.selectedDays),
                ),
                daySelectionState = newDayState,
                holidayState = holidayState.copy(
                    isDisableHolidayEnabled = newDayState.selectedDays.isNotEmpty(),
                    isDisableHolidayChecked = if (newDayState.selectedDays.isEmpty()) false else holidayState.isDisableHolidayChecked,
                ),
            )
        }
    }

    private fun toggleHolidaySkipOption() {
        val newHolidayState = currentState.holidayState.copy(
            isDisableHolidayChecked = !currentState.holidayState.isDisableHolidayChecked,
        )

        updateState {
            copy(holidayState = newHolidayState)
        }

        if (newHolidayState.isDisableHolidayChecked) {
            emitSideEffect(
                AlarmAddEditContract.SideEffect.ShowSnackBar(
                    message = resourceProvider.getString(R.string.alarm_disabled_warning),
                    label = resourceProvider.getString(R.string.alarm_delete_dialog_btn_cancel),
                    iconRes = resourceProvider.getDrawable(core.designsystem.R.drawable.ic_check_green),
                    bottomPadding = 78.dp,
                    onDismiss = { },
                    onAction = {
                        updateState {
                            copy(holidayState = holidayState.copy(isDisableHolidayChecked = false))
                        }
                    },
                ),
            )
        }
    }

    private fun toggleSnoozeOption() {
        val newSnoozeState = currentState.snoozeState.copy(
            isSnoozeEnabled = !currentState.snoozeState.isSnoozeEnabled,
        )
        updateState {
            copy(snoozeState = newSnoozeState)
        }
    }

    private fun setSnoozeInterval(index: Int) {
        val newSnoozeState = currentState.snoozeState.copy(snoozeIntervalIndex = index)
        updateState {
            copy(snoozeState = newSnoozeState)
        }
    }

    private fun setSnoozeRepeatCount(index: Int) {
        val newSnoozeState = currentState.snoozeState.copy(snoozeCountIndex = index)
        updateState {
            copy(snoozeState = newSnoozeState)
        }
    }

    private fun toggleVibrationOption() {
        val newSoundState = currentState.soundState.copy(isVibrationEnabled = !currentState.soundState.isVibrationEnabled)

        if (newSoundState.isVibrationEnabled) {
            hapticFeedbackManager.performHapticFeedback(HapticType.SUCCESS)
        }
        updateState {
            copy(soundState = newSoundState)
        }
    }

    private fun toggleSoundOption() {
        val newSoundState = currentState.soundState.copy(isSoundEnabled = !currentState.soundState.isSoundEnabled)
        if (!newSoundState.isSoundEnabled) {
            alarmUseCase.stopAlarmSound()
        }
        updateState {
            copy(soundState = newSoundState)
        }
    }

    private fun adjustSoundVolume(volume: Int) {
        val newSoundState = currentState.soundState.copy(soundVolume = volume)
        alarmUseCase.updateAlarmVolume(volume)
        updateState {
            copy(soundState = newSoundState)
        }
    }

    private fun selectAlarmSound(index: Int) {
        val newSoundState = currentState.soundState.copy(soundIndex = index)
        updateState {
            copy(soundState = newSoundState)
        }

        val selectedSound = currentState.soundState.sounds[index]
        alarmUseCase.initializeSoundPlayer(selectedSound.uri)
        alarmUseCase.playAlarmSound(currentState.soundState.soundVolume)
    }

    private fun toggleBottomSheet(sheetType: AlarmAddEditContract.BottomSheetType) {
        val newBottomSheetState = if (currentState.bottomSheetState == sheetType) {
            if (currentState.bottomSheetState == AlarmAddEditContract.BottomSheetType.SoundSetting) {
                alarmUseCase.stopAlarmSound()
            }
            null
        } else {
            sheetType
        }
        updateState {
            copy(bottomSheetState = newBottomSheetState)
        }
    }

    private fun getAlarmMessage(amPm: String, hour: Int, minute: Int, selectedDays: Set<AlarmDay>): String {
        val now = java.time.LocalDateTime.now()
        val alarmHour = convertTo24HourFormat(amPm, hour)
        val alarmTimeToday = now.toLocalDate().atTime(alarmHour, minute)
        val nextAlarmDateTime = calculateNextAlarmDateTime(now, alarmTimeToday, selectedDays)
        val duration = java.time.Duration.between(now, nextAlarmDateTime)
        val totalMinutes = duration.toMinutes()
        val days = totalMinutes / (24 * 60)
        val hours = (totalMinutes % (24 * 60)) / 60
        val minutes = totalMinutes % 60

        return when {
            days > 0 -> "${days}일 ${hours}시간 후에 울려요"
            hours > 0 -> "${hours}시간 ${minutes}분 후에 울려요"
            minutes == 0L -> "곧 울려요"
            else -> "${minutes}분 후에 울려요"
        }
    }

    private fun convertTo24HourFormat(amPm: String, hour: Int): Int = when {
        amPm == "오후" && hour != 12 -> hour + 12
        amPm == "오전" && hour == 12 -> 0
        else -> hour
    }

    private fun calculateNextAlarmDateTime(
        now: java.time.LocalDateTime,
        alarmTimeToday: java.time.LocalDateTime,
        selectedDays: Set<AlarmDay>,
    ): java.time.LocalDateTime {
        if (selectedDays.isEmpty()) {
            return if (alarmTimeToday.isBefore(now)) {
                alarmTimeToday.plusDays(1)
            } else {
                alarmTimeToday
            }
        }

        val currentDayOfWeek = now.dayOfWeek.value
        val selectedDaysOfWeek = selectedDays.map { it.toDayOfWeek().value }.sorted()

        if (selectedDaysOfWeek.contains(currentDayOfWeek) && now.toLocalTime().isBefore(alarmTimeToday.toLocalTime())) {
            return alarmTimeToday
        }

        val nextDay = selectedDaysOfWeek.firstOrNull { it > currentDayOfWeek }
            ?: selectedDaysOfWeek.first()
        val daysToAdd = if (nextDay > currentDayOfWeek) {
            nextDay - currentDayOfWeek
        } else {
            7 - (currentDayOfWeek - nextDay)
        }

        val nextAlarmDate = now.toLocalDate().plusDays(daysToAdd.toLong())
        return nextAlarmDate.atTime(alarmTimeToday.toLocalTime())
    }
}
