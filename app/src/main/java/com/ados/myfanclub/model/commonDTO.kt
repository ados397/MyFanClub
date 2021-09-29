package com.ados.myfanclub.model

import java.util.*

data class QuestionDTO(val stat: STAT? = STAT.INFO,
                       val title: String? = null,
                       val content: String? = null,
                       val image: String? = null,
) {
    enum class STAT {
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



data class SignUpDTO(
    var isSelected: Boolean = false,
    var isChecked: Boolean = false,
    val name: String? = null
) {}

data class WeekDTO(
    var week: Int? = 0,
    var startDate: Date? = null,
    val endDate: Date? = null
) {}