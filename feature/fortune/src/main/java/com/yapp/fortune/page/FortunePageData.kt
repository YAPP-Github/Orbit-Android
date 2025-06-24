package com.yapp.fortune.page

import com.yapp.domain.model.Fortune

data class FortunePageData(
    val title: String,
    val description: String,
    val backgroundResId: Int,
    val details: List<Contents> = emptyList(),
    val isHoroscopePage: Boolean = false,
    val isCodyPage: Boolean = false,
    val isLuckyColorPage: Boolean = false,
    val luckyOutfitTop: String = "",
    val luckyOutfitBottom: String = "",
    val luckyOutfitShoes: String = "",
    val luckyOutfitAccessory: String = "",
    val luckyColor: String = "",
    val unluckyColor: String = "",
    val luckyFood: String = "",
)

data class Contents(
    val contentScore: String,
    val contentTitle: String,
    val contentDescription: String,
)

fun Fortune.toFortunePages(): List<FortunePageData> {
    val parts = dailyFortuneTitle.split(",", limit = 2)
    val nickName = parts.getOrNull(0)?.trim() ?: ""
    return listOf(
        FortunePageData(
            title = "오늘의 운세",
            description = "오늘 ${nickName}의 하루는\n행운이 가득해!",
            backgroundResId = core.designsystem.R.drawable.ic_letter_horoscope,
            details = listOf(
                Contents(
                    contentScore = "학업/직장운 ${studyCareerFortune.score}점",
                    contentTitle = studyCareerFortune.title,
                    contentDescription = studyCareerFortune.description,
                ),
                Contents(
                    contentScore = "애정운 ${loveFortune.score}점",
                    contentTitle = loveFortune.title,
                    contentDescription = loveFortune.description,
                ),
            ),
            isHoroscopePage = true,
        ),
        FortunePageData(
            title = "오늘의 운세",
            description = "오늘 ${nickName}의 하루는\n행운이 가득해!",
            backgroundResId = core.designsystem.R.drawable.ic_letter_horoscope,
            details = listOf(
                Contents(
                    contentScore = "건강운 ${healthFortune.score}점",
                    contentTitle = healthFortune.title,
                    contentDescription = healthFortune.description,
                ),
                Contents(
                    contentScore = "재물운 ${wealthFortune.score}점",
                    contentTitle = wealthFortune.title,
                    contentDescription = wealthFortune.description,
                ),
            ),
            isHoroscopePage = true,
        ),
        FortunePageData(
            title = "오늘의 코디",
            description = "오늘은 이렇게 입는 거 어때?\n코디에 참고해봐!",
            backgroundResId = core.designsystem.R.drawable.ic_letter_horoscope,
            isCodyPage = true,
            luckyOutfitTop = luckyOutfitTop,
            luckyOutfitBottom = luckyOutfitBottom,
            luckyOutfitShoes = luckyOutfitShoes,
            luckyOutfitAccessory = luckyOutfitAccessory,
        ),
        FortunePageData(
            title = "오늘 참고해",
            description = "기억해놓고\n" + "일상생활에 반영해 봐!",
            backgroundResId = core.designsystem.R.drawable.ic_letter_horoscope,
            isLuckyColorPage = true,
            luckyColor = luckyColor,
            unluckyColor = unluckyColor,
            luckyFood = luckyFood,
        ),
    )
}
