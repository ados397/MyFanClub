package com.ados.myfanclub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import com.ados.myfanclub.databinding.ActivityFindPasswordBinding
import com.ados.myfanclub.model.UserDTO
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.google.firebase.auth.FirebaseAuth

class FindPasswordActivity : AppCompatActivity() {
    private var _binding: ActivityFindPasswordBinding? = null
    private val binding get() = _binding!!

    private var firebaseAuth : FirebaseAuth? = null
    private val firebaseViewModel : FirebaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFindPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonOk.setOnClickListener {
            val email = binding.editEmail.text.trim().toString()

            firebaseViewModel.findUserFromEmail(email) { userDTO ->
                if (userDTO != null) {
                    if (userDTO.loginType == UserDTO.LoginType.EMAIL) {
                        firebaseAuth?.sendPasswordResetEmail(email)?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "비밀번호 재설정 메일이 발송되었습니다.", Toast.LENGTH_LONG).show()
                            }
                            finish()
                        }?.addOnFailureListener {
                            Toast.makeText(this, "비밀번호 재설정 메일 발송에 실패하였습니다.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this, "소셜 로그인된 사용자는 비밀번호 찾기가 불가능합니다.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "가입되지 않은 이메일 입니다.", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.editEmail.doAfterTextChanged {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(binding.editEmail.text).matches()) {
                binding.textEmailError.text = "이메일 형식으로 입력해 주세요."
                binding.editEmail.setBackgroundResource(R.drawable.edit_rectangle_red)
                binding.buttonOk.isEnabled = false
            } else {
                binding.textEmailError.text = ""
                binding.editEmail.setBackgroundResource(R.drawable.edit_rectangle)
                binding.buttonOk.isEnabled = true
            }

            if (binding.editEmail.text.toString().isEmpty())
                binding.buttonOk.isEnabled = false
        }
    }
}