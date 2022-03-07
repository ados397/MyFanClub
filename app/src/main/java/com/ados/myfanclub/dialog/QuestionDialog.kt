package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.QuestionDialogBinding
import com.ados.myfanclub.model.QuestionDTO

class QuestionDialog(context: Context, var question: QuestionDTO) : Dialog(context), View.OnClickListener {

    lateinit var binding: QuestionDialogBinding

    private val layout = R.layout.question_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = QuestionDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgQuestionOk.visibility = View.GONE
        binding.imgQuestionCancel.visibility = View.GONE

        binding.textTitle.text = question.title
        binding.textContent.text = question.content

        when(question.stat) {
            QuestionDTO.Stat.INFO -> binding.imgStat.setImageResource(R.drawable.information)
            QuestionDTO.Stat.WARNING -> binding.imgStat.setImageResource(R.drawable.warning)
            QuestionDTO.Stat.ERROR -> binding.imgStat.setImageResource(R.drawable.error)
        }

        if (!question.image.isNullOrEmpty()) {
            var imageID = context.resources.getIdentifier(question.image, "drawable", context.packageName)
            if (imageID != null) {
                binding.imgStat.setImageResource(imageID)
            }
        }
    }

    fun setButtonOk(name: String) {
        binding.textQuestionOk.text = name
    }

    fun setButtonCancel(name: String) {
        binding.textQuestionCancel.text = name
    }

    fun showButtonOk(visible: Boolean) {
        if (visible == true) {
            binding.buttonQuestionOk.visibility = View.VISIBLE
        } else {
            binding.buttonQuestionOk.visibility = View.GONE
        }
    }

    fun showButtonCancel(visible: Boolean) {
        if (visible == true) {
            binding.buttonQuestionCancel.visibility = View.VISIBLE
        } else {
            binding.buttonQuestionCancel.visibility = View.GONE
        }
    }

    fun showImgOk(visible: Boolean) {
        if (visible) {
            binding.imgQuestionOk.visibility = View.VISIBLE
        } else {
            binding.imgQuestionOk.visibility = View.GONE
        }
    }

    fun showImgCancel(visible: Boolean) {
        if (visible) {
            binding.imgQuestionCancel.visibility = View.VISIBLE
        } else {
            binding.imgQuestionCancel.visibility = View.GONE
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