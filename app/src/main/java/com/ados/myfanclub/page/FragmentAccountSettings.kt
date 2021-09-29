package com.ados.myfanclub.page

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentTransaction
import com.ados.myfanclub.LoginActivity
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentAccountSettingsBinding
import com.ados.myfanclub.dialog.EditTextModifyDialog
import com.ados.myfanclub.dialog.QuestionDialog
import com.ados.myfanclub.model.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.edit_text_modify_dialog.*
import kotlinx.android.synthetic.main.question_dialog.*

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
    private var _binding: FragmentAccountSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

    private var firestore : FirebaseFirestore? = null
    private var firebaseAuth : FirebaseAuth? = null
    private var googleSignInClient : GoogleSignInClient? = null

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null

    private var currentUser: UserDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            fanClubDTO = it.getParcelable(ARG_PARAM1)
            currentMember = it.getParcelable(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAccountSettingsBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //기본 로그인 방식 사용
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context,gso)

        currentUser = (activity as MainActivity?)?.getUser()


        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editNickname.setText(currentUser?.nickname)
        binding.editAboutMe.setText(currentUser?.aboutMe)

        println("클럽정보 $fanClubDTO")
        println("멤버정보 $currentMember")

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.buttonModifyNickname.setOnClickListener {
            val item = EditTextDTO("닉네임 변경", currentUser?.nickname, 15, "^[가-힣ㄱ-ㅎa-zA-Z0-9.~!@#\$%^&*\\[\\](){}|_-]{1,15}\$", "사용할 수 없는 문자열이 포함되어 있습니다.")
            val dialog = EditTextModifyDialog(requireContext(), item)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.button_modify_cancel.setOnClickListener { // No
                dialog.dismiss()
            }
            dialog.button_modify_ok.setOnClickListener {
                dialog.dismiss()

                val nickname = dialog.edit_content.text.toString().trim()
                if (currentUser?.nickname != nickname) {
                    val question = QuestionDTO(
                        QuestionDTO.STAT.WARNING,
                        "닉네임 변경",
                        "닉네임을 변경하면 되돌릴 수 없습니다.\n정말 변경 하시겠습니까?",
                    )
                    val questionDialog = QuestionDialog(requireContext(), question)
                    questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    questionDialog.setCanceledOnTouchOutside(false)
                    questionDialog.show()
                    questionDialog.button_question_cancel.setOnClickListener { // No
                        questionDialog.dismiss()
                    }
                    questionDialog.button_question_ok.setOnClickListener {
                        questionDialog.dismiss()

                        // 닉네임이 포함된 모든 firebase 수정 (uid 값이 키)
                        // 1. user.nickname
                        // 2. fanClub.masterNickname (팬클럽장 일 경우)
                        // 3. fanClub.member.userNickname
                        // 4. displayBoard.userNickname (이미 출력되었기 때문에 닉네임 수정 안함)
                        // 5. 메인 Activity 반영

                        // 1. user.nickname
                        currentUser?.nickname = nickname
                        firestore?.collection("user")?.document(currentUser?.uid.toString())?.update("nickname", nickname)?.addOnCompleteListener {

                            // 가입한 팬클럽이 있을 경우
                            if (fanClubDTO != null && currentMember != null) {
                                // 2. fanClub.masterNickname (팬클럽장 일 경우)
                                if (currentMember?.position == MemberDTO.POSITION.MEMBER) {
                                    fanClubDTO?.masterNickname = nickname
                                    firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.update("masterNickname", nickname)?.addOnCompleteListener {
                                            // 5. 메인 Activity 반영
                                            (activity as MainActivity?)?.setFanClub(fanClubDTO!!)
                                        }
                                }

                                // 3. fanClub.member.userNickname
                                currentMember?.userNickname = nickname
                                firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.collection("member")?.document(currentMember?.userUid.toString())?.update("userNickname", nickname)?.addOnCompleteListener {
                                    // 5. 메인 Activity 반영
                                    (activity as MainActivity?)?.setMember(currentMember!!)
                                }
                            }

                            // 4. displayBoard.userNickname (이미 출력되었기 때문에 닉네임 수정 안함)
                            /*firestore?.collection("displayBoard")?.document(currentUser?.uid.toString())?.update("nickname", nickname)?.addOnCompleteListener {

                            }*/

                            // 5. 메인 Activity 반영
                            (activity as MainActivity?)?.setUser(currentUser!!)

                            Toast.makeText(activity, "닉네임 변경 완료!", Toast.LENGTH_SHORT).show()
                            binding.editNickname.setText(nickname)
                        }
                    }
                }
            }
        }

        binding.buttonModifyAboutMe.setOnClickListener {
            val item = EditTextDTO("내 소개 변경", currentUser?.aboutMe, 50)
            val dialog = EditTextModifyDialog(requireContext(), item)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.button_modify_cancel.setOnClickListener { // No
                dialog.dismiss()
            }
            dialog.button_modify_ok.setOnClickListener {
                dialog.dismiss()

                val aboutMe = dialog.edit_content.text.toString().trim()
                if (currentUser?.aboutMe != aboutMe) {
                    val question = QuestionDTO(
                        QuestionDTO.STAT.WARNING,
                        "내 소개 변경",
                        "내 소개를 변경하면 되돌릴 수 없습니다.\n정말 변경 하시겠습니까?",
                    )
                    val questionDialog = QuestionDialog(requireContext(), question)
                    questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    questionDialog.setCanceledOnTouchOutside(false)
                    questionDialog.show()
                    questionDialog.button_question_cancel.setOnClickListener { // No
                        questionDialog.dismiss()
                    }
                    questionDialog.button_question_ok.setOnClickListener {
                        questionDialog.dismiss()

                        // 내 소개가 포함된 모든 firebase 수정 (uid 값이 키)
                        // 1. user.aboutMe
                        // 2. fanClub.member.userAboutMe
                        // 3. 메인 Activity 반영

                        // 1. user.aboutMe
                        currentUser?.aboutMe = aboutMe
                        firestore?.collection("user")?.document(currentUser?.uid.toString())?.update("aboutMe", aboutMe)?.addOnCompleteListener {

                            // 가입한 팬클럽이 있을 경우
                            if (fanClubDTO != null && currentMember != null) {
                                // 2. fanClub.member.userAboutMe
                                currentMember?.userAboutMe = aboutMe
                                firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.collection("member")?.document(currentMember?.userUid.toString())?.update("userAboutMe", aboutMe)?.addOnCompleteListener {
                                    // 3. 메인 Activity 반영
                                    (activity as MainActivity?)?.setMember(currentMember!!)
                                }
                            }

                            // 3. 메인 Activity 반영
                            (activity as MainActivity?)?.setUser(currentUser!!)

                            Toast.makeText(activity, "내 소개 변경 완료!", Toast.LENGTH_SHORT).show()
                            binding.editAboutMe.setText(aboutMe)
                        }
                    }
                }
            }
        }

        binding.buttonLogout.setOnClickListener {
            val question = QuestionDTO(
                QuestionDTO.STAT.ERROR,
                "로그아웃",
                "정말 로그아웃 하시겠습니까?"
            )
            val dialog = QuestionDialog(requireContext(), question)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.setButtonOk("로그아웃")
            dialog.button_question_ok.setOnClickListener { // Yes
                firebaseAuth?.signOut()
                //Auth.GoogleSignInApi.signOut()
                googleSignInClient?.signOut()?.addOnCompleteListener {

                }

                Toast.makeText(activity, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()

                activity?.let{
                    val intent = Intent(context, LoginActivity::class.java)
                    startActivity(intent)
                }

                dialog.dismiss()
            }
            dialog.button_question_cancel.setOnClickListener { // No
                dialog.dismiss()

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

    private fun callBackPressed() {
        finishFragment()
    }

    private fun finishFragment() {
        val fragment = FragmentAccountInfo.newInstance(fanClubDTO, currentMember)
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
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
        fun newInstance(param1: FanClubDTO?, param2: MemberDTO?) =
            FragmentAccountSettings().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putParcelable(ARG_PARAM2, param2)
                }
            }
    }
}