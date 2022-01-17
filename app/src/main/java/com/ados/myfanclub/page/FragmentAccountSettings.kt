package com.ados.myfanclub.page

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.ados.myfanclub.LoginActivity
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentAccountSettingsBinding
import com.ados.myfanclub.dialog.*
import com.ados.myfanclub.model.*
import com.ados.myfanclub.viewmodel.FirebaseStorageViewModel
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.edit_text_modify_dialog.*
import kotlinx.android.synthetic.main.gem_question_dialog.*
import kotlinx.android.synthetic.main.password_modify_dialog.*
import kotlinx.android.synthetic.main.question_dialog.*
import java.text.SimpleDateFormat
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
    private val firebaseStorageViewModel : FirebaseStorageViewModel by viewModels()
    private var firebaseAuth : FirebaseAuth? = null
    private var googleSignInClient : GoogleSignInClient? = null

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null

    private var currentUser: UserDTO? = null

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            modifyProfile(uri)
        } else {
            Toast.makeText(context, "이미지를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        fanClubDTO = (activity as MainActivity?)?.getFanClub()
        currentMember = (activity as MainActivity?)?.getMember()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAccountSettingsBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        firebaseAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //기본 로그인 방식 사용
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context,gso)

        currentUser = (activity as MainActivity?)?.getUser()

        // 메뉴는 기본 숨김
        binding.layoutMenu.visibility = View.GONE


        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (currentUser?.imgProfile != null) {
            firebaseStorageViewModel.getUserProfile(currentUser?.uid.toString()) { uri ->
                if (_binding != null) { // 메인 탭에서 바로 사용자 정보 탭 이동 시 팬클럽 뷰가 Destroy 되고 나서 뒤 늦게 들어오는 경우가 있기 때문에 예외 처리
                    Glide.with(requireContext()).load(uri).fitCenter().into(binding.imgProfile)
                }
            }
        }

        binding.editNickname.setText(currentUser?.nickname)
        binding.editAboutMe.setText(currentUser?.aboutMe)

        when (currentUser?.loginType) {
            UserDTO.LoginType.GOOGLE -> binding.editLoginType.setText("구글 로그인")
            else -> binding.editLoginType.setText("이메일 로그인")
        }

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.editAboutMe.setOnTouchListener { view, motionEvent ->
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
            false
        }

        binding.buttonModifyNickname.setOnClickListener {
            currentUser = (activity as MainActivity?)?.getUser()
            if (isBlockChangeNickname()) {
                val question = QuestionDTO(
                    QuestionDTO.Stat.WARNING,
                    "닉네임 변경",
                    "아직 닉네임을 변경할 수 없습니다. 닉네임은 3일마다 변경 가능합니다.\n\n최종변경일 [${SimpleDateFormat("yyyy.MM.dd HH:mm").format(currentUser?.nicknameChangeDate)}]",
                )
                val questionDialog = QuestionDialog(requireContext(), question)
                questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                questionDialog.setCanceledOnTouchOutside(false)
                questionDialog.show()
                questionDialog.showButtonOk(false)
                questionDialog.setButtonCancel("확인")
                questionDialog.button_question_cancel.setOnClickListener { // No
                    questionDialog.dismiss()
                }
            } else {
                val item = EditTextDTO("닉네임 변경", currentUser?.nickname, 15, "^[가-힣ㄱ-ㅎa-zA-Z0-9.~!@#\$%^&*\\[\\](){}|_-]{1,15}\$", "사용할 수 없는 문자열이 포함되어 있습니다.")
                val dialog = EditTextModifyDialog(requireContext(), item)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
                dialog.showImgOk(true)
                dialog.button_modify_cancel.setOnClickListener { // No
                    dialog.dismiss()
                }
                dialog.button_modify_ok.setOnClickListener { // Ok
                    val preferencesDTO = (activity as MainActivity?)?.getPreferences()

                    val question = GemQuestionDTO("다이아를 사용해 닉네임을 변경합니다.\n(3일마다 1회 변경 가능)", preferencesDTO?.priceNickname)
                    val questionDialog = GemQuestionDialog(requireContext(), question)
                    questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    questionDialog.setCanceledOnTouchOutside(false)
                    questionDialog.show()
                    questionDialog.button_gem_question_cancel.setOnClickListener { // No
                        questionDialog.dismiss()
                    }
                    questionDialog.button_gem_question_ok.setOnClickListener { // Ok
                        questionDialog.dismiss()

                        if ((currentUser?.paidGem!! + currentUser?.freeGem!!) < preferencesDTO?.priceNickname!!) {
                            Toast.makeText(activity, "다이아가 부족합니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            val nickname = dialog.edit_content.text.toString().trim()
                            firebaseViewModel.isUsedUserNickname(nickname) { isUsed ->
                                if (isUsed) {
                                    Toast.makeText(activity, "닉네임이 이미 존재합니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    dialog.dismiss()
                                    val oldNickname = currentUser?.nickname
                                    if (currentUser?.nickname != nickname) {
                                        // 닉네임이 포함된 모든 firebase 수정 (uid 값이 키)
                                        // 1. user.nickname
                                        // 2. fanClub.masterNickname (팬클럽장 일 경우)
                                        // 3. fanClub.member.userNickname
                                        // 4. displayBoard.userNickname (이미 출력되었기 때문에 닉네임 수정 안함)
                                        // 5. 메인 Activity 반영

                                        (activity as MainActivity?)?.loading()

                                        // 다이아 차감
                                        val date = Date()
                                        val oldPaidGemCount = currentUser?.paidGem!!
                                        val oldFreeGemCount = currentUser?.freeGem!!
                                        currentUser?.nickname = nickname
                                        currentUser?.nicknameChangeDate = date
                                        firebaseViewModel.updateUserNickname(currentUser!!, preferencesDTO?.priceNickname!!) { userDTO ->
                                            if (userDTO != null) {
                                                currentUser = userDTO

                                                var log = LogDTO("[다이아 차감] 닉네임 변경으로 ${preferencesDTO?.priceNickname} 다이아 사용 ($oldNickname -> $nickname), (paidGem : $oldPaidGemCount -> ${currentUser?.paidGem}, freeGem : $oldFreeGemCount -> ${currentUser?.freeGem})", Date())
                                                firebaseViewModel.writeUserLog(currentUser?.uid.toString(), log) { }

                                                // 가입한 팬클럽이 있을 경우
                                                if (fanClubDTO != null && currentMember != null) {
                                                    // 2. fanClub.masterNickname (팬클럽장 일 경우)
                                                    if (currentMember?.position == MemberDTO.Position.MASTER) {
                                                        fanClubDTO?.masterNickname = nickname
                                                        firebaseViewModel.updateFanClubMasterNickname(fanClubDTO!!) {

                                                        }
                                                    }

                                                    // 3. fanClub.member.userNickname
                                                    currentMember?.userNickname = nickname
                                                    firebaseViewModel.updateMemberNickname(fanClubDTO?.docName.toString(), currentMember!!) {
                                                        (activity as MainActivity?)?.loadingEnd()
                                                    }
                                                } else { // 팬클럽이 없을 경우
                                                    (activity as MainActivity?)?.loadingEnd()
                                                }

                                                Toast.makeText(activity, "닉네임 변경 완료!", Toast.LENGTH_SHORT).show()
                                                binding.editNickname.setText(nickname)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.buttonModifyAboutMe.setOnClickListener {
            currentUser = (activity as MainActivity?)?.getUser()
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
                val oldAboutMe = currentUser?.aboutMe
                if (currentUser?.aboutMe != aboutMe) {
                    val question = QuestionDTO(
                        QuestionDTO.Stat.WARNING,
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
                        firebaseViewModel.updateUserAboutMe(currentUser!!) {
                            var log = LogDTO("[내 소개 변경] $oldAboutMe -> $aboutMe", Date())
                            firebaseViewModel.writeUserLog(currentUser?.uid.toString(), log) { }

                            // 가입한 팬클럽이 있을 경우
                            if (fanClubDTO != null && currentMember != null) {
                                // 2. fanClub.member.userAboutMe
                                currentMember?.userAboutMe = aboutMe
                                firebaseViewModel.updateMemberAboutMe(fanClubDTO?.docName.toString(), currentMember!!) {

                                }
                            }
                            Toast.makeText(activity, "내 소개 변경 완료!", Toast.LENGTH_SHORT).show()
                            binding.editAboutMe.setText(aboutMe)
                        }
                    }
                }
            }
        }

        binding.buttonModifyPassword.setOnClickListener {
            if (currentUser?.loginType != UserDTO.LoginType.EMAIL) {
                Toast.makeText(activity, "소셜 로그인된 상태에서는 비밀번호를 변경할 수 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                val dialog = PasswordModifyDialog(requireContext())
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
                dialog.button_password_modify_cancel.setOnClickListener { // No
                    dialog.dismiss()
                }

                dialog.button_password_modify_ok.setOnClickListener { // Ok
                    val oldPassword = dialog.binding.editPasswordOld.text.toString()
                    val newPassword = dialog.binding.editPasswordNew.text.toString()

                    if (oldPassword == newPassword) {
                        Toast.makeText(activity, "새로운 비밀번호가 기존 비밀번호와 동일 합니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        println("현재 계정 비밀 번호 ${currentUser?.userId}, $oldPassword")
                        val credential = EmailAuthProvider.getCredential(currentUser?.userId.toString(), oldPassword)
                        firebaseAuth?.currentUser!!.reauthenticateAndRetrieveData(credential)?.addOnCompleteListener { task ->
                        //firebaseAuth?.currentUser!!.linkWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
                            if (!task.isSuccessful) {
                                Toast.makeText(activity, "현재 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                dialog.dismiss()
                                val question = QuestionDTO(
                                    QuestionDTO.Stat.WARNING,
                                    "비밀번호 변경",
                                    "비밀번호를 변경하면 되돌릴 수 없습니다.\n정말 변경 하시겠습니까?",
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

                                    firebaseAuth?.currentUser!!.updatePassword(newPassword)?.addOnCompleteListener {
                                        Toast.makeText(activity, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.buttonLogout.setOnClickListener {
            val question = QuestionDTO(
                QuestionDTO.Stat.ERROR,
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
                    (activity as MainActivity?)?.finish()
                }

                dialog.dismiss()
            }
            dialog.button_question_cancel.setOnClickListener { // No
                dialog.dismiss()

            }
        }

        binding.layoutProfile.setOnClickListener {
            openLayout()
        }

        binding.buttonMenuModify.setOnClickListener {
            resultLauncher.launch("image/*")
            closeLayout()
        }

        binding.buttonMenuDelete.setOnClickListener {
            modifyProfile()
            closeLayout()
        }

        binding.buttonMenuCancel.setOnClickListener {
            closeLayout()
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
        val fragment = FragmentAccountInfo()
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
    }

    private fun isBlockChangeNickname() : Boolean {
        // 닉네임 변경 후 3일이 지나야 재 변경 가능
        var isBlock = false
        if (currentUser?.nicknameChangeDate != null) {
            val calendar= Calendar.getInstance()
            calendar.time = currentUser?.nicknameChangeDate
            calendar.add(Calendar.DATE, 3)

            if (Date() < calendar.time) {
                isBlock = true
            }
        }
        return isBlock
    }

    private fun openLayout() {
        if (binding.layoutMenu.visibility == View.GONE) {
            val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
            binding.layoutMenu.startAnimation(translateUp)
        }
        binding.layoutMenu.visibility = View.VISIBLE
    }

    private fun closeLayout() {
        if (binding.layoutMenu.visibility == View.VISIBLE) {
            val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
            binding.layoutMenu.startAnimation(translateDown)
        }
        binding.layoutMenu.visibility = View.GONE
    }

    private fun modifyProfile(uri: Uri? = null) {
        currentUser = (activity as MainActivity?)?.getUser()
        if (uri == null && currentUser?.imgProfile == null) {
            Toast.makeText(activity, "삭제할 프로필이 없습니다.", Toast.LENGTH_SHORT).show()
        } else {
            val question = QuestionDTO(
                QuestionDTO.Stat.WARNING,
                "프로필 변경",
                "프로필을 변경하면 되돌릴 수 없습니다.\n정말 변경 하시겠습니까?"
            )
            if (uri == null) {
                question.title = "프로필 삭제"
                question.content = "프로필을 삭제하면 되돌릴 수 없습니다.\n정말 변경 하시겠습니까?"
            }

            val questionDialog = QuestionDialog(requireContext(), question)
            questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            questionDialog.setCanceledOnTouchOutside(false)
            questionDialog.show()
            questionDialog.button_question_cancel.setOnClickListener { // No
                questionDialog.dismiss()
            }
            questionDialog.button_question_ok.setOnClickListener { // Ok
                questionDialog.dismiss()

                (activity as MainActivity?)?.loading()
                if (uri != null) { // 프로필 업로드
                    var bitmap = (activity as MainActivity?)?.getBitmap(uri)
                    firebaseStorageViewModel.setUserProfile(currentUser?.uid.toString(), bitmap!!) {
                        if (!it) {
                            Toast.makeText(activity, "이미지 업로드 실패 ", Toast.LENGTH_SHORT).show()
                            (activity as MainActivity?)?.loadingEnd()
                        } else {
                            currentUser?.imgProfile = currentUser?.getProfileImageName()
                            firebaseViewModel.updateUserProfile(currentUser!!) {
                                Toast.makeText(activity, "프로필 사진 변경 완료!", Toast.LENGTH_SHORT).show()
                                binding.imgProfile.setImageBitmap(bitmap)
                                (activity as MainActivity?)?.loadingEnd()
                            }
                        }
                    }
                } else { // 프로필 삭제
                    currentUser?.imgProfile = null
                    firebaseViewModel.updateUserProfile(currentUser!!) {
                        Toast.makeText(activity, "프로필 삭제 완료!", Toast.LENGTH_SHORT).show()
                        binding.imgProfile.setImageResource(R.drawable.profile)
                        (activity as MainActivity?)?.loadingEnd()
                    }
                }
            }
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