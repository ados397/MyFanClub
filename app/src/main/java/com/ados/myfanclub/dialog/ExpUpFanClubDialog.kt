package com.ados.myfanclub.dialog


import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.ExpUpFanClubDialogBinding
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.UserDTO
import java.text.DecimalFormat

class ExpUpFanClubDialog(context: Context) : Dialog(context), View.OnClickListener {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    lateinit var binding: ExpUpFanClubDialogBinding

    private val layout = R.layout.exp_up_fan_club_dialog

    var userDTO : UserDTO? = null
    var fanClubDTO : FanClubDTO? = null
    var addExp : Long? = 0L
    var gemCount : Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ExpUpFanClubDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var newExp = fanClubDTO?.exp?.plus(addExp!!)
        var nextExp = fanClubDTO?.getNextLevelExp()
        binding.textLevel.text = "Lv. ${fanClubDTO?.level}"
        binding.textExpOld.text = "${decimalFormat.format(fanClubDTO?.exp)}"
        binding.textExpNew.text = "${decimalFormat.format(newExp)}"
        binding.textExpAdd.text = "(+${decimalFormat.format(addExp)})"
        binding.textExpTotal.text = "0/${decimalFormat.format(nextExp)}"
        binding.textBonusExp.text = "사용자 레벨 경험치 보너스 +${userDTO?.level}"

        if (gemCount!! > 0) {
            binding.layoutGem.visibility = View.VISIBLE
            binding.textGemCount.text = "다이아 ${decimalFormat.format(gemCount!!)}개 사용"
        } else {
            binding.layoutGem.visibility = View.GONE
        }

        if (userDTO?.isPremium()!!) {
            binding.layoutPremium.visibility = View.VISIBLE
        } else {
            binding.layoutPremium.visibility = View.GONE
        }

        var percent = ((newExp?.toDouble()!! / nextExp!!) * 100).toInt()
        binding.progressPercent.progress = percent

        val valueAnim = ValueAnimator()
        valueAnim.setObjectValues(0, newExp.toInt())
        valueAnim.duration = 2000
        valueAnim.addUpdateListener {
            binding.textExpTotal.text = "${decimalFormat.format(it.animatedValue)}/${decimalFormat.format(nextExp)}"
        }
        valueAnim.start()

        ObjectAnimator.ofInt(binding.progressPercent, "progress", 0, percent).apply {
            duration = 2000
            start()
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