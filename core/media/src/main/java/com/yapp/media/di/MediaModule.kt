package com.yapp.media.di

import android.content.ContentResolver
import android.content.Context
import com.yapp.media.storage.ImageSaver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideImageSaver(
        contentResolver: ContentResolver,
    ): ImageSaver {
        return ImageSaver(contentResolver)
    }
}
