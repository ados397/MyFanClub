package com.ados.myfanclub.page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.SuccessCalendarWeek
import com.ados.myfanclub.databinding.ListItemSuccessCalendarWeekBinding
import com.ados.myfanclub.model.WeekDTO
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecyclerViewAdapterSuccessCalendarWeek(date: Date) : RecyclerView.Adapter<RecyclerViewAdapterSuccessCalendarWeek.ViewHolder>() {
    var weekList: ArrayList<WeekDTO> = arrayListOf()
    private var successCalendarWeek: SuccessCalendarWeek = SuccessCalendarWeek(date)

    init {
        successCalendarWeek.initBaseCalendar()
        weekList = successCalendarWeek.weekList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemSuccessCalendarWeekBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return weekList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder?.bind(weekList[position], position)
        if (itemClick != null) {
            holder?.itemView?.setOnClickListener { v ->
                itemClick?.onClick(v, position)

            }
        }
    }

    inner class ViewHolder(private val viewBinding: ListItemSuccessCalendarWeekBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var weekNum = viewBinding.textWeekNum
        var range = viewBinding.textRange
        var process = viewBinding.progressPercent

        fun bind(data: WeekDTO, position: Int) {
            weekNum.text = "${data.week}주차"
            range.text = "(${SimpleDateFormat("MM.dd").format(data.startDate)}~${SimpleDateFormat("MM.dd").format(data.endDate)})"
            process.visibility = View.VISIBLE
        }

    }

    interface ItemClick {
        fun onClick(view: View, position: Int)
    }

    var itemClick: ItemClick? = null






}
