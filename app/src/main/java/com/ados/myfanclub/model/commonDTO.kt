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
    val rewardTutorialGem: Int? = 0, // 튜토리얼 완료 다이아 보상
    val rewardUserCheckoutGem: Int? = 0, // 개인 출석체크 다이아 보상
    val rewardUserExp: Long? = 0L, // 개인 무료 경험치 광고 보상
    val rewardUserExpCount: Int? = 0, // 개인 무료 경험치 광고 하루 시청 가능 수
    val rewardUserExpTime: Int? = 0, // 개인 무료 경험치 광고 충전 시간
    val rewardUserGem: Int? = 0, // 개인 무료 다이아 광고 보상
    val rewardUserGemCount: Int? = 0, // 개인 무료 다이아 광고 하루 시청 가능 수
    val rewardUserGemTime: Int? = 0, // 개인 무료 다이아 광고 충전 시간
    val rewardFanClubCheckoutGem: Int? = 0, // 팬클럽 출석체크 다이아 보상
    val rewardFanClubExp: Long? = 0L, // 팬클럽 무료 경험치 광고 보상
    val rewardFanClubExpCount: Int? = 0, // 팬클럽 무료 경험치 광고 하루 시청 가능 수
    val rewardFanClubExpTime: Int? = 0, // 팬클럽 무료 경험치 광고 충전 시간
    val rewardFanClubGem: Int? = 0, // 팬클럽 무료 다이아 광고 보상
    val rewardFanClubGemCount: Int? = 0, // 팬클럽 무료 다이아 광고 하루 시청 가능 수
    val rewardFanClubGemTime: Int? = 0, // 팬클럽 무료 다이아 광고 충전 시간
    val rewardPremiumPackBuyGem: Int? = 0, // 프리미엄 패키지 구매 다이아 보상
    val rewardPremiumPackCheckoutGem: Int? = 0, // 프리미엄 패키지 매일 다이아 보상
    val priceDisplayBoard: Int? = 0, // 전광판 1회 표시 비용
    val priceNickname: Int? = 0, // 닉네임 변경 비용
    val priceFanClubCreate: Int? = 0, // 팬클럽 창설 비용
    val priceFanClubSymbol: Int? = 0, // 팬클럽 심볼 변경 비용
    val priceFanClubName: Int? = 0, // 팬클럽 이름 변경 비용
    val priceFanClubNotice: Int? = 0, // 팬클럽 전체 공지 발송 비용
    val priceGamble10: Int? = 0, // 10다이아 뽑기 1회 비용
    val priceGamble30: Int? = 0, // 30다이아 뽑기 1회 비용
    val priceGamble100: Int? = 0, // 100다이아 뽑기 1회 비용
    val usedGambleCount: Int? = 0, // 하루에 다이아뽑기 가능한 횟수
    val displayBoardPeriod: Int? = 0, // 메인 전광판 표시 시간 (초)
    val fanClubChatDisplayPeriod: Int? = 0, // 팬클럽 메인 채팅 표시 시간 (초)
    val fanClubChatSendDelay: Int? = 0 // 팬클럽 채팅 전송 간격 (초)
) : Parcelable {
}

data class UpdateDTO (
    var updateUri : String = "https://play.google.com/store/apps/details?id=com.ados.myfanclub", // 업데이트 Uri
    var minVersion : String? = null, // 실행 가능한 최소 버전, 해당 버전 미만은 앱 실행 불가
    var minVersionDisplay : Boolean? = false, // 최소 버전 경고 표시 여부
    var minVersionTitle : String? = null, // 업데이트 경고 제목
    var minVersionDesc : String? = null, // 업데이트 경고 내용
    var updateVersion : String? = null, // 업데이트 필요 버전, 해당 버전 미만은 앱 업데이트 필요
    var updateVersionDisplay : Boolean? = false, // 업데이트 필요 버전 경고 표시 여부
    var updateVersionTitle : String? = null, // 업데이트 경고 제목
    var updateVersionDesc : String? = null, // 업데이트 경고 내용
    var maintenance : Boolean? = false, // 서버 점검 여부
    var maintenanceTitle : String? = null, // 서버 점검 시 표시될 제목
    var maintenanceDesc : String? = null, // 서버 점검 시 표시될 내용
    var maintenanceImgUrl : String? = null // 서버 점검 시 표시할 이미지
) { }

data class AdPolicyDTO(
    var ad_banner: String? = null,
    var ad_interstitial: String? = null,
    var ad_reward1: String? = null,
    var ad_reward2: String? = null,
    var ad_reward3: String? = null
) {}

data class ReportDTO(
    var fromUserUid: String? = null, // 신고자
    var fromUserNickname: String? = null,
    var toUserUid: String? = null, // 신고대상
    var toUserNickname: String? = null,
    var content: String? = null,
    var contentDocName: String? = null,
    var type: Type = Type.DisplayBoard,
    var reason: String? = null,
    var reportTime: Date? = null
) {
    enum class Type {
        DisplayBoard, FanClubChat
    }

    fun getCollectionName() : String {
        return when(type) {
            Type.FanClubChat -> "fanClubChat"
            Type.DisplayBoard -> "displayBoard"
        }
    }
}

data class NoticeDTO(
    var title: String? = null,
    var content: String? = null,
    var imageUrl: String? = null,
    var time: Date? = null,
    var displayMain: Boolean? = null // 메인 공지에 표시 여부
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