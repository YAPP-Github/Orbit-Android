package com.yapp.common.di

import com.yapp.common.tracker.InMemoryFortuneCreationTracker
import com.yapp.domain.tracker.FortuneCreationTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FortuneTrackerModule {
    @Binds
    @Singleton
    abstract fun bindFortuneCreationTracker(
        impl: InMemoryFortuneCreationTracker,
    ): FortuneCreationTracker
}
