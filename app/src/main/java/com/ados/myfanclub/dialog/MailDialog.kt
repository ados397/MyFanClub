package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.MailDialogBinding
import com.ados.myfanclub.model.MailDTO
import java.text.DecimalFormat

class MailDialog(context: Context) : Dialog(context), View.OnClickListener {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    lateinit var binding: MailDialogBinding

    private val layout = R.layout.mail_dialog

    var mailDTO: MailDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MailDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        setInfo()
    }

    fun setInfo() {
        binding.textTitle.text = mailDTO?.title
        binding.textFrom.text = "발신 : ${mailDTO?.from}"
        binding.textContent.text = mailDTO?.content

        val interval = ((mailDTO?.expireTime?.time!!.toLong()) - System.currentTimeMillis()) / 1000
        val day = interval / 86400
        val hour = (interval % 86400) / 3600
        val min = ((interval % 86400) % 3600) / 60
        //val sec = interval % 60

        println("남은 시간 $day, $hour, $min")

        when {
            day > 10 -> binding.textDate.text = "${day}일 남음"
            day > 0 -> binding.textDate.text = "${day}일 ${hour}시간 남음"
            hour > 0 -> binding.textDate.text = "${hour}시간 ${min}분 남음"
            min > 0 -> binding.textDate.text = "${min}분 남음"
        }

        when (mailDTO?.item) {
            MailDTO.Item.NONE -> {
                binding.layoutItem.visibility = View.GONE
                binding.buttonGet.text = "삭제"
            }
            MailDTO.Item.PAID_GEM, MailDTO.Item.FREE_GEM -> {
                binding.layoutItem.visibility = View.VISIBLE
                binding.buttonGet.text = "받기"
                binding.imgItem.setImageResource(R.drawable.diamond)
                binding.textItemCount.text = "${decimalFormat.format(mailDTO?.itemCount)}"
            }
            else -> binding.layoutItem.visibility = View.GONE
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