package com.ados.myfanclub.page

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.ListItemUserRankBinding
import com.ados.myfanclub.model.UserDTO
import com.ados.myfanclub.model.UserExDTO
import com.bumptech.glide.Glide
import java.text.DecimalFormat

class RecyclerViewAdapterUserRank(private val itemsEx: ArrayList<UserExDTO>, var clickListener: OnUserRankItemClickListener) : RecyclerView.Adapter<RecyclerViewAdapterUserRank.ViewHolder>() {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")
    var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = ListItemUserRankBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemsEx.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initializes(itemsEx[position], clickListener)

        itemsEx[position].let { item ->
            with(holder) {
                when (position) {
                    0 -> {
                        Glide.with(imgRank.context).asBitmap().load(R.drawable.award_01).fitCenter().into(holder.imgRank)
                        imgRank.visibility = View.VISIBLE
                        rank.visibility = View.GONE
                        cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.rank1))
                    }
                    1 -> {
                        Glide.with(imgRank.context).asBitmap().load(R.drawable.award_02).fitCenter().into(holder.imgRank)
                        imgRank.visibility = View.VISIBLE
                        rank.visibility = View.GONE
                        cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.rank2))
                    }
                    2 -> {
                        Glide.with(imgRank.context).asBitmap().load(R.drawable.award_03).fitCenter().into(holder.imgRank)
                        imgRank.visibility = View.VISIBLE
                        rank.visibility = View.GONE
                        cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.rank3))
                    }
                    else -> {
                        imgRank.visibility = View.GONE
                        rank.visibility = View.VISIBLE
                        rank.text = "${position+1}"
                        cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.white))
                        //cardView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                    }
                }

                if (item.imgProfileUri != null) {
                    Glide.with(imgProfile.context).load(item.imgProfileUri).fitCenter().into(holder.imgProfile)
                } else {
                    imgProfile.setImageResource(R.drawable.profile)
                }

                if (item.userDTO?.fanClubId != null) {
                    imgFanClub.visibility = View.VISIBLE
                } else {
                    imgFanClub.visibility = View.GONE
                }

                name.text = "${item.userDTO?.nickname}"
                level.text = "Lv. ${item.userDTO?.level}"
                aboutMe.text = item.userDTO?.aboutMe

                if (item.isSelected) {
                    mainLayout.setBackgroundColor(Color.parseColor("#BBD5F8"))
                } else {
                    mainLayout.setBackgroundColor(Color.parseColor("#FFFFFF"))
                }
            }
        }
    }

    fun updateProfile(position: Int, uri: Uri?) {
        itemsEx[position].imgProfileUri = uri
        notifyItemChanged(position)
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

    inner class ViewHolder(private val viewBinding: ListItemUserRankBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        var imgRank = viewBinding.imgRank
        var imgProfile = viewBinding.imgProfile
        var imgFanClub = viewBinding.imgFanClub
        var rank = viewBinding.textRank
        var name = viewBinding.textName
        var level = viewBinding.textLevel
        var aboutMe = viewBinding.textAboutMe
        var cardView = viewBinding.cardView
        var mainLayout = viewBinding.layoutMain

        fun initializes(item: UserExDTO, action:OnUserRankItemClickListener) {
            viewBinding.layoutMain.setOnClickListener {
                action.onItemClick(item, adapterPosition)
            }
        }
    }
}

interface OnUserRankItemClickListener {
    fun onItemClick(item: UserExDTO, position: Int)
}