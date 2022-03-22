package com.ados.myfanclub.page

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.R
import com.ados.myfanclub.SuccessCalendar
import com.ados.myfanclub.SuccessCalendarWeek
import com.ados.myfanclub.databinding.ListItemSuccessCalendarWeekBinding
import com.ados.myfanclub.model.WeekDTO
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecyclerViewAdapterSuccessCalendarWeek(private val percents: MutableMap<String, Int>, private val successCalendarWeek: SuccessCalendarWeek) : RecyclerView.Adapter<RecyclerViewAdapterSuccessCalendarWeek.ViewHolder>() {
    var context: Context? = null
    var weekList: ArrayList<WeekDTO> = arrayListOf()
    //private var successCalendarWeek: SuccessCalendarWeek = SuccessCalendarWeek(date)
    val currentDate = Date()

    init {
        //successCalendarWeek.initBaseCalendar()
        weekList = successCalendarWeek.weekList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = ListItemSuccessCalendarWeekBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return weekList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(weekList[position], position)
        if (itemClick != null) {
            holder.itemView.setOnClickListener { v ->
                itemClick?.onClick(v, position)

            }
        }
    }

    inner class ViewHolder(private val viewBinding: ListItemSuccessCalendarWeekBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var weekNum = viewBinding.textWeekNum
        var range = viewBinding.textRange
        var progress = viewBinding.progressPercent
        var textPercent = viewBinding.textPercent
        var complete = viewBinding.imgComplete

        fun bind(data: WeekDTO, position: Int) {
            weekNum.text = "${data.week}주차"
            range.text = "(${SimpleDateFormat("MM.dd").format(data.startDate!!)}~${SimpleDateFormat("MM.dd").format(data.endDate!!)})"
            progress.visibility = View.VISIBLE

            val weekString = String.format("%02d", data.week)
            val percent = if (percents.contains(weekString)) percents[weekString]!! else 0
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
            val currentRange = SimpleDateFormat("yyyyMMdd").format(currentDate).toInt()
            val startRange = SimpleDateFormat("yyyyMMdd").format(data.startDate!!).toInt()
            val endRange = (SimpleDateFormat("yyyyMMdd").format(data.endDate)).toInt()

            if (currentRange in startRange..endRange) {
                weekNum.setTypeface(weekNum.typeface, Typeface.BOLD)
                weekNum.setTextColor(ContextCompat.getColor(context!!, R.color.text))

                range.setTypeface(range.typeface, Typeface.BOLD)
                range.setTextColor(ContextCompat.getColor(context!!, R.color.text))
            }
        }

    }

    interface ItemClick {
        fun onClick(view: View, position: Int)
    }

    var itemClick: ItemClick? = null






}
