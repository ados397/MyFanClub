package com.ados.myfanclub.model

import android.net.Uri
import android.os.Parcelable
import com.ados.myfanclub.R
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class FanClubDTO(
    var isSelected: Boolean = false,
    val docName: String? = null,
    var name: String? = null,
    var nameChangeDate: Date? = null,
    var description: String? = null,
    var notice: String? = null,
    var imgSymbol: String? = null,
    var imgSymbolCustom: String? = null,
    var imgSymbolUpdateTime: Date? = null,
    var level: Int? = 0,
    var exp: Long? = 0L,
    var expTotal: Long? = 0L,
    var masterUid: String? = null,
    var masterNickname: String? = null,
    var memberCount: Int? = 0,
    var subMasterCount: Int? = 0,
    val createTime: Date? = null
) : Parcelable {

    fun getSymbolCustomImageName() : String {
        return "fan_club_symbol_${docName}.jpg"
    }

    /* 팬클럽 레벨 별 경험치 정의
    * 필요 경험치 = Lv.2 - 50,000, Lv.3 - 100,000, Lv.4 - 150,000 ... Lv.100 - 4,950,000 ...
    * 레벨이 증가할 때마다 요구 경험치 50000씩 증가
    * 경험치 공식 = ((레벨 - 1) * 50,000) + 50,000
    */
    fun getNextLevelExp() : Long {
        return level?.minus(1)?.times(50000)?.plus(50000)?.toLong()!!
    }

    /* 팬클럽 누적 경험치 획득
    */
    fun getTotalExp() : Long {
        var totalExp = 0L
        for (i in 1..(level?.minus(1)!!)) {
            totalExp += i.minus(1).times(50000).plus(50000).toLong()
        }
        totalExp += exp!!
        return totalExp
    }

    /* 팬클럽 레벨 별 가입 가능 멤버 수
    * Lv.1 - 30명, Lv.2 - 45명 ...  Lv.9 - 150명 ... Lv.20 - 315명 ... Lv.100 - 1,515명 ...
    * 30명에서 시작해서 1레벨 마다 15명씩 증가
    * 공식 = ((레벨 - 1) x 15) + 30 (소수점 이하 버림)
    */
    fun getMaxMemberCount() : Int {
        return level?.minus(1)?.times(15)?.plus(30)!!
    }

    /* 팬클럽 레벨 별 부클럽장 수
    * Lv.1 - 3명, Lv.2 - 5명 ...  Lv.9 - 15명 ... Lv.20 - 32명 ... Lv.100 - 152명 ...
    * 공식 = 총 멤버수 / 10
    */
    fun getMaxSubMasterCount() : Int {
        return getMaxMemberCount().div(10)
    }

    /* 팬클럽 레벨 별 스케줄 수
    * Lv.2 - 5개 ...  Lv.10 - 6개 ... Lv.20 - 7개 ... Lv.100 - 15개 ...
    * 10 레벨마다 1개씩 증가
    * 공식 = (레벨 / 10) + 5 (소수점 이하 버림)
    */
    fun getScheduleCount() : Int {
        return level?.div(10)?.plus(5)!!
    }

    /* 팬클럽 출석체크로 획득 가능한 보상 수
    * Lv.1 - 1개, Lv.2 - 1개 ...  Lv.9 - 2개 ... Lv.19 - 3개 ... Lv.100 - 11개 ...
    * 출석체크 최초 30명, 이후 150명 마다 다이아 보상 획득 가능
    * 참고 - 600명 단위 출석체크는 다이아 보상 2개 (600명, 1200명, 1800명, 2400명... 15000명)
    * 공식 = (멤버수 / 150) + 1 (소수점 이하 버림)
    */
    fun getCheckoutRewardCount() : Int {
        return getMaxMemberCount().div(150).plus(1)
    }

    /* 팬클럽 출석체크로 획득 가능한 총 다이아 수
    * 출석체크로 획득 가능한 보상 수 + 600명 마다 다이아 1개씩 추가
    * 600명 마다 출석체크 보상이 다이아 2개이기 때문 (600명, 1200명, 1800명, 2400명... 15000명)
    */
    fun getCheckoutGemCount() : Int {
        return getCheckoutRewardCount().plus(getMaxMemberCount().div(600))
    }

    fun getRewardMemberCount() : Int {
        return getMaxMemberCount().div(150).plus(1)
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
        expTotal = expTotal?.plus(expCount) // 누적 경험치
        return level!!
    }

}

data class FanClubExDTO(
    var fanClubDTO: FanClubDTO? = null,
    var imgSymbolCustomUri: Uri? = null
)

@Parcelize
data class MemberDTO(
    var isSelected: Boolean = false,
    val userUid: String? = null,
    var userNickname: String? = null,
    var userLevel: Int? = 0,
    var userAboutMe: String? = null,
    var contribution: Long? = 0L,
    var position: Position? = Position.MEMBER,
    var requestTime: Date? = null,
    var responseTime: Date? = null,
    var checkoutTime: Date? = null,
    var token: String? = null
) : Parcelable {
    enum class Position {
        MASTER, SUB_MASTER, MEMBER, GUEST
    }

    fun getPositionString(): String {
        var positionString = when (position) {
            Position.MASTER -> "클럽장"
            Position.SUB_MASTER -> "부클럽장"
            Position.MEMBER -> "클럽원"
            else -> "확인불가"
        }
        return positionString
    }

    fun getPositionImage(): Int {
        var positionImage = when (position) {
            Position.MASTER -> R.drawable.medal_icon_09
            Position.SUB_MASTER -> R.drawable.medal_icon_48
            Position.MEMBER -> R.drawable.medal_icon_39
            else -> R.drawable.medal_icon_39
        }
        return positionImage
    }

    fun getCheckoutImage(): Int {
        return if (isCheckout())
            R.drawable.checked
        else
            R.drawable.cancel
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

    fun isMaster() : Boolean {
        return position == Position.MASTER
    }

    fun isAdministrator() : Boolean {
        return position == Position.MASTER || position == Position.SUB_MASTER
    }
}

data class FanClubRewardDTO(
    val docName: String? = null,
    val checkoutCount: Int? = 0,
    var gemCount: Int? = 0,
    var rewardGemGetTime: Date? = null
) {
    fun isRewardGemGet() : Boolean {
        var isGemGet = false
        if (rewardGemGetTime != null) {
            var lastGemGetTime = SimpleDateFormat("yyyy.MM.dd").format(rewardGemGetTime!!)
            var currentTime = SimpleDateFormat("yyyy.MM.dd").format(Date())

            if (lastGemGetTime == currentTime) {
                isGemGet = true
            }
        }
        return isGemGet
    }
}