package com.yapp.data.di

import com.yapp.data.repositoryimpl.AlarmRepositoryImpl
import com.yapp.data.repositoryimpl.FortuneRepositoryImpl
import com.yapp.data.repositoryimpl.RemoteConfigRepositoryImpl
import com.yapp.data.repositoryimpl.SignUpRepositoryImpl
import com.yapp.data.repositoryimpl.UserInfoRepositoryImpl
import com.yapp.domain.repository.AlarmRepository
import com.yapp.domain.repository.FortuneRepository
import com.yapp.domain.repository.RemoteConfigRepository
import com.yapp.domain.repository.SignUpRepository
import com.yapp.domain.repository.UserInfoRepository
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
    abstract fun bindsFortuneRepository(
        fortuneRepository: FortuneRepositoryImpl,
    ): FortuneRepository

    @Binds
    @Singleton
    abstract fun bindsUserInfoRepository(
        userInfoRepository: UserInfoRepositoryImpl,
    ): UserInfoRepository

    @Binds
    @Singleton
    abstract fun bindsSignUpRepository(
        signUpRepository: SignUpRepositoryImpl,
    ): SignUpRepository

    @Binds
    @Singleton
    abstract fun bindsRemoteConfigRepository(
        remoteConfigRepository: RemoteConfigRepositoryImpl,
    ): RemoteConfigRepository
}
