import com.yapp.alarm.AlarmTimeCalculator
import com.yapp.domain.model.Alarm
import com.yapp.domain.model.AlarmDay
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmTimeCalculatorTest {

    private val testZoneId: ZoneId = ZoneId.of("Asia/Seoul")

    // --- 기준 시각 (Fixed Clocks) ---
    private val MONDAY_2024_07_22_10AM: LocalDateTime = LocalDateTime.of(2024, 7, 22, 10, 0, 0)
    private val clockMonday2024_10am: Clock = Clock.fixed(
        MONDAY_2024_07_22_10AM.toInstant(
            testZoneId.rules.getOffset(MONDAY_2024_07_22_10AM)
        ), testZoneId
    )

    private val FRIDAY_2024_07_26_3PM: LocalDateTime = LocalDateTime.of(2024, 7, 26, 15, 0, 0)
    private val clockFriday2024_3pm: Clock = Clock.fixed(
        FRIDAY_2024_07_26_3PM.toInstant(testZoneId.rules.getOffset(FRIDAY_2024_07_26_3PM)),
        testZoneId
    )

    private val MONDAY_2025_01_20_10AM: LocalDateTime = LocalDateTime.of(2025, 1, 20, 10, 0, 0)
    private val clockMonday2025_01_20_10am: Clock = Clock.fixed(
        MONDAY_2025_01_20_10AM.toInstant(
            testZoneId.rules.getOffset(MONDAY_2025_01_20_10AM)
        ), testZoneId
    )

    private val MONDAY_2025_01_20_2_01PM: LocalDateTime = LocalDateTime.of(2025, 1, 20, 14, 1, 0)
    private val clockMonday2025_PrevHoliday_2_01pm: Clock = Clock.fixed(
        MONDAY_2025_01_20_2_01PM.toInstant(
            testZoneId.rules.getOffset(MONDAY_2025_01_20_2_01PM)
        ), testZoneId
    )

    private val MONDAY_HOLIDAY_2025_01_27_10AM: LocalDateTime =
        LocalDateTime.of(2025, 1, 27, 10, 0, 0)
    private val clockMondayHoliday2025_10am: Clock = Clock.fixed(
        MONDAY_HOLIDAY_2025_01_27_10AM.toInstant(
            testZoneId.rules.getOffset(MONDAY_HOLIDAY_2025_01_27_10AM)
        ), testZoneId
    )

    private val MONDAY_2025_02_17_10AM: LocalDateTime = LocalDateTime.of(2025, 2, 17, 10, 0, 0)
    private val clockMonday2025_02_17_10am: Clock = Clock.fixed(
        MONDAY_2025_02_17_10AM.toInstant(
            testZoneId.rules.getOffset(MONDAY_2025_02_17_10AM)
        ), testZoneId
    )

    private fun createTestAlarm(
        hour: Int,
        minute: Int,
        second: Int = 0,
        isHolidayAlarmOff: Boolean = false,
        repeatDays: Int = 0, // 기본값은 비반복
    ): Alarm {
        return Alarm(
            hour = hour,
            minute = minute,
            second = second,
            repeatDays = repeatDays,
            isHolidayAlarmOff = isHolidayAlarmOff,
        )
    }

    private fun getExpectedMillis(dateTime: LocalDateTime, zone: ZoneId = testZoneId): Long {
        return dateTime.atZone(zone).toInstant().toEpochMilli()
    }

    // --- 비반복 알람 시간 계산 (calculateNonRepeatingTimeMillis) 테스트 ---
    @Test
    fun `비반복_알람시간이_오늘_미래이면_오늘_알람시간으로_계산된다`() {
        // 현재: 2024-07-22 (월) 10:00:00
        // 알람: 오늘 14:00:00, 비반복
        // 기대: 2024-07-22 (월) 14:00:00
        val calculator = AlarmTimeCalculator(clockMonday2024_10am)
        val alarmTime = LocalTime.of(14, 0)
        val alarm = createTestAlarm(alarmTime.hour, alarmTime.minute) // repeatDays = 0 (비반복)

        val expectedDateTime = MONDAY_2024_07_22_10AM.with(alarmTime)
        val expectedMillis = getExpectedMillis(expectedDateTime)

        val actualMillis = calculator.calculateNonRepeatingTimeMillis(alarm, testZoneId)
        assertEquals(expectedMillis, actualMillis)
    }

    @Test
    fun `비반복_알람시간이_오늘_과거이면_내일_알람시간으로_계산된다`() {
        // 현재: 2024-07-22 (월) 10:00:00
        // 알람: 오늘 08:00:00 (이미 지남), 비반복
        // 기대: 2024-07-23 (화) 08:00:00
        val calculator = AlarmTimeCalculator(clockMonday2024_10am)
        val alarmTime = LocalTime.of(8, 0)
        val alarm = createTestAlarm(alarmTime.hour, alarmTime.minute) // repeatDays = 0 (비반복)

        val expectedDateTime = MONDAY_2024_07_22_10AM.plusDays(1).with(alarmTime)
        val expectedMillis = getExpectedMillis(expectedDateTime)

        val actualMillis = calculator.calculateNonRepeatingTimeMillis(alarm, testZoneId)
        assertEquals(expectedMillis, actualMillis)
    }

    // --- 다음 반복 요일 알람 시간 계산 (calculateNextRepeatingTimeMillis) 테스트 ---
    @Test
    fun `반복요일알람_오늘이_대상요일이고_알람시간이_미래이며_공휴일건너뛰기_비활성시_오늘로_계산된다`() {
        // 현재: 2024-07-22 (월) 10:00:00
        // 알람: 매주 월요일 14:00:00, 공휴일 건너뛰기 비활성
        // 기대: 2024-07-22 (월) 14:00:00
        val calculator = AlarmTimeCalculator(clockMonday2024_10am)
        val alarmTime = LocalTime.of(14, 0)
        val alarm = createTestAlarm(
            hour = alarmTime.hour,
            minute = alarmTime.minute,
            isHolidayAlarmOff = false,
            repeatDays = AlarmDay.MON.bitValue // 월요일 반복
        )

        val expectedDateTime = MONDAY_2024_07_22_10AM.with(alarmTime)
        val expectedMillis = getExpectedMillis(expectedDateTime)

        val actualMillis =
            calculator.calculateNextRepeatingTimeMillis(alarm, AlarmDay.MON, testZoneId)
        assertEquals(expectedMillis, actualMillis)
    }

    @Test
    fun `반복요일알람_오늘이_공휴일인_울릴요일이고_알람시간이_미래이며_공휴일건너뛰기_비활성시_오늘_공휴일로_계산된다`() {
        // 현재: 2025-01-27 (월, 공휴일) 10:00:00
        // 알람: 매주 월요일 14:00:00, 공휴일 건너뛰기 비활성
        // 기대: 2025-01-27 (월, 공휴일) 14:00:00 (건너뛰기 비활성이므로 오늘 공휴일이어도 울림)
        val calculator = AlarmTimeCalculator(clockMondayHoliday2025_10am)
        val alarmTime = LocalTime.of(14, 0)
        val alarm = createTestAlarm(
            hour = alarmTime.hour,
            minute = alarmTime.minute,
            isHolidayAlarmOff = false,
            repeatDays = AlarmDay.MON.bitValue // 월요일 반복
        )

        val expectedDateTime = MONDAY_HOLIDAY_2025_01_27_10AM.with(alarmTime)
        val expectedMillis = getExpectedMillis(expectedDateTime)

        val actualMillis =
            calculator.calculateNextRepeatingTimeMillis(alarm, AlarmDay.MON, testZoneId)
        assertEquals(expectedMillis, actualMillis)
    }

    @Test
    fun `반복요일알람_다음주_울릴요일이_공휴일이고_공휴일건너뛰기_비활성시_다음주_공휴일로_계산된다`() {
        // 현재: 2025-01-20 (월) 10:00:00 (공휴일 아닌 월요일)
        // 알람: 매주 월요일 09:00:00, 공휴일 건너뛰기 비활성
        // 다음 주 월요일: 2025-01-27 (공휴일)
        // 기대: 2025-01-27 (월, 공휴일) 09:00:00 (건너뛰기 비활성이므로 다음 주 공휴일이어도 울림)
        val calculator = AlarmTimeCalculator(clockMonday2025_01_20_10am)
        val alarmTime = LocalTime.of(9, 0)
        val alarm = createTestAlarm(
            hour = alarmTime.hour,
            minute = alarmTime.minute,
            isHolidayAlarmOff = false,
            repeatDays = AlarmDay.MON.bitValue // 월요일 반복
        )

        val expectedDateTime = LocalDateTime.of(2025, 1, 27, 9, 0, 0)
        val expectedMillis = getExpectedMillis(expectedDateTime)

        val actualMillis =
            calculator.calculateNextRepeatingTimeMillis(alarm, AlarmDay.MON, testZoneId)
        assertEquals(expectedMillis, actualMillis)
    }

    @Test
    fun `반복요일알람_대상요일이_이번주_미래요일이고_공휴일건너뛰기_비활성시_해당요일로_계산된다`() {
        // 현재: 2024-07-22 (월) 10:00:00
        // 알람: 매주 수요일 11:00:00, 공휴일 건너뛰기 비활성
        // 기대: 2024-07-24 (수) 11:00:00
        val calculator = AlarmTimeCalculator(clockMonday2024_10am)
        val alarmTime = LocalTime.of(11, 0)
        val alarm = createTestAlarm(
            hour = alarmTime.hour,
            minute = alarmTime.minute,
            isHolidayAlarmOff = false,
            repeatDays = AlarmDay.WED.bitValue // 수요일 반복
        )

        val expectedDateTime =
            MONDAY_2024_07_22_10AM.plusDays(2).with(alarmTime) // 2024-07-24 (수) 11:00
        val expectedMillis = getExpectedMillis(expectedDateTime)

        val actualMillis =
            calculator.calculateNextRepeatingTimeMillis(alarm, AlarmDay.WED, testZoneId)
        assertEquals(expectedMillis, actualMillis)
    }

    @Test
    fun `반복요일알람_오늘이_대상요일이나_시간이_지났고_다음주_해당요일이_공휴일이며_공휴일_비활성시_다음주_공휴일로_계산된다`() {
        // 현재: 2025-01-20 (월) 14:01 (월요일 14:00 알람 후)
        // 알람: 매주 월요일 14:00, isHolidayAlarmOff = false
        // 다음주 월요일: 2025-01-27 (공휴일)
        // 기대: 2025-01-27 (월) 14:00 (옵션 Off이므로 공휴일이어도 울림)
        val calculator = AlarmTimeCalculator(clockMonday2025_PrevHoliday_2_01pm)
        val alarmTime = LocalTime.of(14, 0)
        val alarm = createTestAlarm(
            hour = alarmTime.hour,
            minute = alarmTime.minute,
            isHolidayAlarmOff = false,
            repeatDays = AlarmDay.MON.bitValue // 월요일 반복
        )

        val expectedDateTime = LocalDateTime.of(2025, 1, 27, 14, 0, 0)
        val expectedMillis = getExpectedMillis(expectedDateTime)

        val actualMillis =
            calculator.calculateNextRepeatingTimeMillis(alarm, AlarmDay.MON, testZoneId)
        assertEquals(expectedMillis, actualMillis)
    }

    @Test
    fun `반복요일알람_오늘이_대상요일이나_시간이_지났고_다음주_해당요일이_공휴일이며_공휴일건너뛰기_활성시_다다음주_해당요일로_계산된다`() {
        // 현재: 2025-01-20 (월) 14:01 (월요일 14:00 알람 후)
        // 알람: 매주 월요일 14:00, isHolidayAlarmOff = true
        // 다음주 월요일: 2025-01-27 (공휴일)
        // 기대: 다다음주 월요일 2025-02-03 (월) 14:00
        val calculator = AlarmTimeCalculator(clockMonday2025_PrevHoliday_2_01pm)
        val alarmTime = LocalTime.of(14, 0)
        val alarm = createTestAlarm(
            hour = alarmTime.hour,
            minute = alarmTime.minute,
            isHolidayAlarmOff = true,
            repeatDays = AlarmDay.MON.bitValue // 월요일 반복
        )

        val expectedDateTime = LocalDateTime.of(2025, 2, 3, 14, 0, 0)
        val expectedMillis = getExpectedMillis(expectedDateTime)

        val actualMillis =
            calculator.calculateNextRepeatingTimeMillis(alarm, AlarmDay.MON, testZoneId)
        assertEquals(expectedMillis, actualMillis)
    }

    @Test
    fun `반복요일알람_오늘이_공휴일인_대상요일이고_알람시간이_미래이며_공휴일건너뛰기_활성시_다음주_해당요일로_계산된다`() {
        // 현재: 2025-01-27 (월, 공휴일) 10:00
        // 알람: 매주 월요일 14:00, isHolidayAlarmOff = true
        // 기대: 다음주 월요일 2025-02-03 (월) 14:00
        val calculator = AlarmTimeCalculator(clockMondayHoliday2025_10am)
        val alarmTime = LocalTime.of(14, 0)
        val alarm = createTestAlarm(
            hour = alarmTime.hour,
            minute = alarmTime.minute,
            isHolidayAlarmOff = true,
            repeatDays = AlarmDay.MON.bitValue // 월요일 반복
        )

        val expectedDateTime = LocalDateTime.of(2025, 2, 3, 14, 0, 0)
        val expectedMillis = getExpectedMillis(expectedDateTime)

        val actualMillis =
            calculator.calculateNextRepeatingTimeMillis(alarm, AlarmDay.MON, testZoneId)
        assertEquals(expectedMillis, actualMillis)
    }

    @Test
    fun `반복요일알람_오늘이_공휴일인_대상요일이고_알람시간이_미래이며_공휴일건너뛰기_비활성시_오늘_공휴일로_계산된다`() {
        // 현재: 2025-01-27 (월, 공휴일) 10:00
        // 알람: 매주 월요일 14:00, isHolidayAlarmOff = false
        // 기대: 오늘 2025-01-27 (월) 14:00
        val calculator = AlarmTimeCalculator(clockMondayHoliday2025_10am)
        val alarmTime = LocalTime.of(14, 0)
        val alarm = createTestAlarm(
            hour = alarmTime.hour,
            minute = alarmTime.minute,
            isHolidayAlarmOff = false,
            repeatDays = AlarmDay.MON.bitValue // 월요일 반복
        )

        val expectedDateTime = MONDAY_HOLIDAY_2025_01_27_10AM.with(alarmTime)
        val expectedMillis = getExpectedMillis(expectedDateTime)

        val actualMillis =
            calculator.calculateNextRepeatingTimeMillis(alarm, AlarmDay.MON, testZoneId)
        assertEquals(expectedMillis, actualMillis)
    }


    // --- 다음 주간 재예약 알람 시간 계산 (calculateNextWeeklyRescheduledTimeMillis) 테스트 ---
    @Test
    fun `주간재예약_현재_월요일오전_대상도_월요일_공휴일건너뛰기_비활성_다음주_월요일이_공휴일아닐때_다음주_월요일로_계산된다`() {
        // 현재: 2024-07-22 (월) 10:00
        // 알람: 매주 월요일 14:00, isHolidayAlarmOff = false
        // 다음주 월요일: 2024-07-29 (공휴일 아님)
        // 기대: 2024-07-29 (월) 14:00
        val calculator = AlarmTimeCalculator(clockMonday2024_10am)
        val alarmTime = LocalTime.of(14, 0)
        val alarm = createTestAlarm(
            hour = alarmTime.hour,
            minute = alarmTime.minute,
            isHolidayAlarmOff = false,
            repeatDays = AlarmDay.MON.bitValue // 월요일 반복
        )

        val expectedDateTime = LocalDateTime.of(2024, 7, 29, 14, 0, 0)
        val expectedMillis = getExpectedMillis(expectedDateTime)

        val actualMillis =
            calculator.calculateNextWeeklyRescheduledTimeMillis(alarm, AlarmDay.MON, testZoneId)
        assertEquals(expectedMillis, actualMillis)
    }

    @Test
    fun `주간재예약_다음주_울릴요일이_공휴일이고_공휴일건너뛰기_비활성시_다음주_공휴일로_계산된다`() {
        // 현재: 2025-01-20 (월) 10:00:00 (설 연휴 전 주 월요일 오전)
        // 알람: 매주 월요일 14:00:00, 공휴일 건너뛰기 비활성
        // 다음주 월요일: 2025-01-27 (공휴일)
        // 기대: 2025-01-27 (월, 공휴일) 14:00:00 (건너뛰기 비활성이므로 다음주 공휴일이어도 울림)
        val calculator = AlarmTimeCalculator(clockMonday2025_01_20_10am)
        val alarmTime = LocalTime.of(14, 0)
        val alarm = createTestAlarm(
            hour = alarmTime.hour,
            minute = alarmTime.minute,
            isHolidayAlarmOff = false,
            repeatDays = AlarmDay.MON.bitValue // 월요일 반복
        )

        val expectedDateTime = LocalDateTime.of(2025, 1, 27, 14, 0, 0)
        val expectedMillis = getExpectedMillis(expectedDateTime)

        val actualMillis =
            calculator.calculateNextWeeklyRescheduledTimeMillis(alarm, AlarmDay.MON, testZoneId)
        assertEquals(expectedMillis, actualMillis)
    }

    @Test
    fun `주간재예약_현재_금요일오후_대상은_월요일_공휴일건너뛰기_비활성시_다다음주_월요일로_계산된다`() {
        // 현재: 2024-07-26 (금) 15:00
        // 알람: 매주 월요일 14:00, isHolidayAlarmOff = false
        // 로직: 가장 가까운 다음 월요일(29일)의 그 다음 주 월요일(5일)
        // 기대: 2024-08-05 (월) 14:00
        val calculator = AlarmTimeCalculator(clockFriday2024_3pm)
        val alarmTime = LocalTime.of(14, 0)
        val alarm = createTestAlarm(
            hour = alarmTime.hour,
            minute = alarmTime.minute,
            isHolidayAlarmOff = false,
            repeatDays = AlarmDay.MON.bitValue // 월요일 반복
        )

        val expectedDateTime = LocalDateTime.of(2024, 8, 5, 14, 0, 0)
        val expectedMillis = getExpectedMillis(expectedDateTime)

        val actualMillis =
            calculator.calculateNextWeeklyRescheduledTimeMillis(alarm, AlarmDay.MON, testZoneId)
        assertEquals(expectedMillis, actualMillis)
    }

    @Test
    fun `주간재예약_현재_월요일_대상도_월요일_공휴일건너뛰기_활성_다음주_월요일이_공휴일일때_다다음주_월요일로_계산된다`() {
        // 현재: 2025-01-20 (월) 10:00 (설 연휴 전 주 월요일 오전)
        // 알람: 매주 월요일 14:00, isHolidayAlarmOff = true
        // 다음주 월요일: 2025-01-27 (공휴일)
        // 기대: 다다음주 월요일 2025-02-03 (월) 14:00
        val calculator = AlarmTimeCalculator(clockMonday2025_01_20_10am)
        val alarmTime = LocalTime.of(14, 0)
        val alarm = createTestAlarm(
            hour = alarmTime.hour,
            minute = alarmTime.minute,
            isHolidayAlarmOff = true,
            repeatDays = AlarmDay.MON.bitValue // 월요일 반복
        )

        val expectedDateTime = LocalDateTime.of(2025, 2, 3, 14, 0, 0)
        val expectedMillis = getExpectedMillis(expectedDateTime)

        val actualMillis =
            calculator.calculateNextWeeklyRescheduledTimeMillis(alarm, AlarmDay.MON, testZoneId)
        assertEquals(expectedMillis, actualMillis)
    }

    @Test
    fun `주간재예약_현재_월요일_대상도_월요일_공휴일건너뛰기_활성_다음주_월요일이_공휴일아닐때_다음주_월요일로_계산된다`() {
        // 현재: 2025-02-17 (월) 10:00 (삼일절 연휴 전전 주 월요일)
        // 알람: 매주 월요일 14:00, isHolidayAlarmOff = true
        // 다음주 월요일: 2025-02-24 (공휴일 아님)
        // 기대: 2025-02-24 (월) 14:00
        val calculator = AlarmTimeCalculator(clockMonday2025_02_17_10am)
        val alarmTime = LocalTime.of(14, 0)
        val alarm = createTestAlarm(
            hour = alarmTime.hour,
            minute = alarmTime.minute,
            isHolidayAlarmOff = true,
            repeatDays = AlarmDay.MON.bitValue // 월요일 반복
        )

        val expectedDateTime = LocalDateTime.of(2025, 2, 24, 14, 0, 0)
        val expectedMillis = getExpectedMillis(expectedDateTime)

        val actualMillis =
            calculator.calculateNextWeeklyRescheduledTimeMillis(alarm, AlarmDay.MON, testZoneId)
        assertEquals(expectedMillis, actualMillis)
    }
}
