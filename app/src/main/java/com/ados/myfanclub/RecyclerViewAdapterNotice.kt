package com.ados.myfanclub

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.databinding.ListItemNoticeBinding
import com.ados.myfanclub.model.NoticeDTO
import com.bumptech.glide.Glide
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class RecyclerViewAdapterNotice(private val items: ArrayList<NoticeDTO>) : RecyclerView.Adapter<RecyclerViewAdapterNotice.ViewHolder>() {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemNoticeBinding.inflate(LayoutInflater.from(parent.context) , parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initialize()

        items[position].let { item ->
            with(holder) {
                layoutContent.visibility = View.GONE
                buttonExpand.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)

                title.text = item.title
                textTime.text = SimpleDateFormat("yyyy.MM.dd HH:mm").format(item.time!!)
                content.text = item.content?.replace("\\n","\n")

                if (!item.imageUrl.isNullOrEmpty()) {
                    Glide.with(imgNotice.context).load(item.imageUrl).fitCenter().into(imgNotice)
                    imgNotice.visibility = View.VISIBLE
                } else {
                    imgNotice.visibility = View.GONE
                }

                //text_timer.text = "[${String.format("%02d",hour)}시${String.format("%02d",min)}분${String.format("%02d",sec)}초] 후 티켓이 사라집니다."



                /*if (item.isSelected) {
                    mainLayout.setBackgroundColor(Color.parseColor("#BBD5F8"))
                } else {
                    mainLayout.setBackgroundColor(Color.parseColor("#FFFFFF"))
                }*/
            }
        }
    }

    /*fun clearItems() {
        for (item in items) {
            item.favorite = false
        }
        notifyDataSetChanged()
    }*/

    /*fun selectItem(position: Int) {
        for (item in items) {
            item.isSelected = false
        }
        items[position].isSelected = true

        notifyDataSetChanged()
    }*/

    inner class ViewHolder(private val viewBinding: ListItemNoticeBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var isExpanded = false
        var imgType = viewBinding.imgType
        var imgNotice = viewBinding.imgNotice
        var mainLayout = viewBinding.layoutMain
        val title = viewBinding.textTitle
        val content = viewBinding.textContent
        val textTime = viewBinding.textTime
        var layoutTitle = viewBinding.layoutTitle
        var layoutContent = viewBinding.layoutContent
        var buttonExpand = viewBinding.buttonExpand

        fun initialize() {
            viewBinding.buttonExpand.setOnClickListener {
                //viewBinding.layoutContent.visibility = View.VISIBLE

                isExpanded = toggleLayout(!isExpanded, viewBinding.buttonExpand, viewBinding.layoutContent)
            }
        }

        private fun toggleLayout(isExpanded: Boolean, view: View, rv: View): Boolean {
            ToggleAnimation.toggleArrow(view, isExpanded)
            if (isExpanded) {
                ToggleAnimation.expandAction(rv)
            } else {
                ToggleAnimation.collapse(rv)
            }
            return isExpanded
        }

    }

}