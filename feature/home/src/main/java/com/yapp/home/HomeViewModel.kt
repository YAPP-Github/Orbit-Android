package com.yapp.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.ViewModel
import com.yapp.common.util.ResourceProvider
import com.yapp.domain.model.Alarm
import com.yapp.domain.repository.FortuneRepository
import com.yapp.domain.repository.UserInfoRepository
import com.yapp.domain.usecase.AlarmUseCase
import com.yapp.home.util.AlarmDateTimeFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val alarmUseCase: AlarmUseCase,
    private val resourceProvider: ResourceProvider,
    private val alarmDateTimeFormatter: AlarmDateTimeFormatter,
    private val fortuneRepository: FortuneRepository,
    private val userInfoRepository: UserInfoRepository,
    @Named("appVersion") private val appVersion: String,
    @ApplicationContext private val context: Context,
) : ViewModel(), ContainerHost<HomeContract.State, HomeContract.SideEffect> {

    override val container: Container<HomeContract.State, HomeContract.SideEffect> = container(
        initialState = HomeContract.State(),
    ) {
        intent {
            repeatOnSubscription {
                loadAllAlarms()
                loadDailyFortuneState()
                loadUserName()
                loadUpdateNoticeVisibility()
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
            HomeContract.Action.HideUpdateNotice -> hideUpdateNotice()
            HomeContract.Action.OnClickDontShowAgain -> setUpdateNoticeDontShowVersion()
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
                alarmUseCase.scheduleAlarm(updatedAlarm)
            } else {
                alarmUseCase.unScheduleAlarm(updatedAlarm)
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
                alarmUseCase.scheduleAlarm(updatedAlarm)
            } else {
                alarmUseCase.unScheduleAlarm(updatedAlarm)
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
            alarmUseCase.unScheduleAlarm(alarm)
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
            alarmUseCase.scheduleAlarm(alarm)
        }
    }

    private fun restLastAddedAlarmIndex() = intent {
        reduce { state.copy(lastAddedAlarmIndex = null) }
    }

    private fun loadAllAlarms() = intent {
        reduce { state.copy(initialLoading = true) }

        alarmUseCase.getAllAlarms().collect { alarms ->
            reduce {
                state.copy(
                    alarms = alarms,
                    initialLoading = false,
                )
            }
            updateDeliveryTime(alarms)
        }
    }

    private fun editAlarm(alarmId: Long) = intent {
        postSideEffect(HomeContract.SideEffect.NavigateToEditAlarm(alarmId))
    }

    private fun updateDeliveryTime(alarms: List<Alarm>) = intent {
        val deliveryTimeFormats = AlarmDateTimeFormatter.DeliveryTimeFormats(
            noAlarm = resourceProvider.getString(R.string.home_fortune_no_alarm),
            today = resourceProvider.getString(R.string.home_fortune_delivery_today, "%s"),
            tomorrow = resourceProvider.getString(R.string.home_fortune_delivery_tomorrow, "%s"),
            thisYear = resourceProvider.getString(R.string.home_fortune_delivery_this_year, "%s"),
            otherYear = resourceProvider.getString(R.string.home_fortune_delivery_other_year, "%s"),
        )

        val formattedTime = alarmDateTimeFormatter.getFormattedEarliestUpcomingAlarmDeliveryTime(
            alarms = alarms,
            formats = deliveryTimeFormats,
        )
        reduce { state.copy(deliveryTime = formattedTime) }
    }

    private fun loadDailyFortune() = intent {
        val fortuneDate = fortuneRepository.fortuneDateEpochFlow.firstOrNull()
        val todayDate = LocalDate.now().toEpochDay()

        if (fortuneDate != todayDate) {
            processAction(HomeContract.Action.ShowNoDailyFortuneDialog)
        } else {
            fortuneRepository.markFortuneTooltipShown()
            postSideEffect(HomeContract.SideEffect.NavigateToFortune)
        }
    }

    private fun loadDailyFortuneState() = intent {
        val todayDate = LocalDate.now().toEpochDay()

        combine(
            fortuneRepository.fortuneDateEpochFlow,
            fortuneRepository.fortuneScoreFlow,
            fortuneRepository.shouldShowFortuneToolTipFlow,
        ) { fortuneDate, fortuneScore, shouldShowTooltip ->
            val isTodayFortuneAvailable = fortuneDate == todayDate
            val finalFortuneScore = if (isTodayFortuneAvailable) fortuneScore ?: -1 else -1

            Pair(finalFortuneScore, shouldShowTooltip)
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

    private fun loadUpdateNoticeVisibility() = intent {
        if (!isOnlineNow()) {
            reduce { state.copy(isUpdateNoticeVisible = false) }
            return@intent
        }

        val dontShowVersion =
            userInfoRepository.updateNoticeDontShowVersionFlow.firstOrNull()
        val lastShownDate =
            userInfoRepository.updateNoticeLastShownDateEpochFlow.firstOrNull()

        val today = LocalDate.now().toEpochDay()

        val shouldShow = when {
            dontShowVersion != null && dontShowVersion == appVersion -> false
            lastShownDate != null && lastShownDate == today -> false
            else -> true
        }

        if (shouldShow) userInfoRepository.markUpdateNoticeShownToday()

        reduce { state.copy(isUpdateNoticeVisible = shouldShow) }
    }

    private fun setUpdateNoticeDontShowVersion() = intent {
        userInfoRepository.markUpdateNoticeDontShow(appVersion)
        reduce { state.copy(isUpdateNoticeVisible = false) }
    }

    private fun hideUpdateNotice() = intent {
        reduce { state.copy(isUpdateNoticeVisible = false) }
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

    private fun isOnlineNow(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false

        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
