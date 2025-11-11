package com.yapp.media.di

import com.yapp.domain.media.AlarmSoundManager
import com.yapp.media.sound.AlarmSoundManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AlarmSoundModule {
    @Binds
    @Singleton
    fun bindAlarmSoundManager(
        impl: AlarmSoundManagerImpl,
    ): AlarmSoundManager
}
