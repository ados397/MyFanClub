package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.LevelUpUserDialogBinding
import com.ados.myfanclub.model.UserDTO
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
import java.text.DecimalFormat
import kotlin.concurrent.thread

class LevelUpUserDialog(context: Context) : Dialog(context), View.OnClickListener {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    lateinit var binding: LevelUpUserDialogBinding
    var mainActivity: MainActivity? = null

    private val layout = R.layout.level_up_user_dialog

    var oldUserDTO : UserDTO? = null
    var newUserDTO : UserDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LevelUpUserDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        observeTutorial()

        setInfo()
    }

    fun setInfo() {
        binding.textLevel.text = "Lv. ${newUserDTO?.level}"
        binding.textScheduleOld.text = "${decimalFormat.format(oldUserDTO?.getScheduleCount())}"
        binding.textScheduleNew.text = "${decimalFormat.format(newUserDTO?.getScheduleCount())}"
        if (oldUserDTO?.getScheduleCount() != newUserDTO?.getScheduleCount()) {
            binding.textScheduleNew.setTextColor(ContextCompat.getColor(context, R.color.master))
            binding.imgScheduleUp.visibility = View.VISIBLE
        } else {
            binding.textScheduleNew.setTextColor(ContextCompat.getColor(context, R.color.white))
            binding.imgScheduleUp.visibility = View.GONE
        }

        binding.textCheckoutExpOld.text = "${decimalFormat.format(oldUserDTO?.level)}"
        binding.textCheckoutExpNew.text = "${decimalFormat.format(newUserDTO?.level)}"
        if (oldUserDTO?.level != newUserDTO?.level) {
            binding.textCheckoutExpNew.setTextColor(ContextCompat.getColor(context, R.color.master))
            binding.imgCheckoutExpUp.visibility = View.VISIBLE
        } else {
            binding.textCheckoutExpNew.setTextColor(ContextCompat.getColor(context, R.color.white))
            binding.imgCheckoutExpUp.visibility = View.GONE
        }

        binding.textDonationExpOld.text = "${decimalFormat.format(oldUserDTO?.level)}"
        binding.textDonationExpNew.text = "${decimalFormat.format(newUserDTO?.level)}"
        if (oldUserDTO?.level != newUserDTO?.level) {
            binding.textDonationExpNew.setTextColor(ContextCompat.getColor(context, R.color.master))
            binding.imgDonationExpUp.visibility = View.VISIBLE
        } else {
            binding.textDonationExpNew.setTextColor(ContextCompat.getColor(context, R.color.white))
            binding.imgDonationExpUp.visibility = View.GONE
        }

        addedOptionMessage()

        // 레벨업 보상
        binding.textItemCount.text = decimalFormat.format(newUserDTO?.getLevelUpGemCount())
    }

    private fun addedOptionMessage() {
        binding.textAddedOption.text = when (newUserDTO?.level) {
            3 -> "이제 팬클럽 가입이 가능합니다."
            5 -> "이제 전광판 등록이 가능합니다."
            7 -> "이제 팬클럽 창설이 가능합니다."
            15 -> "새로운 전광판 컬러를 사용가능합니다."
            25 -> "새로운 전광판 컬러를 사용가능합니다."
            35 -> "새로운 전광판 컬러를 사용가능합니다."
            45 -> "새로운 전광판 컬러를 사용가능합니다."
            55 -> "새로운 전광판 컬러를 사용가능합니다."
            65 -> "새로운 전광판 컬러를 사용가능합니다."
            75 -> "새로운 전광판 컬러를 사용가능합니다."
            85 -> "새로운 전광판 컬러를 사용가능합니다."
            95 -> "새로운 전광판 컬러를 사용가능합니다."
            else -> ""
        }

        if (binding.textAddedOption.text.isNullOrEmpty()) {
            binding.layoutAddedOption.visibility = View.GONE
        } else {
            binding.layoutAddedOption.visibility = View.VISIBLE
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

    private fun observeTutorial() {
        mainActivity?.getTutorialStep()?.observe(mainActivity!!) {
            thread(start = true) {
                Thread.sleep(100)
                mainActivity?.runOnUiThread {
                    onTutorial(mainActivity?.getTutorialStep()?.value!!)
                }
            }
        }
    }

    private fun onTutorial(step: Int) {
        when (step) {
            17 -> {
                println("튜토리얼 Step - $step")
                TapTargetSequence(this)
                    .targets(
                        TapTarget.forView(binding.layoutMain,
                            "축하합니다! 레벨업에 성공하셨습니다! 레벨이 오를때마다 팬클럽에서의 활동이 더욱 빛이 날 것입니다!",
                            "- OK 버튼을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(context, R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.imgItem,
                            "레벨업 할때마다 보상으로 다이아가 제공 됩니다. 레벨이 높아질수록 보상 다이아도 많아 집니다.",
                            "- 보상 다이아는 우편함에서 획득 가능합니다.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .targetRadius(70)
                            .tintTarget(true)).listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            binding.buttonLevelUpUserOk.performClick()
                            mainActivity?.addTutorialStep()
                        }
                        override fun onSequenceStep(tutorialStep: TapTarget, targetClicked: Boolean) {

                        }
                        override fun onSequenceCanceled(lastTarget: TapTarget) {
                        }
                    }).start()

            }
        }
    }
}