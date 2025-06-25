package com.yapp.data

import com.yapp.data.remote.dto.response.FortuneDetail
import com.yapp.data.remote.dto.response.FortuneResponse
import com.yapp.data.remote.dto.response.toDomain
import org.junit.Assert.assertEquals
import org.junit.Test

class FortuneMapperTest {

    @Test
    fun `FortuneResponse를 도메인 모델로 매핑하면 올바르게 변환된다`() {
        val response = dummyFortuneResponse()
        val domain = response.toDomain()

        assertEquals(response.id, domain.id)
        assertEquals(response.dailyFortune, domain.dailyFortuneTitle)
        assertEquals(response.dailyFortuneDescription, domain.dailyFortuneDescription)
        assertEquals(response.avgFortuneScore, domain.avgFortuneScore)
        assertEquals(response.studyCareerFortune.toDomain(), domain.studyCareerFortune)
        assertEquals(response.luckyFood, domain.luckyFood)
    }

    @Test
    fun `FortuneDetail을 도메인 모델로 매핑하면 올바르게 변환된다`() {
        val detail = FortuneDetail(score = 85, title = "Success", description = "Great things happen")
        val domain = detail.toDomain()

        assertEquals(85, domain.score)
        assertEquals("Success", domain.title)
        assertEquals("Great things happen", domain.description)
    }

    private fun dummyFortuneResponse() = FortuneResponse(
        id = 123,
        dailyFortune = "Today is your lucky day",
        dailyFortuneDescription = "You'll find success in your endeavors.",
        avgFortuneScore = 88,
        studyCareerFortune = dummyDetail(),
        wealthFortune = dummyDetail(),
        healthFortune = dummyDetail(),
        loveFortune = dummyDetail(),
        luckyOutfitTop = "T-shirt",
        luckyOutfitBottom = "Shorts",
        luckyOutfitShoes = "Sneakers",
        luckyOutfitAccessory = "Bracelet",
        unluckyColor = "Gray",
        luckyColor = "Yellow",
        luckyFood = "Sushi"
    )

    private fun dummyDetail() = FortuneDetail(
        score = 90,
        title = "High Energy",
        description = "You will feel energetic all day."
    )
}
