package com.ados.myfanclub

import com.ados.myfanclub.model.WeekDTO
import java.util.*

class SuccessCalendar(date: Date) {

    companion object {
        const val DAYS_OF_WEEK = 7
        const val LOW_OF_CALENDAR = 6
    }

    private val calendar = Calendar.getInstance()

    var prevTail = 0
    var nextHead = 0
    var currentMaxDate = 0

    var dateList = arrayListOf<Int>()

    init {
        calendar.time = date
    }

    fun initBaseCalendar() {
        makeMonthDate()
    }

    private fun makeMonthDate() {

        dateList.clear()

        calendar.set(Calendar.DATE, 1)

        currentMaxDate = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        prevTail = calendar.get(Calendar.DAY_OF_WEEK) - 1

        makePrevTail(calendar.clone() as Calendar)
        makeCurrentMonth(calendar)

        nextHead = LOW_OF_CALENDAR * DAYS_OF_WEEK - (prevTail + currentMaxDate)
        makeNextHead()
    }

    private fun makePrevTail(calendar: Calendar) {
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1)
        val maxDate = calendar.getActualMaximum(Calendar.DATE)
        var maxOffsetDate = maxDate - prevTail

        for (i in 1..prevTail) dateList.add(++maxOffsetDate)
    }

    private fun makeCurrentMonth(calendar: Calendar) {
        for (i in 1..calendar.getActualMaximum(Calendar.DATE)) dateList.add(i)
    }

    private fun makeNextHead() {
        var date = 1

        for (i in 1..nextHead) dateList.add(date++)
    }

}

class SuccessCalendarWeek(date: Date) {
    private val inputDate: Date
    private val calendar = Calendar.getInstance()
    var weekList = arrayListOf<WeekDTO>()

    init {
        calendar.time = date
        inputDate = date
    }

    fun initBaseCalendar() {
        makeMonthWeek()
    }

    fun getCurrentWeek() : WeekDTO? {
        val calStart = Calendar.getInstance()
        val calEnd = Calendar.getInstance()
        for (week in weekList) {
            calStart.time = week.startDate
            calStart.set(Calendar.HOUR, 0)
            calStart.set(Calendar.MINUTE, 0)
            calStart.set(Calendar.SECOND, 0)

            calEnd.time = week.endDate
            calEnd.set(Calendar.HOUR, 23)
            calEnd.set(Calendar.MINUTE, 59)
            calEnd.set(Calendar.SECOND, 59)

            if (inputDate >= calStart.time && inputDate <= calEnd.time) {
                return week
            }
        }
        return null
    }

    private fun makeMonthWeek() {
        weekList.clear()
        calendar.set(Calendar.DATE, 1)

        // 주차를 나누는 기준은 목요일
        // 현재 달의 1일이 월화수목 중 하나라면 첫 째주, 금토일 중 하나라면 저번달 마지막 주
        // 금 - 6, 토 - 7, 일 - 1 즉 1일이 저번달의 마지막 주 이므로 다음주 월요일을 1째주로 지정
        var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        var month = calendar.get(Calendar.MONTH)
        var startDate = Date()

        //println("날짜 ${calendar.time}, dayOfWeek $dayOfWeek")

        if (isCurrentWeek(dayOfWeek)) { // 1일이 이번달 1째주에 포함
            // 월요일이 아니라면 저번달에서 월요일을 찾아서 1째주의 시작날짜로 지정
            if (dayOfWeek != 2) {
                calendar.add(Calendar.DATE, -(dayOfWeek - 2))
            }
            startDate = calendar.time
        } else { // 1일이 저번달 마지막주에 포함
            for (i in 1..7) { // 월요일을 찾아서 1째주 시작날짜로 지정
                calendar.add(Calendar.DATE, 1)
                //println("월요일 찾기 ${SimpleDateFormat("yyyy.MM.dd").format(calendar.time)}, dayOfWeek ${calendar.get(Calendar.DAY_OF_WEEK)}")
                if (calendar.get(Calendar.DAY_OF_WEEK) == 2) {
                    //println("찾음")
                    startDate = calendar.time
                    break
                }
            }
        }

        calendar.add(Calendar.DATE, 6)
        var endDate = calendar.time
        weekList.add(WeekDTO(1, startDate, endDate))

        for (i in 2..5) {
            calendar.add(Calendar.DATE, 1)
            startDate = calendar.time

            calendar.add(Calendar.DATE, 6)
            endDate = calendar.time

            if (month != calendar.get(Calendar.MONTH)) { // 월이 바뀌었으면 1일을 체크하여 마지막주에 포함하는지 확인
                var calenderTemp = calendar
                calenderTemp.set(Calendar.DATE, 1)
                if (isCurrentWeek(calenderTemp.get(Calendar.DAY_OF_WEEK))) {
                    break
                }
            }

            weekList.add(WeekDTO(i, startDate, endDate))
        }
    }

    private fun isCurrentWeek(dayOfWeek: Int) : Boolean {
        return when (dayOfWeek) {
            1, 6, 7 -> false // 일-1, 금-6, 토-7 목요일이 포함되어 있지 않은 주이기 때문에 false
            else -> true // 목요일이 포함되어 있는 주이기 때문에 true
        }
    }
}