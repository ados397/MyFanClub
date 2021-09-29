package com.ados.myfanclub.page

import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.ListItemScheduleBinding
import com.ados.myfanclub.model.ScheduleDTO
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecyclerViewAdapterSchedule(private val items: ArrayList<ScheduleDTO>, private val clickListener: OnScheduleItemClickListener, private val startDragListener: OnStartDragListener) : RecyclerView.Adapter<RecyclerViewAdapterSchedule.ViewHolder>(), SwipeHelperCallback.OnItemMoveListener {

    var showReorderIcon : Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initializes(holder, items[position])

        items[position].let { item ->
            with(holder) {
                no.text = "${position + 1}"
                title.text = "${item.title}"
                range.text = "${SimpleDateFormat("yyyy.MM.dd").format(item.startDate)} ~ ${SimpleDateFormat("yyyy.MM.dd").format(item.endDate)}"

                when (item.cycle) {
                    ScheduleDTO.CYCLE.DAY -> imgScheduleType.setImageResource(R.drawable.schedule_day)
                    ScheduleDTO.CYCLE.WEEK -> imgScheduleType.setImageResource(R.drawable.schedule_week)
                    ScheduleDTO.CYCLE.MONTH -> imgScheduleType.setImageResource(R.drawable.schedule_month)
                    ScheduleDTO.CYCLE.PERIOD -> imgScheduleType.setImageResource(R.drawable.schedule_period)
                }

                if (showReorderIcon) {
                    imgReorder.visibility = View.VISIBLE
                } else {
                    imgReorder.visibility = View.GONE
                }

                if (item.isSelected) {
                    mainLayout.setBackgroundColor(Color.parseColor("#BBD5F8"))
                } else {
                    mainLayout.setBackgroundColor(Color.parseColor("#FFFFFF"))
                }
            }
        }
    }

    // 이미 선택된 항목을 선택할 경우 선택을 해제하고 false 반환, 아닐경우 해당항목 선택 후 true 반환
    fun selectItem(position: Int) : Boolean {
        return if (items[position].isSelected) {
            items[position].isSelected = false
            notifyDataSetChanged()
            false
        } else {
            for (item in items) {
                item.isSelected = false
            }
            items[position].isSelected = true
            notifyDataSetChanged()
            true
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        val fromOrder = items[fromPosition].order
        val toOrder = items[toPosition].order

        items[fromPosition].order = toOrder
        items[toPosition].order = fromOrder

        Collections.swap(items, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    inner class ViewHolder(private val viewBinding: ListItemScheduleBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var no = viewBinding.textNo
        var title = viewBinding.textTitle
        var range = viewBinding.textRange
        var imgScheduleType = viewBinding.imgScheduleType
        var mainLayout = viewBinding.layoutMain
        var imgReorder = viewBinding.imgReorder

        fun initializes(holder: ViewHolder, item: ScheduleDTO) {
            viewBinding.layoutItem.setOnClickListener {
                clickListener.onItemClick(item, adapterPosition)
            }
            viewBinding.imgReorder.setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    startDragListener.onStartDrag(holder)
                }
                return@setOnTouchListener true
            }
        }
    }


}

interface OnScheduleItemClickListener {
    fun onItemClick(item: ScheduleDTO, position: Int)
}

interface OnStartDragListener {
    fun onStartDrag(holder: RecyclerViewAdapterSchedule.ViewHolder)
}