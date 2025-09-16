package com.yapp.orbit.di

import com.yapp.orbit.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppVersionModule {
    @Provides
    @Singleton
    @Named("appVersion")
    fun provideAppVersion(): String = BuildConfig.VERSION_NAME
}
