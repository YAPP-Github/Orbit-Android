package com.yapp.data.local.di

import com.yapp.data.local.repositoryimpl.AlarmRepositoryImpl
import com.yapp.data.local.repositoryimpl.ImageRepositoryImpl
import com.yapp.data.local.repositoryimpl.UserDataRepositoryImpl
import com.yapp.domain.repository.AlarmRepository
import com.yapp.domain.repository.ImageRepository
import com.yapp.domain.repository.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindsAlarmRepository(
        alarmRepository: AlarmRepositoryImpl,
    ): AlarmRepository

    @Binds
    @Singleton
    abstract fun bindsImageRepository(
        imageRepository: ImageRepositoryImpl,
    ): ImageRepository

    @Binds
    @Singleton
    abstract fun bindsUserDataRepository(
        userDataRepository: UserDataRepositoryImpl,
    ): UserDataRepository
}
