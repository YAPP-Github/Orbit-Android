package com.yapp.remoteconfig.di

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.yapp.remoteconfig.FirebaseRemoteConfigManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteConfigModule {

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return Firebase.remoteConfig.apply {
            setConfigSettingsAsync(
                remoteConfigSettings {
                    minimumFetchIntervalInSeconds = 3600L
                },
            )
        }
    }

    @Provides
    @Singleton
    fun provideRemoteConfigManager(
        remoteConfig: FirebaseRemoteConfig,
    ): FirebaseRemoteConfigManager = FirebaseRemoteConfigManager(remoteConfig)
}
