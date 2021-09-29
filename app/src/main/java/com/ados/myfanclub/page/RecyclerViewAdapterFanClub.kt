package com.ados.myfanclub.page

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.databinding.ListItemFanClubBinding
import com.ados.myfanclub.model.FanClubDTO
import com.bumptech.glide.Glide

class RecyclerViewAdapterFanClub(private val items: ArrayList<FanClubDTO>, var clickListener: OnFanClubItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterFanClub.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemFanClubBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initializes(items[position], clickListener)

        items[position].let { item ->
            with(holder) {
                var imageID = itemView.context.resources.getIdentifier(item.imgSymbol, "drawable", itemView.context.packageName)
                if (image != null && imageID > 0) {
                    //iconImage?.setImageResource(item)
                    Glide.with(image.context)
                        .asBitmap()
                        .load(imageID) ///feed in path of the image
                        .fitCenter()
                        .into(holder.image)
                }

                name.text = "${item.name}"
                level.text = "Lv. ${item.level}"
                master.text = "${item.masterNickname}"
                count.text = "${item.count}/${item.countMax}"

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

    inner class ViewHolder(private val viewBinding: ListItemFanClubBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var image = viewBinding.imgSymbol
        var name = viewBinding.textName
        var level = viewBinding.textLevel
        var master = viewBinding.textMaster
        var count = viewBinding.textCount
        var mainLayout = viewBinding.layoutMain

        fun initializes(item: FanClubDTO, action:OnFanClubItemClickListener) {
            viewBinding.layoutMain.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }
    }


}

interface OnFanClubItemClickListener {
    fun onItemClick(item: FanClubDTO, position: Int)
}