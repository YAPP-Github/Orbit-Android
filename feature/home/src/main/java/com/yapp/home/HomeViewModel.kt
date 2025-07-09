package com.yapp.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.yapp.common.util.ResourceProvider
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.toAlarmDays
import com.yapp.domain.model.toDayOfWeek
import com.yapp.domain.repository.FortuneRepository
import com.yapp.domain.repository.UserInfoRepository
import com.yapp.domain.scheduler.AlarmScheduler
import com.yapp.domain.usecase.AlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import feature.home.R
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.syntax.simple.repeatOnSubscription
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val alarmUseCase: AlarmUseCase,
    private val resourceProvider: ResourceProvider,
    private val alarmScheduler: AlarmScheduler,
    private val fortuneRepository: FortuneRepository,
    private val userInfoRepository: UserInfoRepository,
) : ViewModel(), ContainerHost<HomeContract.State, HomeContract.SideEffect> {

    override val container: Container<HomeContract.State, HomeContract.SideEffect> = container(
        initialState = HomeContract.State(),
    ) {
        intent {
            repeatOnSubscription {
                loadAllAlarms()
                loadDailyFortuneState()
                loadUserName()
            }
        }
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

    fun scrollToAddedAlarm(id: Long) = intent {
        val newAlarmIndex = state.alarms.indexOfFirst { it.id == id }
        if (newAlarmIndex == -1) return@intent

        reduce {
            state.copy(
                lastAddedAlarmIndex = newAlarmIndex,
            )
        }

        postSideEffect(
            HomeContract.SideEffect.ShowSnackBar(
                message = resourceProvider.getString(R.string.alarm_added),
                iconRes = resourceProvider.getDrawable(core.designsystem.R.drawable.ic_check_green),
                onAction = { },
                onDismiss = { },
            ),
        )
    }

    fun scrollToUpdatedAlarm(id: Long) = intent {
        val updatedAlarmIndex = state.alarms.indexOfFirst { it.id == id }
        if (updatedAlarmIndex == -1) return@intent

        reduce {
            state.copy(
                lastAddedAlarmIndex = updatedAlarmIndex,
            )
        }
    }

    private fun navigateToAlarmCreation() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateToAddAlarm)
    }

    private fun toggleMultiSelectionMode() = intent {
        reduce {
            state.copy(
                isSelectionMode = !state.isSelectionMode,
                selectedAlarmIds = emptySet(),
                dropdownMenuExpanded = false,
            )
        }
    }

    private fun showDropDownMenu() = intent {
        reduce { state.copy(dropdownMenuExpanded = true) }
    }

    private fun showSortDropDownMenu() = intent {
        reduce {
            state.copy(
                dropdownMenuExpanded = false,
                sortDropDownMenuExpanded = true,
            )
        }
    }

    private fun hideDropDownMenu() = intent {
        reduce {
            state.copy(
                dropdownMenuExpanded = false,
                sortDropDownMenuExpanded = false,
            )
        }
    }

    private fun toggleAlarmSelection(alarmId: Long) = intent {
        reduce {
            val updatedSelection = state.selectedAlarmIds.toMutableSet().apply {
                if (contains(alarmId)) remove(alarmId) else add(alarmId)
            }
            state.copy(selectedAlarmIds = updatedSelection)
        }
    }

    private fun toggleAllAlarmSelection() = intent {
        reduce {
            val allIds = state.alarms.map { it.id }.toSet()
            val updatedSelection = if (state.selectedAlarmIds == allIds) emptySet() else allIds
            state.copy(selectedAlarmIds = updatedSelection)
        }
    }

    private fun toggleAlarmActivation(alarmId: Long) = intent {
        val currentIndex = state.alarms.indexOfFirst { it.id == alarmId }
        if (currentIndex == -1) return@intent

        val currentAlarm = state.alarms[currentIndex]
        val previousState = currentAlarm.isAlarmActive // 기존 상태 저장
        val updatedAlarm = currentAlarm.copy(isAlarmActive = !currentAlarm.isAlarmActive)

        alarmUseCase.updateAlarmActive(alarmId, updatedAlarm.isAlarmActive).onSuccess {
            val updatedAlarms = state.alarms.toMutableList()
            updatedAlarms[currentIndex] = updatedAlarm

            val hasActivatedAlarm = updatedAlarms.any { it.isAlarmActive }
            reduce {
                state.copy(
                    alarms = updatedAlarms,
                    isNoActivatedAlarmDialogVisible = !hasActivatedAlarm,
                    pendingAlarmToggle = if (!hasActivatedAlarm) alarmId to previousState else null,
                )
            }

            if (updatedAlarm.isAlarmActive) {
                alarmScheduler.scheduleAlarm(updatedAlarm)
            } else {
                alarmScheduler.unScheduleAlarm(updatedAlarm)
            }
        }.onFailure { error ->
            Log.e("HomeViewModel", "Failed to update alarm state", error)
        }
    }

    private fun showDeleteDialog() = intent {
        reduce { state.copy(isDeleteDialogVisible = true) }
    }

    private fun hideDeleteDialog() = intent {
        reduce { state.copy(isDeleteDialogVisible = false) }
    }

    private fun confirmDeletion() = intent {
        deleteAlarms(state.selectedAlarmIds)
        reduce {
            state.copy(
                selectedAlarmIds = emptySet(),
                isDeleteDialogVisible = false,
            )
        }
    }

    private fun showNoActivatedAlarmDialog() = intent {
        reduce { state.copy(isNoActivatedAlarmDialogVisible = true) }
    }

    private fun hideNoActivatedAlarmDialog() = intent {
        reduce {
            state.copy(
                isNoActivatedAlarmDialogVisible = false,
                pendingAlarmToggle = null,
            )
        }
    }

    private fun rollbackAlarmActivation() = intent {
        val pendingAlarm = state.pendingAlarmToggle ?: return@intent
        val (alarmId, previousState) = pendingAlarm

        val currentIndex = state.alarms.indexOfFirst { it.id == alarmId }
        if (currentIndex == -1) return@intent

        val currentAlarm = state.alarms[currentIndex]
        val restoredAlarm = currentAlarm.copy(isAlarmActive = previousState)

        alarmUseCase.updateAlarm(restoredAlarm).onSuccess { updatedAlarm ->
            val updatedAlarms = state.alarms.toMutableList()
            updatedAlarms[currentIndex] = updatedAlarm
            reduce {
                state.copy(
                    alarms = updatedAlarms,
                    pendingAlarmToggle = null,
                    isNoActivatedAlarmDialogVisible = false,
                )
            }

            if (updatedAlarm.isAlarmActive) {
                alarmScheduler.scheduleAlarm(updatedAlarm)
            } else {
                alarmScheduler.unScheduleAlarm(updatedAlarm)
            }
        }.onFailure { error ->
            Log.e("HomeViewModel", "Failed to rollback alarm state", error)
        }
    }

    private fun deleteSingleAlarm(alarmId: Long) {
        deleteAlarms(setOf(alarmId))
    }

    private fun deleteAlarms(alarmIds: Set<Long>) = intent {
        if (alarmIds.isEmpty()) return@intent

        val alarmsToDelete = state.alarms
            .filter { it.id in alarmIds }

        alarmsToDelete.forEach { alarm ->
            alarmUseCase.deleteAlarm(alarm.id)
            alarmScheduler.unScheduleAlarm(alarm)
        }

        if (state.activeItemMenu != null) {
            hideItemMenu()
        }

        postSideEffect(
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

    private fun restoreDeletedAlarms(alarmsWithIndex: List<Alarm>) = intent {
        alarmsWithIndex.forEach { alarm ->
            alarmUseCase.insertAlarm(alarm)
            alarmScheduler.scheduleAlarm(alarm)
        }
    }

    private fun restLastAddedAlarmIndex() = intent {
        reduce { state.copy(lastAddedAlarmIndex = null) }
    }

    private fun loadAllAlarms() = intent {
        reduce { state.copy(initialLoading = true) }

        alarmUseCase.getAllAlarms().collect {
            reduce {
                state.copy(
                    alarms = it,
                    initialLoading = false,
                )
            }
            updateDeliveryTime(it)
        }
    }

    private fun editAlarm(alarmId: Long) = intent {
        postSideEffect(HomeContract.SideEffect.NavigateToEditAlarm(alarmId))
    }

    private fun updateDeliveryTime(alarms: List<Alarm>) = intent {
        val earliestAlarm = alarms
            .filter { it.isAlarmActive }
            .minByOrNull { alarm ->
                getNextAlarmDateWithTime(alarm.isAm, alarm.hour, alarm.minute, alarm.repeatDays)
            }

        val deliveryTime = earliestAlarm?.let { alarm ->
            val alarmDateTime = getNextAlarmDateWithTime(alarm.isAm, alarm.hour, alarm.minute, alarm.repeatDays)
            alarmDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        } ?: "NONE"

        reduce { state.copy(deliveryTime = formatDeliveryTime(deliveryTime)) }
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

    private fun loadDailyFortune() = intent {
        val fortuneDate = fortuneRepository.fortuneDateFlow.firstOrNull()
        val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

        Log.d("HomeViewModel", "fortuneDate: $fortuneDate, todayDate: $todayDate")

        if (fortuneDate != todayDate) {
            processAction(HomeContract.Action.ShowNoDailyFortuneDialog)
        } else {
            fortuneRepository.markFortuneAsChecked()
            postSideEffect(HomeContract.SideEffect.NavigateToFortune)
        }
    }

    private fun loadDailyFortuneState() = intent {
        val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

        combine(
            fortuneRepository.fortuneDateFlow,
            fortuneRepository.fortuneScoreFlow,
            fortuneRepository.hasNewFortuneFlow,
        ) { fortuneDate, fortuneScore, hasNewFortune ->
            val isTodayFortuneAvailable = fortuneDate == todayDate
            val finalFortuneScore = if (isTodayFortuneAvailable) fortuneScore ?: -1 else -1

            Pair(finalFortuneScore, hasNewFortune)
        }.collect { (finalFortuneScore, hasNewFortune) ->
            reduce {
                state.copy(
                    lastFortuneScore = finalFortuneScore,
                    hasNewFortune = hasNewFortune,
                    isToolTipVisible = hasNewFortune,
                )
            }
        }
    }

    private fun loadUserName() = intent {
        userInfoRepository.userNameFlow.first { userName ->
            reduce { state.copy(name = userName ?: "") }
            true
        }
    }

    private fun showNoDailyFortuneDialog() = intent {
        reduce { state.copy(isNoDailyFortuneDialogVisible = true) }
    }

    private fun hideNoDailyFortuneDialog() = intent {
        reduce { state.copy(isNoDailyFortuneDialogVisible = false) }
    }

    private fun hideToolTip() = intent {
        reduce { state.copy(isToolTipVisible = false) }
    }

    private fun navigateToSetting() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateToSetting)
    }

    private fun showItemMenu(alarmId: Long, x: Float, y: Float) = intent {
        reduce {
            state.copy(
                activeItemMenu = alarmId,
                activeItemMenuPosition = x to y,
            )
        }
    }

    private fun hideItemMenu() = intent {
        reduce {
            state.copy(
                activeItemMenu = null,
                activeItemMenuPosition = null,
            )
        }
    }

    private fun setSortOrder(sortOrder: HomeContract.AlarmSortOrder) = intent {
        reduce { state.copy(sortOrder = sortOrder) }
        hideDropDownMenu()
    }
}
