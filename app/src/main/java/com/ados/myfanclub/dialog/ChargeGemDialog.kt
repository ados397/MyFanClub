package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.ChargeGemDialogBinding
import java.text.DecimalFormat

class ChargeGemDialog(context: Context) : Dialog(context), View.OnClickListener {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    lateinit var binding: ChargeGemDialogBinding

    private val layout = R.layout.charge_gem_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ChargeGemDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)


        /*binding.layoutGemPackage3.imgGem.setImageResource(R.drawable.diamond_pack3)
        binding.layoutGemPackage3.textGemCount.text = "${decimalFormat.format(750)}\n + ${decimalFormat.format(250)} 다이아"
        binding.layoutGemPackage3.textPrice.text = "\\ ${decimalFormat.format(22000)}"

        binding.layoutGemPackage4.imgGem.setImageResource(R.drawable.diamond_pack4)
        binding.layoutGemPackage4.textGemCount.text = "${decimalFormat.format(2000)}\n + ${decimalFormat.format(650)} 다이아"
        binding.layoutGemPackage4.textPrice.text = "\\ ${decimalFormat.format(55000)}"*/
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