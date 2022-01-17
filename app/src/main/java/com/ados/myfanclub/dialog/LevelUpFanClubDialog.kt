package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.LevelUpFanClubDialogBinding
import com.ados.myfanclub.model.FanClubDTO
import java.text.DecimalFormat

class LevelUpFanClubDialog(context: Context) : Dialog(context), View.OnClickListener {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    lateinit var binding: LevelUpFanClubDialogBinding

    private val layout = R.layout.level_up_fan_club_dialog

    var oldFanClubDTO : FanClubDTO? = null
    var newFanClubDTO : FanClubDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LevelUpFanClubDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.textLevel.text = "Lv. ${newFanClubDTO?.level}"
        binding.textMembersOld.text = "${decimalFormat.format(oldFanClubDTO?.getMaxMemberCount())}"
        binding.textMembersNew.text = "${decimalFormat.format(newFanClubDTO?.getMaxMemberCount())}"
        binding.textMembersNew.setTextColor(ContextCompat.getColor(context, R.color.master))
        binding.imgMembersUp.visibility = View.VISIBLE

        binding.textScheduleOld.text = "${decimalFormat.format(oldFanClubDTO?.getScheduleCount())}"
        binding.textScheduleNew.text = "${decimalFormat.format(newFanClubDTO?.getScheduleCount())}"
        if (oldFanClubDTO?.getScheduleCount() != newFanClubDTO?.getScheduleCount()) {
            binding.textScheduleNew.setTextColor(ContextCompat.getColor(context, R.color.master))
            binding.imgScheduleUp.visibility = View.VISIBLE
        } else {
            binding.textScheduleNew.setTextColor(ContextCompat.getColor(context, R.color.white))
            binding.imgScheduleUp.visibility = View.GONE
        }

        binding.textCheckoutGemOld.text = "${decimalFormat.format(oldFanClubDTO?.getCheckoutGemCount())}"
        binding.textCheckoutGemNew.text = "${decimalFormat.format(newFanClubDTO?.getCheckoutGemCount())}"
        if (oldFanClubDTO?.getCheckoutGemCount() != newFanClubDTO?.getCheckoutGemCount()) {
            binding.textCheckoutGemNew.setTextColor(ContextCompat.getColor(context, R.color.master))
            binding.imgCheckoutGemUp.visibility = View.VISIBLE
        } else {
            binding.textCheckoutGemNew.setTextColor(ContextCompat.getColor(context, R.color.white))
            binding.imgCheckoutGemUp.visibility = View.GONE
        }

        if (oldFanClubDTO?.getCheckoutGemCount() != newFanClubDTO?.getCheckoutGemCount()) {
            binding.layoutAddedOption.visibility = View.VISIBLE
            binding.textAddedOption.text = "더욱 많은 출석 보상 획득이 가능합니다."
        } else {
            binding.layoutAddedOption.visibility = View.VISIBLE
            binding.textAddedOption.text = "더욱 많은 클럽 멤버 모집이 가능합니다."
        }

        // 레벨업 보상
        //binding.textItemCount.text = decimalFormat.format(newUserDTO?.getLevelUpGemCount())
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