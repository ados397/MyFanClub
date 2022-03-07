package com.ados.myfanclub.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.ListItemFanClubRewardBinding
import com.ados.myfanclub.model.FanClubRewardDTO
import java.text.DecimalFormat
import kotlin.collections.ArrayList

class RecyclerViewAdapterFanClubReward(private val items: ArrayList<FanClubRewardDTO>, private val fanClubCheckoutCount: Int, var clickListener: OnFanClubRewardItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterFanClubReward.ViewHolder>() {
    var context: Context? = null
    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = ListItemFanClubRewardBinding.inflate(LayoutInflater.from(parent.context) , parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initalize(items[position],clickListener)

        items[position].let { item ->
            with(holder) {
                title.text = "${decimalFormat.format(item.checkoutCount)}명 출석 보상"
                content.text = "${decimalFormat.format(item.checkoutCount)}명 출석 완료 시 보상 획득"
                itemCount.text = item.gemCount.toString()

                // 보상 획득 조건 충족
                if (fanClubCheckoutCount >= item.checkoutCount!!) {
                    progress.progress = 100
                    progressText.text = "${item.checkoutCount}/${item.checkoutCount}"

                    if (!item.isRewardGemGet()) {
                        buttonGet.isEnabled = true
                        buttonGet.background = AppCompatResources.getDrawable(context!!, R.drawable.btn_round_pay)
                        textButtonGet.setTextColor(ContextCompat.getColor(context!!, R.color.text))
                    } else {
                        buttonGet.isEnabled = false
                        buttonGet.background = AppCompatResources.getDrawable(context!!, R.drawable.btn_round9)
                        textButtonGet.setTextColor(ContextCompat.getColor(context!!, R.color.text_button_disable))
                        textButtonGet.text = "획득완료"
                    }
                } else {
                    progress.progress = ((fanClubCheckoutCount.toDouble() / item.checkoutCount) * 100).toInt()
                    progressText.text = "${fanClubCheckoutCount}/${item.checkoutCount}"

                    buttonGet.isEnabled = false
                    buttonGet.background = AppCompatResources.getDrawable(context!!, R.drawable.btn_round9)
                    textButtonGet.setTextColor(ContextCompat.getColor(context!!, R.color.text_button_disable))
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

    /*fun selectItem(position: Int) {
        for (item in items) {
            item.isSelected = false
        }
        items[position].isSelected = true

        notifyDataSetChanged()
    }*/

    inner class ViewHolder(private val viewBinding: ListItemFanClubRewardBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var mainLayout = viewBinding.layoutMain
        val title = viewBinding.textTitle
        val content = viewBinding.textContent
        var imgItem = viewBinding.imgItem
        var itemCount = viewBinding.textItemCount
        var buttonGet = viewBinding.buttonGet
        var textButtonGet = viewBinding.textButtonGet
        var progress = viewBinding.progress
        var progressText = viewBinding.textProgress
        var layoutItem = viewBinding.layoutItem

        fun initalize(item: FanClubRewardDTO, action:OnFanClubRewardItemClickListener) {
            /*itemView.setOnClickListener {
                action.onItemClick(item, adapterPosition)
                itemView.img_favorite.setImageResource(R.drawable.star_icon_fill)
            }*/
            /*itemView.movie_item_relative_layout.setOnClickListener {
                action.onItemClick(item, adapterPosition)
                itemView.img_favorite.setImageResource(R.drawable.star_icon_fill)
            }*/
            /*viewBinding.layoutMain.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }*/
            viewBinding.buttonGet.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }

    }

}

interface OnFanClubRewardItemClickListener {
    fun onItemClick(item: FanClubRewardDTO, position: Int)
}