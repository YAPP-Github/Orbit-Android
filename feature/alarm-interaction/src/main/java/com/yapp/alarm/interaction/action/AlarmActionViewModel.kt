package com.yapp.alarm.interaction.action

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yapp.alarm.pendingIntent.interaction.createAlarmDismissIntent
import com.yapp.alarm.pendingIntent.interaction.createAlarmSnoozeIntent
import com.yapp.domain.model.Alarm
import com.yapp.domain.repository.FortuneRepository
import com.yapp.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AlarmActionViewModel @Inject constructor(
    private val app: Application,
    private val fortuneRepository: FortuneRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<AlarmActionContract.State, AlarmActionContract.SideEffect>(
    AlarmActionContract.State(),
) {
    private val alarmJson: String? = savedStateHandle.get<String>("alarm")
    private val alarm: Alarm? = alarmJson?.let { Alarm.fromJson(it) }

    init {
        fetchIsFirstMission()
        updateState {
            copy(
                snoozeEnabled = alarm?.isSnoozeEnabled ?: false,
                snoozeCount = alarm?.snoozeCount ?: 5,
                snoozeInterval = alarm?.snoozeInterval ?: 5,
            )
        }

        startClock()
    }

    private fun fetchIsFirstMission() {
        viewModelScope.launch {
            val fortuneDate = fortuneRepository.fortuneDateFlow.firstOrNull()
            val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val isFirstMission = fortuneDate != todayDate

            updateState {
                copy(isFirstMission = isFirstMission)
            }
        }
    }

    private fun startClock() {
        viewModelScope.launch {
            while (isActive) {
                val now = LocalTime.now()
                val today = LocalDate.now()
                val dayOfWeek = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)

                updateState {
                    copy(
                        isAm = now.hour < 12,
                        hour = if (now.hour % 12 == 0) 12 else now.hour % 12,
                        minute = now.minute,
                        todayDate = "${today.monthValue}월 ${today.dayOfMonth}일 $dayOfWeek",
                        initialLoading = false,
                    )
                }

                delay(1000L)
            }
        }
    }

    fun processAction(action: AlarmActionContract.Action) {
        when (action) {
            is AlarmActionContract.Action.Snooze -> snooze()
            is AlarmActionContract.Action.Dismiss -> dismiss()
        }
    }

    private fun snooze() {
        sendAlarmSnoozeEventToAlarmReceiver()
        updateState {
            copy(
                snoozeCount = if (currentState.snoozeCount == -1) {
                    currentState.snoozeCount
                } else {
                    currentState.snoozeCount - 1
                },
            )
        }
        alarm?.let {
            emitSideEffect(AlarmActionContract.SideEffect.NavigateToAlarmSnooze(it))
        }
    }

    private fun dismiss() {
        sendAlarmDismissEventToAlarmReceiver()
    }

    private fun sendAlarmSnoozeEventToAlarmReceiver() {
        alarm?.let {
            val alarmSnoozeIntent = createAlarmSnoozeIntent(
                context = app,
                alarm = it,
            )
            app.sendBroadcast(alarmSnoozeIntent)
        }
    }

    private fun sendAlarmDismissEventToAlarmReceiver() {
        alarm?.id?.let { id ->
            val alarmDismissIntent = createAlarmDismissIntent(
                context = app,
                notificationId = id,
            )
            app.sendBroadcast(alarmDismissIntent)
        }
    }
}
