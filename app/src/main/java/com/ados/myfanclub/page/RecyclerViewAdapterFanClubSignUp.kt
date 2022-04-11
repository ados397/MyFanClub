package com.ados.myfanclub.page

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.databinding.ListItemFanClubSignUpBinding
import com.ados.myfanclub.model.MemberDTO
import com.ados.myfanclub.model.MemberExDTO
import java.text.SimpleDateFormat

class RecyclerViewAdapterFanClubSignUp(private val itemsEx: ArrayList<MemberExDTO>, var clickListener: OnFanClubSignUpItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterFanClubSignUp.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemFanClubSignUpBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemsEx.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initializes(itemsEx[position], clickListener)

        itemsEx[position].let { item ->
            with(holder) {
                checkBox.isChecked = item.isSelected
                name.text = item.memberDTO?.userNickname
                level.text = "Lv. ${item.memberDTO?.userLevel}"
                requestTime.text = SimpleDateFormat("yyyy.MM.dd HH:mm").format(item.memberDTO?.requestTime!!)
                aboutMe.text = item.memberDTO?.userAboutMe
            }
        }
    }

    // 체크 모두 해제
    fun releaseCheckAll() {
        for (item in itemsEx) {
           item.isSelected = false
        }
        notifyDataSetChanged()
    }

    // 체크된 항목이 하나라도 있으면 true 반환 함수
    fun isChecked() : Boolean {
        for (item in itemsEx) {
            if (item.isSelected) {
                return true
            }
        }
        return false
    }

    inner class ViewHolder(private val viewBinding: ListItemFanClubSignUpBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var checkBox = viewBinding.checkbox
        var name = viewBinding.textName
        var level = viewBinding.textLevel
        var requestTime = viewBinding.textRequestTime
        var aboutMe = viewBinding.textAboutMe
        var mainLayout = viewBinding.layoutMain

        fun initializes(item: MemberExDTO, action:OnFanClubSignUpItemClickListener) {
            viewBinding.checkbox.setOnClickListener {
                item.isSelected = viewBinding.checkbox.isChecked
                action.onItemClick(item, adapterPosition)
            }
        }
    }


}

interface OnFanClubSignUpItemClickListener {
    fun onItemClick(item: MemberExDTO, position: Int)
}