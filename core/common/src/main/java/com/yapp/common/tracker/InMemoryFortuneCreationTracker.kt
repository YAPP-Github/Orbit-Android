package com.yapp.common.tracker

import com.yapp.domain.model.FortuneCreationState
import com.yapp.domain.tracker.FortuneCreationTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryFortuneCreationTracker @Inject constructor() : FortuneCreationTracker {
    private val _state = MutableStateFlow<FortuneCreationState>(FortuneCreationState.Start)
    override val state: StateFlow<FortuneCreationState> = _state.asStateFlow()

    override fun start() {
        _state.value = FortuneCreationState.Start
    }

    override fun succeed(fortuneId: Long) {
        _state.value = FortuneCreationState.Success(fortuneId)
    }

    override fun fail() {
        _state.value = FortuneCreationState.Failure
    }
}
