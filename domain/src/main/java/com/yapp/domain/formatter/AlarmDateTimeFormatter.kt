package com.yapp.domain.formatter

import android.util.Log
import com.yapp.domain.model.Alarm // 프로젝트의 Alarm 모델 경로에 맞게 수정
import com.yapp.domain.model.toAlarmDays // domain 모듈의 확장 함수 경로
import com.yapp.domain.model.toDayOfWeek // domain 모듈의 확장 함수 경로
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

class AlarmDateTimeFormatter @Inject constructor() {

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
            if (deliveryDateTimeString.equals("NONE", ignoreCase = true)) {
                return formats.noAlarm
            }

            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
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
            Log.e("Formatter", "Failed to parse deliveryDateTimeString: $deliveryDateTimeString", e) // 로깅 고려
            formats.noAlarm
        } catch (e: Exception) {
            Log.e("Formatter", "Unexpected error formatting deliveryDateTimeString: $deliveryDateTimeString", e)
            formats.noAlarm
        }
    }

    /**
     * 활성화된 알람 목록에서 가장 먼저 울릴 알람 시간을 찾아,
     * 지정된 포맷에 맞춰 사용자에게 보여줄 문자열로 변환합니다.
     *
     * @param alarms 알람 목록
     * @param formats 포맷팅 규칙을 담은 데이터 클래스
     * @param now 현재 시간 (테스트 용이성을 위해 주입받음)
     * @return 포맷팅된 다음 알람 시간 문자열. 활성화된 알람이 없으면 formats.noAlarm 반환.
     */
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
                    Log.e("Formatter", "Error calculating next occurrence for alarm: $alarm", e)
                    null // 예외 발생 시 null로 처리
                }
            }
            .minOrNull()

        val deliveryDateTimeString = earliestAlarmDateTime?.format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
        ) ?: "NONE"

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
