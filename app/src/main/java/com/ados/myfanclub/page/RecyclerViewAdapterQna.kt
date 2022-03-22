package com.ados.myfanclub.page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.ToggleAnimation
import com.ados.myfanclub.databinding.ListItemQnaBinding
import com.ados.myfanclub.model.QnaDTO
import com.bumptech.glide.Glide
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class RecyclerViewAdapterQna(private val items: ArrayList<QnaDTO>) : RecyclerView.Adapter<RecyclerViewAdapterQna.ViewHolder>() {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemQnaBinding.inflate(LayoutInflater.from(parent.context) , parent,false)
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
                //buttonExpand.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)

                textTime.text = SimpleDateFormat("yyyy.MM.dd HH:mm").format(item.createTime!!)
                title.text = item.title
                content.text = item.content?.replace("\\n","\n")

                if (item.answer.isNullOrEmpty()) {
                    answer.text = "답변을 기다리고 있습니다."
                    answerWait.visibility = View.VISIBLE
                    answerComplete.visibility = View.GONE
                } else {
                    answer.text = item.answer?.replace("\\n","\n")
                    answerWait.visibility = View.GONE
                    answerComplete.visibility = View.VISIBLE
                }

                if (!item.imageUrl.isNullOrEmpty()) {
                    Glide.with(imgContent.context).load(item.imageUrl).fitCenter().into(imgContent)
                    imgContent.visibility = View.VISIBLE
                } else {
                    imgContent.visibility = View.GONE
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

    inner class ViewHolder(private val viewBinding: ListItemQnaBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var isExpanded = false
        var imgType = viewBinding.imgType
        var imgContent = viewBinding.imgContent
        var mainLayout = viewBinding.layoutMain
        val textTime = viewBinding.textTime
        val title = viewBinding.textTitle
        val content = viewBinding.textContent
        val answer = viewBinding.textAnswer
        val answerWait = viewBinding.textAnswerWait
        val answerComplete = viewBinding.textAnswerComplete
        var layoutTitle = viewBinding.layoutTitle
        var layoutContent = viewBinding.layoutContent
        var buttonExpand = viewBinding.buttonExpand

        fun initialize() {
            viewBinding.buttonExpand.setOnClickListener {
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