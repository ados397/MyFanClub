package com.ados.myfanclub.page

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.ListItemFanClubRankBinding
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.FanClubExDTO
import com.bumptech.glide.Glide
import java.text.DecimalFormat

class RecyclerViewAdapterFanClubRank(private val itemsEx: ArrayList<FanClubExDTO>, var clickListener: OnFanClubRankItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterFanClubRank.ViewHolder>() {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")
    var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = ListItemFanClubRankBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemsEx.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initializes(itemsEx[position], clickListener)

        itemsEx[position].let { item ->
            with(holder) {
                when (position) {
                    0 -> {
                        Glide.with(imgRank.context).asBitmap().load(R.drawable.award_01).fitCenter().into(holder.imgRank)
                        imgRank.visibility = View.VISIBLE
                        rank.visibility = View.GONE
                        cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.rank1))
                        //imgRank.setImageResource(R.drawable.award_01)
                        //cardView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFA2"))
                    }
                    1 -> {
                        Glide.with(imgRank.context).asBitmap().load(R.drawable.award_02).fitCenter().into(holder.imgRank)
                        imgRank.visibility = View.VISIBLE
                        rank.visibility = View.GONE
                        cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.rank2))
                        //imgRank.setImageResource(R.drawable.award_02)
                        //cardView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#C4EDFF"))
                    }
                    2 -> {
                        Glide.with(imgRank.context).asBitmap().load(R.drawable.award_03).fitCenter().into(holder.imgRank)
                        imgRank.visibility = View.VISIBLE
                        rank.visibility = View.GONE
                        cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.rank3))
                        //imgRank.setImageResource(R.drawable.award_03)
                        //cardView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFE0EC"))
                    }
                    else -> {
                        imgRank.visibility = View.GONE
                        rank.visibility = View.VISIBLE
                        rank.text = "${position+1}"
                        cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.white))
                        //cardView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                    }
                }

                if (item.imgSymbolCustomUri != null) {
                    Glide.with(imgSymbol.context).load(item.imgSymbolCustomUri).fitCenter().into(holder.imgSymbol)
                } else {
                    var imageID = itemView.context.resources.getIdentifier(item.fanClubDTO?.imgSymbol, "drawable", itemView.context.packageName)
                    if (imageID > 0) {
                        //iconImage?.setImageResource(item)
                        Glide.with(imgSymbol.context)
                            .asBitmap()
                            .load(imageID) ///feed in path of the image
                            .fitCenter()
                            .into(holder.imgSymbol)
                    }
                }

                name.text = "${item.fanClubDTO?.name}"
                level.text = "Lv. ${item.fanClubDTO?.level}"
                //exp.text = "${decimalFormat.format(item.getTotalExp())}"
                exp.text = "${decimalFormat.format(item.fanClubDTO?.expTotal)}"
                count.text = "${item.fanClubDTO?.memberCount}/${item.fanClubDTO?.getMaxMemberCount()}"
                description.text = item.fanClubDTO?.description

                if (item.fanClubDTO?.isSelected == true) {
                    mainLayout.setBackgroundColor(Color.parseColor("#BBD5F8"))
                } else {
                    mainLayout.setBackgroundColor(Color.parseColor("#FFFFFF"))
                }
            }
        }
    }

    fun updateSymbol(position: Int, uri: Uri?) {
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

    inner class ViewHolder(private val viewBinding: ListItemFanClubRankBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var imgRank = viewBinding.imgRank
        var imgSymbol = viewBinding.imgSymbol
        var rank = viewBinding.textRank
        var name = viewBinding.textName
        var level = viewBinding.textLevel
        var exp = viewBinding.textExp
        var count = viewBinding.textCount
        var description = viewBinding.textDescription
        var cardView = viewBinding.cardView
        var mainLayout = viewBinding.layoutMain

        fun initializes(item: FanClubExDTO, action:OnFanClubRankItemClickListener) {
            viewBinding.layoutMain.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }
    }
}

interface OnFanClubRankItemClickListener {
    fun onItemClick(item: FanClubExDTO, position: Int)
}