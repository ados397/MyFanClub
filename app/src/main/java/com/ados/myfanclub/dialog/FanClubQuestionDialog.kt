package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FanClubQuestionDialogBinding
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.MemberDTO
import java.text.DecimalFormat

class FanClubQuestionDialog(context: Context) : Dialog(context), View.OnClickListener {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    lateinit var binding: FanClubQuestionDialogBinding

    private val layout = R.layout.fan_club_question_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FanClubQuestionDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun setMemberPosition(member: MemberDTO, fanClub: FanClubDTO) {
        binding.textSubTitle.text = "팬클럽 알림"
        binding.textTitle.text = "팬클럽 등급 변경"
        binding.imgInfo.setImageResource(member.getPositionImage())
        binding.textInfo.text = "${member.getPositionString()} 등급이 되었습니다."
        binding.textContent.text = "[${fanClub.name}]"
        binding.textDescription.text = "팬클럽에서 확인해 보세요."
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

    override fun onBackPressed() {
        //super.onBackPressed()
    }
}