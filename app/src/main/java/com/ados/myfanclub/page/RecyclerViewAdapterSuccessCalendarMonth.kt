package com.ados.myfanclub.page

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.SuccessCalendar
import com.ados.myfanclub.databinding.ListItemSuccessCalendarMonthBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecyclerViewAdapterSuccessCalendarMonth(private val calendarLayout: LinearLayout, val date: Date) : RecyclerView.Adapter<RecyclerViewAdapterSuccessCalendarMonth.ViewHolder>() {
    var monthList: ArrayList<Int> = arrayListOf()
    var successCalendar: SuccessCalendar = SuccessCalendar(date)

    init {
        monthList.clear()
        for (i in 1..12) monthList.add(i)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemSuccessCalendarMonthBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return monthList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // list_item_calendar 높이 지정
        //val h = calendarLayout.height / 6
        //holder.itemView.layoutParams.height = h

        holder?.bind(monthList[position], position)
        if (itemClick != null) {
            holder?.itemView?.setOnClickListener { v ->
                itemClick?.onClick(v, position)

            }
        }
    }

    inner class ViewHolder(private val viewBinding: ListItemSuccessCalendarMonthBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var month = viewBinding.textMonth
        var process = viewBinding.progressPercent

        fun bind(data: Int, position: Int) {
            val firstDateIndex = successCalendar.prevTail
            val lastDateIndex = monthList.size - successCalendar.nextHead - 1

            // 날짜 표시
            month.text = "${data.toString()}월"

            // 오늘 날짜 처리
            var dateString: String = SimpleDateFormat("dd").format(date)
            var dateInt = dateString.toInt()
            if (monthList[position] == dateInt) {
                month.setTypeface(month.typeface, Typeface.BOLD)
            }

            // 현재 월의 1일 이전, 현재 월의 마지막일 이후 값의 텍스트를 회색처리
            if (position < firstDateIndex || position > lastDateIndex) {
                //itemCalendarDateText.setTextAppearance(R.style.LightColorTextViewStyle)
                //itemCalendarDotView.background = null
                process.visibility = View.GONE
            }
        }

    }

    interface ItemClick {
        fun onClick(view: View, position: Int)
    }

    var itemClick: ItemClick? = null
}
