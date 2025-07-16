package com.yapp.alarm

import android.app.AlarmManager
import android.app.Application
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
        val triggerMillis = alarmTimeCalculator.calculateNextTriggerTimeForRepeatingDay(alarm, day)
        val pendingIntent = createAlarmReceiverPendingIntentForSchedule(app, alarm, day)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            pendingIntent,
        )
    }

    private fun setNonRepeatingAlarm(alarm: Alarm) {
        val triggerMillis = alarmTimeCalculator.calculateNextTriggerTimeForNonRepeating(alarm)
        val pendingIntent = createAlarmReceiverPendingIntentForSchedule(app, alarm)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            pendingIntent,
        )
    }

    fun scheduleUpcomingWeeklyAlarm(alarm: Alarm, day: AlarmDay) {
        val triggerMillis = alarmTimeCalculator.calculateNextUpcomingWeeklyAlarmTime(alarm, day)
        val pendingIntent = createAlarmReceiverPendingIntentForSchedule(app, alarm, day)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
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
