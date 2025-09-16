package com.yapp.database.di

import android.content.Context
import androidx.room.Room
import com.yapp.database.AlarmDao
import com.yapp.database.AlarmDatabase
import com.yapp.database.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun providesAlarmDatabase(
        @ApplicationContext context: Context,
    ): AlarmDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AlarmDatabase::class.java,
            AlarmDatabase.DATABASE_NAME,
        )
            .addMigrations(DatabaseMigrations.MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun providesAlarmDao(
        database: AlarmDatabase,
    ): AlarmDao {
        return database.alarmDao()
    }
}
