package com.ados.myfanclub.page

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.ados.myfanclub.LoginActivity
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.MySharedPreferences
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentAccountSettingsBinding
import com.ados.myfanclub.dialog.*
import com.ados.myfanclub.model.*
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentAccountSettings.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentAccountSettings : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentAccountSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private var firebaseAuth : FirebaseAuth? = null
    private var googleSignInClient : GoogleSignInClient? = null

    private var questionDialog : QuestionDialog? = null
    private var passwordModifyDialog : PasswordModifyDialog? = null
    private var documentDialog : DocumentDialog? = null

    private var currentUserEx: UserExDTO? = null
    private var toast : Toast? = null

    private val sharedPreferences: MySharedPreferences by lazy {
        MySharedPreferences(requireContext())
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
        _binding = FragmentAccountSettingsBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        binding.layoutFaq.visibility = View.GONE
        firebaseViewModel.getFaq()
        firebaseViewModel.faqDTOs.observe(viewLifecycleOwner) {
            if (firebaseViewModel.faqDTOs.value != null) {
                if (firebaseViewModel.faqDTOs.value!!.size > 0) {
                    binding.layoutFaq.visibility = View.VISIBLE
                }
            }
        }

        firebaseAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //?????? ????????? ?????? ??????
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(),gso)

        currentUserEx = (activity as MainActivity?)?.getUserEx()

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val info: PackageInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
        val localVersion = info.versionName
        binding.textVersion.text = "version : $localVersion"

        when (currentUserEx?.userDTO?.loginType) {
            UserDTO.LoginType.GOOGLE -> binding.textLoginTypeInput.text = "?????? ?????????"
            else -> binding.textLoginTypeInput.text = "????????? ?????????"
        }

        binding.textLoginIdInput.text = currentUserEx?.userDTO?.userId

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.buttonModifyPassword.setOnClickListener {
            if (currentUserEx?.userDTO?.loginType != UserDTO.LoginType.EMAIL) {
                Toast.makeText(activity, "?????? ???????????? ??????????????? ??????????????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show()
            } else {
                if (passwordModifyDialog == null) {
                    passwordModifyDialog = PasswordModifyDialog(requireContext())
                    passwordModifyDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    passwordModifyDialog?.setCanceledOnTouchOutside(false)
                }
                passwordModifyDialog?.show()
                passwordModifyDialog?.setInfo()
                passwordModifyDialog?.binding?.buttonPasswordModifyCancel?.setOnClickListener {
                    passwordModifyDialog?.dismiss()
                }

                passwordModifyDialog?.binding?.buttonPasswordModifyOk?.setOnClickListener { // Ok
                    val oldPassword = passwordModifyDialog?.binding?.editPasswordOld?.text.toString()
                    val newPassword = passwordModifyDialog?.binding?.editPasswordNew?.text.toString()

                    if (oldPassword == newPassword) {
                        Toast.makeText(activity, "????????? ??????????????? ?????? ??????????????? ?????? ?????????.", Toast.LENGTH_SHORT).show()
                    } else {
                        println("?????? ?????? ?????? ?????? ${currentUserEx?.userDTO?.userId}, $oldPassword")
                        val credential = EmailAuthProvider.getCredential(currentUserEx?.userDTO?.userId.toString(), oldPassword)
                        firebaseAuth?.currentUser!!.reauthenticateAndRetrieveData(credential).addOnCompleteListener { task ->
                            //firebaseAuth?.currentUser!!.linkWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
                            if (!task.isSuccessful) {
                                Toast.makeText(activity, "?????? ??????????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show()
                            } else {
                                passwordModifyDialog?.dismiss()
                                val question = QuestionDTO(
                                    QuestionDTO.Stat.WARNING,
                                    "???????????? ??????",
                                    "??????????????? ???????????? ????????? ??? ????????????.\n?????? ?????? ???????????????????",
                                )
                                if (questionDialog == null) {
                                    questionDialog = QuestionDialog(requireContext(), question)
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

                                questionDialog?.binding?.buttonQuestionOk?.setOnClickListener {
                                    questionDialog?.dismiss()
                                    questionDialog = null

                                    firebaseAuth?.currentUser!!.updatePassword(newPassword).addOnCompleteListener {
                                        Toast.makeText(activity, "??????????????? ?????????????????????.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.layoutTermsOfUse.setOnClickListener {
            firebaseViewModel.getTermsOfUse { document ->
                onDocumentDialog(document)
            }
        }

        binding.layoutPrivacyPolicy.setOnClickListener {
            firebaseViewModel.getPrivacyPolicy { document ->
                onDocumentDialog(document)
            }
        }

        binding.layoutOpenSourceLicense.setOnClickListener {
            firebaseViewModel.getOpenSourceLicense { document ->
                onDocumentDialog(document)
            }
        }

        binding.layoutFaq.setOnClickListener {
            val fragment = FragmentAccountFaq()
            parentFragmentManager.beginTransaction().apply{
                replace(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
                commit()
            }
        }

        binding.layoutQna.setOnClickListener {
            val fragment = FragmentAccountQna()
            parentFragmentManager.beginTransaction().apply{
                replace(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
                commit()
            }
        }

        binding.layoutLogout.setOnClickListener {
            val question = QuestionDTO(
                QuestionDTO.Stat.ERROR,
                "????????????",
                "?????? ???????????? ???????????????????"
            )
            if (questionDialog == null) {
                questionDialog = QuestionDialog(requireContext(), question)
                questionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                questionDialog?.setCanceledOnTouchOutside(false)
            } else {
                questionDialog?.question = question
            }
            questionDialog?.show()
            questionDialog?.setInfo()
            questionDialog?.setButtonOk("????????????")
            questionDialog?.binding?.buttonQuestionCancel?.setOnClickListener { // No
                questionDialog?.dismiss()
                questionDialog = null
            }

            questionDialog?.binding?.buttonQuestionOk?.setOnClickListener {
                firebaseAuth?.signOut()
                //Auth.GoogleSignInApi.signOut()
                googleSignInClient?.signOut()?.addOnCompleteListener { }

                sharedPreferences.putInt(MySharedPreferences.PREF_KEY_LAST_FAN_CLUB_LEVEL, 0)
                sharedPreferences.putString(MySharedPreferences.PREF_KEY_LAST_MEMBER_POSITION, "")

                Toast.makeText(activity, "???????????? ???????????????.", Toast.LENGTH_SHORT).show()

                activity?.let{
                    val intent = Intent(context, LoginActivity::class.java)
                    startActivity(intent)
                    (activity as MainActivity?)?.finish()
                }

                questionDialog?.dismiss()
                questionDialog = null
            }
        }

        binding.layoutDeleteAccount.setOnClickListener {
            if (!currentUserEx?.userDTO?.fanClubId.isNullOrEmpty()) {
                callToast("????????? ?????? ??? ???????????????.")
            } else if (currentUserEx?.userDTO?.isPremium()!!) {
                callToast("???????????? ????????? ?????? ??? ???????????????. (?????? ??????: ${currentUserEx?.userDTO?.getPremiumDay()}???)")
            } else {
                val fragment = FragmentAccountDeleteAccount()
                parentFragmentManager.beginTransaction().apply{
                    replace(R.id.layout_fragment, fragment)
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    addToBackStack(null)
                    commit()
                }
            }
        }
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
        val fragment = FragmentAccountInfo()
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
    }

    private fun callToast(message: String) {
        if (toast == null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        } else {
            toast?.setText(message)
        }
        toast?.show()
    }

    private fun onDocumentDialog(document: String) {
        if (documentDialog == null) {
            documentDialog = DocumentDialog(requireContext(), document)
            documentDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            documentDialog?.setCanceledOnTouchOutside(false)
        } else {
            documentDialog?.content = document
        }
        documentDialog?.show()
        documentDialog?.setInfo()
        documentDialog?.showButtonOk(false)
        documentDialog?.binding?.buttonDocumentCancel?.setOnClickListener { // No
            documentDialog?.dismiss()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentAccountSettings.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentAccountSettings().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}