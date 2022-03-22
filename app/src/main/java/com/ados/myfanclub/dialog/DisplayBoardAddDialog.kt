package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.DisplayBoardAddDialogBinding
import com.ados.myfanclub.model.*

class DisplayBoardAddDialog(context: Context) : Dialog(context) {
    lateinit var binding: DisplayBoardAddDialogBinding
    private val layout = R.layout.display_board_add_dialog

    var currentUser: UserDTO? = null

    val anim = AlphaAnimation(0.1f, 1.0f)

    private var toast : Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DisplayBoardAddDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        anim.duration = 800
        anim.startOffset = 20
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        setInfo()

        binding.editDisplayBoard.doAfterTextChanged {
            binding.layoutDisplayBoardTest.textDisplayBoard.text = binding.editDisplayBoard.text
            binding.textDisplayBoardLen.text = "${binding.editDisplayBoard.text.length}/40"
        }

        binding.layoutDisplayBoardTest.textDisplayBoard.setOnFocusChangeListener { _, b ->
            if (!b) {
                binding.layoutDisplayBoardTest.textDisplayBoard.clearAnimation()
            }
        }

        binding.buttonPreview.setOnClickListener {
            binding.layoutDisplayBoardTest.textDisplayBoard.requestFocus()
            binding.layoutDisplayBoardTest.textDisplayBoard.startAnimation(anim)
        }

        binding.buttonColor1.setOnClickListener { clickColorButton(5, ContextCompat.getColor(context, R.color.display_board_1)) }
        binding.buttonColor2.setOnClickListener { clickColorButton(15, ContextCompat.getColor(context, R.color.display_board_2)) }
        binding.buttonColor3.setOnClickListener { clickColorButton(25, ContextCompat.getColor(context, R.color.display_board_3)) }
        binding.buttonColor4.setOnClickListener { clickColorButton(35, ContextCompat.getColor(context, R.color.display_board_4)) }
        binding.buttonColor5.setOnClickListener { clickColorButton(45, ContextCompat.getColor(context, R.color.display_board_5)) }
        binding.buttonColor6.setOnClickListener { clickColorButton(55, ContextCompat.getColor(context, R.color.display_board_6)) }
        binding.buttonColor7.setOnClickListener { clickColorButton(65, ContextCompat.getColor(context, R.color.display_board_7)) }
        binding.buttonColor8.setOnClickListener { clickColorButton(75, ContextCompat.getColor(context, R.color.display_board_8)) }
        binding.buttonColor9.setOnClickListener { clickColorButton(85, ContextCompat.getColor(context, R.color.display_board_9)) }
        binding.buttonColor10.setOnClickListener { clickColorButton(95, ContextCompat.getColor(context, R.color.display_board_10)) }
    }

    fun setInfo() {
        binding.layoutDisplayBoardTest.textDisplayBoard.text = "전광판 테스트"
        binding.layoutDisplayBoardTest.textDisplayBoard.setTextColor(ContextCompat.getColor(context, R.color.display_board_1))

        if (currentUser?.level!! >= 5) { binding.buttonColor1.setCompoundDrawables(null, null, null, null) }
        if (currentUser?.level!! >= 15) { binding.buttonColor2.setCompoundDrawables(null, null, null, null) }
        if (currentUser?.level!! >= 25) { binding.buttonColor3.setCompoundDrawables(null, null, null, null) }
        if (currentUser?.level!! >= 35) { binding.buttonColor4.setCompoundDrawables(null, null, null, null) }
        if (currentUser?.level!! >= 45) { binding.buttonColor5.setCompoundDrawables(null, null, null, null) }
        if (currentUser?.level!! >= 55) { binding.buttonColor6.setCompoundDrawables(null, null, null, null) }
        if (currentUser?.level!! >= 65) { binding.buttonColor7.setCompoundDrawables(null, null, null, null) }
        if (currentUser?.level!! >= 75) { binding.buttonColor8.setCompoundDrawables(null, null, null, null) }
        if (currentUser?.level!! >= 85) { binding.buttonColor9.setCompoundDrawables(null, null, null, null) }
        if (currentUser?.level!! >= 95) { binding.buttonColor10.setCompoundDrawables(null, null, null, null) }
    }

    private fun clickColorButton(level: Int, color: Int) {
        if (currentUser?.level!! >= level) {
            binding.layoutDisplayBoardTest.textDisplayBoard.setTextColor(color)
        } else {
            var toastText = "레벨 [ $level ] 달성 시 사용 가능합니다."
            if (toast == null) {
                toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT)
            } else {
                toast?.setText(toastText)
            }
            toast?.show()
        }
    }

    private fun init() {
        //button_ok.setOnClickListener(this)
    }

}