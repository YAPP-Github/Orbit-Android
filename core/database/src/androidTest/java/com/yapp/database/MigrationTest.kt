package com.yapp.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val testDbName = "test_alarm_database"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AlarmDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @After
    fun tearDown() {
        InstrumentationRegistry.getInstrumentation()
            .targetContext.deleteDatabase(testDbName)
    }

    @Test
    @Throws(IOException::class)
    fun `버전1에서_버전2로_마이그레이션시_새_컬럼이_기본값으로_채워짐`() {
        helper.createDatabase(testDbName, 1).apply {
            execSQL(
                """
                INSERT INTO alarm_database (
                    id,
                    isAm,
                    hour,
                    minute,
                    second,
                    repeatDays,
                    isHolidayAlarmOff,
                    isSnoozeEnabled,
                    snoozeInterval,
                    snoozeCount,
                    isVibrationEnabled,
                    isSoundEnabled,
                    soundUri,
                    soundVolume,
                    isAlarmActive
                ) VALUES (
                    null,        -- id (autoGenerate)
                    0,           -- isAm = false
                    11,           -- hour
                    30,          -- minute
                    0,           -- second
                    0,           -- repeatDays
                    0,           -- isHolidayAlarmOff = false
                    1,           -- isSnoozeEnabled = true
                    5,           -- snoozeInterval
                    3,           -- snoozeCount
                    1,           -- isVibrationEnabled = true
                    1,           -- isSoundEnabled = true
                    'alarm.mp3', -- soundUri
                    70,          -- soundVolume
                    1            -- isAlarmActive = true
                )
                """.trimIndent(),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(testDbName, 2, true, DatabaseMigrations.MIGRATION_1_2)

        val cursor = db.query("SELECT hour, missionType, missionCount FROM ${AlarmDatabase.DATABASE_NAME}")
        cursor.use {
            assertEquals(1, it.count)
            it.moveToFirst()
            assertEquals(1, it.getInt(it.getColumnIndexOrThrow("missionType")))
            assertEquals(10, it.getInt(it.getColumnIndexOrThrow("missionCount")))
        }
    }

    @Test
    @Throws(IOException::class)
    fun `버전1에서_버전2로_마이그레이션시_12시간_포맷이_24시간_포맷으로_정확히_변환되는지_확인`() {
        helper.createDatabase(testDbName, 1).apply {
            // 4가지 케이스 삽입
            listOf(
                Triple(1, 12, 0), // 오전 12시 → 0시
                Triple(0, 12, 12), // 오후 12시 → 12시
                Triple(1, 7, 7), // 오전 7시 → 7시
                Triple(0, 7, 19), // 오후 7시 → 19시
            ).forEach { (isAm, hour12, _) ->
                execSQL(
                    """
                INSERT INTO alarm_database (
                    id, isAm, hour, minute, second, repeatDays, isHolidayAlarmOff,
                    isSnoozeEnabled, snoozeInterval, snoozeCount, isVibrationEnabled,
                    isSoundEnabled, soundUri, soundVolume, isAlarmActive
                ) VALUES (
                    null, $isAm, $hour12, 0, 0, 0, 0, 1, 5, 3, 1, 1, 'alarm.mp3', 70, 1
                )
                    """.trimIndent(),
                )
            }
            close()
        }

        val db =
            helper.runMigrationsAndValidate(testDbName, 2, true, DatabaseMigrations.MIGRATION_1_2)

        val expected = listOf(0, 12, 7, 19) // 기대 결과: 변환된 hour 순서
        val cursor = db.query("SELECT hour FROM ${AlarmDatabase.DATABASE_NAME}")
        cursor.use {
            assertEquals(4, it.count)
            var idx = 0
            while (it.moveToNext()) {
                val actual = it.getInt(it.getColumnIndexOrThrow("hour"))
                assertEquals(expected[idx], actual)
                idx++
            }
        }
    }
}
