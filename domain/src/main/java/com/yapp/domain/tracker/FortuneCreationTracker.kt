package com.yapp.domain.tracker

import com.yapp.domain.model.FortuneCreationState
import kotlinx.coroutines.flow.StateFlow

interface FortuneCreationTracker {
    val state: StateFlow<FortuneCreationState>
    fun start()
    fun succeed(fortuneId: Long)
    fun fail()
}
