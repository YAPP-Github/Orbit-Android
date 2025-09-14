package com.yapp.domain.model

sealed class FortuneCreateStatus {
    data object Idle : FortuneCreateStatus()
    data object Creating : FortuneCreateStatus()
    data class Success(val fortuneId: Long) : FortuneCreateStatus()
    data object Failure : FortuneCreateStatus()
}
