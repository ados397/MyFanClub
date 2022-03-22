package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.SendNoticeDialogBinding
import com.ados.myfanclub.model.*


class SendNoticeDialog(context: Context) : Dialog(context) {

    lateinit var binding: SendNoticeDialogBinding
    private val layout = R.layout.send_notice_dialog

    var currentUser: UserDTO? = null
    var fanClubDTO: FanClubDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SendNoticeDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        //window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setInfo()

        binding.editNoticeContent.setOnTouchListener { _, _ ->
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
            false
        }

        binding.editNoticeTitle.doAfterTextChanged {
            if (binding.editNoticeTitle.text.toString().trim().isNullOrEmpty()) {
                binding.textNoticeTitleError.text = "제목을 입력해 주세요."
                binding.editNoticeTitle.setBackgroundResource(R.drawable.edit_rectangle_red)
            } else {
                binding.textNoticeTitleError.text = ""
                binding.editNoticeTitle.setBackgroundResource(R.drawable.edit_rectangle)
            }

            binding.textNoticeTitleLen.text = "${binding.editNoticeTitle.text.length}/30"
        }

        binding.editNoticeContent.doAfterTextChanged {
            if (binding.editNoticeContent.text.toString().trim().isNullOrEmpty()) {
                binding.textNoticeContentError.text = "내용을 입력해 주세요."
                binding.editNoticeContent.setBackgroundResource(R.drawable.edit_rectangle_red)
            } else {
                binding.textNoticeContentError.text = ""
                binding.editNoticeContent.setBackgroundResource(R.drawable.edit_rectangle)
            }

            binding.textNoticeContentLen.text = "${binding.editNoticeContent.text.length}/100"
        }
    }

    fun setInfo() {
        binding.editNoticeTitle.setText("")
        binding.editNoticeContent.setText("")
    }

    private fun init() {
        //button_ok.setOnClickListener(this)
    }

}