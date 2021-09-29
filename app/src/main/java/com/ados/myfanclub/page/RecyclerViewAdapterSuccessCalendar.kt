package com.ados.myfanclub.page

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.SuccessCalendar
import com.ados.myfanclub.databinding.ListItemSuccessCalendarBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecyclerViewAdapterSuccessCalendar(private val calendarLayout: LinearLayout, val date: Date) : RecyclerView.Adapter<RecyclerViewAdapterSuccessCalendar.ViewHolder>() {
    var dataList: ArrayList<Int> = arrayListOf()
    var successCalendar: SuccessCalendar = SuccessCalendar(date)

    init {
        successCalendar.initBaseCalendar()
        dataList = successCalendar.dateList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemSuccessCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // list_item_calendar 높이 지정
        val h = calendarLayout.height / 6
        holder.itemView.layoutParams.height = h

        holder?.bind(dataList[position], position)
        if (itemClick != null) {
            holder?.itemView?.setOnClickListener { v ->
                itemClick?.onClick(v, position)

            }
        }
    }

    inner class ViewHolder(private val viewBinding: ListItemSuccessCalendarBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var day = viewBinding.textDay
        var progress = viewBinding.progressPercent

        fun bind(data: Int, position: Int) {
            val firstDateIndex = successCalendar.prevTail
            val lastDateIndex = dataList.size - successCalendar.nextHead - 1

            // 날짜 표시
            day.text = data.toString()

            // 오늘 날짜 처리
            var currentDay = (SimpleDateFormat("dd").format(Date())).toInt()
            var currentMonth = (SimpleDateFormat("MM").format(Date())).toInt()
            var selMonth = (SimpleDateFormat("MM").format(date)).toInt()

            if (dataList[position] == currentDay && selMonth == currentMonth) {
                day.setTypeface(day.typeface, Typeface.BOLD)
            }

            // 현재 월의 1일 이전, 현재 월의 마지막일 이후 값의 텍스트를 회색처리
            if (position < firstDateIndex || position > lastDateIndex) {
                //itemCalendarDateText.setTextAppearance(R.style.LightColorTextViewStyle)
                //itemCalendarDotView.background = null
                day.visibility = View.GONE
                progress.visibility = View.GONE
            }
        }

    }

    interface ItemClick {
        fun onClick(view: View, position: Int)
    }

    var itemClick: ItemClick? = null
}
