package com.yapp.data

import com.yapp.data.local.datasource.FortuneLocalDataSource
import com.yapp.data.remote.datasource.FortuneDataSource
import com.yapp.data.remote.dto.response.FortuneDetail
import com.yapp.data.remote.dto.response.FortuneResponse
import com.yapp.data.remote.dto.response.toDomain
import com.yapp.data.repositoryimpl.FortuneRepositoryImpl
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FortuneRepositoryImplTest {

    private val remoteDataSource = mockk<FortuneDataSource>()
    private val localDataSource = mockk<FortuneLocalDataSource>(relaxed = true)

    private val repository = FortuneRepositoryImpl(
        fortuneRemoteDataSource = remoteDataSource,
        fortuneLocalDataSource = localDataSource,
    )

    @Test
    fun `운세 요청에 성공하면 도메인 모델로 반환된다`() = runTest {
        val response = dummyFortuneResponse()
        coEvery { remoteDataSource.postFortune(1L) } returns Result.success(response)

        val result = repository.postFortune(1L)

        assert(result.isSuccess)
        assertEquals(response.toDomain(), result.getOrNull())
    }

    @Test
    fun `운세 상세 조회에 실패하면 실패 결과를 반환한다`() = runTest {
        val exception = RuntimeException("Not found")
        coEvery { remoteDataSource.getFortune(2L) } returns Result.failure(exception)

        val result = repository.getFortune(2L)

        assert(result.isFailure)
    }

    private fun dummyFortuneResponse() = FortuneResponse(
        id = 1L,
        dailyFortune = "Good luck",
        dailyFortuneDescription = "You will be lucky today",
        avgFortuneScore = 90,
        studyCareerFortune = dummyDetail(),
        wealthFortune = dummyDetail(),
        healthFortune = dummyDetail(),
        loveFortune = dummyDetail(),
        luckyOutfitTop = "Hoodie",
        luckyOutfitBottom = "Jeans",
        luckyOutfitShoes = "Sneakers",
        luckyOutfitAccessory = "Watch",
        unluckyColor = "Black",
        luckyColor = "White",
        luckyFood = "Pizza",
    )

    private fun dummyDetail() = FortuneDetail(
        score = 100,
        title = "Title",
        description = "Description"
    )
}
