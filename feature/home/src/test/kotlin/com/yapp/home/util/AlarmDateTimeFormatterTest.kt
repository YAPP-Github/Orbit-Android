package com.yapp.home.util

import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmDay
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class AlarmDateTimeFormatterTest {

    private lateinit var formatter: AlarmDateTimeFormatter
    private val fixedNow: LocalDateTime = LocalDateTime.of(2023, 10, 26, 10, 0, 0) // 목요일
    private val fixedClock: Clock = Clock.fixed(fixedNow.atZone(ZoneId.of("Asia/Seoul")).toInstant(), ZoneId.of("Asia/Seoul"))

    private val testLocale: Locale = Locale.KOREA

    @Before
    fun `테스트_준비`() {
        formatter = AlarmDateTimeFormatter(clock = fixedClock)
    }

    private fun getLocalizedFormatter(pattern: String): DateTimeFormatter {
        return DateTimeFormatter.ofPattern(pattern).withLocale(testLocale)
    }

    private val deliveryFormats = AlarmDateTimeFormatter.DeliveryTimeFormats(
        noAlarm = "받을 수 있는 운세가 없어요",
        today = "%1\$s 도착",
        tomorrow = "내일 %1\$s 도착",
        thisYear = "%1\$s 도착",
        otherYear = "%1\$s 도착",
        todayTimePattern = "a h:mm", // 예시: "오후 2:30"
        thisYearDatePattern = "M월 d일 a h:mm", // 예시: "11월 20일 오후 2:30"
        otherYearDatePattern = "yy년 M월 d일 a h:mm" // 예시: "24년 1월 15일 오전 9:00"
    )

    @Test
    fun `가장빠른_알람시간_포맷팅_활성알람_없으면_수정된_알람없음_반환`() {
        val alarms = listOf(
            Alarm(id = 1, hour = 14, minute = 0, repeatDays = 0, isAlarmActive = false)
        )
        val result = formatter.getFormattedEarliestUpcomingAlarmDeliveryTime(
            alarms,
            deliveryFormats,
            fixedNow
        )
        assertEquals(deliveryFormats.noAlarm, result)
    }

    @Test
    fun `가장빠른_알람시간_포맷팅_오늘_미래_활성알람_하나면_수정된_오늘형식_반환`() {
        val alarms = listOf(
            Alarm(id = 1, hour = 14, minute = 30, repeatDays = 0, isAlarmActive = true)
        )
        // deliveryFormats.today = "%1$s 도착"
        // deliveryFormats.todayTimePattern = "a h:mm" -> "오후 2:30"
        val result = formatter.getFormattedEarliestUpcomingAlarmDeliveryTime(
            alarms,
            deliveryFormats,
            fixedNow
        )

        val expectedAlarmTime = LocalDateTime.of(2023, 10, 26, 14, 30)
        val formattedExpectedTime =
            expectedAlarmTime.format(getLocalizedFormatter(deliveryFormats.todayTimePattern))
        assertEquals(String.format(deliveryFormats.today, formattedExpectedTime), result)
    }

    @Test
    fun `가장빠른_알람시간_포맷팅_내일_활성알람_하나면_수정된_내일형식_반환`() {
        val alarms = listOf(
            Alarm(id = 1, hour = 8, minute = 0, repeatDays = 0, isAlarmActive = true)
        )
        // deliveryFormats.tomorrow = "내일 %1$s 도착"
        // deliveryFormats.todayTimePattern = "a h:mm" -> "오전 8:00"
        val result = formatter.getFormattedEarliestUpcomingAlarmDeliveryTime(
            alarms,
            deliveryFormats,
            fixedNow
        )

        val expectedAlarmTime = fixedNow.toLocalDate().plusDays(1).atTime(8, 0)
        val formattedExpectedTime =
            expectedAlarmTime.format(getLocalizedFormatter(deliveryFormats.todayTimePattern)) // 내일이지만 시간 포맷은 todayTimePattern 사용
        assertEquals(String.format(deliveryFormats.tomorrow, formattedExpectedTime), result)
    }

    @Test
    fun `가장빠른_알람시간_포맷팅_올해_다른날짜면_수정된_올해형식_반환`() {
        // fixedNow = 2023년 10월 26일 (목요일) 10:00
        // 목표: 11월 5일 (일요일) 14:30 에 알람이 울리도록.
        // 이 날짜는 fixedNow 기준 "오늘"도 "내일"도 아님.
        val alarmsForThisYearTest = listOf(
            Alarm(
                id = 1,
                hour = 14, // 알람 시간
                minute = 30,
                repeatDays = AlarmDay.SUN.bitValue, // 일요일 반복
                isAlarmActive = true,
            )
        )

        val result = formatter.getFormattedEarliestUpcomingAlarmDeliveryTime(
            alarmsForThisYearTest,
            deliveryFormats,
            fixedNow
        )

        val expectedAlarmTime = LocalDateTime.of(2023, 10, 29, 14, 30)
        val formattedExpectedTime =
            expectedAlarmTime.format(getLocalizedFormatter(deliveryFormats.thisYearDatePattern))
        assertEquals(String.format(deliveryFormats.thisYear, formattedExpectedTime), result)
    }

    @Test
    fun `가장빠른_알람시간_포맷팅_다른해면_수정된_다른해형식_반환`() {
        // 현재 시간을 2023년 12월 31일 10:00 으로 설정
        val nowInLate2023 = LocalDateTime.of(2023, 12, 31, 10, 0, 0)

        // 알람이 다음 해인 2024년 1월 1일 9:00 에 울리도록 설정 (단일 알람)
        val alarmsForNewYear = listOf(
            Alarm(
                id = 1,
                hour = 9,
                minute = 0,
                repeatDays = 0, // 단일 알람
                isAlarmActive = true,
            )
        )

        // formatter.getFormattedEarliestUpcomingAlarmDeliveryTime 내부에서
        // calculateNextOccurrence(9, 0, 0, nowInLate2023)가 호출됨.
        // nowInLate2023 (2023-12-31 10:00) 기준으로, 알람 시간 09:00은 과거이므로,
        // 다음 날인 2024-01-01 09:00이 반환되어야 함.
        val result = formatter.getFormattedEarliestUpcomingAlarmDeliveryTime(
            alarmsForNewYear,
            deliveryFormats,
            nowInLate2023
        )

        // deliveryFormats.otherYear = "%1$s 도착"
        // deliveryFormats.otherYearDatePattern = "yy년 M월 d일 a h:mm"
        // nowInLate2023의 year (2023)와 결과 날짜의 year (2024)가 다르므로 "otherYear" 포맷 사용
        val expectedAlarmTime = LocalDateTime.of(2024, 1, 1, 9, 0)
        val formattedExpectedTime =
            expectedAlarmTime.format(getLocalizedFormatter(deliveryFormats.otherYearDatePattern))
        assertEquals(String.format(deliveryFormats.otherYear, formattedExpectedTime), result)
    }

    @Test
    fun `가장빠른_알람시간_포맷팅_여러_활성알람중_가장빠른것_정확히_포맷팅_수정된형식`() {
        val alarms = listOf(
            Alarm(id = 1, hour = 15, minute = 0, repeatDays = 0, isAlarmActive = true), // 오늘 15:00
            Alarm(id = 2, hour = 12, minute = 0, repeatDays = 0, isAlarmActive = true), // 오늘 12:00 (이게 더 빠름)
            Alarm(id = 3, hour = 9, minute = 0, repeatDays = 0, isAlarmActive = false),
            Alarm(id = 4, hour = 8, minute = 0, repeatDays = AlarmDay.FRI.bitValue, isAlarmActive = true) // 내일 08:00
        )
        val result = formatter.getFormattedEarliestUpcomingAlarmDeliveryTime(
            alarms,
            deliveryFormats,
            fixedNow
        )

        val expectedAlarmTime = LocalDateTime.of(2023, 10, 26, 12, 0)
        val formattedExpectedTime =
            expectedAlarmTime.format(getLocalizedFormatter(deliveryFormats.todayTimePattern))
        assertEquals(String.format(deliveryFormats.today, formattedExpectedTime), result)
    }

    @Test
    fun `날짜시간문자열_포맷팅_잘못된_날짜형식이면_수정된_알람없음_반환`() {
        val result = formatter.getFormattedEarliestUpcomingAlarmDeliveryTime(
            emptyList(),
            deliveryFormats,
            fixedNow
        )
        assertEquals(deliveryFormats.noAlarm, result)
    }

    private val timeFormats = AlarmDateTimeFormatter.TimeDifferenceFormats(
        daysHoursMinutesFormat = "%1\$d일 %2\$d시간 %3\$d분 후에 울려요",
        hoursMinutesFormat = "%1\$d시간 %2\$d분 후에 울려요",
        minutesFormat = "%1\$d분 후에 울려요",
        soonFormat = "곧 울려요"
    )

    @Test
    fun `시간차이_포맷팅_차이없거나_과거면_곧울려요_반환`() {
        assertEquals(
            timeFormats.soonFormat,
            formatter.formatTimeDifference(fixedNow, fixedNow, timeFormats)
        )
        assertEquals(
            timeFormats.soonFormat,
            formatter.formatTimeDifference(fixedNow, fixedNow.minusMinutes(1), timeFormats)
        )
    }

    @Test
    fun `시간차이_포맷팅_1분미만_차이면_곧울려요_반환`() {
        val future = fixedNow.plusSeconds(30)
        assertEquals(
            timeFormats.soonFormat,
            formatter.formatTimeDifference(fixedNow, future, timeFormats)
        )
    }

    @Test
    fun `시간차이_포맷팅_25분_차이면_정확한_문자열_반환`() {
        val futureTime = fixedNow.plusMinutes(25)
        val result = formatter.formatTimeDifference(fixedNow, futureTime, timeFormats)
        assertEquals(String.format(testLocale, timeFormats.minutesFormat, 25L), result)
    }

    @Test
    fun `시간차이_포맷팅_70분_차이면_정확한_문자열_반환`() {
        val futureTime = fixedNow.plusMinutes(70) // 1시간 10분
        val result = formatter.formatTimeDifference(fixedNow, futureTime, timeFormats)
        assertEquals(String.format(testLocale, timeFormats.hoursMinutesFormat, 1L, 10L), result)
    }

    @Test
    fun `시간차이_포맷팅_1일_1시간_5분_차이면_정확한_문자열_반환`() {
        val futureTime = fixedNow.plusDays(1).plusHours(1).plusMinutes(5)
        val result = formatter.formatTimeDifference(fixedNow, futureTime, timeFormats)
        assertEquals(
            String.format(testLocale, timeFormats.daysHoursMinutesFormat, 1L, 1L, 5L),
            result
        )
    }
}
