package com.yapp.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.yapp.alarm.AlarmHelper
import com.yapp.common.util.ResourceProvider
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.toAlarmDays
import com.yapp.domain.model.toDayOfWeek
import com.yapp.domain.repository.UserDataRepository
import com.yapp.domain.usecase.AlarmUseCase
import com.yapp.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import feature.home.R
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val alarmUseCase: AlarmUseCase,
    private val resourceProvider: ResourceProvider,
    private val alarmHelper: AlarmHelper,
    private val userDataRepository: UserDataRepository,
) : BaseViewModel<HomeContract.State, HomeContract.SideEffect>(
    initialState = HomeContract.State(),
) {
    init {
        loadAllAlarms()
        loadDailyFortuneState()
        loadUserName()
    }

    fun processAction(action: HomeContract.Action) {
        when (action) {
            HomeContract.Action.NavigateToAlarmCreation -> navigateToAlarmCreation()
            HomeContract.Action.ToggleMultiSelectionMode -> toggleMultiSelectionMode()
            HomeContract.Action.ShowDropDownMenu -> showDropDownMenu()
            HomeContract.Action.ShowSortDropDownMenu -> showSortDropDownMenu()
            HomeContract.Action.HideDropDownMenu -> hideDropDownMenu()
            is HomeContract.Action.ToggleAlarmSelection -> toggleAlarmSelection(action.alarmId)
            HomeContract.Action.ToggleAllAlarmSelection -> toggleAllAlarmSelection()
            is HomeContract.Action.ToggleAlarmActivation -> toggleAlarmActivation(action.alarmId)
            is HomeContract.Action.SwipeToDeleteAlarm -> deleteSingleAlarm(action.id)
            HomeContract.Action.ShowDeleteDialog -> showDeleteDialog()
            HomeContract.Action.HideDeleteDialog -> hideDeleteDialog()
            HomeContract.Action.ShowNoActivatedAlarmDialog -> showNoActivatedAlarmDialog()
            HomeContract.Action.HideNoActivatedAlarmDialog -> hideNoActivatedAlarmDialog()
            HomeContract.Action.ShowNoDailyFortuneDialog -> showNoDailyFortuneDialog()
            HomeContract.Action.HideNoDailyFortuneDialog -> hideNoDailyFortuneDialog()
            HomeContract.Action.HideToolTip -> hideToolTip()
            HomeContract.Action.RollbackPendingAlarmToggle -> rollbackAlarmActivation()
            HomeContract.Action.ConfirmDeletion -> confirmDeletion()
            is HomeContract.Action.DeleteSingleAlarm -> deleteSingleAlarm(action.alarmId)
            HomeContract.Action.LoadMoreAlarms -> loadAllAlarms()
            HomeContract.Action.ResetLastAddedAlarmIndex -> restLastAddedAlarmIndex()
            is HomeContract.Action.EditAlarm -> editAlarm(action.alarmId)
            HomeContract.Action.ShowDailyFortune -> loadDailyFortune()
            HomeContract.Action.NavigateToSetting -> navigateToSetting()
            is HomeContract.Action.ShowItemMenu -> showItemMenu(action.alarmId, action.x, action.y)
            HomeContract.Action.HideItemMenu -> hideItemMenu()
            is HomeContract.Action.SetSortOrder -> setSortOrder(action.sortOrder)
        }
    }

    fun scrollToAddedAlarm(id: Long) {
        val newAlarmIndex = currentState.alarms.indexOfFirst { it.id == id }
        if (newAlarmIndex == -1) return

        updateState {
            copy(
                lastAddedAlarmIndex = newAlarmIndex,
            )
        }

        emitSideEffect(
            HomeContract.SideEffect.ShowSnackBar(
                message = resourceProvider.getString(R.string.alarm_added),
                iconRes = resourceProvider.getDrawable(core.designsystem.R.drawable.ic_check_green),
                onAction = { },
                onDismiss = { },
            ),
        )
    }

    fun scrollToUpdatedAlarm(id: Long) {
        val updatedAlarmIndex = currentState.alarms.indexOfFirst { it.id == id }
        if (updatedAlarmIndex == -1) return

        updateState {
            copy(
                lastAddedAlarmIndex = updatedAlarmIndex,
            )
        }
    }

    private fun navigateToAlarmCreation() {
        emitSideEffect(HomeContract.SideEffect.NavigateToAddAlarm)
    }

    private fun toggleMultiSelectionMode() {
        updateState {
            copy(
                isSelectionMode = !currentState.isSelectionMode,
                selectedAlarmIds = emptySet(),
                dropdownMenuExpanded = false,
            )
        }
    }

    private fun showDropDownMenu() {
        updateState { copy(dropdownMenuExpanded = true) }
    }

    private fun showSortDropDownMenu() {
        updateState {
            copy(
                dropdownMenuExpanded = false,
                sortDropDownMenuExpanded = true,
            )
        }
    }

    private fun hideDropDownMenu() {
        updateState {
            copy(
                dropdownMenuExpanded = false,
                sortDropDownMenuExpanded = false,
            )
        }
    }

    private fun toggleAlarmSelection(alarmId: Long) {
        updateState {
            val updatedSelection = currentState.selectedAlarmIds.toMutableSet().apply {
                if (contains(alarmId)) remove(alarmId) else add(alarmId)
            }
            copy(selectedAlarmIds = updatedSelection)
        }
    }

    private fun toggleAllAlarmSelection() {
        updateState {
            val allIds = currentState.alarms.map { it.id }.toSet()
            val updatedSelection = if (currentState.selectedAlarmIds == allIds) emptySet() else allIds
            copy(selectedAlarmIds = updatedSelection)
        }
    }

    private fun toggleAlarmActivation(alarmId: Long) {
        viewModelScope.launch {
            val currentIndex = currentState.alarms.indexOfFirst { it.id == alarmId }
            if (currentIndex == -1) return@launch

            val currentAlarm = currentState.alarms[currentIndex]
            val previousState = currentAlarm.isAlarmActive // 기존 상태 저장
            val updatedAlarm = currentAlarm.copy(isAlarmActive = !currentAlarm.isAlarmActive)

            alarmUseCase.updateAlarmActive(alarmId, updatedAlarm.isAlarmActive).onSuccess {
                val updatedAlarms = currentState.alarms.toMutableList()
                updatedAlarms[currentIndex] = updatedAlarm

                val hasActivatedAlarm = updatedAlarms.any { it.isAlarmActive }
                updateState {
                    copy(
                        alarms = updatedAlarms,
                        isNoActivatedAlarmDialogVisible = !hasActivatedAlarm,
                        pendingAlarmToggle = if (!hasActivatedAlarm) alarmId to previousState else null,
                    )
                }

                if (updatedAlarm.isAlarmActive) {
                    alarmHelper.scheduleAlarm(updatedAlarm)
                } else {
                    alarmHelper.unScheduleAlarm(updatedAlarm)
                }
            }.onFailure { error ->
                Log.e("HomeViewModel", "Failed to update alarm state", error)
            }
        }
    }

    private fun showDeleteDialog() {
        updateState { copy(isDeleteDialogVisible = true) }
    }

    private fun hideDeleteDialog() {
        updateState { copy(isDeleteDialogVisible = false) }
    }

    private fun confirmDeletion() {
        deleteAlarms(currentState.selectedAlarmIds)
        updateState {
            copy(
                selectedAlarmIds = emptySet(),
                isDeleteDialogVisible = false,
            )
        }
    }

    private fun showNoActivatedAlarmDialog() {
        updateState { copy(isNoActivatedAlarmDialogVisible = true) }
    }

    private fun hideNoActivatedAlarmDialog() {
        updateState {
            copy(
                isNoActivatedAlarmDialogVisible = false,
                pendingAlarmToggle = null,
            )
        }
    }

    private fun rollbackAlarmActivation() {
        val pendingAlarm = currentState.pendingAlarmToggle ?: return
        val (alarmId, previousState) = pendingAlarm

        viewModelScope.launch {
            val currentIndex = currentState.alarms.indexOfFirst { it.id == alarmId }
            if (currentIndex == -1) return@launch

            val currentAlarm = currentState.alarms[currentIndex]
            val restoredAlarm = currentAlarm.copy(isAlarmActive = previousState)

            alarmUseCase.updateAlarm(restoredAlarm).onSuccess { updatedAlarm ->
                val updatedAlarms = currentState.alarms.toMutableList()
                updatedAlarms[currentIndex] = updatedAlarm
                updateState {
                    copy(
                        alarms = updatedAlarms,
                        pendingAlarmToggle = null,
                        isNoActivatedAlarmDialogVisible = false,
                    )
                }

                if (updatedAlarm.isAlarmActive) {
                    alarmHelper.scheduleAlarm(updatedAlarm)
                } else {
                    alarmHelper.unScheduleAlarm(updatedAlarm)
                }
            }.onFailure { error ->
                Log.e("HomeViewModel", "Failed to rollback alarm state", error)
            }
        }
    }

    private fun deleteSingleAlarm(alarmId: Long) {
        deleteAlarms(setOf(alarmId))
    }

    private fun deleteAlarms(alarmIds: Set<Long>) {
        if (alarmIds.isEmpty()) return

        val alarmsToDelete = currentState.alarms
            .filter { it.id in alarmIds }

        viewModelScope.launch {
            alarmsToDelete.forEach { alarm ->
                alarmUseCase.deleteAlarm(alarm.id)
                alarmHelper.unScheduleAlarm(alarm)
            }
        }

        if (currentState.activeItemMenu != null) {
            hideItemMenu()
        }

        emitSideEffect(
            HomeContract.SideEffect.ShowSnackBar(
                message = resourceProvider.getString(R.string.alarm_deleted),
                label = resourceProvider.getString(R.string.alarm_delete_dialog_btn_cancel),
                iconRes = resourceProvider.getDrawable(core.designsystem.R.drawable.ic_check_green),
                onDismiss = { },
                onAction = {
                    restoreDeletedAlarms(alarmsToDelete)
                },
            ),
        )
    }

    private fun restoreDeletedAlarms(alarmsWithIndex: List<Alarm>) {
        viewModelScope.launch {
            alarmsWithIndex.forEach { alarm ->
                alarmUseCase.insertAlarm(alarm)
                alarmHelper.scheduleAlarm(alarm)
            }
        }
    }

    private fun restLastAddedAlarmIndex() {
        updateState { copy(lastAddedAlarmIndex = null) }
    }

    private fun loadAllAlarms() {
        updateState { copy(initialLoading = true) }

        viewModelScope.launch {
            alarmUseCase.getAllAlarms().collect {
                updateState {
                    copy(
                        alarms = it,
                        initialLoading = false,
                    )
                }
                updateDeliveryTime(it)
            }
        }
    }

    private fun editAlarm(alarmId: Long) {
        emitSideEffect(HomeContract.SideEffect.NavigateToEditAlarm(alarmId))
    }

    private fun updateDeliveryTime(alarms: List<Alarm>) {
        val earliestAlarm = alarms
            .filter { it.isAlarmActive }
            .minByOrNull { alarm ->
                getNextAlarmDateWithTime(alarm.isAm, alarm.hour, alarm.minute, alarm.repeatDays)
            }

        val deliveryTime = earliestAlarm?.let { alarm ->
            val alarmDateTime = getNextAlarmDateWithTime(alarm.isAm, alarm.hour, alarm.minute, alarm.repeatDays)
            alarmDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        } ?: "NONE"

        updateState { copy(deliveryTime = formatDeliveryTime(deliveryTime)) }
    }

    private fun getNextAlarmDateWithTime(isAm: Boolean, hour: Int, minute: Int, repeatDays: Int): LocalDateTime {
        val now = LocalDateTime.now()

        val alarmHour = when {
            isAm && hour == 12 -> 0
            !isAm && hour != 12 -> hour + 12
            else -> hour
        }
        val alarmTime = LocalTime.of(alarmHour, minute)
        val todayAlarm = LocalDateTime.of(now.toLocalDate(), alarmTime)

        // 반복 요일이 설정되지 않은 경우 → 단일 알람
        if (repeatDays == 0) {
            return if (todayAlarm.isAfter(now)) todayAlarm else todayAlarm.plusDays(1)
        }

        // 비트마스크 기반 반복 요일 추출
        val selectedDays = repeatDays.toAlarmDays().map { it.toDayOfWeek() }.sortedBy { it.value }
        val currentDayOfWeek = now.dayOfWeek

        // 가장 빠른 다음 알람 날짜 계산
        val nextDayOffset = selectedDays
            .map { (it.value + 7 - currentDayOfWeek.value) % 7 }
            .filter { it > 0 || todayAlarm.isAfter(now) }
            .minOrNull() ?: (selectedDays.first().value + 7 - currentDayOfWeek.value)

        return todayAlarm.plusDays(nextDayOffset.toLong())
    }

    private fun formatDeliveryTime(deliveryTime: String): String {
        return try {
            if (deliveryTime == "NONE") return resourceProvider.getString(R.string.home_fortune_no_alarm)

            val inputDateTime = LocalDateTime.parse(deliveryTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            val now = LocalDateTime.now()
            val today = now.toLocalDate()
            val tomorrow = today.plusDays(1)

            return when {
                inputDateTime.toLocalDate() == today ->
                    resourceProvider.getString(R.string.home_fortune_delivery_today, inputDateTime.format(DateTimeFormatter.ofPattern("a h:mm")))
                inputDateTime.toLocalDate() == tomorrow ->
                    resourceProvider.getString(R.string.home_fortune_delivery_tomorrow, inputDateTime.format(DateTimeFormatter.ofPattern("a h:mm")))
                inputDateTime.year == now.year ->
                    resourceProvider.getString(
                        R.string.home_fortune_delivery_this_year,
                        inputDateTime.format(DateTimeFormatter.ofPattern("M월 d일 a h:mm")),
                    )
                else ->
                    resourceProvider.getString(
                        R.string.home_fortune_delivery_other_year,
                        inputDateTime.format(DateTimeFormatter.ofPattern("yy년 M월 d일 a h:mm")),
                    )
            }
        } catch (e: Exception) {
            resourceProvider.getString(R.string.home_fortune_no_alarm)
        }
    }

    private fun loadDailyFortune() {
        viewModelScope.launch {
            val fortuneDate = userDataRepository.fortuneDateFlow.firstOrNull()
            val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

            Log.d("HomeViewModel", "fortuneDate: $fortuneDate, todayDate: $todayDate")

            if (fortuneDate != todayDate) {
                processAction(HomeContract.Action.ShowNoDailyFortuneDialog)
            } else {
                userDataRepository.markFortuneAsChecked()
                emitSideEffect(HomeContract.SideEffect.NavigateToFortune)
            }
        }
    }

    private fun loadDailyFortuneState() {
        viewModelScope.launch {
            val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

            combine(
                userDataRepository.fortuneDateFlow,
                userDataRepository.fortuneScoreFlow,
                userDataRepository.hasNewFortuneFlow,
            ) { fortuneDate, fortuneScore, hasNewFortune ->
                val isTodayFortuneAvailable = fortuneDate == todayDate
                val finalFortuneScore = if (isTodayFortuneAvailable) fortuneScore ?: -1 else -1

                Pair(finalFortuneScore, hasNewFortune)
            }.collect { (finalFortuneScore, hasNewFortune) ->
                updateState {
                    copy(
                        lastFortuneScore = finalFortuneScore,
                        hasNewFortune = hasNewFortune,
                        isToolTipVisible = hasNewFortune,
                    )
                }
            }
        }
    }

    private fun loadUserName() {
        viewModelScope.launch {
            userDataRepository.userNameFlow.collect { userName ->
                updateState { copy(name = userName ?: "") }
            }
        }
    }

    private fun showNoDailyFortuneDialog() {
        updateState { copy(isNoDailyFortuneDialogVisible = true) }
    }

    private fun hideNoDailyFortuneDialog() {
        updateState { copy(isNoDailyFortuneDialogVisible = false) }
    }

    private fun hideToolTip() {
        updateState { copy(isToolTipVisible = false) }
    }

    private fun navigateToSetting() {
        emitSideEffect(HomeContract.SideEffect.NavigateToSetting)
    }

    private fun showItemMenu(alarmId: Long, x: Float, y: Float) {
        updateState {
            copy(
                activeItemMenu = alarmId,
                activeItemMenuPosition = x to y,
            )
        }
    }

    private fun hideItemMenu() {
        updateState {
            copy(
                activeItemMenu = null,
                activeItemMenuPosition = null,
            )
        }
    }

    private fun setSortOrder(sortOrder: HomeContract.AlarmSortOrder) {
        updateState { copy(sortOrder = sortOrder) }
        hideDropDownMenu()
    }
}
