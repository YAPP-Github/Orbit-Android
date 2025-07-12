package com.yapp.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object DatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE ${AlarmDatabase.DATABASE_NAME} ADD COLUMN missionType TEXT NOT NULL DEFAULT 'TAP'")
            database.execSQL("ALTER TABLE ${AlarmDatabase.DATABASE_NAME} ADD COLUMN missionCount INTEGER NOT NULL DEFAULT 10")
        }
    }
}
