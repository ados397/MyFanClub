package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.NoticeDialogBinding
import com.ados.myfanclub.model.NoticeDTO
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat

class NoticeDialog(context: Context) : Dialog(context), View.OnClickListener {
    lateinit var binding: NoticeDialogBinding

    private val layout = R.layout.notice_dialog

    var noticeDTO: NoticeDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NoticeDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.textContent.movementMethod = ScrollingMovementMethod.getInstance()
        binding.textNoticeLink.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        if (noticeDTO != null) {
            binding.textTitle.text = noticeDTO?.title
            binding.textTime.text = SimpleDateFormat("yyyy.MM.dd HH:mm").format(noticeDTO?.time!!)
            binding.textContent.text = noticeDTO?.content?.replace("\\n","\n")

            if (!noticeDTO?.imageUrl.isNullOrEmpty()) {
                Glide.with(context).load(noticeDTO?.imageUrl).fitCenter().into(binding.imgNotice)
                binding.imgNotice.visibility = View.VISIBLE
            } else {
                binding.imgNotice.visibility = View.GONE
            }
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