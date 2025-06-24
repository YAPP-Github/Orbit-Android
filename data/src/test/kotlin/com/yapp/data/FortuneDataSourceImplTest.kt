package com.yapp.data

import com.yapp.data.remote.datasource.FortuneDataSourceImpl
import com.yapp.data.remote.dto.response.FortuneResponse
import com.yapp.data.remote.service.ApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FortuneDataSourceImplTest {

    private lateinit var dataSource: FortuneDataSourceImpl
    private val apiService: ApiService = mockk()

    @Before
    fun setup() {
        dataSource = FortuneDataSourceImpl(apiService)
    }

    @Test
    fun `운세 등록에 성공하면 성공 Result를 반환한다`() = runTest {
        // Given
        val userId = 1L
        val mockResponse = mockk<FortuneResponse>()
        coEvery { apiService.postFortune(userId) } returns mockResponse

        // When
        val result = dataSource.postFortune(userId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
        coVerify { apiService.postFortune(userId) }
    }

    @Test
    fun `운세 등록 중 예외가 발생하면 실패 Result를 반환한다`() = runTest {
        // Given
        val userId = 1L
        coEvery { apiService.postFortune(userId) } throws RuntimeException("Network Error")

        // When
        val result = dataSource.postFortune(userId)

        // Then
        assertTrue(result.isFailure)
        coVerify { apiService.postFortune(userId) }
    }

    @Test
    fun `운세 조회에 성공하면 성공 Result를 반환한다`() = runTest {
        // Given
        val fortuneId = 10L
        val mockResponse = mockk<FortuneResponse>()
        coEvery { apiService.getFortune(fortuneId) } returns mockResponse

        // When
        val result = dataSource.getFortune(fortuneId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
        coVerify { apiService.getFortune(fortuneId) }
    }

    @Test
    fun `운세 조회 중 예외가 발생하면 실패 Result를 반환한다`() = runTest {
        // Given
        val fortuneId = 10L
        coEvery { apiService.getFortune(fortuneId) } throws RuntimeException("Network Error")

        // When
        val result = dataSource.getFortune(fortuneId)

        // Then
        assertTrue(result.isFailure)
        coVerify { apiService.getFortune(fortuneId) }
    }
}
