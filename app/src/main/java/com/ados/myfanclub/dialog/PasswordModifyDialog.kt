package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.PasswordModifyDialogBinding
import com.ados.myfanclub.model.EditTextDTO

class PasswordModifyDialog(context: Context) : Dialog(context), View.OnClickListener {

    lateinit var binding: PasswordModifyDialogBinding

    private val layout = R.layout.edit_text_modify_dialog

    private var passwordOldOK: Boolean = false
    private var passwordNewOK: Boolean = false
    private var passwordConfirmOK: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PasswordModifyDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonPasswordModifyOk.isEnabled = false
        binding.imgModifyOk.visibility = View.GONE
        binding.imgModifyCancel.visibility = View.GONE

        binding.editPasswordOld.doAfterTextChanged {
            passwordOldOK = !binding.editPasswordOld.text.isNullOrEmpty()

            visibleOkButton()
        }

        binding.editPasswordNew.doAfterTextChanged {
            if (!isValidPassword(binding.editPasswordNew.text.toString())) {
                binding.textPasswordNewError.text = "비밀번호는 6자 이상 숫자, 영문, 특수문자 중 2가지가 포함되어야 합니다."
                binding.editPasswordNew.setBackgroundResource(R.drawable.edit_rectangle_red)
                passwordNewOK = false
            } else {
                binding.textPasswordNewError.text = ""
                binding.editPasswordNew.setBackgroundResource(R.drawable.edit_rectangle)
                passwordNewOK = true
            }

            if (binding.editPasswordNew.text.toString().isEmpty())
                passwordNewOK = false

            isValidPasswordConfirm()

            visibleOkButton()
        }

        binding.editPasswordConfirm.doAfterTextChanged {
            isValidPasswordConfirm()

            if (binding.editPasswordConfirm.text.toString().isEmpty())
                passwordConfirmOK = false

            visibleOkButton()
        }
    }

    private fun isValidPassword(password: String) : Boolean {
        if (password.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*\$".toRegex())) {
            return false
        }

        return password.matches("^(?=.*[a-zA-Z0-9])(?=.*[a-zA-Z!@#\$%^&*])(?=.*[0-9~!@#\$%^&*]).{6,30}\$".toRegex())
    }

    private fun visibleOkButton() {
        println("passwordOldOK $passwordOldOK, passwordNewOK $passwordNewOK, passwordConfirmOK $passwordConfirmOK")
        binding.buttonPasswordModifyOk.isEnabled = passwordOldOK && passwordNewOK && passwordConfirmOK
        if (binding.buttonPasswordModifyOk.isEnabled) {
            binding.textModifyOk.setTextColor(ContextCompat.getColor(context, R.color.text))
        } else {
            binding.textModifyOk.setTextColor(ContextCompat.getColor(context, R.color.text_disable))
        }
    }

    private fun isValidPasswordConfirm() {
        if (binding.editPasswordConfirm.text.toString() != binding.editPasswordNew.text.toString()) {
            binding.textPasswordConfirmError.text = "비밀번호가 일치하지 않습니다."
            binding.editPasswordConfirm.setBackgroundResource(R.drawable.edit_rectangle_red)
            passwordConfirmOK = false
        } else {
            binding.textPasswordConfirmError.text = ""
            binding.editPasswordConfirm.setBackgroundResource(R.drawable.edit_rectangle)
            passwordConfirmOK = true
        }
    }

    fun setButtonOk(name: String) {
        binding.textModifyOk.text = name
    }

    fun setButtonCancel(name: String) {
        binding.textModifyCancel.text = name
    }

    fun showButtonOk(visible: Boolean) {
        if (visible) {
            binding.buttonPasswordModifyOk.visibility = View.VISIBLE
        } else {
            binding.buttonPasswordModifyOk.visibility = View.GONE
        }
    }

    fun showButtonCancel(visible: Boolean) {
        if (visible) {
            binding.buttonPasswordModifyCancel.visibility = View.VISIBLE
        } else {
            binding.buttonPasswordModifyCancel.visibility = View.GONE
        }
    }

    fun showImgOk(visible: Boolean) {
        if (visible) {
            binding.imgModifyOk.visibility = View.VISIBLE
        } else {
            binding.imgModifyOk.visibility = View.GONE
        }
    }

    fun showImgCancel(visible: Boolean) {
        if (visible) {
            binding.imgModifyCancel.visibility = View.VISIBLE
        } else {
            binding.imgModifyCancel.visibility = View.GONE
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