package com.yapp.data.local.di

import com.yapp.data.local.datasource.AlarmLocalDataSource
import com.yapp.data.local.datasource.AlarmLocalDataSourceImpl
import com.yapp.data.local.datasource.FortuneLocalDataSource
import com.yapp.data.local.datasource.FortuneLocalDataSourceImpl
import com.yapp.data.local.datasource.UserLocalDataSource
import com.yapp.data.local.datasource.UserLocalDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    @Singleton
    abstract fun bindsAlarmDataSource(
        alarmLocalDataSource: AlarmLocalDataSourceImpl,
    ): AlarmLocalDataSource

    @Binds
    @Singleton
    abstract fun bindsFortuneDataSource(
        fortuneLocalDataSource: FortuneLocalDataSourceImpl,
    ): FortuneLocalDataSource

    @Binds
    @Singleton
    abstract fun bindsUserDataSource(
        userLocalDataSource: UserLocalDataSourceImpl,
    ): UserLocalDataSource
}
