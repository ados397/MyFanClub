package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.GemQuestionDialogBinding
import com.ados.myfanclub.model.GemQuestionDTO
import java.text.DecimalFormat

class GemQuestionDialog(context: Context, var question: GemQuestionDTO) : Dialog(context), View.OnClickListener {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    lateinit var binding: GemQuestionDialogBinding

    private val layout = R.layout.gem_question_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GemQuestionDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.textTitle.text = question.content
        binding.textCount.text = "${decimalFormat.format(question.gemCount)}"
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