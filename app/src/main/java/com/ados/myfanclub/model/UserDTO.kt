package com.ados.myfanclub.model

import android.net.Uri
import android.os.Parcelable
import com.ados.myfanclub.R
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@Parcelize
data class UserDTO(
    var uid: String? = null,
    var userId: String? = null,
    var loginType: LoginType? = LoginType.EMAIL,
    var nickname: String? = null,
    var nicknameChangeDate: Date? = null,
    var level: Int? = 0,
    var exp: Long? = 0L,
    var paidGem: Int? = 0,
    var freeGem: Int? = 0,
    var firstGemPackage: MutableMap<String,Boolean> = mutableMapOf("1" to true, "2" to true, "3" to true, "4" to true, "5" to true),
    var eventGemPackage: MutableMap<String,Boolean> = mutableMapOf("1" to true, "2" to true, "3" to true, "4" to true, "5" to true),
    var questSuccessTimes: MutableMap<String,Date?> = mutableMapOf("1" to null, "2" to null, "3" to null, "4" to null, "5" to null, "6" to null, "7" to null, "8" to null),
    var questGemGetTimes: MutableMap<String,Date?> = mutableMapOf("0" to null, "1" to null, "2" to null, "3" to null, "4" to null, "5" to null, "6" to null, "7" to null, "8" to null),
    var premiumExpireTime: Date? = null,
    var premiumGemGetTime: Date? = null,
    var fanClubId: String? = null,
    var fanClubRequestId: ArrayList<String> = arrayListOf(),
    var fanClubQuitDate: Date? = null,
    var fanClubDeportationDate: Date? = null,
    var mainTitle: String? = null,
    var imgProfile: String? = null,
    var imgProfileUpdateTime: Date? = null,
    var aboutMe: String? = null,
    var checkoutTime: Date? = null,
    var createTime: Date? = null,
    var loginTime: Date? = null,
    var blockStartTime: Date? = null,
    var blockEndTime: Date? = null,
    var blockReason: String? = null,
    var deleteTime: Date? = null, // 회원 탈퇴 시간
    var tutorialEndedTime: Date? = null,
    var token: String? = null
) : Parcelable {
    enum class LoginType {
        EMAIL, GOOGLE, FACEBOOK
    }

    fun getTotalGem() : Int {
        return paidGem?.plus(freeGem!!)!!
    }

    // 다이아 사용 (무료 다이아 먼저 사용)
    fun useGem(gemCount: Int) {
        if (freeGem!! >= gemCount) {
            freeGem = freeGem?.minus(gemCount)
        } else {
            val remainder = gemCount - freeGem!!
            paidGem = paidGem?.minus(remainder)
            freeGem = 0
        }
    }

    /* 사용자 레벨 별 경험치 정의
    * 필요 경험치 = Lv.2 - 200, Lv.3 - 230, Lv.4 - 260 ... Lv.100 - 3140 ...
    * 레벨이 증가할 때마다 요구 경험치 30씩 증가
    * 공식 = ((레벨 - 1) * 30) + 200
    */
    fun getNextLevelExp() : Long {
        return level?.minus(1)?.times(30)?.plus(200)?.toLong()!!
    }

    /* 사용자 레벨 별 스케줄 수
    * Lv.2 - 5개 ...  Lv.10 - 6개 ... Lv.20 - 7개 ... Lv.100 - 15개 ...
    * 10 레벨마다 1개씩 증가
    * 공식 = (레벨 / 10) + 5 (소수점 이하 버림)
    */
    fun getScheduleCount() : Int {
        return level?.div(10)?.plus(5)!!
    }

    /* 레벨업 보상 다이아
    * Lv.2 - 3개 ...  Lv.10 - 4개 ... Lv.20 - 5개 ... Lv.100 - 13개 ...
    * 10 레벨마다 1개씩 증가
    * 공식 = (레벨 / 10) + 3 (소수점 이하 버림)
    */
    fun getLevelUpGemCount() : Int {
        return level?.div(10)?.plus(3)!!
    }

    fun getCheckoutImage(): Int {
        return if (isCheckout())
            R.drawable.checked
        else
            R.drawable.cancel
    }

    fun getProfileImageName() : String {
        return "user_profile_${uid}.jpg"
    }

    fun addExp(expCount: Long) : Int {
        val getNextLevelExp = getNextLevelExp()
        val plusExp = exp?.plus(expCount)!!
        if (plusExp >= getNextLevelExp) { // 레벨업
            level = level?.plus(1)
            exp = plusExp - getNextLevelExp
        } else { // 경험치만 추가
            exp = plusExp
        }
        return level!!
    }

    fun isCheckout() : Boolean {
        var isCheckout = false
        if (checkoutTime != null) {
            var lastCheckoutTime = SimpleDateFormat("yyyy.MM.dd").format(checkoutTime!!)
            var currentTime = SimpleDateFormat("yyyy.MM.dd").format(Date())

            if (lastCheckoutTime == currentTime) {
                isCheckout = true
            }
        }
        return isCheckout
    }

    // 프리미엄 패키지 만료시간이 남았을때만 유효
    fun isPremium() : Boolean {
        return Date() <= premiumExpireTime
    }

    // 프리미엄 패키지 남은 일 수 획득
    fun getPremiumDay() : Int {
        var day = -1
        if (isPremium()) {
            val interval = ((premiumExpireTime?.time!!.toLong()) - System.currentTimeMillis()) / 1000
            day = (interval / 86400).toInt()
        }
        return day
    }

    // 프리미엄 패키지 갱신 기간 여부 (7일 이하로 남았을 때 갱신 가능)
    fun isPremiumRenew() : Boolean {
        return getPremiumDay() in 0..7
    }

    // 오늘 프리미엄 패키지 다이아 수령 여부
    fun isPremiumGemGet() : Boolean {
        var isGet = false
        if (premiumGemGetTime != null) {
            var lastGetTime = SimpleDateFormat("yyyy.MM.dd").format(premiumGemGetTime!!)
            var currentTime = SimpleDateFormat("yyyy.MM.dd").format(Date())

            if (lastGetTime == currentTime) {
                isGet = true
            }
        }
        return isGet
    }

    // 사용자 차단 여부
    fun isBlock() : Boolean {
        return if (blockEndTime == null)
            false
        else
            Date() < blockEndTime
    }
}

data class UserExDTO(
    var userDTO: UserDTO? = null,
    var isBlocked: Boolean = false,
    var isSelected: Boolean = false,
    var imgProfileUri: Uri? = null
)

data class DisplayBoardDTO(
    var docName: String? = null,
    var displayText: String? = null,
    var userUid: String? = null,
    var userNickname: String? = null,
    var color: Int? = 0,
    var order: Long? = 0L,
    var createTime: Date? = null
) { }

data class DisplayBoardExDTO(
    val displayBoardDTO: DisplayBoardDTO? = null,
    var isBlocked: Boolean = false,
    var isSelected: Boolean = false,
    var imgProfileUri: Uri? = null
) { }

@Parcelize
data class MailDTO(
    val docName: String? = null,
    var title: String? = null,
    var content: String? = null,
    var from: String? = null,
    var item: Item? = Item.NONE,
    var itemCount: Int? = 0,
    var sendTime: Date? = null,
    var expireTime: Date? = null,
    var read: Boolean? = false,
    var deleted: Boolean = false
) : Parcelable {
    enum class Item {
        NONE, PAID_GEM, FREE_GEM
    }
}

data class QuestDTO(
    var title: String? = null,
    var content: String? = null,
    var gemCount: Int? = 0,
    var questSuccessTime: Date? = null,
    var questGemGetTime: Date? = null
) {
    fun isQuestSuccess() : Boolean {
        var isSuccess = false
        if (questSuccessTime != null) {
            var lastQuestTime = SimpleDateFormat("yyyy.MM.dd").format(questSuccessTime!!)
            var currentTime = SimpleDateFormat("yyyy.MM.dd").format(Date())

            if (lastQuestTime == currentTime) {
                isSuccess = true
            }
        }
        return isSuccess
    }

    fun isQuestGemGet() : Boolean {
        var isGemGet = false
        if (questGemGetTime != null) {
            var lastGemGetTime = SimpleDateFormat("yyyy.MM.dd").format(questGemGetTime!!)
            var currentTime = SimpleDateFormat("yyyy.MM.dd").format(Date())

            if (lastGemGetTime == currentTime) {
                isGemGet = true
            }
        }
        return isGemGet
    }
}