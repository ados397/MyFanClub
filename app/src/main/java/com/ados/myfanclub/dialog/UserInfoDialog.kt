package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.UserInfoDialogBinding
import com.ados.myfanclub.model.MemberDTO
import com.ados.myfanclub.model.UserExDTO
import com.bumptech.glide.Glide
import java.text.DecimalFormat

class UserInfoDialog(context: Context, var member: MemberDTO, var user: UserExDTO) : Dialog(context), View.OnClickListener {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    lateinit var binding: UserInfoDialogBinding

    private val layout = R.layout.user_info_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserInfoDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (user.imgProfileUri != null) {
            Glide.with(context).load(user.imgProfileUri).fitCenter().into(binding.imgProfile)
        } else {
            binding.imgProfile.setImageResource(R.drawable.profile)
        }

        binding.textName.text = user?.userDTO?.nickname
        binding.textLevel.text = "Lv. ${user?.userDTO?.level}"

        binding.imgPosition.setImageResource(member?.getPositionImage()!!)
        binding.textPosition.text = member?.getPositionString()
        binding.textContribution.text = "기여도 : ${decimalFormat.format(member?.contribution)}"
        binding.imgCheckout.setImageResource(member?.getCheckoutImage()!!)

        binding.editAboutMe.setText(user?.userDTO?.aboutMe)
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