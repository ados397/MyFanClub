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
import com.ados.myfanclub.databinding.MaintenanceDialogBinding
import com.ados.myfanclub.model.NoticeDTO
import com.ados.myfanclub.model.UpdateDTO
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat

class MaintenanceDialog(context: Context, private val jobType: JobType) : Dialog(context), View.OnClickListener {
    enum class JobType {
        MAINTENANCE, // 서버 점검
        UPDATE_IMMEDIATE, // 필수 업데이트
        UPDATE_FLEXIBLE // 권장 업데이트
    }

    lateinit var binding: MaintenanceDialogBinding

    private val layout = R.layout.maintenance_dialog

    var updateDTO: UpdateDTO? = null
    var currentVersion = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MaintenanceDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.imgNotice.visibility = View.GONE
        binding.textContent.movementMethod = ScrollingMovementMethod.getInstance()
        if (updateDTO != null) {
            when (jobType) {
                JobType.MAINTENANCE -> {
                    binding.textMainTitle.text = "서버 점검"
                    binding.textTitle.text = updateDTO?.maintenanceTitle
                    binding.textContent.text = updateDTO?.maintenanceDesc?.replace("\\n","\n")
                    binding.buttonMaintenanceOk.text = "종료"

                    if (!updateDTO?.maintenanceImgUrl.isNullOrEmpty()) {
                        Glide.with(context).load(updateDTO?.maintenanceImgUrl).fitCenter().into(binding.imgNotice)
                        binding.imgNotice.visibility = View.VISIBLE
                    }

                    binding.textVersion.visibility = View.GONE
                    binding.buttonMaintenanceCancel.visibility = View.GONE
                }
                JobType.UPDATE_IMMEDIATE -> {
                    binding.textMainTitle.text = "필수 업데이트"
                    binding.textTitle.text = updateDTO?.minVersionTitle
                    binding.textContent.text = updateDTO?.minVersionDesc?.replace("\\n","\n")
                    binding.buttonMaintenanceOk.text = "업데이트"
                    binding.textVersion.text = "version : $currentVersion"

                    binding.textVersion.visibility = View.VISIBLE
                    binding.buttonMaintenanceCancel.visibility = View.GONE
                }
                JobType.UPDATE_FLEXIBLE -> {
                    binding.textMainTitle.text = "권장 업데이트"
                    binding.textTitle.text = updateDTO?.updateVersionTitle
                    binding.textContent.text = updateDTO?.updateVersionDesc?.replace("\\n","\n")
                    binding.buttonMaintenanceOk.text = "업데이트"
                    binding.textVersion.text = "version : $currentVersion"

                    binding.textVersion.visibility = View.VISIBLE
                    binding.buttonMaintenanceCancel.visibility = View.VISIBLE
                }
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

    override fun onBackPressed() {
        //super.onBackPressed()
    }
}