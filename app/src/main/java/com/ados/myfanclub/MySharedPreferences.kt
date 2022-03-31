package com.ados.myfanclub

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class MySharedPreferences(context: Context) {
    companion object {
        const val PREF_KEY_AVAILABLE_USER_EXP_GEM = "availableUserExpGem"
        const val PREF_KEY_REWARD_USER_EXP_COUNT = "rewardUserExpCount"
        const val PREF_KEY_REWARD_USER_GEM_COUNT = "rewardUserGemCount"
        const val PREF_KEY_REWARD_FAN_CLUB_EXP_COUNT = "rewardFanClubExpCount"
        const val PREF_KEY_REWARD_FAN_CLUB_GEM_COUNT = "rewardFanClubGemCount"
        const val PREF_KEY_REWARD_GAMBLE_COUNT_COUNT = "rewardGambleCountCount"

        const val PREF_KEY_AVAILABLE_FAN_CLUB_EXP_GEM = "availableFanClubExpGem"
        const val PREF_KEY_REWARD_USER_EXP_TIME = "rewardUserExpTime"
        const val PREF_KEY_REWARD_USER_GEM_TIME = "rewardUserGemTime"
        const val PREF_KEY_REWARD_FAN_CLUB_EXP_TIME = "rewardFanClubExpTime"
        const val PREF_KEY_REWARD_FAN_CLUB_GEM_TIME = "rewardFanClubGemTime"
        const val PREF_KEY_REWARD_GAMBLE_COUNT_TIME = "rewardGambleCountTime"

        const val PREF_KEY_FAN_CLUB_CHAT_SEND_TIME = "fanClubChatSendTime"

        const val PREF_KEY_LAST_FAN_CLUB_LEVEL = "lastFanClubLevel"
        const val PREF_KEY_LAST_MEMBER_POSITION = "lastMemberPosition"
    }

    private var pref: SharedPreferences = context.getSharedPreferences("storage", Context.MODE_PRIVATE)

    // 광고 카운트는 key에 날짜를 추가하여 매일 날짜별로 데이터 관리
    fun putAdCount(key: String?, value: Int) {
        val keyString = "${key}${SimpleDateFormat("yyyyMMdd").format(Date())}"
        putInt(keyString, value)
    }

    fun putInt(key: String?, value: Int) {
        val editor = pref.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun putLong(key: String?, value: Long) {
        val editor = pref.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun putString(key: String?, value: String) {
        val editor = pref.edit()
        editor.putString(key, value)
        editor.apply()
    }

    // 광고 카운트는 key에 날짜를 추가하여 매일 날짜별로 데이터 관리
    fun getAdCount(key: String?, default: Int): Int {
        val keyString = "${key}${SimpleDateFormat("yyyyMMdd").format(Date())}"
        return getInt(keyString, default)
    }

    fun getInt(key: String?, default: Int): Int {
        return pref.getInt(key, default)
    }

    fun getLong(key: String?, default: Long): Long {
        return pref.getLong(key, default)
    }

    fun getString(key: String?, default: String): String? {
        return pref.getString(key, default)
    }
}