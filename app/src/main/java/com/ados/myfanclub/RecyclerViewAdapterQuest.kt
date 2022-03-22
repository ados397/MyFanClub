package com.ados.myfanclub

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.databinding.ListItemQuestBinding
import com.ados.myfanclub.model.QuestDTO
import java.text.DecimalFormat
import kotlin.collections.ArrayList

class RecyclerViewAdapterQuest(private val items: ArrayList<QuestDTO>, var clickListener: OnQuestItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterQuest.ViewHolder>() {
    var context: Context? = null
    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = ListItemQuestBinding.inflate(LayoutInflater.from(parent.context) , parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initialize(items[position],clickListener)

        items[position].let { item ->
            with(holder) {
                title.text = item.title
                content.text = item.content
                itemCount.text = "${decimalFormat.format(item.gemCount)}"

                if (position == 0) { // 일일 과제 모두 달성
                    var count = 0
                    for (i in 1 until items.size) {
                        if (items[i].isQuestSuccess()) {
                            count++
                        }
                    }

                    var percent = ((count.toDouble() / (items.size-1)) * 100).toInt()
                    progress.progress = percent
                    progressText.text = "$count/${items.size-1}"

                    if (count == (items.size-1) && !item.isQuestGemGet()) { // 모든 일일 퀘스트 완료
                        buttonGet.isEnabled = true
                        buttonGet.background = AppCompatResources.getDrawable(context!!, R.drawable.btn_round_pay)
                        textButtonGet.setTextColor(ContextCompat.getColor(context!!, R.color.text))
                    } else {
                        if (count == (items.size-1)) {
                            textButtonGet.text = "획득완료"
                        }

                        buttonGet.isEnabled = false
                        buttonGet.background = AppCompatResources.getDrawable(context!!, R.drawable.btn_round9)
                        textButtonGet.setTextColor(ContextCompat.getColor(context!!, R.color.text_button_disable))
                    }
                } else {
                    if (item.isQuestSuccess()) {
                        progress.progress = 100
                        progressText.text = "1/1"

                        if (!item.isQuestGemGet()) {
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
                        progress.progress = 0
                        progressText.text = "0/1"

                        buttonGet.isEnabled = false
                        buttonGet.background = AppCompatResources.getDrawable(context!!, R.drawable.btn_round9)
                        textButtonGet.setTextColor(ContextCompat.getColor(context!!, R.color.text_button_disable))
                    }
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

    inner class ViewHolder(private val viewBinding: ListItemQuestBinding) : RecyclerView.ViewHolder(viewBinding.root) {
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

        fun initialize(item: QuestDTO, action:OnQuestItemClickListener) {
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

interface OnQuestItemClickListener {
    fun onItemClick(item: QuestDTO, position: Int)
}