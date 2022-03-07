package com.ados.myfanclub.page

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.R
import com.ados.myfanclub.SuccessCalendar
import com.ados.myfanclub.databinding.ListItemSuccessCalendarDayBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecyclerViewAdapterSuccessCalendarDay(private val calendarLayout: LinearLayout, val date: Date, private val percents: MutableMap<String, Int>, private val successCalendar: SuccessCalendar) : RecyclerView.Adapter<RecyclerViewAdapterSuccessCalendarDay.ViewHolder>() {
    var context: Context? = null
    var dataList: ArrayList<Int> = arrayListOf()
    //var successCalendar: SuccessCalendar = SuccessCalendar(date)
    val currentDate = Date()

    init {
        //successCalendar.initBaseCalendar()
        dataList = successCalendar.dateList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = ListItemSuccessCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ViewHolder(private val viewBinding: ListItemSuccessCalendarDayBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var day = viewBinding.textDay
        var progress = viewBinding.progressPercent
        var complete = viewBinding.imgComplete

        fun bind(data: Int, position: Int) {
            val firstDateIndex = successCalendar.prevTail
            val lastDateIndex = dataList.size - successCalendar.nextHead - 1

            // 날짜 표시
            day.text = data.toString()

            val dayString = String.format("%02d", data)
            val percent = if (percents.contains(dayString)) percents[dayString]!! else 0
            progress.progress = percent
            if (percent < 100) {
                complete.visibility = View.GONE
                when {
                    percent < 40 -> {
                        progress.progressBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.progress_background_0))
                        progress.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.progress_0))
                    }
                    percent < 70 -> {
                        progress.progressBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.progress_background_40))
                        progress.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.progress_40))
                    }
                    else -> {
                        progress.progressBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.progress_background_70))
                        progress.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.progress_70))
                    }
                }
            } else {
                complete.visibility = View.VISIBLE
                progress.progressBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.progress_background_100))
                progress.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.progress_100))
            }


            // 현재 날짜 표시
            val currentDay = (SimpleDateFormat("dd").format(currentDate)).toInt()
            val currentMonth = (SimpleDateFormat("MM").format(currentDate)).toInt()
            val selMonth = (SimpleDateFormat("MM").format(date)).toInt()

            if (dataList[position] == currentDay && selMonth == currentMonth) {
                day.setTypeface(day.typeface, Typeface.BOLD)
                day.setTextColor(ContextCompat.getColor(context!!, R.color.text))
            }

            // 현재 월의 1일 이전, 현재 월의 마지막일 이후 값의 텍스트를 회색처리
            if (position < firstDateIndex || position > lastDateIndex) {
                //itemCalendarDateText.setTextAppearance(R.style.LightColorTextViewStyle)
                //itemCalendarDotView.background = null
                day.visibility = View.GONE
                progress.visibility = View.GONE
                complete.visibility = View.GONE
            }
        }

    }

    interface ItemClick {
        fun onClick(view: View, position: Int)
    }

    var itemClick: ItemClick? = null
}
