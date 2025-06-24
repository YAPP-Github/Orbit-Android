package com.yapp.network.di

import com.yapp.common.buildconfig.BuildConfigFieldProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(
        buildConfigFieldProvider: BuildConfigFieldProvider,
    ): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        if (buildConfigFieldProvider.get().isDebug) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    @Provides
    @Singleton
    @Auth
    fun provideAuthOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: Interceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()

    @Provides
    @Singleton
    @NoneAuth
    fun provideNoneAuthOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .addInterceptor(loggingInterceptor)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .connectTimeout(300, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Auth
    fun provideAuthRetrofit(@Auth okHttpClient: OkHttpClient, buildConfigFieldProvider: BuildConfigFieldProvider): Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(buildConfigFieldProvider.get().baseUrl)
        .client(okHttpClient)
        .build()

    @Provides
    @Singleton
    @NoneAuth
    fun provideNoneAuthRetrofit(
        @NoneAuth okHttpClient: OkHttpClient,
        buildConfigFieldProvider: BuildConfigFieldProvider,
        json: Json,
    ): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(buildConfigFieldProvider.get().baseUrl)
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    @S3
    fun provideS3Retrofit(@NoneAuth okHttpClient: OkHttpClient, buildConfigFieldProvider: BuildConfigFieldProvider): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(buildConfigFieldProvider.get().baseUrl)
            .client(okHttpClient)
            .build()
}
