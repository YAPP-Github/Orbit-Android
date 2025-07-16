package com.yapp.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yapp.database.AlarmDatabase.Companion.DATABASE_NAME

internal object DatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 1. 새 스키마로 임시 테이블 생성 (isAm 컬럼 제외, missionType, missionCount 추가 및 기본값 변경)
            database.execSQL(
                """
                CREATE TABLE ${DATABASE_NAME}_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    hour INTEGER NOT NULL,
                    minute INTEGER NOT NULL,
                    second INTEGER NOT NULL,
                    repeatDays INTEGER NOT NULL,
                    isHolidayAlarmOff INTEGER NOT NULL,
                    isSnoozeEnabled INTEGER NOT NULL,
                    snoozeInterval INTEGER NOT NULL,
                    snoozeCount INTEGER NOT NULL,
                    isVibrationEnabled INTEGER NOT NULL,
                    isSoundEnabled INTEGER NOT NULL,
                    soundUri TEXT NOT NULL,
                    soundVolume INTEGER NOT NULL,
                    isAlarmActive INTEGER NOT NULL,
                    missionType INTEGER NOT NULL DEFAULT 1,  -- 타입 INTEGER, 기본값 1
                    missionCount INTEGER NOT NULL DEFAULT 10 -- 타입 INTEGER, 기본값 10
                )
                """.trimIndent(),
            )

            // 2. 기존 테이블에서 새 임시 테이블로 데이터 복사 (isAm 컬럼은 복사하지 않음)
            database.execSQL(
                """
                INSERT INTO ${DATABASE_NAME}_new (
                    id, hour, minute, second, repeatDays, isHolidayAlarmOff,
                    isSnoozeEnabled, snoozeInterval, snoozeCount, isVibrationEnabled,
                    isSoundEnabled, soundUri, soundVolume, isAlarmActive
                    -- missionType, missionCount는 CREATE TABLE에서 정의된 기본값으로 자동 채워짐
                )
                SELECT
                    id,
                    -- hour를 24시간 형식으로 변환합니다.
                    -- 예시: isAm 컬럼이 0 (PM)이고 hour가 12가 아니면 hour + 12
                    -- 예시: isAm 컬럼이 1 (AM)이고 hour가 12 (자정)이면 0으로 변환
                    -- 실제 isAm 컬럼의 의미와 값에 따라 아래 로직을 조정해야 합니다.
                    CASE
                        WHEN isAm = 0 AND hour != 12 THEN hour + 12 -- 오후 1시 ~ 11시 -> 13 ~ 23시
                        WHEN isAm = 1 AND hour = 12 THEN 0          -- 오전 12시 (자정) -> 0시
                        ELSE hour                                   -- 그 외 (오전 1시 ~ 11시, 오후 12시(정오))
                    END AS hour_24,
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
                FROM $DATABASE_NAME
                """.trimIndent(),
            )

            // 3. 기존 테이블 삭제
            database.execSQL("DROP TABLE $DATABASE_NAME")

            // 4. 임시 테이블의 이름을 기존 테이블 이름으로 변경
            database.execSQL("ALTER TABLE ${DATABASE_NAME}_new RENAME TO $DATABASE_NAME")
        }
    }
}
