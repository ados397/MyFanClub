package com.ados.myfanclub.page

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.databinding.ListItemFanClubBinding
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.FanClubExDTO
import com.bumptech.glide.Glide

class RecyclerViewAdapterFanClub(private val itemsEx: ArrayList<FanClubExDTO>, var clickListener: OnFanClubItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterFanClub.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemFanClubBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemsEx.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initializes(itemsEx[position], clickListener)

        itemsEx[position].let { item ->
            with(holder) {
                if (item.imgSymbolCustomUri != null) {
                    Glide.with(image.context).load(item.imgSymbolCustomUri).fitCenter().into(holder.image)
                } else {
                    var imageID = itemView.context.resources.getIdentifier(item.fanClubDTO?.imgSymbol, "drawable", itemView.context.packageName)
                    if (image != null && imageID > 0) {
                        //iconImage?.setImageResource(item)
                        Glide.with(image.context)
                            .asBitmap()
                            .load(imageID) ///feed in path of the image
                            .fitCenter()
                            .into(holder.image)
                    }
                }

                name.text = "${item.fanClubDTO?.name}"
                level.text = "Lv. ${item.fanClubDTO?.level}"
                master.text = "${item.fanClubDTO?.masterNickname}"
                count.text = "${item.fanClubDTO?.memberCount}/${item.fanClubDTO?.getMaxMemberCount()}"

                if (item.fanClubDTO?.isSelected == true) {
                    mainLayout.setBackgroundColor(Color.parseColor("#BBD5F8"))
                } else {
                    mainLayout.setBackgroundColor(Color.parseColor("#FFFFFF"))
                }
            }
        }
    }

    fun updateSymbol(position: Int, uri: Uri) {
        itemsEx[position].imgSymbolCustomUri = uri
        notifyItemChanged(position)
    }

    // 이미 선택된 항목을 선택할 경우 선택을 해제하고 false 반환, 아닐경우 해당항목 선택 후 true 반환
    fun selectItem(position: Int) : Boolean {
        return if (itemsEx[position].fanClubDTO?.isSelected == true) {
            itemsEx[position].fanClubDTO?.isSelected = false
            notifyDataSetChanged()
            false
        } else {
            for (item in itemsEx) {
                item.fanClubDTO?.isSelected = false
            }
            itemsEx[position].fanClubDTO?.isSelected = true
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

        fun initializes(item: FanClubExDTO, action:OnFanClubItemClickListener) {
            viewBinding.layoutMain.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }
    }


}

interface OnFanClubItemClickListener {
    fun onItemClick(item: FanClubExDTO, position: Int)
}