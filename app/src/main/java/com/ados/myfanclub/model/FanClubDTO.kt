package com.ados.myfanclub.model

import android.os.Parcelable
import com.ados.myfanclub.R
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class FanClubDTO(
    var isSelected: Boolean = false,
    val docName: String? = null,
    val name: String? = null,
    var description: String? = null,
    var notice: String? = null,
    var imgSymbol: String? = null,
    val level: Int? = 0,
    val exp: Double? = 0.0,
    val masterUid: String? = null,
    var masterNickname: String? = null,
    var count: Int? = 0,
    val countMax: Int? = 0,
    val createTime: Date? = null
) : Parcelable {}

@Parcelize
data class MemberDTO(
    var isSelected: Boolean = false,
    val userUid: String? = null,
    var userNickname: String? = null,
    val userLevel: Int? = 0,
    var userAboutMe: String? = null,
    val contribution: Int? = 0,
    var position: POSITION? = POSITION.MEMBER,
    var requestTime: Date? = null,
    var responseTime: Date? = null,
    val isCheckout: Boolean? = false
) : Parcelable {
    enum class POSITION {
        MASTER, SUB_MASTER, MEMBER, GUEST
    }

    fun getPositionString(): String {
        var positionString = ""
        when (position) {
            POSITION.MASTER -> positionString = "클럽장"
            POSITION.SUB_MASTER -> positionString = "부클럽장"
            POSITION.MEMBER -> positionString = "클럽원"
        }
        return positionString
    }

    fun getPositionImage(): Int {
        var positionImage = 0
        when (position) {
            POSITION.MASTER -> positionImage = R.drawable.medal_icon_09
            POSITION.SUB_MASTER -> positionImage =  R.drawable.medal_icon_48
            POSITION.MEMBER -> positionImage =  R.drawable.medal_icon_39
        }
        return positionImage
    }

    fun getCheckoutImage(): Int {
        return if (isCheckout!!)
            R.drawable.checked
        else
            R.drawable.cancel
    }
}