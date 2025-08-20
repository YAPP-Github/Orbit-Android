package com.yapp.alarm.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.yapp.alarm.AlarmConstants
import com.yapp.alarm.AndroidAlarmScheduler
import com.yapp.alarm.services.AlarmService
import com.yapp.analytics.AnalyticsEvent
import com.yapp.analytics.AnalyticsHelper
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.toAlarmDay
import com.yapp.domain.model.toTimeString
import com.yapp.domain.repository.FortuneRepository
import com.yapp.domain.usecase.AlarmUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    @Inject
    lateinit var androidAlarmScheduler: AndroidAlarmScheduler

    @Inject
    lateinit var fortuneRepository: FortuneRepository

    @Inject
    lateinit var alarmUseCase: AlarmUseCase

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return

        val alarmServiceIntent = createAlarmServiceIntent(context, intent)
        when (intent.action) {
            AlarmConstants.ACTION_ALARM_TRIGGERED -> {
                val alarm: Alarm? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    alarmServiceIntent.getParcelableExtra(
                        AlarmConstants.EXTRA_ALARM,
                        Alarm::class.java,
                    )
                } else {
                    @Suppress("DEPRECATION")
                    alarmServiceIntent.getParcelableExtra(AlarmConstants.EXTRA_ALARM)
                }
                analyticsHelper.logEvent(
                    AnalyticsEvent(
                        type = "alarm_ring",
                        properties = mapOf(
                            AnalyticsEvent.AlarmPropertiesKeys.ALARM_ID to "${alarm?.id}",
                            AnalyticsEvent.AlarmPropertiesKeys.ALARM_TIME to alarm?.toTimeString(),
                        ),
                    ),
                )

                context.startForegroundService(alarmServiceIntent)
            }

            AlarmConstants.ACTION_ALARM_SNOOZED -> {
                val alarm: Alarm? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(AlarmConstants.EXTRA_ALARM, Alarm::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(AlarmConstants.EXTRA_ALARM)
                }
                analyticsHelper.logEvent(
                    AnalyticsEvent(
                        type = "alarm_snooze",
                        properties = mapOf(
                            AnalyticsEvent.AlarmPropertiesKeys.ALARM_ID to "${alarm?.id}",
                        ),
                    ),
                )
                alarm?.let { handleSnooze(context, it) }

                Toast.makeText(
                    context,
                    "알람이 ${alarm?.snoozeInterval}분 후 다시 울려요",
                    Toast.LENGTH_SHORT,
                ).show()
            }

            AlarmConstants.ACTION_ALARM_DISMISSED -> {
                val notificationId = intent.getLongExtra(AlarmConstants.EXTRA_NOTIFICATION_ID, -1L)
                val missionType = intent.getIntExtra(AlarmConstants.EXTRA_MISSION_TYPE, -1)
                val missionCount = intent.getIntExtra(AlarmConstants.EXTRA_MISSION_COUNT, -1)

                if (notificationId == -1L) {
                    Log.e("AlarmReceiver", "notificationId 수신 실패")
                    return
                }

                androidAlarmScheduler.cancelSnoozedAlarm(notificationId)
                context.stopService(alarmServiceIntent)

                CoroutineScope(Dispatchers.IO).launch {
                    val alarms = alarmUseCase.getAllAlarms().first()

                    val isSnoozeId = notificationId >= AlarmConstants.SNOOZE_ID_OFFSET

                    fun Alarm.ringsToday(): Boolean {
                        if (repeatDays == 0) return true

                        val todayAlarmDay = LocalDate.now().dayOfWeek.toAlarmDay()
                        return (repeatDays and todayAlarmDay.bitValue) != 0
                    }

                    val earliestIdToday: Long? = alarms
                        .asSequence()
                        .filter { it.isAlarmActive && it.ringsToday() }
                        .sortedWith(compareBy({ it.hour }, { it.minute }, { it.second }))
                        .firstOrNull()
                        ?.id

                    val isEarliestAlarmDismissedToday =
                        !isSnoozeId && (earliestIdToday == notificationId)

                    val isFirstAlarm = alarms.firstOrNull()?.id == notificationId
                    analyticsHelper.logEvent(
                        AnalyticsEvent(
                            type = "alarm_dismiss",
                            properties = mapOf(
                                AnalyticsEvent.AlarmPropertiesKeys.ALARM_ID to "$notificationId",
                                AnalyticsEvent.AlarmPropertiesKeys.DISMISS_IS_FIRST_ALARM to isFirstAlarm,
                            ),
                        ),
                    )

                    sendBroadCastToCloseAlarmInteractionActivity(
                        context = context,
                        notificationId = notificationId,
                        missionType = missionType,
                        missionCount = missionCount,
                        isEarliestToday = isEarliestAlarmDismissedToday,
                    )
                }

                Toast.makeText(context, "알람이 해제되었어요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createAlarmServiceIntent(
        context: Context,
        intent: Intent,
    ): Intent {
        return Intent(context, AlarmService::class.java).apply {
            putExtras(intent.extras!!)
        }
    }

    private fun handleSnooze(context: Context, alarm: Alarm) {
        if (alarm.snoozeCount == 0) return

        val newSnoozeCount = if (alarm.snoozeCount == -1) {
            alarm.snoozeCount
        } else {
            alarm.snoozeCount - 1
        }

        val snoozeDateTime = LocalDateTime.now()
            .plusMinutes(alarm.snoozeInterval.toLong())

        val updatedAlarm = alarm.copy(
            hour = snoozeDateTime.hour,
            minute = snoozeDateTime.minute,
            second = snoozeDateTime.second,
            repeatDays = 0,
            snoozeCount = newSnoozeCount,
            id = alarm.id + AlarmConstants.SNOOZE_ID_OFFSET,
        )

        context.stopService(Intent(context, AlarmService::class.java))
        androidAlarmScheduler.scheduleAlarm(updatedAlarm)
    }

    private fun sendBroadCastToCloseAlarmInteractionActivity(
        context: Context,
        notificationId: Long,
        missionType: Int,
        missionCount: Int,
        isEarliestToday: Boolean,
    ) {
        val intent = Intent(AlarmConstants.ACTION_ALARM_INTERACTION_ACTIVITY_CLOSE).apply {
            putExtra(AlarmConstants.EXTRA_IS_SNOOZED, false)
            putExtra(AlarmConstants.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(AlarmConstants.EXTRA_MISSION_TYPE, missionType)
            putExtra(AlarmConstants.EXTRA_MISSION_COUNT, missionCount)
            putExtra(AlarmConstants.EXTRA_IS_FIRST_TODAY, isEarliestToday)
        }
        context.sendBroadcast(intent)
    }
}
