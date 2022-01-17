package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.TermsOfUseDialogBinding


class TermsOfUseDialog(context: Context) : Dialog(context) {

    lateinit var binding: TermsOfUseDialogBinding
    private val layout = R.layout.terms_of_use_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TermsOfUseDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        //window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun init() {
        //button_ok.setOnClickListener(this)
    }
}