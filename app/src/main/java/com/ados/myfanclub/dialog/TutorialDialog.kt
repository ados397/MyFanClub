package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.TutorialDialogBinding

class TutorialDialog(context: Context) : Dialog(context), View.OnClickListener {
    lateinit var binding: TutorialDialogBinding

    private val layout = R.layout.tutorial_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TutorialDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

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