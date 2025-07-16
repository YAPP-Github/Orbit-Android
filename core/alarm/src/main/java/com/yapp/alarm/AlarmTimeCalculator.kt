package com.yapp.alarm

import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmDay
import com.yapp.domain.model.toDayOfWeek
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class AlarmTimeCalculator @Inject constructor(
    private val clock: Clock,
) {
    private val holidayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private fun isHoliday(date: LocalDateTime): Boolean {
        val dateString = date.format(holidayDateFormatter)
        if (date.year == 2025) {
            return AlarmConstants.HOLIDAYS_2025.contains(dateString)
        }
        return false
    }

    private fun createInitialAlarmDateTime(alarm: Alarm, now: LocalDateTime): LocalDateTime {
        return now
            .withHour(alarm.hour)
            .withMinute(alarm.minute)
            .withSecond(alarm.second)
            .withNano(0)
    }

    fun calculateNextTriggerTimeForRepeatingDay(
        alarm: Alarm,
        day: AlarmDay,
        zoneId: ZoneId = clock.zone,
    ): Long {
        val now = createInitialAlarmDateTime(alarm, LocalDateTime.now(clock))
        var alarmDateTime = now
            .withHour(alarm.hour)
            .withMinute(alarm.minute)
            .withSecond(alarm.second)
            .withNano(0)
        val targetDayOfWeek = day.toDayOfWeek()

        while (alarmDateTime.dayOfWeek != targetDayOfWeek || alarmDateTime.isBefore(now)) {
            alarmDateTime = alarmDateTime.plusDays(1)
        }
        return alarmDateTime.atZone(zoneId).toInstant().toEpochMilli()
    }

    fun calculateNextTriggerTimeForNonRepeating(
        alarm: Alarm,
        zoneId: ZoneId = clock.zone,
    ): Long {
        val now = LocalDateTime.now(clock)
        var alarmDateTime = createInitialAlarmDateTime(alarm, now)

        if (alarmDateTime.isBefore(now)) {
            alarmDateTime = alarmDateTime.plusDays(1)
        }
        return alarmDateTime.atZone(zoneId).toInstant().toEpochMilli()
    }

    fun calculateNextUpcomingWeeklyAlarmTime(
        alarm: Alarm,
        targetDay: AlarmDay,
        zoneId: ZoneId = clock.zone,
    ): Long {
        val now = LocalDateTime.now(clock)
        // 주간 알람은 'now'를 기준으로 targetDay의 alarm 시간을 찾고 그 다음 주를 계산
        var alarmDateTimeCandidate = createInitialAlarmDateTime(alarm, now)
        val dayOfWeekForTarget = targetDay.toDayOfWeek()

        while (alarmDateTimeCandidate.dayOfWeek != dayOfWeekForTarget || alarmDateTimeCandidate.isBefore(now)) {
            alarmDateTimeCandidate = alarmDateTimeCandidate.plusDays(1)
        }

        val initialTriggerTimeCandidate = alarmDateTimeCandidate.plusWeeks(1)
        var finalTriggerDateTime = initialTriggerTimeCandidate

        while (isHoliday(finalTriggerDateTime)) {
            finalTriggerDateTime = finalTriggerDateTime.plusWeeks(1)
        }
        return finalTriggerDateTime.atZone(zoneId).toInstant().toEpochMilli()
    }
}
