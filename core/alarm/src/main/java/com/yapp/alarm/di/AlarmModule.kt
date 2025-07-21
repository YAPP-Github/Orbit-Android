package com.yapp.alarm.di

import android.app.AlarmManager
import android.content.Context
import com.yapp.alarm.AlarmTimeCalculator
import com.yapp.alarm.AndroidAlarmScheduler
import com.yapp.domain.scheduler.AlarmScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmModule {
    @Binds
    @Singleton
    abstract fun bindsAlarmScheduler(
        alarmScheduler: AndroidAlarmScheduler,
    ): AlarmScheduler

    companion object {
        @Provides
        @Singleton
        fun provideAlarmTimeCalculator(clock: Clock): AlarmTimeCalculator {
            return AlarmTimeCalculator(clock)
        }

        @Provides
        @Singleton
        fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager {
            return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }
    }
}
