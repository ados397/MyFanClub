package com.ados.myfanclub.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class UserDTO(
    var uid: String? = null,
    var userId: String? = null,
    var loginType: LoginType? = LoginType.EMAIL,
    var nickname: String? = null,
    val level: Int? = 0,
    val exp: Double? = 0.0,
    var imageUrl: String? = null,
    var fanClubId: String? = null,
    var mainTitle: String? = null,
    var aboutMe: String? = null,
    var createTime: Date? = null
) : Parcelable {
    enum class LoginType {
        EMAIL, GOOGLE, FACEBOOK
    }
}

data class DisplayBoardDTO(
    var displayText: String? = null,
    var userUid: String? = null,
    var userNickname: String? = null,
    var color: Int? = 0,
    var order: Long? = 0L,
    var createTime: Date? = null
) { }