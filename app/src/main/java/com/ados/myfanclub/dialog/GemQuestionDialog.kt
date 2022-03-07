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
import com.ados.myfanclub.databinding.GemQuestionDialogBinding
import com.ados.myfanclub.model.GemQuestionDTO
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
import java.text.DecimalFormat

class GemQuestionDialog(context: Context, var question: GemQuestionDTO) : Dialog(context), View.OnClickListener {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    lateinit var binding: GemQuestionDialogBinding
    var mainActivity: MainActivity? = null

    private val layout = R.layout.gem_question_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GemQuestionDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        observeTutorial()

        setInfo()
    }

    fun setInfo() {
        binding.textTitle.text = question.content
        binding.textCount.text = "${decimalFormat.format(question.gemCount)}"
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
            onTutorial(mainActivity?.getTutorialStep()?.value!!)
        }
    }

    private fun onTutorial(step: Int) {
        when (step) {
            16 -> {
                println("튜토리얼 Step - $step")
                TapTargetView.showFor(this,
                    TapTarget.forView(binding.buttonGemQuestionOk,
                        "튜토리얼 중에는 다이아가 소모되지 않습니다.",
                        "- 확인 버튼을 눌러주세요.")
                        .cancelable(false)
                        .dimColor(R.color.black)
                        .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                        .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                        .titleTextSize(18) // Specify the size (in sp) of the title text
                        .transparentTarget(true)
                        .targetRadius(65)
                        .tintTarget(true),object : TapTargetView.Listener() {
                        // The listener can listen for regular clicks, long clicks or cancels
                        override fun onTargetClick(view: TapTargetView) {
                            super.onTargetClick(view) // This call is optional

                            binding.buttonGemQuestionOk.performClick()
                            mainActivity?.addTutorialStep()
                        }
                    })
            }
        }
    }
}