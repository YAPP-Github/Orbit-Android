package com.yapp.data.remote.datasource

import android.util.Log
import com.yapp.data.remote.dto.request.SignUpRequest
import com.yapp.data.remote.service.ApiService
import com.yapp.network.model.ApiError
import com.yapp.network.utils.safeApiCall
import javax.inject.Inject

class SignUpDataSourceImpl @Inject constructor(
    private val apiService: ApiService,
) : SignUpDataSource {

    override suspend fun postSignUp(request: SignUpRequest): Result<Long> {
        return safeApiCall {
            val response = apiService.postSignUp(request)

            if (response.isSuccessful) {
                val rawResponse = response.body()?.string()?.trim() ?: ""
                Log.d("SignUpDataSource", "서버 응답: $rawResponse")

                if (rawResponse.isNotEmpty() && rawResponse.all { it.isDigit() }) {
                    rawResponse.toLong()
                } else {
                    throw ApiError("예상치 못한 서버 응답 형식: $rawResponse")
                }
            } else {
                throw ApiError("서버 오류: ${response.code()} - ${response.errorBody()?.string()}")
            }
        }
    }
}
