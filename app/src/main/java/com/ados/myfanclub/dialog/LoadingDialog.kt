package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.LoadingDialogBinding
import com.bumptech.glide.Glide

class LoadingDialog(context: Context) : Dialog(context) {

    lateinit var binding: LoadingDialogBinding
    private val layout = R.layout.loading_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoadingDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        Glide.with(context).load(R.raw.loading).into(binding.imgLoading)
    }
}