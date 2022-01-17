package com.ados.myfanclub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.databinding.ListItemMailBinding
import com.ados.myfanclub.model.MailDTO
import java.text.DecimalFormat

class RecyclerViewAdapterMail(private val items: ArrayList<MailDTO>, var clickListener: OnMailItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterMail.ViewHolder>() {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemMailBinding.inflate(LayoutInflater.from(parent.context) , parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initalize(items.get(position),clickListener)

        items[position].let { item ->
            with(holder) {
                title.text = item.title
                content.text = item.content

                val interval = ((item.expireTime?.time!!.toLong()) - System.currentTimeMillis()) / 1000
                val day = interval / 86400
                val hour = (interval % 86400) / 3600
                val min = ((interval % 86400) % 3600) / 60
                val sec = interval % 60

                when {
                    day > 10 -> date.text = "${day}일 남음"
                    day > 0 -> date.text = "${day}일 ${hour}시간 남음"
                    hour > 0 -> date.text = "${hour}시간 ${min}분 남음"
                    min > 0 -> date.text = "${min}분 남음"
                }

                when (item.item) {
                    MailDTO.Item.NONE -> {
                        layoutItem.visibility = View.GONE
                    }
                    MailDTO.Item.PAID_GEM, MailDTO.Item.FREE_GEM -> {
                        layoutItem.visibility = View.VISIBLE
                        imgItem.setImageResource(R.drawable.diamond)
                        itemCount.text = "${decimalFormat.format(item.itemCount)}"
                    }
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

    inner class ViewHolder(private val viewBinding: ListItemMailBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var mainLayout = viewBinding.layoutMain
        val title = viewBinding.textTitle
        val content = viewBinding.textContent
        val date = viewBinding.textDate
        var imgItem = viewBinding.imgItem
        var itemCount = viewBinding.textItemCount
        var layoutItem = viewBinding.layoutItem

        fun initalize(item: MailDTO, action:OnMailItemClickListener) {
            /*itemView.setOnClickListener {
                action.onItemClick(item, adapterPosition)
                itemView.img_favorite.setImageResource(R.drawable.star_icon_fill)
            }*/
            /*itemView.movie_item_relative_layout.setOnClickListener {
                action.onItemClick(item, adapterPosition)
                itemView.img_favorite.setImageResource(R.drawable.star_icon_fill)
            }*/
            viewBinding.layoutMain.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
            /*viewBinding.imgFavorite.setOnClickListener {
                action.onItemClick_favorite(item, viewBinding.imgFavorite)
            }*/
        }

    }

}

interface OnMailItemClickListener {
    fun onItemClick(item: MailDTO, position: Int)
}