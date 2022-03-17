package com.ados.myfanclub.page

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.ados.myfanclub.LoginActivity
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentAccountDeleteAccountBinding
import com.ados.myfanclub.dialog.QuestionDialog
import com.ados.myfanclub.model.QuestionDTO
import com.ados.myfanclub.model.UserExDTO
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.question_dialog.*
import java.text.DecimalFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentAccountDeleteAccount.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentAccountDeleteAccount : Fragment() {
    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentAccountDeleteAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private var firebaseAuth : FirebaseAuth? = null
    private var googleSignInClient : GoogleSignInClient? = null

    private var currentUserEx: UserExDTO? = null

    inner class CheckboxListener : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            when (buttonView?.id) {
                R.id.checkbox_explain_1, R.id.checkbox_explain_2, R.id.checkbox_explain_3, R.id.checkbox_explain_last -> checkValid()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAccountDeleteAccountBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        firebaseAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //기본 로그인 방식 사용
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context,gso)

        currentUserEx = (activity as MainActivity?)?.getUserEx()

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textGemCount.text = "${decimalFormat.format(currentUserEx?.userDTO?.paidGem!! + currentUserEx?.userDTO?.freeGem!!)} 다이아"

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.buttonOk.setOnClickListener {
            val question = QuestionDTO(
                QuestionDTO.Stat.ERROR,
                "회원 탈퇴",
                "회원 탈퇴 시 모든 정보가 삭제되며 복구가 불가능 합니다.\n\n정말 탈퇴 하시겠습니까?"
            )
            val dialog = QuestionDialog(requireContext(), question)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.setButtonOk("탈퇴하기")
            dialog.button_question_ok.setOnClickListener { // Yes
                currentUserEx?.userDTO?.deleteTime = Date()
                firebaseViewModel.updateUserDeleteTime(currentUserEx?.userDTO!!) {
                    firebaseAuth?.signOut()
                    //Auth.GoogleSignInApi.signOut()
                    googleSignInClient?.signOut()?.addOnCompleteListener { }

                    Toast.makeText(activity, "탈퇴처리가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                    dialog.dismiss()

                    val fragment = FragmentAccountDeleteAccountDone()
                    parentFragmentManager.beginTransaction().apply{
                        replace(R.id.layout_fragment, fragment)
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        addToBackStack(null)
                        commit()
                    }
                }
            }
            dialog.button_question_cancel.setOnClickListener { // No
                dialog.dismiss()

            }
        }

        binding.checkboxExplain1.setOnCheckedChangeListener(CheckboxListener())
        binding.checkboxExplain2.setOnCheckedChangeListener(CheckboxListener())
        binding.checkboxExplain3.setOnCheckedChangeListener(CheckboxListener())
        binding.checkboxExplainLast.setOnCheckedChangeListener(CheckboxListener())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                callBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    private fun callBackPressed() {
        finishFragment()
    }

    private fun finishFragment() {
        val fragment = FragmentAccountSettings()
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
    }

    private fun checkValid() {
        binding.buttonOk.isEnabled = binding.checkboxExplain1.isChecked && binding.checkboxExplain2.isChecked && binding.checkboxExplain3.isChecked && binding.checkboxExplainLast.isChecked
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentAccountDeleteAccount.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentAccountDeleteAccount().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}