package com.ados.myfanclub.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

data class QuestionDTO(var stat: Stat? = Stat.INFO,
                       var title: String? = null,
                       var content: String? = null,
                       var image: String? = null,
) {
    enum class Stat {
        INFO, WARNING, ERROR
    }
}

data class GemQuestionDTO(val content: String? = null,
                          val gemCount: Int? = 0
) {
    enum class Stat {
        INFO, WARNING, ERROR
    }
}

data class EditTextDTO(val title: String? = null,
                       val content: String? = null,
                       val length: Int? = 0,
                       val regex: String? = null,
                       val regexErrorMsg: String? = null
) { }

data class CalendarDTO(
    var StartDate: Boolean = false,
    var EndDate: String? = null
) {}

data class LogDTO(
    var log: String? = null,
    var insertTime: Date? = null
) {}



data class SignUpDTO(
    var isSelected: Boolean = false,
    var isChecked: Boolean = false,
    val name: String? = null
) {}

data class WeekDTO(
    var week: Int? = 0,
    var year: Int? = 0,
    var month: Int? = 0,
    var startDate: Date? = null,
    val endDate: Date? = null
) {}

@Parcelize
data class PreferencesDTO (
    val availableUserExpGem: Int? = 0, // 개인 경험치 레벨업에 하루 사용 가능한 다이아 수
    val availableFanClubExpGem: Int? = 0, // 팬클럽 경험치 레벨업에 하루 사용 가능한 다이아 수
    val rewardUserExp: Long? = 0L, // 개인 무료 경험치 광고 보상
    val rewardUserExpCount: Int? = 0, // 개인 무료 경험치 광고 하루 시청 가능 수
    val rewardUserExpTime: Int? = 0, // 개인 무료 경험치 광고 충전 시간
    val rewardUserGem: Int? = 0, // 개인 무료 다이아 광고 보상
    val rewardUserGemCount: Int? = 0, // 개인 무료 다이아 광고 하루 시청 가능 수
    val rewardUserGemTime: Int? = 0, // 개인 무료 다이아 광고 충전 시간
    val rewardFanClubExp: Long? = 0L, // 팬클럽 무료 경험치 광고 보상
    val rewardFanClubExpCount: Int? = 0, // 팬클럽 무료 경험치 광고 하루 시청 가능 수
    val rewardFanClubExpTime: Int? = 0, // 팬클럽 무료 경험치 광고 충전 시간
    val rewardFanClubGem: Int? = 0, // 팬클럽 무료 다이아 광고 보상
    val rewardFanClubGemCount: Int? = 0, // 팬클럽 무료 다이아 광고 하루 시청 가능 수
    val rewardFanClubGemTime: Int? = 0, // 팬클럽 무료 다이아 광고 충전 시간
    val rewardPremiumPackBuyGem: Int? = 0, // 프리미엄 패키지 구매 다이아 보상
    val rewardPremiumPackCheckoutGem: Int? = 0, // 프리미엄 패키지 매일 다이아 보상
    val priceDisplayBoard: Int? = 0,
    val priceNickname: Int? = 0,
    val priceFanClubCreate: Int? = 0,
    val priceFanClubSymbol: Int? = 0,
    val priceFanClubName: Int? = 0,
    val priceFanClubNotice: Int? = 0,
    val displayBoardPeriod: Int? = 0
) : Parcelable {
}

data class AdPolicyDTO(
    var ad_banner: String? = null,
    var ad_interstitial: String? = null,
    var ad_reward1: String? = null,
    var ad_reward2: String? = null,
    var ad_reward3: String? = null
) {}

data class NotificationBody(
    val to: String,
    val data: NotificationData
) {
    data class NotificationData(
        val title: String,
        val userId : String,
        val message: String
    )
}