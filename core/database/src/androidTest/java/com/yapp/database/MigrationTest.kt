package com.yapp.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
                    1,           -- isAm = true
                    7,           -- hour
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

        val cursor = db.query("SELECT missionType, missionCount FROM ${AlarmDatabase.DATABASE_NAME}")
        cursor.use {
            assertEquals(1, it.count)
            it.moveToFirst()
            assertEquals("TAP", it.getString(it.getColumnIndexOrThrow("missionType")))
            assertEquals(10, it.getInt(it.getColumnIndexOrThrow("missionCount")))
        }
    }
}
