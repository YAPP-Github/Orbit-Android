package com.yapp.domain.formatter

import com.yapp.domain.model.Alarm
import com.yapp.domain.model.toAlarmDays
import com.yapp.domain.model.toDayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

class AlarmDateTimeFormatter @Inject constructor() {

    companion object {
        private const val NO_ALARM_STRING = "NONE"
        private const val DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm"
    }

    data class DeliveryTimeFormats(
        val noAlarm: String,
        val today: String, // 예: "오늘 %s"
        val tomorrow: String, // 예: "내일 %s"
        val thisYear: String, // 예: "%s" (날짜와 시간만)
        val otherYear: String, // 예: "%s" (년도, 날짜, 시간)
        val todayTimePattern: String = "a h:mm",
        val thisYearDatePattern: String = "M월 d일 a h:mm",
        val otherYearDatePattern: String = "yy년 M월 d일 a h:mm",
    )

    data class TimeDifferenceFormats(
        val daysHoursMinutesFormat: String, // 예: "%1$d일 %2$d시간 %3$d분 후에 울려요"
        val hoursMinutesFormat: String, // 예: "%1$d시간 %2$d분 후에 울려요"
        val minutesFormat: String, // 예: "%1$d분 후에 울려요"
        val soonFormat: String, // 예: "곧 울려요"
    )

    fun calculateNextOccurrence(
        hour: Int,
        minute: Int,
        repeatDays: Int,
        now: LocalDateTime,
    ): LocalDateTime {
        val alarmTime = LocalTime.of(hour, minute)
        val todayAlarmDateTime = LocalDateTime.of(now.toLocalDate(), alarmTime)

        if (repeatDays == 0) { // 단일 알람
            return if (todayAlarmDateTime.isAfter(now)) {
                todayAlarmDateTime
            } else {
                todayAlarmDateTime.plusDays(1)
            }
        }

        val selectedDaysOfWeek = repeatDays.toAlarmDays()
            .map { it.toDayOfWeek() }
            .sortedBy { it.value }

        if (selectedDaysOfWeek.isEmpty()) { // 방어 코드: 실제로는 toAlarmDays가 빈 리스트를 반환하지 않도록 설계되어야 함
            return if (todayAlarmDateTime.isAfter(now)) todayAlarmDateTime else todayAlarmDateTime.plusDays(1)
        }

        val currentDayOfWeek = now.dayOfWeek

        // 오늘 알람이 가능한지 확인
        if (selectedDaysOfWeek.contains(currentDayOfWeek) && todayAlarmDateTime.isAfter(now)) {
            return todayAlarmDateTime
        }

        for (dayOffset in 0..7) {
            val nextPotentialDate = now.toLocalDate().plusDays(dayOffset.toLong())
            val dayOfWeekPotentialDate = nextPotentialDate.dayOfWeek
            val potentialAlarmDateTime = nextPotentialDate.atTime(alarmTime)

            if (selectedDaysOfWeek.contains(dayOfWeekPotentialDate)) {
                if (potentialAlarmDateTime.isAfter(now)) {
                    return potentialAlarmDateTime
                }
            }
        }

        return now.toLocalDate().plusDays(1).atTime(alarmTime) // fallback: 다음 날 같은 시간
    }

    private fun formatDeliveryDateTimeString(
        deliveryDateTimeString: String, // "yyyy-MM-dd'T'HH:mm" 포맷 또는 "NONE"
        formats: DeliveryTimeFormats,
        now: LocalDateTime,
    ): String {
        return try {
            if (deliveryDateTimeString.equals(NO_ALARM_STRING, ignoreCase = true)) {
                return formats.noAlarm
            }

            val inputFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)
            val alarmOccurrenceDateTime = LocalDateTime.parse(deliveryDateTimeString, inputFormatter) // 변수명 inputDateTime -> alarmOccurrenceDateTime
            val today = now.toLocalDate()
            val tomorrow = today.plusDays(1)
            val formattedTimeOrDateTime: String

            when {
                // 1. 년도가 현재 년도와 다르면 'otherYear' 포맷 적용
                alarmOccurrenceDateTime.year != now.year -> {
                    formattedTimeOrDateTime = alarmOccurrenceDateTime.format(DateTimeFormatter.ofPattern(formats.otherYearDatePattern))
                    return String.format(formats.otherYear, formattedTimeOrDateTime)
                }
                // 2. (년도가 같고) 날짜가 오늘이면 'today' 포맷 적용
                alarmOccurrenceDateTime.toLocalDate() == today -> {
                    formattedTimeOrDateTime = alarmOccurrenceDateTime.format(DateTimeFormatter.ofPattern(formats.todayTimePattern))
                    return String.format(formats.today, formattedTimeOrDateTime)
                }
                // 3. (년도가 같고) 날짜가 내일이면 'tomorrow' 포맷 적용
                alarmOccurrenceDateTime.toLocalDate() == tomorrow -> {
                    // 내일은 특별히 시간만 표시 (요구사항에 따라 변경 가능)
                    formattedTimeOrDateTime = alarmOccurrenceDateTime.format(DateTimeFormatter.ofPattern(formats.todayTimePattern))
                    return String.format(formats.tomorrow, formattedTimeOrDateTime)
                }
                // 4. 그 외의 경우 (년도가 같고, 오늘이나 내일이 아닌 다른 날) 'thisYear' 포맷 적용
                else -> {
                    formattedTimeOrDateTime = alarmOccurrenceDateTime.format(DateTimeFormatter.ofPattern(formats.thisYearDatePattern))
                    return String.format(formats.thisYear, formattedTimeOrDateTime)
                }
            }
        } catch (e: DateTimeParseException) {
            formats.noAlarm
        } catch (e: Exception) {
            formats.noAlarm
        }
    }

    fun getFormattedEarliestUpcomingAlarmDeliveryTime(
        alarms: List<Alarm>,
        formats: DeliveryTimeFormats,
        now: LocalDateTime = LocalDateTime.now(), // 기본값으로 현재 시간 사용
    ): String {
        val earliestAlarmDateTime = alarms
            .filter { it.isAlarmActive }
            .mapNotNull { alarm ->
                try {
                    calculateNextOccurrence(alarm.hour, alarm.minute, alarm.repeatDays, now)
                } catch (e: Exception) {
                    null // 예외 발생 시 null로 처리
                }
            }
            .minOrNull()

        val deliveryDateTimeString = earliestAlarmDateTime?.format(
            DateTimeFormatter.ofPattern(DATE_TIME_FORMAT),
        ) ?: NO_ALARM_STRING

        return formatDeliveryDateTimeString(deliveryDateTimeString, formats, now)
    }

    fun formatTimeDifference(
        baseTime: LocalDateTime,
        futureTime: LocalDateTime,
        formats: TimeDifferenceFormats,
    ): String {
        if (!futureTime.isAfter(baseTime)) {
            return formats.soonFormat
        }

        val duration = Duration.between(baseTime, futureTime)

        if (duration.toNanos() <= 0) {
            return formats.soonFormat
        }

        val totalMinutes = duration.toMinutes()
        if (totalMinutes < 1) {
            return formats.soonFormat
        }

        val days = duration.toDays()
        val remainingHours = duration.toHours() % 24
        val remainingMinutes = duration.toMinutes() % 60

        return when {
            days > 0 -> String.format(formats.daysHoursMinutesFormat, days, remainingHours, remainingMinutes)
            remainingHours > 0 -> String.format(formats.hoursMinutesFormat, remainingHours, remainingMinutes)
            else -> String.format(formats.minutesFormat, remainingMinutes)
        }
    }
}
