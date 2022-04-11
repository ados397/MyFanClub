package com.ados.myfanclub.page

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.ListItemFanClubMemberBinding
import com.ados.myfanclub.model.MemberDTO
import com.ados.myfanclub.model.MemberExDTO
import java.text.DecimalFormat

class RecyclerViewAdapterFanClubMember(private val itemsEx: ArrayList<MemberExDTO>, var clickListener: OnFanClubMemberItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterFanClubMember.ViewHolder>() {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")
    var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = ListItemFanClubMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemsEx.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initializes(itemsEx[position], clickListener)

        itemsEx[position].let { item ->
            with(holder) {
                if (item.isBlocked) {
                    name.text = "차단됨"
                    name.setTextColor(ContextCompat.getColor(context!!, R.color.text_disable2))
                    aboutMe.text = "내가 신고한 사용자 입니다."
                    aboutMe.setTextColor(ContextCompat.getColor(context!!, R.color.text_disable2))
                } else {
                    name.text = item.memberDTO?.userNickname
                    name.setTextColor(ContextCompat.getColor(context!!, R.color.text))
                    aboutMe.text = item.memberDTO?.userAboutMe
                    aboutMe.setTextColor(ContextCompat.getColor(context!!, R.color.text_sub))
                }

                level.text = "Lv. ${item.memberDTO?.userLevel}"
                contribution.text = "기여도 : ${item.memberDTO?.getDisplayContribution()}"

                imagePosition.setImageResource(item.memberDTO?.getPositionImage()!!)
                positionText.text = item.memberDTO?.getPositionString()

                imageCheckout.setImageResource(item.memberDTO?.getCheckoutImage()!!)

                when (item.memberDTO?.position) {
                    MemberDTO.Position.MASTER -> layoutPosition.setBackgroundColor(ContextCompat.getColor(context!!, R.color.master))
                    MemberDTO.Position.SUB_MASTER -> layoutPosition.setBackgroundColor(ContextCompat.getColor(context!!, R.color.sub_master))
                    MemberDTO.Position.MEMBER -> layoutPosition.setBackgroundColor(ContextCompat.getColor(context!!, R.color.member))
                    //MemberDTO.POSITION.MASTER -> layoutPosition.setBackgroundColor(Color.parseColor("#FFD500"))
                    //MemberDTO.POSITION.SUB_MASTER -> layoutPosition.setBackgroundColor(Color.parseColor("#FF8C00"))
                    //MemberDTO.POSITION.MEMBER -> layoutPosition.setBackgroundColor(Color.parseColor("#0099FF"))
                    else -> layoutPosition.setBackgroundColor(ContextCompat.getColor(context!!, R.color.member))
                }

                if (item.isSelected) {
                    mainLayout.setBackgroundColor(Color.parseColor("#BBD5F8"))
                } else {
                    mainLayout.setBackgroundColor(Color.parseColor("#FFFFFF"))
                }
            }
        }
    }

    // 이미 선택된 항목을 선택할 경우 선택을 해제하고 false 반환, 아닐경우 해당항목 선택 후 true 반환
    fun selectItem(position: Int) : Boolean {
        return if (itemsEx[position].isSelected) {
            itemsEx[position].isSelected = false
            notifyDataSetChanged()
            false
        } else {
            for (item in itemsEx) {
                item.isSelected = false
            }
            itemsEx[position].isSelected = true
            notifyDataSetChanged()
            true
        }
    }

    inner class ViewHolder(private val viewBinding: ListItemFanClubMemberBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var imagePosition = viewBinding.imgPosition
        var name = viewBinding.textName
        var level = viewBinding.textLevel
        var contribution = viewBinding.textContribution
        var positionText = viewBinding.textPosition
        var aboutMe = viewBinding.textAboutMe
        var mainLayout = viewBinding.layoutMain
        var imageCheckout = viewBinding.imgCheckout
        var layoutPosition = viewBinding.layoutPosition

        fun initializes(item: MemberExDTO, action:OnFanClubMemberItemClickListener) {
            viewBinding.layoutMain.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }
    }


}

interface OnFanClubMemberItemClickListener {
    fun onItemClick(item: MemberExDTO, position: Int)
}