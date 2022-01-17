package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.LevelUpUserDialogBinding
import com.ados.myfanclub.model.UserDTO
import java.text.DecimalFormat

class LevelUpUserDialog(context: Context) : Dialog(context), View.OnClickListener {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    lateinit var binding: LevelUpUserDialogBinding

    private val layout = R.layout.level_up_user_dialog

    var oldUserDTO : UserDTO? = null
    var newUserDTO : UserDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LevelUpUserDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.textLevel.text = "Lv. ${newUserDTO?.level}"
        binding.textScheduleOld.text = "${decimalFormat.format(oldUserDTO?.getScheduleCount())}"
        binding.textScheduleNew.text = "${decimalFormat.format(newUserDTO?.getScheduleCount())}"
        if (oldUserDTO?.getScheduleCount() != newUserDTO?.getScheduleCount()) {
            binding.textScheduleNew.setTextColor(ContextCompat.getColor(context, R.color.master))
            binding.imgScheduleUp.visibility = View.VISIBLE
        } else {
            binding.textScheduleNew.setTextColor(ContextCompat.getColor(context, R.color.white))
            binding.imgScheduleUp.visibility = View.GONE
        }

        binding.textCheckoutExpOld.text = "${decimalFormat.format(oldUserDTO?.level)}"
        binding.textCheckoutExpNew.text = "${decimalFormat.format(newUserDTO?.level)}"
        if (oldUserDTO?.level != newUserDTO?.level) {
            binding.textCheckoutExpNew.setTextColor(ContextCompat.getColor(context, R.color.master))
            binding.imgCheckoutExpUp.visibility = View.VISIBLE
        } else {
            binding.textCheckoutExpNew.setTextColor(ContextCompat.getColor(context, R.color.white))
            binding.imgCheckoutExpUp.visibility = View.GONE
        }

        binding.textDonationExpOld.text = "${decimalFormat.format(oldUserDTO?.level)}"
        binding.textDonationExpNew.text = "${decimalFormat.format(newUserDTO?.level)}"
        if (oldUserDTO?.level != newUserDTO?.level) {
            binding.textDonationExpNew.setTextColor(ContextCompat.getColor(context, R.color.master))
            binding.imgDonationExpUp.visibility = View.VISIBLE
        } else {
            binding.textDonationExpNew.setTextColor(ContextCompat.getColor(context, R.color.white))
            binding.imgDonationExpUp.visibility = View.GONE
        }

        addedOptionMessage()

        // 레벨업 보상
        binding.textItemCount.text = decimalFormat.format(newUserDTO?.getLevelUpGemCount())
    }

    private fun addedOptionMessage() {
        binding.textAddedOption.text = when (newUserDTO?.level) {
            3 -> "이제 팬클럽 가입이 가능합니다."
            5 -> "이제 전광판 등록이 가능합니다."
            7 -> "이제 팬클럽 창설이 가능합니다."
            15 -> "새로운 전광판 컬러를 사용가능합니다."
            25 -> "새로운 전광판 컬러를 사용가능합니다."
            35 -> "새로운 전광판 컬러를 사용가능합니다."
            45 -> "새로운 전광판 컬러를 사용가능합니다."
            55 -> "새로운 전광판 컬러를 사용가능합니다."
            65 -> "새로운 전광판 컬러를 사용가능합니다."
            75 -> "새로운 전광판 컬러를 사용가능합니다."
            85 -> "새로운 전광판 컬러를 사용가능합니다."
            95 -> "새로운 전광판 컬러를 사용가능합니다."
            else -> ""
        }

        if (binding.textAddedOption.text.isNullOrEmpty()) {
            binding.layoutAddedOption.visibility = View.GONE
        } else {
            binding.layoutAddedOption.visibility = View.VISIBLE
        }
    }

    private fun init() {
        //button_ok.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        /*when (v.id) {
            R.id.button_ok -> {
                dismiss()
            }
        }*/
    }
}