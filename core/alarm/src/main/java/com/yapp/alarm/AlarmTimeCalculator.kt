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

    private fun isHoliday(dateToCheck: LocalDateTime): Boolean {
        if (dateToCheck.year == 2025) {
            val dateString = dateToCheck.format(holidayDateFormatter)
            return AlarmConstants.HOLIDAYS_2025.contains(dateString)
        }
        return false
    }

    private fun skipHolidaysIfEnabled(initialDateTime: LocalDateTime, alarm: Alarm): LocalDateTime {
        if (!alarm.isHolidayAlarmOff) return initialDateTime

        var adjustedDateTime = initialDateTime
        while (isHoliday(adjustedDateTime)) {
            adjustedDateTime = adjustedDateTime.plusWeeks(1)
        }

        return adjustedDateTime
    }

    private fun getAlarmDateTimeOnDate(alarm: Alarm, now: LocalDateTime): LocalDateTime {
        return now
            .withHour(alarm.hour)
            .withMinute(alarm.minute)
            .withSecond(alarm.second)
            .withNano(0)
    }

    fun calculateNextRepeatingTimeMillis(
        alarm: Alarm,
        alarmDay: AlarmDay,
        zoneId: ZoneId = clock.zone,
    ): Long {
        val now = LocalDateTime.now(clock)
        val targetDayOfWeek = alarmDay.toDayOfWeek()

        val alarmDateTimeToday = getAlarmDateTimeOnDate(alarm, now)

        var nextAlarmDateTimeCandidate = alarmDateTimeToday

        while (nextAlarmDateTimeCandidate.dayOfWeek != targetDayOfWeek || nextAlarmDateTimeCandidate.isBefore(now)) {
            nextAlarmDateTimeCandidate = nextAlarmDateTimeCandidate.plusDays(1)
        }

        nextAlarmDateTimeCandidate = skipHolidaysIfEnabled(nextAlarmDateTimeCandidate, alarm)

        return nextAlarmDateTimeCandidate.atZone(zoneId).toInstant().toEpochMilli()
    }

    fun calculateNonRepeatingTimeMillis(
        alarm: Alarm,
        zoneId: ZoneId = clock.zone,
    ): Long {
        val now = LocalDateTime.now(clock)
        var alarmDateTime = getAlarmDateTimeOnDate(alarm, now)

        if (alarmDateTime.isBefore(now)) {
            alarmDateTime = alarmDateTime.plusDays(1)
        }

        return alarmDateTime.atZone(zoneId).toInstant().toEpochMilli()
    }

    fun calculateNextWeeklyRescheduledTimeMillis(
        alarm: Alarm,
        alarmTargetDay: AlarmDay,
        zoneId: ZoneId = clock.zone,
    ): Long {
        val now = LocalDateTime.now(clock)
        val targetDayOfWeek = alarmTargetDay.toDayOfWeek()

        var initialAlarmDateTimeCandidate = getAlarmDateTimeOnDate(alarm, now)

        while (initialAlarmDateTimeCandidate.dayOfWeek != targetDayOfWeek || initialAlarmDateTimeCandidate.isBefore(now)) {
            initialAlarmDateTimeCandidate = initialAlarmDateTimeCandidate.plusDays(1)
        }

        val nextWeeklyAlarmDateTimeCandidate = initialAlarmDateTimeCandidate.plusWeeks(1)
        val nextWeeklyAlarmDateTime = skipHolidaysIfEnabled(nextWeeklyAlarmDateTimeCandidate, alarm)

        return nextWeeklyAlarmDateTime.atZone(zoneId).toInstant().toEpochMilli()
    }
}
