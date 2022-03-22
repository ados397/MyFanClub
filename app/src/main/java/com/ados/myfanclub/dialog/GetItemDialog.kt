package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.GetItemDialogBinding
import com.ados.myfanclub.model.MailDTO
import java.text.DecimalFormat

class GetItemDialog(context: Context) : Dialog(context), View.OnClickListener {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    lateinit var binding: GetItemDialogBinding

    private val layout = R.layout.get_item_dialog

    var mailDTO: MailDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GetItemDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setInfo()
    }

    fun setInfo() {
        when (mailDTO?.item) {
            MailDTO.Item.NONE -> {
                binding.imgItem.visibility = View.GONE
                binding.textItemCount.text = "${decimalFormat.format(mailDTO?.itemCount)}"
            }
            MailDTO.Item.PAID_GEM, MailDTO.Item.FREE_GEM -> {
                binding.imgItem.visibility = View.VISIBLE
                binding.imgItem.setImageResource(R.drawable.diamond)
                binding.textItemCount.text = "${decimalFormat.format(mailDTO?.itemCount)}"
            }
            else -> binding.imgItem.visibility = View.GONE
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