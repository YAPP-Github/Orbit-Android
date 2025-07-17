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
    private val fixedNow = LocalDateTime.of(2023, 10, 26, 10, 0, 0) // 목요일
    private val fixedClock = Clock.fixed(fixedNow.atZone(ZoneId.of("Asia/Seoul")).toInstant(), ZoneId.of("Asia/Seoul"))
    private val testLocale: Locale = Locale.KOREA

    @Before
    fun `테스트_준비`() {
        formatter = AlarmDateTimeFormatter(clock = fixedClock, displayLocale = testLocale)
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
        todayTimePattern = "a h:mm",
        thisYearDatePattern = "M월 d일 a h:mm",
        otherYearDatePattern = "yy년 M월 d일 a h:mm"
    )

    @Test
    fun `가장빠른_알람시간_포맷팅_활성알람_없으면_수정된_알람없음_반환`() {
        // given
        val alarms = listOf(Alarm(id = 1, hour = 14, minute = 0, repeatDays = 0, isAlarmActive = false))

        // when
        val result = formatter.getFormattedEarliestUpcomingAlarmDeliveryTime(alarms, deliveryFormats, fixedNow)

        // then
        assertEquals(deliveryFormats.noAlarm, result)
    }

    @Test
    fun `가장빠른_알람시간_포맷팅_오늘_미래_활성알람_하나면_수정된_오늘형식_반환`() {
        // given
        val alarms = listOf(Alarm(id = 1, hour = 14, minute = 30, repeatDays = 0, isAlarmActive = true))
        val expectedTime = LocalDateTime.of(2023, 10, 26, 14, 30)
        val expected = String.format(deliveryFormats.today, expectedTime.format(getLocalizedFormatter(deliveryFormats.todayTimePattern)))

        // when
        val result = formatter.getFormattedEarliestUpcomingAlarmDeliveryTime(alarms, deliveryFormats, fixedNow)

        // then
        assertEquals(expected, result)
    }

    @Test
    fun `가장빠른_알람시간_포맷팅_내일_활성알람_하나면_수정된_내일형식_반환`() {
        // given
        val alarms = listOf(Alarm(id = 1, hour = 8, minute = 0, repeatDays = 0, isAlarmActive = true))
        val expectedTime = fixedNow.toLocalDate().plusDays(1).atTime(8, 0)
        val expected = String.format(deliveryFormats.tomorrow, expectedTime.format(getLocalizedFormatter(deliveryFormats.todayTimePattern)))

        // when
        val result = formatter.getFormattedEarliestUpcomingAlarmDeliveryTime(alarms, deliveryFormats, fixedNow)

        // then
        assertEquals(expected, result)
    }

    @Test
    fun `가장빠른_알람시간_포맷팅_올해_다른날짜면_수정된_올해형식_반환`() {
        // given
        val alarms = listOf(Alarm(id = 1, hour = 14, minute = 30, repeatDays = AlarmDay.SUN.bitValue, isAlarmActive = true))
        val expectedTime = LocalDateTime.of(2023, 10, 29, 14, 30)
        val expected = String.format(deliveryFormats.thisYear, expectedTime.format(getLocalizedFormatter(deliveryFormats.thisYearDatePattern)))

        // when
        val result = formatter.getFormattedEarliestUpcomingAlarmDeliveryTime(alarms, deliveryFormats, fixedNow)

        // then
        assertEquals(expected, result)
    }

    @Test
    fun `가장빠른_알람시간_포맷팅_다른해면_수정된_다른해형식_반환`() {
        // given
        val now = LocalDateTime.of(2023, 12, 31, 10, 0, 0)
        val alarms = listOf(Alarm(id = 1, hour = 9, minute = 0, repeatDays = 0, isAlarmActive = true))
        val expectedTime = LocalDateTime.of(2024, 1, 1, 9, 0)
        val expected = String.format(deliveryFormats.otherYear, expectedTime.format(getLocalizedFormatter(deliveryFormats.otherYearDatePattern)))

        // when
        val result = formatter.getFormattedEarliestUpcomingAlarmDeliveryTime(alarms, deliveryFormats, now)

        // then
        assertEquals(expected, result)
    }

    @Test
    fun `가장빠른_알람시간_포맷팅_여러_활성알람중_가장빠른것_정확히_포맷팅_수정된형식`() {
        // given
        val alarms = listOf(
            Alarm(id = 1, hour = 15, minute = 0, repeatDays = 0, isAlarmActive = true), // 오늘 15:00
            Alarm(id = 2, hour = 12, minute = 0, repeatDays = 0, isAlarmActive = true), // 오늘 12:00 (이게 더 빠름)
            Alarm(id = 3, hour = 9, minute = 0, repeatDays = 0, isAlarmActive = false),
            Alarm(id = 4, hour = 8, minute = 0, repeatDays = AlarmDay.FRI.bitValue, isAlarmActive = true) // 내일 08:00
        )
        val expectedTime = LocalDateTime.of(2023, 10, 26, 12, 0)
        val expected = String.format(deliveryFormats.today, expectedTime.format(getLocalizedFormatter(deliveryFormats.todayTimePattern)))

        // when
        val result = formatter.getFormattedEarliestUpcomingAlarmDeliveryTime(alarms, deliveryFormats, fixedNow)

        // then
        assertEquals(expected, result)
    }

    @Test
    fun `날짜시간문자열_포맷팅_잘못된_날짜형식이면_수정된_알람없음_반환`() {
        // given
        val alarms = emptyList<Alarm>()

        // when
        val result = formatter.getFormattedEarliestUpcomingAlarmDeliveryTime(alarms, deliveryFormats, fixedNow)

        // then
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
        // when & then
        assertEquals(timeFormats.soonFormat, formatter.formatTimeDifference(fixedNow, fixedNow, timeFormats))
        assertEquals(timeFormats.soonFormat, formatter.formatTimeDifference(fixedNow, fixedNow.minusMinutes(1), timeFormats))
    }

    @Test
    fun `시간차이_포맷팅_1분미만_차이면_곧울려요_반환`() {
        // given
        val future = fixedNow.plusSeconds(30)

        // when
        val result = formatter.formatTimeDifference(fixedNow, future, timeFormats)

        // then
        assertEquals(timeFormats.soonFormat, result)
    }

    @Test
    fun `시간차이_포맷팅_25분_차이면_정확한_문자열_반환`() {
        // given
        val futureTime = fixedNow.plusMinutes(25)
        val expected = String.format(testLocale, timeFormats.minutesFormat, 25L)

        // when
        val result = formatter.formatTimeDifference(fixedNow, futureTime, timeFormats)

        // then
        assertEquals(expected, result)
    }

    @Test
    fun `시간차이_포맷팅_70분_차이면_정확한_문자열_반환`() {
        // given
        val futureTime = fixedNow.plusMinutes(70)
        val expected = String.format(testLocale, timeFormats.hoursMinutesFormat, 1L, 10L)

        // when
        val result = formatter.formatTimeDifference(fixedNow, futureTime, timeFormats)

        // then
        assertEquals(expected, result)
    }

    @Test
    fun `시간차이_포맷팅_1일_1시간_5분_차이면_정확한_문자열_반환`() {
        // given
        val futureTime = fixedNow.plusDays(1).plusHours(1).plusMinutes(5)
        val expected = String.format(testLocale, timeFormats.daysHoursMinutesFormat, 1L, 1L, 5L)

        // when
        val result = formatter.formatTimeDifference(fixedNow, futureTime, timeFormats)

        // then
        assertEquals(expected, result)
    }
}
