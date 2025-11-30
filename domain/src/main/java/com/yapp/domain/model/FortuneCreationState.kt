package com.yapp.domain.model

sealed class FortuneCreationState {
    data object Start : FortuneCreationState()
    data class Success(val fortuneId: Long) : FortuneCreationState()
    data object Failure : FortuneCreationState()
}
