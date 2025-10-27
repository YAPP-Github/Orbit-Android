package com.yapp.alarm

import android.app.AlarmManager
import android.app.Application
import android.util.Log
import com.yapp.alarm.pendingIntent.schedule.createAlarmReceiverPendingIntentForSchedule
import com.yapp.alarm.pendingIntent.schedule.createAlarmReceiverPendingIntentForUnSchedule
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmDay
import com.yapp.domain.model.toAlarmDays
import com.yapp.domain.scheduler.AlarmScheduler
import javax.inject.Inject

class AndroidAlarmScheduler @Inject constructor(
    private val app: Application,
    private val alarmManager: AlarmManager,
    private val alarmTimeCalculator: AlarmTimeCalculator,
) : AlarmScheduler {

    private fun logSchedule(tag: String, alarm: Alarm, triggerMillis: Long, extra: String = "") {
        Log.d("ScheduleTrace", "scheduleAlarm Called", Throwable())
        Log.d(
            "AlarmSchedule",
            "[$tag] id=${alarm.id}, repeatDays=${alarm.repeatDays}, " +
                "time=${java.time.Instant.ofEpochMilli(triggerMillis)} $extra",
        )
    }

    override fun scheduleAlarm(alarm: Alarm) {
        val selectedDays = alarm.repeatDays.toAlarmDays()

        if (selectedDays.isEmpty()) {
            setNonRepeatingAlarm(alarm)
        } else {
            selectedDays.forEach { day ->
                setRepeatingAlarm(day, alarm)
            }
        }
    }

    private fun setRepeatingAlarm(day: AlarmDay, alarm: Alarm) {
        val triggerMillis = alarmTimeCalculator.calculateNextRepeatingTimeMillis(alarm, day)
        val pendingIntent = createAlarmReceiverPendingIntentForSchedule(app, alarm, day)
        logSchedule("REPEAT", alarm, triggerMillis, "day=$day")
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(
                triggerMillis,
                pendingIntent,
            ),
            pendingIntent,
        )
    }

    private fun setNonRepeatingAlarm(alarm: Alarm) {
        val triggerMillis = alarmTimeCalculator.calculateNonRepeatingTimeMillis(alarm)
        val pendingIntent = createAlarmReceiverPendingIntentForSchedule(app, alarm)
        logSchedule("NON_REPEAT", alarm, triggerMillis)
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(
                triggerMillis,
                pendingIntent,
            ),
            pendingIntent,
        )
    }

    fun rescheduleUpcomingWeeklyAlarm(alarm: Alarm, day: AlarmDay) {
        val triggerMillis = alarmTimeCalculator.calculateNextWeeklyRescheduledTimeMillis(alarm, day)
        val pendingIntent = createAlarmReceiverPendingIntentForSchedule(app, alarm, day)
        logSchedule("RESCHEDULE_WEEKLY", alarm, triggerMillis, "day=$day")
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(
                triggerMillis,
                pendingIntent,
            ),
            pendingIntent,
        )
    }

    override fun unScheduleAlarm(alarm: Alarm) {
        val selectedDays = alarm.repeatDays.toAlarmDays()

        if (selectedDays.isEmpty()) {
            val pendingIntent = createAlarmReceiverPendingIntentForUnSchedule(
                app,
                alarm,
                null,
            )
            alarmManager.cancel(pendingIntent)
        } else {
            selectedDays.forEach { day ->
                val pendingIntent = createAlarmReceiverPendingIntentForUnSchedule(
                    app,
                    alarm,
                    day,
                )
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    fun cancelSnoozedAlarm(alarmId: Long) {
        val snoozedAlarmId = alarmId + AlarmConstants.SNOOZE_ID_OFFSET
        val pendingIntent = createAlarmReceiverPendingIntentForUnSchedule(app, Alarm(id = snoozedAlarmId))
        alarmManager.cancel(pendingIntent)
    }
}
