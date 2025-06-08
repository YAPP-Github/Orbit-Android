package com.yapp.data.remote.di

import com.yapp.data.remote.service.ApiService
import com.yapp.network.di.NoneAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun providesApiService(@NoneAuth retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}
