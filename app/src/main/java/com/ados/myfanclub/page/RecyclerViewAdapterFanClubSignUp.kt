package com.ados.myfanclub.page

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.databinding.ListItemFanClubSignUpBinding
import com.ados.myfanclub.model.MemberDTO
import java.text.SimpleDateFormat

class RecyclerViewAdapterFanClubSignUp(private val items: ArrayList<MemberDTO>, var clickListener: OnFanClubSignUpItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterFanClubSignUp.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemFanClubSignUpBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initializes(items[position], clickListener)

        items[position].let { item ->
            with(holder) {
                checkBox.isChecked = item.isSelected
                name.text = item.userNickname
                level.text = "Lv. ${item.userLevel}"
                requestTime.text = SimpleDateFormat("yyyy.MM.dd HH:mm").format(item.requestTime)
                aboutMe.text = item.userAboutMe
            }
        }
    }

    // 체크된 항목이 하나라도 있으면 true 반환 함수
    fun isChecked() : Boolean {
        for (item in items) {
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

        fun initializes(item: MemberDTO, action:OnFanClubSignUpItemClickListener) {
            viewBinding.checkbox.setOnClickListener {
                item.isSelected = viewBinding.checkbox.isChecked
                action.onItemClick(item, adapterPosition)
            }
        }
    }


}

interface OnFanClubSignUpItemClickListener {
    fun onItemClick(item: MemberDTO, position: Int)
}