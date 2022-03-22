package com.ados.myfanclub.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.databinding.ListItemFanClubSymbolBinding

class RecyclerViewAdapterFanClubSymbol(private val items: ArrayList<Int>, var clickListener: SelectFanClubSymbolDialog) : RecyclerView.Adapter<RecyclerViewAdapterFanClubSymbol.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemFanClubSymbolBinding.inflate(LayoutInflater.from(parent.context) , parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initialize(items.get(position),clickListener)

        items[position].let { item ->
            with(holder) {
                if (position > 0) {
                    number.text = "$position"
                } else {
                    number.text = "사진 불러오기"
                }

                //Glide.with(holder.itemView.context).load(item.iconImage).apply(
                    //RequestOptions().centerCrop()).into(iconImage)

                //var imageID = itemView.context.resources.getIdentifier(item.iconImage, "drawable", itemView.context.packageName)
                if (item > 0) {
                    imgSymbol.setImageResource(item)
                    /*Glide.with(iconImage.context)
                        .asBitmap()
                        .load(item) ///feed in path of the image
                        .fitCenter()
                        .into(holder.iconImage)*/
                }

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

    inner class ViewHolder(private val viewBinding: ListItemFanClubSymbolBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var mainLayout = viewBinding.layoutFanClubSymbol
        var imgSymbol = viewBinding.imgSymbol
        val number = viewBinding.textNumber

        fun initialize(item: Int, action:OnFanClubSymbolClickListener) {
            /*itemView.setOnClickListener {
                action.onItemClick(item, adapterPosition)
                itemView.img_favorite.setImageResource(R.drawable.star_icon_fill)
            }*/
            /*itemView.movie_item_relative_layout.setOnClickListener {
                action.onItemClick(item, adapterPosition)
                itemView.img_favorite.setImageResource(R.drawable.star_icon_fill)
            }*/
            viewBinding.layoutFanClubSymbol.setOnClickListener  {
                action.onItemClick(item, adapterPosition)
            }
            /*viewBinding.imgFavorite.setOnClickListener {
                action.onItemClick_favorite(item, viewBinding.imgFavorite)
            }*/
        }

    }

}

interface OnFanClubSymbolClickListener {
    fun onItemClick(item: Int, position: Int)
}