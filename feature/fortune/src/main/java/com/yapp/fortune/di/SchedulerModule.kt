package com.yapp.fortune.di

import com.yapp.alarm.scheduler.PostFortuneTaskScheduler
import com.yapp.fortune.scheduler.WorkManagerPostFortuneTaskScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SchedulerModule {
    @Binds
    @Singleton
    abstract fun bindsPostFortuneTaskScheduler(
        postFortuneTaskScheduler: WorkManagerPostFortuneTaskScheduler,
    ): PostFortuneTaskScheduler
}
