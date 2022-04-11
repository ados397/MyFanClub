package com.ados.myfanclub.page

import android.content.Context
import android.graphics.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.ListItemMissionBinding
import com.ados.myfanclub.model.DashboardMissionDTO
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList

class RecyclerViewAdapterMission(private val items: ArrayList<DashboardMissionDTO>, var clickListener: OnMissionItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterMission.ViewHolder>() {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")
    var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = ListItemMissionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initializes(items[position], clickListener)

        items[position].let { item ->
            with(holder) {
                if (item.isBlocked) {
                    title.text = "내가 신고한 스케줄 입니다."
                    title.setTextColor(ContextCompat.getColor(context!!, R.color.text_disable2))
                } else {
                    title.text = "${item.scheduleDTO?.title}"
                    title.setTextColor(ContextCompat.getColor(context!!, R.color.text))
                }

                count.text = "${item.scheduleProgressDTO?.count}/${item.scheduleDTO?.count}"
                textPeriod.text = SimpleDateFormat("M월 d일 까지").format(item.scheduleDTO?.endDate!!)

                var percentage = ((item.scheduleProgressDTO?.count?.toDouble()!! / item.scheduleDTO?.count!!) * 100).toInt()
                percent.text = "$percentage%"
                progress.progress = percentage

                //progress.setBackgroundColor(Color.RED)

                //progress.progressDrawable.colorFilter = BlendModeColorFilter(Color.RED, BlendMode.SRC_IN)



                //progress.indeterminateTintList = ColorStateList(Color.RED, Color.BLUE, Color.BLACK, Color.CYAN)



                if (percentage < 100) {
                    imgComplete.visibility = View.GONE
                    when {
                        percentage < 40 -> {
                            holder.setPercent(ContextCompat.getColor(context!!, R.color.progress_0))
                        }
                        percentage < 70 -> {
                            //holder.setPercent(Color.parseColor("#F79256"))
                            holder.setPercent(ContextCompat.getColor(context!!, R.color.progress_40))
                        }
                        else -> {
                            //holder.setPercent(Color.parseColor("#03DAC5"))
                            holder.setPercent(ContextCompat.getColor(context!!, R.color.progress_70))
                        }
                    }
                } else {
                    imgComplete.visibility = View.VISIBLE
                    //holder.setPercent(Color.parseColor("#0099FF"))
                    holder.setPercent(ContextCompat.getColor(context!!, R.color.progress_100))
                }
            }
        }
    }

    inner class ViewHolder(private val viewBinding: ListItemMissionBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var title = viewBinding.textTitle
        var count = viewBinding.textCount
        var percent = viewBinding.textPercent
        var progress = viewBinding.progressPercent
        var imgComplete = viewBinding.imgComplete
        var layoutPercent = viewBinding.layoutPercent
        var textPeriod = viewBinding.textPeriod
        var mainLayout = viewBinding.layoutMain

        fun initializes(item: DashboardMissionDTO, action:OnMissionItemClickListener) {
            viewBinding.layoutMain.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }

        fun setPercent(color: Int) {
            layoutPercent.setBackgroundColor(color)
            percent.setTextColor(color)
            //progress.progressDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            progress.progressDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_ATOP)

        }
    }


}

interface OnMissionItemClickListener {
    fun onItemClick(item: DashboardMissionDTO, position: Int)
}