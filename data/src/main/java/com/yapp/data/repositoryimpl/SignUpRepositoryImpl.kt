package com.yapp.data.repositoryimpl

import android.util.Log
import com.yapp.data.remote.datasource.SignUpDataSource
import com.yapp.data.remote.dto.request.SignUpRequest
import com.yapp.domain.repository.SignUpRepository
import javax.inject.Inject

class SignUpRepositoryImpl @Inject constructor(
    private val signUpDataSource: SignUpDataSource,
) : SignUpRepository {

    override suspend fun postSignUp(
        name: String,
        calendarType: String,
        birthDate: String,
        birthTime: String,
        gender: String,
    ): Result<Long> {
        val request = SignUpRequest.fromState(name, calendarType, birthDate, birthTime, gender)

        return signUpDataSource.postSignUp(request).map { userId ->
            Log.d("SignUpRepository", "회원가입 성공! userId=$userId")
            userId
        }
    }
}
