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
import com.ados.myfanclub.databinding.ListItemSuccessCalendarMonthBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecyclerViewAdapterSuccessCalendarMonth(val date: Date, private val percents: MutableMap<String, Int>, private val successCalendar: SuccessCalendar) : RecyclerView.Adapter<RecyclerViewAdapterSuccessCalendarMonth.ViewHolder>() {
    var context: Context? = null
    var monthList: ArrayList<Int> = arrayListOf()
    //var successCalendar: SuccessCalendar = SuccessCalendar(date)
    val currentDate = Date()

    init {
        monthList.clear()
        for (i in 1..12) monthList.add(i)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
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

        holder.bind(monthList[position], position)
        if (itemClick != null) {
            holder.itemView.setOnClickListener { v ->
                itemClick?.onClick(v, position)

            }
        }
    }

    inner class ViewHolder(private val viewBinding: ListItemSuccessCalendarMonthBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var month = viewBinding.textMonth
        var progress = viewBinding.progressPercent
        var textPercent = viewBinding.textPercent
        var complete = viewBinding.imgComplete

        fun bind(data: Int, position: Int) {
            val firstDateIndex = successCalendar.prevTail
            val lastDateIndex = monthList.size - successCalendar.nextHead - 1

            // 날짜 표시
            month.text = "${data.toString()}월"

            val dayString = String.format("%02d", data)
            val percent = if (percents.contains(dayString)) percents[dayString]!! else 0
            progress.progress = percent
            textPercent.text = "$percent%"
            if (percent < 100) {
                complete.visibility = View.GONE
                when {
                    percent < 40 -> {
                        progress.progressBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.progress_background_0))
                        progress.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.progress_0))
                        textPercent.setTextColor(ContextCompat.getColor(context!!, R.color.progress_0))
                    }
                    percent < 70 -> {
                        progress.progressBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.progress_background_40))
                        progress.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.progress_40))
                        textPercent.setTextColor(ContextCompat.getColor(context!!, R.color.progress_40))
                    }
                    else -> {
                        progress.progressBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.progress_background_70))
                        progress.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.progress_70))
                        textPercent.setTextColor(ContextCompat.getColor(context!!, R.color.progress_70))
                    }
                }
            } else {
                complete.visibility = View.VISIBLE
                progress.progressBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.progress_background_100))
                progress.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.progress_100))
                textPercent.setTextColor(ContextCompat.getColor(context!!, R.color.progress_100))
            }

            // 현재 날짜 표시
            val currentMonth = SimpleDateFormat("MM").format(currentDate).toInt()
            val currentYear = SimpleDateFormat("yyyy").format(currentDate).toInt()
            val selYear = (SimpleDateFormat("yyyy").format(date)).toInt()

            if (monthList[position] == currentMonth && currentYear == selYear) {
                month.setTypeface(month.typeface, Typeface.BOLD)
                month.setTextColor(ContextCompat.getColor(context!!, R.color.text))
            }

            // 현재 월의 1일 이전, 현재 월의 마지막일 이후 값의 텍스트를 회색처리
            if (position < firstDateIndex || position > lastDateIndex) {
                //itemCalendarDateText.setTextAppearance(R.style.LightColorTextViewStyle)
                //itemCalendarDotView.background = null
                //progress.visibility = View.GONE
            }
        }

    }

    interface ItemClick {
        fun onClick(view: View, position: Int)
    }

    var itemClick: ItemClick? = null
}
