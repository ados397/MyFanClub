package com.ados.myfanclub.model

import com.ados.myfanclub.SuccessCalendarWeek
import java.text.SimpleDateFormat
import java.util.*

data class DashboardMissionDTO(
    var type: Type = Type.PERSONAL,
    var scheduleDTO: ScheduleDTO? = null,
    var scheduleProgressDTO: ScheduleProgressDTO? = null
) {
    enum class Type {
        PERSONAL, FAN_CLUB
    }
}

data class ScheduleDTO(
    var isSelected: Boolean = false,
    var docName: String? = null,
    var order: Long? = 0L,
    var title: String? = null,
    var startDate: Date? = null,
    var endDate: Date? = null,
    var purpose: String? = null,
    var action: Action? = null,
    var appDTO: AppDTO? = null,
    var url: String? = null,
    var cycle: Cycle? = Cycle.DAY,
    var count: Long? = 0L,
    var isPhoto: Boolean = false,
    var isAlarm: Boolean? = false,
    var alarmDTO: AlarmDTO = AlarmDTO()
) {
    enum class Action {
        APP, URL, ETC
    }

    enum class Cycle {
        DAY, WEEK, MONTH, PERIOD
    }

    fun getProgressDocName() : String {
        var docName = ""
        when (cycle) {
            Cycle.DAY -> docName = SimpleDateFormat("yyyyMMdd").format(Date())
            Cycle.WEEK -> {
                var successCalendarWeek = SuccessCalendarWeek(Date())
                successCalendarWeek.initBaseCalendar()
                var week = successCalendarWeek.getCurrentWeek()
                if (week != null) {
                    docName = "${SimpleDateFormat("yyyyMMdd").format(week.startDate!!)}${SimpleDateFormat("yyyyMMdd").format(week.endDate!!)}"
                }
            }
            Cycle.MONTH -> docName = SimpleDateFormat("yyyyMM").format(Date())
            Cycle.PERIOD -> docName = "${SimpleDateFormat("yyMMdd").format(startDate!!)}${SimpleDateFormat("yyMMdd").format(endDate!!)}"
            else -> docName = SimpleDateFormat("yyyyMMdd").format(Date())
        }
        return docName
    }

    fun isScheduleVisible(selectedCycle: Cycle) : Boolean {
        if (selectedCycle != cycle) {
            return false
        }

        val calStart = Calendar.getInstance()
        calStart.time = startDate!!
        calStart.set(Calendar.HOUR, 0)
        calStart.set(Calendar.MINUTE, 0)
        calStart.set(Calendar.SECOND, 0)

        val calEnd = Calendar.getInstance()
        calEnd.time = endDate!!
        calEnd.set(Calendar.HOUR_OF_DAY, 23)
        calEnd.set(Calendar.MINUTE, 59)
        calEnd.set(Calendar.SECOND, 59)

        var date = Date()

        return date >= calStart.time && date <= calEnd.time
    }

    fun isExpired() : Boolean {
        var endTime = SimpleDateFormat("yyyyMMdd").format(endDate!!).toInt()
        var currentTime = SimpleDateFormat("yyyyMMdd").format(Date()).toInt()
        return endTime < currentTime
    }
}

data class AppDTO(
    var isSelected: Boolean = false,
    val packageName: String? = null,
    val appName: String? = null,
    val iconImage: String? = null,
    val order: Int? = 0
) {}

data class AlarmDTO(
    var alarmDate: Date? = null,
    var alarmHour: Int? = 0,
    var alarmMinute: Int? = 0,
    var dayOfWeek: MutableMap<String,Boolean> = mutableMapOf()
) {
    init {
        clearDayOfWeek()
    }

    fun clearDayOfWeek() {
        (1..7).forEach { dayOfWeek[it.toString()] = false }
    }

    fun everyDayOfWeek() {
        (1..7).forEach { dayOfWeek[it.toString()] = true }
    }
}

data class ScheduleProgressDTO(
    var docName: String? = null, // 날짜형식 (일: "20210907", 주:"2021090920210912" , 월: "202109", 기간: "210909210930"")
    var count: Long? = 0L,
    var countMax: Long? = 0L
) {}