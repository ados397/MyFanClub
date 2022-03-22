package com.ados.myfanclub.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentFanClubManagementBinding
import com.ados.myfanclub.dialog.EditTextModifyDialog
import com.ados.myfanclub.dialog.QuestionDialog
import com.ados.myfanclub.model.*
import com.ados.myfanclub.repository.FirebaseRepository
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFanClubManagement.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFanClubManagement : Fragment(), OnFanClubSignUpItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentFanClubManagementBinding? = null
    private val binding get() = _binding!!

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterFanClubSignUp

    private val firebaseViewModel : FirebaseViewModel by viewModels()

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null
    private var members : ArrayList<MemberDTO> = arrayListOf()

    private var questionDialog: QuestionDialog? = null
    private var editTextModifyDialog: EditTextModifyDialog? = null

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
        _binding = FragmentFanClubManagementBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        recyclerView = rootView.findViewById(R.id.rv_fan_club_sign_up)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 메뉴는 기본 숨김
        binding.layoutMenu.visibility = View.GONE

        setFanClubInfo()
        refreshMembers()

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefreshLayout.setOnRefreshListener {
            refresh()
            refreshMembers()
        }

        binding.buttonModifyDescription.setOnClickListener {
            // 관리자 권한이 없어졌는지 확인
            if (!(parentFragment as FragmentFanClubMain?)?.isRemoveAdmin()!!) {
                modifyDescription()
            }
        }

        binding.buttonApproval.setOnClickListener {
            // 관리자 권한이 없어졌는지 확인
            if (!(parentFragment as FragmentFanClubMain?)?.isRemoveAdmin()!!) {
                hideMenu()

                (activity as MainActivity?)?.loading()
                fanClubDTO = (activity as MainActivity?)?.getFanClub()
                if (fanClubDTO?.memberCount!! >= fanClubDTO?.getMaxMemberCount()!!) {
                    Toast.makeText(activity, "더 이상 팬클럽원을 받을 수 없습니다. 팬클럽 레벨을 올려 주세요.", Toast.LENGTH_SHORT).show()
                    refreshMembers()
                    (activity as MainActivity?)?.loadingEnd()
                } else {
                    var jobCount = 0
                    var successCount = 0
                    val date = Date()
                    var currentUser = (activity as MainActivity?)?.getUser()
                    for (member in members) {
                        if (!member.isSelected) {
                            continue
                        }

                        jobCount++
                        member.isSelected = false
                        member.position = MemberDTO.Position.MEMBER
                        member.responseTime = date

                        // 이미 다른 클럽에 가입된 사용자가 아닌지 확인
                        firebaseViewModel.getHaveFanClub(member.userUid.toString()) { userDTO ->
                            if (userDTO == null) {
                                Toast.makeText(activity, "이미 다른 팬클럽에 가입된 사용자 입니다.", Toast.LENGTH_SHORT).show()
                                successCount++
                            } else {
                                // 팬클럽 멤버로 등록
                                firebaseViewModel.updateMember(fanClubDTO?.docName.toString(), member) { // 팬클럽 멤버 등록
                                    firebaseViewModel.addFanClubMemberCount(fanClubDTO?.docName.toString(), 1) { fanClub -> // 팬클럽 멤버수 1 증가
                                        if (fanClub != null) {
                                            // 사용자 정보에 팬클럽 ID 기록 및 팬클럽 신청 이력 삭제
                                            userDTO.fanClubId = fanClubDTO?.docName
                                            firebaseViewModel.updateUserFanClubApproval(userDTO) {
                                                successCount++
                                            }

                                            // 팬클럽 가입 승인 우편으로 발송
                                            val docName = "master${System.currentTimeMillis()}"
                                            val calendar= Calendar.getInstance()
                                            calendar.add(Calendar.DATE, 7)
                                            var mail = MailDTO(docName,"팬클럽 가입 승인", "축하합니다! 팬클럽 [${fanClubDTO?.name}]에 가입 되었습니다! 멋진 팬클럽 멤버들과 함께 매너있고 즐거운 팬클럽 활동을 시작해보세요!", "시스템", MailDTO.Item.NONE, 0, date, calendar.time)
                                            firebaseViewModel.sendUserMail(member.userUid.toString(), mail) {
                                                var log = LogDTO("[팬클럽 가입 승인] 팬클럽 정보 - Name : ${fanClubDTO?.name}, docName : ${fanClubDTO?.docName} 우편 발송, 유효기간 : ${SimpleDateFormat("yyyy.MM.dd HH:mm").format(calendar.time)}까지", date)
                                                firebaseViewModel.writeUserLog(member.userUid.toString(), log) { }
                                            }

                                            // 팬클럽 로그 기록
                                            var log = LogDTO("[팬클럽 가입 승인] 사용자 정보 (uid : ${member.userUid}, nickname : ${member.userNickname}), 승인한 운영진 정보 (uid : ${currentUser?.uid}, nickname : ${currentUser?.nickname})", date)
                                            firebaseViewModel.writeFanClubLog(fanClubDTO?.docName.toString(), log) { }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    timer(period = 100)
                    {
                        if (jobCount == successCount) {
                            cancel()
                            activity?.runOnUiThread {
                                Toast.makeText(activity, "가입 승인 완료!", Toast.LENGTH_SHORT).show()
                                refreshMembers()
                            }
                        }
                    }
                }
            }
        }

        binding.buttonReject.setOnClickListener {
            // 관리자 권한이 없어졌는지 확인
            if (!(parentFragment as FragmentFanClubMain?)?.isRemoveAdmin()!!) {
                hideMenu()
                (activity as MainActivity?)?.loading()
                var jobCount = 0
                var successCount = 0
                for (member in members) {
                    if (member.isSelected) {
                        jobCount++
                        firebaseViewModel.updateUserFanClubReject(fanClubDTO?.docName.toString(), member.userUid.toString()) {
                            successCount++
                        }
                    }
                }

                timer(period = 100)
                {
                    if (jobCount == successCount) {
                        cancel()
                        activity?.runOnUiThread {
                            Toast.makeText(activity, "가입 거절 완료!", Toast.LENGTH_SHORT).show()
                            refreshMembers()
                        }
                    }
                }
            }
        }

        binding.buttonFanClubQuit.setOnClickListener {
            fanClubDTO = (activity as MainActivity?)?.getFanClub()
            currentMember = (activity as MainActivity?)?.getMember()

            fanClubQuit()
        }

        binding.buttonMenuCancel.setOnClickListener {
            recyclerViewAdapter.releaseCheckAll()
            selectRecyclerView()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentFanClubManagement.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentFanClubManagement().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setFanClubInfo() {
        binding.editDescription.setText(fanClubDTO?.description)

        when {
            isAdministrator() -> { // 클럽장, 부클럽장 메뉴 활성화
                binding.buttonModifyDescription.visibility = View.VISIBLE
                binding.layoutSignUp.visibility = View.VISIBLE
                binding.editDescription.maxLines = 4
            }
            else -> {
                binding.buttonModifyDescription.visibility = View.GONE
                binding.layoutSignUp.visibility = View.GONE
                binding.editDescription.maxLines = 10
            }
        }
    }

    private fun refresh() {
        (activity as MainActivity?)?.loading()
        fanClubDTO = (activity as MainActivity?)?.getFanClub()
        currentMember = (activity as MainActivity?)?.getMember()

        setFanClubInfo()
        (activity as MainActivity?)?.loadingEnd()

        Toast.makeText(context, "새로 고침", Toast.LENGTH_SHORT).show()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun refreshMembers() {
        firebaseViewModel.getMembers(fanClubDTO?.docName.toString(), FirebaseRepository.MemberType.GUEST_ONLY) {
            members = it
            recyclerViewAdapter = RecyclerViewAdapterFanClubSignUp(members, this)
            recyclerView.adapter = recyclerViewAdapter
            (activity as MainActivity?)?.loadingEnd()
        }
    }

    private fun isMaster() : Boolean {
        return currentMember?.position == MemberDTO.Position.MASTER
    }

    private fun isAdministrator() : Boolean {
        return currentMember?.position == MemberDTO.Position.MASTER || currentMember?.position == MemberDTO.Position.SUB_MASTER
    }

    private fun hideMenu() {
        val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
        binding.layoutMenu.visibility = View.GONE
        binding.layoutMenu.startAnimation(translateDown)
        //recyclerView.smoothSnapToPosition(position)
    }

    private fun modifyDescription() {
        val item = EditTextDTO("팬클럽 소개 변경", fanClubDTO?.description, 600)
        if (editTextModifyDialog == null) {
            editTextModifyDialog = EditTextModifyDialog(requireContext(), item)
            editTextModifyDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            editTextModifyDialog?.setCanceledOnTouchOutside(false)
        } else {
            editTextModifyDialog?.item = item
        }
        editTextModifyDialog?.show()
        editTextModifyDialog?.setInfo()
        editTextModifyDialog?.binding?.buttonModifyCancel?.setOnClickListener { // No
            editTextModifyDialog?.dismiss()
        }
        editTextModifyDialog?.binding?.buttonModifyOk?.setOnClickListener { // Ok
            editTextModifyDialog?.dismiss()

            val question = QuestionDTO(
                QuestionDTO.Stat.WARNING,
                "팬클럽 소개 변경",
                "팬클럽 소개 변경하면 되돌릴 수 없습니다.\n정말 변경 하시겠습니까?",
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
            }
            questionDialog?.binding?.buttonQuestionOk?.setOnClickListener { // Ok
                questionDialog?.dismiss()

                fanClubDTO?.description = editTextModifyDialog?.binding?.editContent?.text.toString()
                firebaseViewModel.updateFanClubDescription(fanClubDTO!!) {
                    Toast.makeText(activity, "팬클럽 소개 변경 완료!", Toast.LENGTH_SHORT).show()
                    binding.editDescription.setText(fanClubDTO?.description)
                }
            }
        }
    }

    private fun fanClubQuit() {
        // 클럽장은 클럽장 위임 전에 탈퇴가 불가능 하지만 클럽장 혼자만 남아있다면 탈퇴 가능
        if (currentMember?.position == MemberDTO.Position.MASTER && fanClubDTO?.memberCount!! > 1) {
            val question = QuestionDTO(
                QuestionDTO.Stat.ERROR,
                "팬클럽 탈퇴",
                "클럽장은 팬클럽 탈퇴를 할 수 없습니다. 클럽장을 위임한 후 탈퇴가 가능합니다.",
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
            questionDialog?.setButtonCancel("확인")
            questionDialog?.showButtonOk(false)
            questionDialog?.binding?.buttonQuestionCancel?.setOnClickListener { // No
                questionDialog?.dismiss()
            }
        } else {
            val question = QuestionDTO(
                QuestionDTO.Stat.ERROR,
                "팬클럽 탈퇴",
                "팬클럽 탈퇴 시 24시간 동안 팬클럽 가입이 제한되며 기여도 및 모든 정보가 초기화 됩니다. 정말 탈퇴 하시겠습니까?",
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
            }
            questionDialog?.binding?.buttonQuestionOk?.setOnClickListener { // Ok
                questionDialog?.dismiss()

                (activity as MainActivity?)?.loading()

                // 팬클럽 멤버에서 삭제
                firebaseViewModel.deleteMember(fanClubDTO?.docName.toString(), currentMember!!) { fanClub ->
                    if (fanClub != null) {
                        // 사용자 정보에서 팬클럽 정보 삭제
                        val user = (activity as MainActivity?)?.getUser()!!
                        firebaseViewModel.deleteUserFanClubId(user.uid.toString()) { }

                        if (fanClub.memberCount!! <= 0) { // 팬클럽 멤버가 남아있지 않다면 팬클럽 삭제
                            firebaseViewModel.deleteFanClub(fanClub.docName.toString()) {
                                var log = LogDTO("[팬클럽 삭제] 팬클럽 명 : ${fanClubDTO?.name}(${fanClubDTO?.docName.toString()}), 마지막 클럽장 : ${currentMember?.userNickname}(${currentMember?.userUid.toString()})", Date())
                                firebaseViewModel.writeAdminLog(log) { }
                            }
                        } else {
                            var log = LogDTO("[클럽원 탈퇴] ${currentMember?.userNickname}(${currentMember?.userUid.toString()})", Date())
                            firebaseViewModel.writeFanClubLog(fanClub.docName.toString(), log) { }
                        }

                        var log = LogDTO("[팬클럽 탈퇴] 팬클럽 명 : ${fanClubDTO?.name}(${fanClubDTO?.docName.toString()})", Date())
                        firebaseViewModel.writeUserLog(currentMember?.userUid.toString(), log) { }
                    }
                    (activity as MainActivity?)?.loadingEnd()
                }
            }
        }
    }

    private fun selectRecyclerView() {
        if (recyclerViewAdapter.isChecked()) { // 체크된 항목이 하나라도 있으면 표시
            if (!binding.layoutMenu.isVisible) { // 이미 표시되어 있을 경우 중복 동작 안하도록 처리
                val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
                binding.layoutMenu.visibility = View.VISIBLE
                binding.layoutMenu.startAnimation(translateUp)
                //recyclerView.smoothSnapToPosition(position)
            }
        } else { // 모두 체크 해제라면 메뉴 숨김
            hideMenu()
        }
    }

    override fun onItemClick(item: MemberDTO, position: Int) {
        selectRecyclerView()
    }
}