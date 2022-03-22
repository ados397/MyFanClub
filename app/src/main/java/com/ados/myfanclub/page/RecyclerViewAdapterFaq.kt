package com.ados.myfanclub.page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.ToggleAnimation
import com.ados.myfanclub.databinding.ListItemFaqBinding
import com.ados.myfanclub.model.FaqDTO
import com.bumptech.glide.Glide
import java.text.DecimalFormat

class RecyclerViewAdapterFaq(private val items: ArrayList<FaqDTO>) : RecyclerView.Adapter<RecyclerViewAdapterFaq.ViewHolder>() {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemFaqBinding.inflate(LayoutInflater.from(parent.context) , parent,false)
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

                title.text = item.question
                content.text = item.answer?.replace("\\n","\n")

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

    inner class ViewHolder(private val viewBinding: ListItemFaqBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var isExpanded = false
        var imgType = viewBinding.imgType
        var imgContent = viewBinding.imgContent
        var mainLayout = viewBinding.layoutMain
        val title = viewBinding.textTitle
        val content = viewBinding.textContent
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