package com.yapp.data.remote.di

import com.yapp.data.remote.datasource.FortuneDataSource
import com.yapp.data.remote.datasource.FortuneDataSourceImpl
import com.yapp.data.remote.datasource.SignUpDataSource
import com.yapp.data.remote.datasource.SignUpDataSourceImpl
import com.yapp.data.remote.datasource.UserInfoDataSource
import com.yapp.data.remote.datasource.UserInfoDataSourceImpl
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
    abstract fun bindsSignUpDataSource(
        signUpDataSource: SignUpDataSourceImpl,
    ): SignUpDataSource

    @Binds
    @Singleton
    abstract fun bindsUserInfoDataSource(
        userInfoDataSource: UserInfoDataSourceImpl,
    ): UserInfoDataSource

    @Binds
    @Singleton
    abstract fun bindsFortuneDataSource(
        fortuneDataSource: FortuneDataSourceImpl,
    ): FortuneDataSource
}
