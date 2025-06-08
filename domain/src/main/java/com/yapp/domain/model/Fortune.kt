package com.yapp.domain.model

data class Fortune(
    val id: Long,
    val dailyFortuneTitle: String,
    val dailyFortuneDescription: String,
    val avgFortuneScore: Int,
    val studyCareerFortune: FortuneDetailModel,
    val wealthFortune: FortuneDetailModel,
    val healthFortune: FortuneDetailModel,
    val loveFortune: FortuneDetailModel,
    val luckyOutfitTop: String,
    val luckyOutfitBottom: String,
    val luckyOutfitShoes: String,
    val luckyOutfitAccessory: String,
    val unluckyColor: String,
    val luckyColor: String,
    val luckyFood: String,
)

data class FortuneDetailModel(
    val score: Int,
    val title: String,
    val description: String,
)
