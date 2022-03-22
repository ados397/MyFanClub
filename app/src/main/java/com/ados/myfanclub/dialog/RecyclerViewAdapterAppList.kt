package com.ados.myfanclub.dialog

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.databinding.ListItemAppListBinding
import com.ados.myfanclub.model.AppDTO
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class RecyclerViewAdapterAppList(private val items: ArrayList<AppDTO>, var clickListener: SelectAppDialog) : RecyclerView.Adapter<RecyclerViewAdapterAppList.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemAppListBinding.inflate(LayoutInflater.from(parent.context) , parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initialize(items.get(position),clickListener)

        items[position].let { item ->
            with(holder) {
                appName.text = item.appName

                Glide.with(holder.itemView.context).load(item.iconImage).apply(
                    RequestOptions().centerCrop()).into(iconImage)

                if (item.isSelected) {
                    mainLayout.setBackgroundColor(Color.parseColor("#BBD5F8"))
                } else {
                    mainLayout.setBackgroundColor(Color.parseColor("#FFFFFF"))
                }
            }
        }
    }

    /*fun clearItems() {
        for (item in items) {
            item.favorite = false
        }
        notifyDataSetChanged()
    }*/

    fun selectItem(position: Int) {
        for (item in items) {
            item.isSelected = false
        }
        items[position].isSelected = true

        notifyDataSetChanged()
    }

    inner class ViewHolder(private val viewBinding: ListItemAppListBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var mainLayout = viewBinding.layoutAppList
        var iconImage = viewBinding.imgIcon
        val appName = viewBinding.textAppName

        fun initialize(item: AppDTO, action:OnAppListClickListener) {
            /*itemView.setOnClickListener {
                action.onItemClick(item, adapterPosition)
                itemView.img_favorite.setImageResource(R.drawable.star_icon_fill)
            }*/
            /*itemView.movie_item_relative_layout.setOnClickListener {
                action.onItemClick(item, adapterPosition)
                itemView.img_favorite.setImageResource(R.drawable.star_icon_fill)
            }*/
            viewBinding.layoutAppList.setOnClickListener  {
                action.onItemClick(item, adapterPosition)
            }
            /*viewBinding.imgFavorite.setOnClickListener {
                action.onItemClick_favorite(item, viewBinding.imgFavorite)
            }*/
        }

    }

}

interface OnAppListClickListener {
    fun onItemClick(item: AppDTO, position: Int)
}