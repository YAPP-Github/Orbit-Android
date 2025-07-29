package com.yapp.alarm.interaction.action

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.yapp.alarm.pendingIntent.interaction.createAlarmDismissIntent
import com.yapp.alarm.pendingIntent.interaction.createAlarmSnoozeIntent
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.MissionType
import com.yapp.domain.repository.FortuneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
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
) : ViewModel(), ContainerHost<AlarmActionContract.State, AlarmActionContract.SideEffect> {

    override val container: Container<AlarmActionContract.State, AlarmActionContract.SideEffect> = container(
        initialState = AlarmActionContract.State(),
    ) {
        fetchShouldShowMissionStart()
        initializeAlarmState()
        startClock()
    }

    private val alarmJson: String? = savedStateHandle.get<String>("alarm")
    private val alarm: Alarm? = alarmJson?.let { Alarm.fromJson(it) }

    fun processAction(action: AlarmActionContract.Action) {
        when (action) {
            is AlarmActionContract.Action.Snooze -> snooze()
            is AlarmActionContract.Action.Dismiss -> dismiss()
        }
    }

    private fun initializeAlarmState() = intent {
        reduce {
            state.copy(
                snoozeEnabled = alarm?.isSnoozeEnabled ?: false,
                snoozeCount = alarm?.snoozeCount ?: 5,
                snoozeInterval = alarm?.snoozeInterval ?: 5,
            )
        }
    }

    private fun fetchShouldShowMissionStart() = intent {
        val missionType = alarm?.missionType
        val fortuneDate = fortuneRepository.fortuneDateFlow.firstOrNull()
        val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val shouldShowMissionStart = missionType != null &&
            missionType != MissionType.NONE &&
            fortuneDate != todayDate

        reduce {
            state.copy(shouldShowMissionStart = shouldShowMissionStart)
        }
    }

    private fun startClock() = intent {
        while (true) {
            val now = LocalTime.now()
            val today = LocalDate.now()
            val dayOfWeek = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)

            reduce {
                state.copy(
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

    private fun snooze() = intent {
        sendAlarmSnoozeEventToAlarmReceiver()
        reduce {
            state.copy(
                snoozeCount = if (state.snoozeCount == -1) {
                    state.snoozeCount
                } else {
                    state.snoozeCount - 1
                },
            )
        }
        alarm?.let {
            postSideEffect(AlarmActionContract.SideEffect.NavigateToAlarmSnooze(it))
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
        alarm?.let { alarm ->
            val alarmDismissIntent = createAlarmDismissIntent(
                context = app,
                notificationId = alarm.id,
                missionType = alarm.missionType.value,
                missionCount = alarm.missionCount,
            )
            app.sendBroadcast(alarmDismissIntent)
        }
    }
}
