package com.yapp.alarm

import android.app.AlarmManager
import android.app.Application
import android.util.Log
import com.yapp.alarm.pendingIntent.schedule.createAlarmReceiverPendingIntentForSchedule
import com.yapp.alarm.pendingIntent.schedule.createAlarmReceiverPendingIntentForUnSchedule
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmDay
import com.yapp.domain.model.toAlarmDays
import com.yapp.domain.model.toDayOfWeek
import com.yapp.domain.scheduler.AlarmScheduler
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class AndroidAlarmScheduler @Inject constructor(
    private val app: Application,
    private val alarmManager: AlarmManager,
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

    fun scheduleWeeklyAlarm(alarm: Alarm, day: AlarmDay) {
        val initialTriggerMillis = getNextAlarmTimeMillis(alarm, day) + AlarmConstants.WEEK_INTERVAL_MILLIS
        val triggerMillis = findNextNonHolidayDate(initialTriggerMillis)

        val pendingIntent = createAlarmReceiverPendingIntentForSchedule(app, alarm, day)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            pendingIntent,
        )

        Log.d("AlarmHelper", "Scheduled weekly alarm for $day at: $triggerMillis")
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
        Log.d("AlarmHelper", "Canceled snoozed alarm with id: $snoozedAlarmId")
    }

    private fun setRepeatingAlarm(day: AlarmDay, alarm: Alarm) {
        val alarmReceiverPendingIntent =
            createAlarmReceiverPendingIntentForSchedule(app, alarm, day)
        val firstAlarmTriggerMillis = getNextAlarmTimeMillis(alarm, day)

        Log.d("AlarmHelper", "Setting repeating alarm id: ${alarm.id} at: $firstAlarmTriggerMillis")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            firstAlarmTriggerMillis,
            alarmReceiverPendingIntent,
        )
    }

    private fun setNonRepeatingAlarm(alarm: Alarm) {
        val alarmReceiverPendingIntent =
            createAlarmReceiverPendingIntentForSchedule(app, alarm)

        val triggerMillis = getNextAlarmTimeMillis(alarm, null)

        Log.d("AlarmHelper", "Setting one-time alarm at: $triggerMillis")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            alarmReceiverPendingIntent,
        )
    }

    private fun getNextAlarmTimeMillis(alarm: Alarm, day: AlarmDay?): Long {
        val now = LocalDateTime.now().withNano(0) // 밀리초 제거하여 정확한 초 기준 설정

        var alarmDateTime = now.withHour(alarm.hour).withMinute(alarm.minute).withSecond(alarm.second)

        if (day != null) {
            val targetDayOfWeek = day.toDayOfWeek()
            while (alarmDateTime.dayOfWeek != targetDayOfWeek || alarmDateTime.isBefore(now)) {
                alarmDateTime = alarmDateTime.plusDays(1)
            }
        } else {
            if (alarmDateTime.isBefore(now)) {
                alarmDateTime = alarmDateTime.plusDays(1)
            }
        }

        val epochMillis = alarmDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        Log.d("AlarmHelper", "Alarm scheduled at: $alarmDateTime (epochMillis=$epochMillis)")

        return epochMillis
    }

    private fun findNextNonHolidayDate(initialMillis: Long): Long {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        var adjustedMillis = initialMillis

        while (true) {
            val localDate = Instant.ofEpochMilli(adjustedMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val dateString = localDate.format(dateFormatter)

            if (!AlarmConstants.HOLIDAYS_2025.contains(dateString)) {
                return adjustedMillis // 공휴일이 아니라면 해당 날짜 반환
            }

            // 공휴일이라면 다음 1주 뒤로 이동
            adjustedMillis += AlarmConstants.WEEK_INTERVAL_MILLIS
        }
    }
}
