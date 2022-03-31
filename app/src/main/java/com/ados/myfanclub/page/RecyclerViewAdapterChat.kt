package com.ados.myfanclub.page

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.ListItemChatBinding
import com.ados.myfanclub.dialog.ImageViewDialog
import com.ados.myfanclub.model.DisplayBoardDTO
import com.ados.myfanclub.model.DisplayBoardExDTO
import com.ados.myfanclub.model.FanClubExDTO
import com.ados.myfanclub.model.MemberDTO
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class RecyclerViewAdapterChat(private val itemsEx: ArrayList<DisplayBoardExDTO>, private val memberDTO: MemberDTO, var clickListener: OnChatItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterChat.ViewHolder>() {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")
    var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = ListItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemsEx.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initializes(itemsEx[position], clickListener)

        itemsEx[position].let { item ->
            with(holder) {
                if (item.displayBoardDTO != null) {
                    if (item.displayBoardDTO.userUid.isNullOrEmpty()) {
                        layoutChatYou.visibility = View.GONE
                        layoutChatMe.visibility = View.GONE
                        layoutSystem.visibility = View.GONE

                        if (!item.displayBoardDTO.displayText.isNullOrEmpty()) {
                            layoutSystem.visibility = View.VISIBLE
                            contentSystem.text = item.displayBoardDTO.displayText
                        }
                    } else {
                        if (memberDTO.userUid == item.displayBoardDTO.userUid) { // 내가 입력한 채팅
                            layoutChatYou.visibility = View.GONE
                            layoutChatMe.visibility = View.VISIBLE
                            layoutSystem.visibility = View.GONE

                            contentMe.text = item.displayBoardDTO.displayText
                            dateMe.text = SimpleDateFormat("M월 d일").format(item.displayBoardDTO.createTime!!)
                            timeMe.text = SimpleDateFormat("a h:mm").format(item.displayBoardDTO.createTime!!)
                        } else { // 다른 사람이 입력한 채팅
                            layoutChatYou.visibility = View.VISIBLE
                            layoutChatMe.visibility = View.GONE
                            layoutSystem.visibility = View.GONE

                            if (item.imgProfileUri != null) {
                                Glide.with(imgProfile.context).load(item.imgProfileUri).fitCenter().into(holder.imgProfile)
                            } else {
                                imgProfile.setImageResource(R.drawable.profile)
                            }

                            nickname.text = item.displayBoardDTO.userNickname
                            if (item.isBlocked) {
                                contentYou.text = "내가 신고한 대화입니다."
                                contentYou.setTextColor(ContextCompat.getColor(context!!, R.color.text_disable2))
                                buttonReport.visibility = View.GONE
                            } else {
                                contentYou.text = item.displayBoardDTO.displayText
                                contentYou.setTextColor(ContextCompat.getColor(context!!, R.color.text))
                                buttonReport.visibility = View.VISIBLE
                            }

                            dateYou.text = SimpleDateFormat("M월 d일").format(item.displayBoardDTO.createTime!!)
                            timeYou.text = SimpleDateFormat("a h:mm").format(item.displayBoardDTO.createTime!!)
                        }
                    }
                }
            }
        }
    }

    fun updateProfile(position: Int, uri: Uri?) {
        itemsEx[position].imgProfileUri = uri
        notifyItemChanged(position)
    }

    inner class ViewHolder(private val viewBinding: ListItemChatBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var layoutChatYou = viewBinding.layoutChatYou
        var layoutChatMe = viewBinding.layoutChatMe
        var layoutSystem = viewBinding.layoutSystem
        var contentYou = viewBinding.textContentYou
        var contentMe = viewBinding.textContentMe
        var contentSystem = viewBinding.textContentSystem
        var dateYou = viewBinding.textDateYou
        var dateMe = viewBinding.textDateMe
        var timeYou = viewBinding.textTimeYou
        var timeMe = viewBinding.textTimeMe
        var nickname = viewBinding.textNickname
        var imgProfile = viewBinding.imgProfile
        var mainLayout = viewBinding.layoutMain
        var buttonReport = viewBinding.buttonReport
        private var imageViewDialog: ImageViewDialog? = null

        fun initializes(item: DisplayBoardExDTO, action:OnChatItemClickListener) {
            viewBinding.layoutMain.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }

            viewBinding.buttonReport.setOnClickListener {
                action.onItemClickReport(item, adapterPosition)
            }

            viewBinding.imgProfile.setOnClickListener {
                if (imageViewDialog == null) {
                    imageViewDialog = ImageViewDialog(context!!)
                    imageViewDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    imageViewDialog?.setCanceledOnTouchOutside(false)
                }
                imageViewDialog?.imageUri = item.imgProfileUri
                imageViewDialog?.imageID = R.drawable.profile
                imageViewDialog?.show()
                imageViewDialog?.setInfo()
                imageViewDialog?.binding?.buttonCancel?.setOnClickListener { // No
                    imageViewDialog?.dismiss()
                    imageViewDialog = null
                }
            }
        }
    }
}

interface OnChatItemClickListener {
    fun onItemClick(item: DisplayBoardExDTO, position: Int)
    fun onItemClickReport(item: DisplayBoardExDTO, position: Int)
}