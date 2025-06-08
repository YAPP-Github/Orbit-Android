package com.yapp.data.remote.di

import com.yapp.data.remote.repositoryimpl.FortuneRepositoryImpl
import com.yapp.data.remote.repositoryimpl.RemoteConfigRepositoryImpl
import com.yapp.data.remote.repositoryimpl.SignUpRepositoryImpl
import com.yapp.data.remote.repositoryimpl.UserInfoRepositoryImpl
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
    abstract fun bindsSignUpRepository(
        signUpRepository: SignUpRepositoryImpl,
    ): SignUpRepository

    @Binds
    @Singleton
    abstract fun bindsUserInfoRepository(
        userInfoRepository: UserInfoRepositoryImpl,
    ): UserInfoRepository

    @Binds
    @Singleton
    abstract fun bindsFortuneRepository(
        fortuneRepository: FortuneRepositoryImpl,
    ): FortuneRepository

    @Binds
    @Singleton
    abstract fun bindsRemoteConfigRepository(
        remoteConfigRepository: RemoteConfigRepositoryImpl,
    ): RemoteConfigRepository
}
