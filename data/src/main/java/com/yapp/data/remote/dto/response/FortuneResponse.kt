package com.yapp.data.remote.dto.response

import com.yapp.domain.model.Fortune
import com.yapp.domain.model.FortuneDetailModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FortuneResponse(
    @SerialName("id") val id: Long,
    @SerialName("dailyFortuneTitle") val dailyFortune: String,
    @SerialName("dailyFortuneDescription") val dailyFortuneDescription: String,
    @SerialName("avgFortuneScore") val avgFortuneScore: Int,
    @SerialName("studyCareerFortune") val studyCareerFortune: FortuneDetail,
    @SerialName("wealthFortune") val wealthFortune: FortuneDetail,
    @SerialName("healthFortune") val healthFortune: FortuneDetail,
    @SerialName("loveFortune") val loveFortune: FortuneDetail,
    @SerialName("luckyOutfitTop") val luckyOutfitTop: String,
    @SerialName("luckyOutfitBottom") val luckyOutfitBottom: String,
    @SerialName("luckyOutfitShoes") val luckyOutfitShoes: String,
    @SerialName("luckyOutfitAccessory") val luckyOutfitAccessory: String,
    @SerialName("unluckyColor") val unluckyColor: String,
    @SerialName("luckyColor") val luckyColor: String,
    @SerialName("luckyFood") val luckyFood: String,
)

@Serializable
data class FortuneDetail(
    @SerialName("score") val score: Int,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
)

fun FortuneResponse.toDomain(): Fortune {
    return Fortune(
        id = this.id,
        dailyFortuneTitle = dailyFortune,
        dailyFortuneDescription = dailyFortuneDescription,
        avgFortuneScore = avgFortuneScore,
        studyCareerFortune = studyCareerFortune.toDomain(),
        wealthFortune = wealthFortune.toDomain(),
        healthFortune = healthFortune.toDomain(),
        loveFortune = loveFortune.toDomain(),
        luckyOutfitTop = luckyOutfitTop,
        luckyOutfitBottom = luckyOutfitBottom,
        luckyOutfitShoes = luckyOutfitShoes,
        luckyOutfitAccessory = luckyOutfitAccessory,
        unluckyColor = unluckyColor,
        luckyColor = luckyColor,
        luckyFood = luckyFood,
    )
}

fun FortuneDetail.toDomain(): FortuneDetailModel {
    return FortuneDetailModel(
        score = score,
        title = title,
        description = description,
    )
}
