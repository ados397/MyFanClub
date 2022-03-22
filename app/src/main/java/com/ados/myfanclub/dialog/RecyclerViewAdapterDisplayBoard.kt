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
import com.ados.myfanclub.model.DisplayBoardExDTO
import com.ados.myfanclub.page.OnChatItemClickListener

class RecyclerViewAdapterDisplayBoard(private val itemsEx: ArrayList<DisplayBoardExDTO>, var clickListener: OnDisplayBoardItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterDisplayBoard.ViewHolder>() {

    var context: Context? = null
    val anim = AlphaAnimation(0.3f, 1.0f)

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
        return itemsEx.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initializes(itemsEx[position], clickListener)

        itemsEx[position].let { item ->
            with(holder) {
                if (!item.displayBoardDTO?.userUid.isNullOrEmpty()) {
                    val textColor = if (item.isBlocked) {
                        title.text = "${item.displayBoardDTO?.userNickname} : 내가 신고한 글입니다.".replace(" ", "\u00A0")
                        ContextCompat.getColor(context!!, R.color.text_disable)
                    } else {
                        title.text = "${item.displayBoardDTO?.userNickname} : ${item.displayBoardDTO?.displayText}".replace(" ", "\u00A0")
                        item.displayBoardDTO?.color!!
                    }

                    val nicknameLen = item.displayBoardDTO?.userNickname.toString().length + 3
                    //val displayLen = item.displayBoardDTO?.displayText.toString().length

                    val ssb = SpannableStringBuilder(title.text)
                    ssb.apply {
                        setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.white)), 0, nicknameLen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        setSpan(ForegroundColorSpan(textColor), nicknameLen, title.text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE )
                    }
                    title.text = ssb
                    title.startAnimation(anim)
                }
            }
        }
    }

    inner class ViewHolder(private val viewBinding: ListItemDisplayBoardBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var title = viewBinding.textTitle

        fun initializes(item: DisplayBoardExDTO, action: OnDisplayBoardItemClickListener) {
            viewBinding.layoutMain.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }
    }
}

interface OnDisplayBoardItemClickListener {
    fun onItemClick(item: DisplayBoardExDTO, position: Int)
}