package com.yapp.alarm.interaction.snooze

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.yapp.alarm.pendingIntent.interaction.createAlarmDismissIntent
import com.yapp.domain.model.Alarm
import com.yapp.domain.repository.FortuneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class AlarmSnoozeTimerViewModel @Inject constructor(
    private val app: Application,
    savedStateHandle: SavedStateHandle,
    private val fortuneRepository: FortuneRepository,
) : ViewModel(), ContainerHost<AlarmSnoozeTimerContract.State, AlarmSnoozeTimerContract.SideEffect> {

    override val container: Container<AlarmSnoozeTimerContract.State, AlarmSnoozeTimerContract.SideEffect> = container(
        initialState = AlarmSnoozeTimerContract.State(),
    ) {
        fetchIsFirstMission()
        startClock()
    }

    private val alarmJson: String? = savedStateHandle.get<String>("alarm")
    private val alarm: Alarm? = alarmJson?.let { Alarm.fromJson(it) }

    fun processAction(action: AlarmSnoozeTimerContract.Action) {
        when (action) {
            is AlarmSnoozeTimerContract.Action.Dismiss -> dismiss()
        }
    }

    private fun fetchIsFirstMission() = intent {
        val fortuneDate = fortuneRepository.fortuneDateFlow.firstOrNull()
        val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val isFirstMission = fortuneDate != todayDate

        reduce {
            state.copy(isFirstMission = isFirstMission)
        }
    }

    private fun startClock() = intent {
        val nowMillis = System.currentTimeMillis()
        val nextSnoozeTimeMillis = alarm?.let { getNextSnoozeAlarmTimeMillis(it.snoozeInterval) } ?: nowMillis
        val remainingMillis = max(0, nextSnoozeTimeMillis - nowMillis)
        val remainingSeconds = (remainingMillis / 1000).toInt()

        reduce {
            state.copy(
                remainingSeconds = remainingSeconds,
                totalSeconds = remainingSeconds,
                alarmTimeStamp = nextSnoozeTimeMillis / 1000,
                initialLoading = true,
            )
        }

        while (true) {
            val currentTime = System.currentTimeMillis() / 1000
            val remaining = max(0, state.alarmTimeStamp - currentTime)

            reduce {
                state.copy(
                    remainingSeconds = remaining.toInt(),
                    initialLoading = false,
                )
            }

            if (remaining.toInt() == 0) break

            delay(1000L)
        }
    }

    private fun dismiss() {
        sendAlarmDismissEventToAlarmReceiver()
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

    private fun getNextSnoozeAlarmTimeMillis(
        snoozeInterval: Int,
    ): Long {
        val now = LocalDateTime.now().withNano(0).plusMinutes(snoozeInterval.toLong())

        return now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
