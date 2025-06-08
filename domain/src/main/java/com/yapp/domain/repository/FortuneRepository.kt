package com.yapp.domain.repository

import com.yapp.domain.model.Fortune

interface FortuneRepository {
    suspend fun postFortune(userId: Long): Result<Fortune>
    suspend fun getFortune(fortuneId: Long): Result<Fortune>
}
