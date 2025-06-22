package com.yapp.alarm.di

import com.yapp.alarm.AndroidAlarmScheduler
import com.yapp.domain.scheduler.AlarmScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmSchedulerModule {
    @Binds
    @Singleton
    abstract fun bindsAlarmScheduler(
        alarmScheduler: AndroidAlarmScheduler,
    ): AlarmScheduler
}
