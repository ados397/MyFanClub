package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.ReportDialogBinding
import com.ados.myfanclub.model.ReportDTO
import java.text.DecimalFormat
import java.util.*

class ReportDialog(context: Context) : Dialog(context), View.OnClickListener {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    lateinit var binding: ReportDialogBinding

    private val layout = R.layout.report_dialog

    var reportDTO: ReportDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ReportDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        binding.buttonReportCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonReportOk.setOnClickListener {
            when (binding.radioGroup.checkedRadioButtonId){
                R.id.radio_1 -> reportDTO?.reason = "스팸홍보/도배글입니다."
                R.id.radio_2 -> reportDTO?.reason = "음란물입니다."
                R.id.radio_3 -> reportDTO?.reason = "불법정보를 포함하고 있습니다."
                R.id.radio_4 -> reportDTO?.reason = "청소년에게 유해한 내용입니다."
                R.id.radio_5 -> reportDTO?.reason = "욕설/생명경시/혐오/차별적 표현입니다."
                R.id.radio_6 -> reportDTO?.reason = "개인정보 노출 게시물입니다."
                R.id.radio_7 -> reportDTO?.reason = "불쾌한 표현이 있습니다."
            }

            if (reportDTO?.reason.isNullOrEmpty()) {
                Toast.makeText(context, "신고 사유를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show()
            } else {
                reportDTO?.reportTime = Date()
                dismiss()
            }
        }
    }

    fun setInfo() {
        if (reportDTO != null) {
            binding.radioGroup.clearCheck()
            binding.textNickname.text = reportDTO?.toUserNickname
            binding.textContent.text = reportDTO?.content

            binding.textNicknameTitle.text = when (reportDTO?.type) {
                ReportDTO.Type.FanClub -> "팬클럽 : "
                ReportDTO.Type.Schedule -> "제목 : "
                else -> "작성자 : "
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