package com.ados.myfanclub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import com.ados.myfanclub.databinding.ActivityJoinBinding
import com.ados.myfanclub.model.LogDTO
import com.ados.myfanclub.model.MailDTO
import com.ados.myfanclub.model.UserDTO
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*


class JoinActivity : AppCompatActivity() {
    private var _binding: ActivityJoinBinding? = null
    private val binding get() = _binding!!

    private var firebaseAuth : FirebaseAuth? = null
    private val firebaseViewModel : FirebaseViewModel by viewModels()

    private var emailOK: Boolean = false
    private var passwordOK: Boolean = false
    private var passwordConfirmOK: Boolean = false
    private var nicknameOK: Boolean = false

    private var currentUser: UserDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        currentUser = intent.getParcelableExtra("user")
        if (currentUser != null) { // null 이 아니라면 소셜 로그인, 이메일, 비밀번호는 입력하지 않음
            binding.editEmail.setText(currentUser?.userId)
            binding.editEmail.isEnabled = false
            emailOK = true

            binding.editPassword.setText("password_sample")
            binding.editPassword.isEnabled = false
            passwordOK = true

            binding.editPasswordConfirm.setText("password_sample")
            binding.editPasswordConfirm.isEnabled = false
            passwordConfirmOK = true
        }

        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonOk.setOnClickListener {
            var email = binding.editEmail.text.toString().trim()
            var nickname = binding.editNickname.text.toString().trim()
            var password = binding.editPassword.text.toString().trim()

            if (!verifyNickname(nickname)) {
                Toast.makeText(this, "사용할 수 없는 닉네임 입니다.", Toast.LENGTH_SHORT).show()
            } else {
                firebaseViewModel.findUserFromEmail(email) { userDTO ->
                    if (userDTO != null) {
                        Toast.makeText(this, "이미 가입된 이메일 입니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        firebaseViewModel.isUsedUserNickname(nickname) { isUsed ->
                            if (isUsed) {
                                Toast.makeText(this, "닉네임이 이미 존재합니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                var addUser = UserDTO()
                                addUser.userId = email
                                addUser.nickname = nickname
                                addUser.level = 1
                                addUser.exp = 0L
                                addUser.paidGem = 0
                                addUser.freeGem = 0
                                addUser.mainTitle = ""
                                addUser.aboutMe = ""
                                addUser.premiumExpireTime = Date()
                                addUser.createTime = Date()

                                if (currentUser != null) { // null 이 아니라면 소셜 로그인, 이미 로그인 처리는 되어 있음, firestore 데이터 기록 후 메인페이지 이동
                                    addUser.uid = firebaseAuth?.currentUser?.uid
                                    addUser.loginType = currentUser?.loginType
                                    writeFirestoreAndFinish(addUser, false)
                                } else {
                                    firebaseAuth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            firebaseAuth?.currentUser?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                                                    if (verifyTask.isSuccessful) {
                                                        addUser.uid = firebaseAuth?.currentUser?.uid
                                                        addUser.loginType = UserDTO.LoginType.EMAIL
                                                        writeFirestoreAndFinish(addUser, true)
                                                    } else {
                                                        Toast.makeText(this, "인증메일 발송에 실패하였습니다. 잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                        } else if (!task.exception?.message.isNullOrEmpty()) {
                                            Toast.makeText(this, "회원가입에 실패하였습니다. 잠시 후 다시 시도해 보세요.", Toast.LENGTH_SHORT).show()
                                            //Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.editEmail.doAfterTextChanged {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(binding.editEmail.text).matches()) {
                binding.textEmailError.text = "이메일 형식으로 입력해 주세요."
                binding.editEmail.setBackgroundResource(R.drawable.edit_rectangle_red)
                emailOK = false
            } else {
                binding.textEmailError.text = ""
                binding.editEmail.setBackgroundResource(R.drawable.edit_rectangle)
                emailOK = true
            }

            if (binding.editEmail.text.toString().isEmpty())
                emailOK = false

            visibleOkButton()
        }

        binding.editPassword.doAfterTextChanged {
            if (!isValidPassword(binding.editPassword.text.toString())) {
                binding.textPasswordError.text = "비밀번호는 6자 이상 숫자, 영문, 특수문자 중 2가지가 포함되어야 합니다."
                binding.editPassword.setBackgroundResource(R.drawable.edit_rectangle_red)
                passwordOK = false
            } else {
                binding.textPasswordError.text = ""
                binding.editPassword.setBackgroundResource(R.drawable.edit_rectangle)
                passwordOK = true
            }

            if (binding.editPassword.text.toString().isEmpty())
                passwordOK = false

            isValidPasswordConfirm()

            visibleOkButton()
        }

        binding.editPasswordConfirm.doAfterTextChanged {
            isValidPasswordConfirm()

            if (binding.editPasswordConfirm.text.toString().isEmpty())
                passwordConfirmOK = false

            visibleOkButton()
        }

        binding.editNickname.doAfterTextChanged {
            if (!isValidNickname(binding.editNickname.text.toString())) {
                binding.textNicknameError.text = "사용할 수 없는 문자열이 포함되어 있습니다."
                binding.editNickname.setBackgroundResource(R.drawable.edit_rectangle_red)
                nicknameOK = false
            } else {
                binding.textNicknameError.text = ""
                binding.editNickname.setBackgroundResource(R.drawable.edit_rectangle)
                nicknameOK = true
            }

            if (binding.editNickname.text.toString().isEmpty())
                nicknameOK = false

            binding.textNicknameLen.text = "${binding.editNickname.text.length}/15"

            visibleOkButton()
        }
    }

    private fun verifyNickname(nickname: String) : Boolean {
        return !(nickname == "마이팬클럽" ||
                nickname == "관리자" ||
                nickname == "운영자" ||
                nickname == "운영진" ||
                nickname.equals("admin", true) ||
                nickname.equals("administrator", true))
    }

    private fun writeFirestoreAndFinish(user: UserDTO, isEmailAuth: Boolean) {
        firebaseViewModel.updateUser(user) {
            val gemCount = 5
            val calendar= Calendar.getInstance()
            calendar.add(Calendar.DATE, 7)
            val docName = "master${System.currentTimeMillis()}"
            var mail = MailDTO(docName,"마이팬클럽 회원가입을 축하합니다!", "마이팬클럽에 오신걸 환영합니다!\n\n마이팬클럽에서 마음 맞는\n팬클럽원들과 함께\n나의 스타를 응원하며\n스마트한 '덕질 라이프'를 즐겨 보세요!\n\n\n회원가입을 축하하며\n소정의 축하 다이아를 드립니다.", "운영자", MailDTO.Item.FREE_GEM, gemCount, Date(), calendar.time)
            firebaseViewModel.sendUserMail(user.uid.toString(), mail) {
                var log = LogDTO("[회원가입] 축하 다이아 $gemCount 개 우편 발송, 유효기간 : ${SimpleDateFormat("yyyy.MM.dd HH:mm").format(calendar.time)}까지", Date())
                firebaseViewModel.writeUserLog(user.uid.toString(), log) { }
            }

            if (isEmailAuth) { // 이메일 회원가입 시 이메일 인증 후 로그인됨
                Toast.makeText(this, "회원가입을 위한 인증메일을 보냈습니다. 인증 후 로그인 해주세요.", Toast.LENGTH_LONG).show()
            } else { // 소셜 회원가입 시 바로 로그인됨
                Toast.makeText(this, "회원가입이 완료 되었습니다.", Toast.LENGTH_SHORT).show()
                var intent = Intent(this, MainActivity::class.java)
                intent.putExtra("user", user)
                startActivity(intent)
            }

            finish()
        }
    }

    private fun isValidPasswordConfirm() {
        if (binding.editPasswordConfirm.text.toString() != binding.editPassword.text.toString()) {
            binding.textPasswordConfirmError.text = "비밀번호가 일치하지 않습니다."
            binding.editPasswordConfirm.setBackgroundResource(R.drawable.edit_rectangle_red)
            passwordConfirmOK = false
        } else {
            binding.textPasswordConfirmError.text = ""
            binding.editPasswordConfirm.setBackgroundResource(R.drawable.edit_rectangle)
            passwordConfirmOK = true
        }
    }

    private fun isKorean(s: String): Boolean {
        var i = 0
        while (i < s.length) {
            val c = s.codePointAt(i)
            if (c in 0xAC00..0xD800)
                return true
            i += Character.charCount(c)
        }
        return false
    }


    private fun isValidPassword(password: String) : Boolean {
        if (password.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*\$".toRegex())) {
            return false
        }

        return password.matches("^(?=.*[a-zA-Z0-9])(?=.*[a-zA-Z!@#\$%^&*])(?=.*[0-9~!@#\$%^&*]).{6,30}\$".toRegex())
    }

    private fun isValidNickname(nickname: String) : Boolean {
        val exp = Regex("^[가-힣ㄱ-ㅎa-zA-Z0-9.~!@#\$%^&*\\[\\](){}|_-]{1,15}\$")
        return !nickname.isNullOrEmpty() && exp.matches(nickname)
    }

    private fun visibleOkButton() {
        binding.buttonOk.isEnabled = emailOK && passwordOK && passwordConfirmOK && nicknameOK
    }
}