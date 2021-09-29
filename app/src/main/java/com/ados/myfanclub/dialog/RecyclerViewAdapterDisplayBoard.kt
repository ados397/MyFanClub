package com.ados.myfanclub.dialog

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.ListItemDisplayBoardBinding
import com.ados.myfanclub.model.DisplayBoardDTO

class RecyclerViewAdapterDisplayBoard(private val items: ArrayList<DisplayBoardDTO>) : RecyclerView.Adapter<RecyclerViewAdapterDisplayBoard.ViewHolder>() {

    var context: Context? = null
    val anim = AlphaAnimation(0.1f, 1.0f)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = ListItemDisplayBoardBinding.inflate(LayoutInflater.from(parent.context) , parent,false)
        anim.duration = 800
        anim.startOffset = 20
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items[position].let { item ->
            with(holder) {
                if (!item.userUid.isNullOrEmpty()) {
                    title.text = "${item.userNickname} : ${item.displayText}"

                    val nicknameLen = item.userNickname.toString().length + 3
                    val displayLen = item.displayText.toString().length

                    val ssb = SpannableStringBuilder(title.text)
                    ssb.apply {
                        setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.white)), 0, nicknameLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        setSpan(ForegroundColorSpan(item.color!!), nicknameLen, title.text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE )
                    }
                    title.text = ssb
                    title.startAnimation(anim)
                }
            }
        }
    }

    inner class ViewHolder(private val viewBinding: ListItemDisplayBoardBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var title = viewBinding.textTitle

    }

}