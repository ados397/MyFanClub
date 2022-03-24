package com.ados.myfanclub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.Window
import android.widget.Toast
import com.ados.myfanclub.databinding.ActivityLoginEmailVerifyBinding
import com.ados.myfanclub.dialog.LoadingDialog
import com.ados.myfanclub.dialog.QuestionDialog
import com.ados.myfanclub.model.QuestionDTO
import com.google.firebase.auth.FirebaseAuth

class LoginEmailVerifyActivity : AppCompatActivity() {
    private var _binding: ActivityLoginEmailVerifyBinding? = null
    private val binding get() = _binding!!

    private var firebaseAuth : FirebaseAuth? = null

    private var loadingDialog : LoadingDialog? = null
    private var questionDialog: QuestionDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginEmailVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val email = intent.getStringExtra("email")
        val password = intent.getStringExtra("password")

        binding.textEmail.text = email

        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonSendEmail.setOnClickListener {
            val question = QuestionDTO(
                QuestionDTO.Stat.INFO,
                "인증메일 재전송",
                "${email}로 인증메일을 재전송 하시겠습니까?"
            )
            if (questionDialog == null) {
                questionDialog = QuestionDialog(this, question)
                questionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                questionDialog?.setCanceledOnTouchOutside(false)
            } else {
                questionDialog?.question = question
            }
            questionDialog?.show()
            questionDialog?.setInfo()
            questionDialog?.binding?.buttonQuestionCancel?.setOnClickListener { // No
                questionDialog?.dismiss()
                questionDialog = null
            }
            questionDialog?.binding?.buttonQuestionOk?.setOnClickListener { // Ok
                questionDialog?.dismiss()
                questionDialog = null

                loading()
                firebaseAuth?.signInWithEmailAndPassword(email!!, password!!)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseAuth?.currentUser?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                            firebaseAuth?.signOut()
                            if (verifyTask.isSuccessful) {
                                loadingEnd()
                                val question2 = QuestionDTO(
                                    QuestionDTO.Stat.INFO,
                                    "인증메일 재전송 완료",
                                    "${email}로 인증메일을 재전송 하였습니다.",
                                )
                                val dialog = QuestionDialog(this, question2)
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                                dialog.setCanceledOnTouchOutside(false)
                                dialog.show()
                                dialog.showButtonOk(false)
                                dialog.setButtonCancel("확인")
                                dialog.binding.buttonQuestionCancel.setOnClickListener { // No
                                    dialog.dismiss()
                                    finish()
                                }
                            } else {
                                loadingEnd()
                                Toast.makeText(this, "인증메일 발송에 실패하였습니다. 잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        loadingEnd()
                        Toast.makeText(this, "인증메일 발송에 실패하였습니다. 잠시 후 다시 시도해 주세요", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loading() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(this)
            loadingDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            loadingDialog?.setCanceledOnTouchOutside(false)
        }
        loadingDialog?.show()
    }

    private fun loadingEnd() {
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            if (loadingDialog != null) {
                loadingDialog?.dismiss()
            }
        }, 400)
    }
}