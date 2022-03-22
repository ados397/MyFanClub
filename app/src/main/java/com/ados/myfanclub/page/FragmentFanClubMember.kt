package com.ados.myfanclub.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentFanClubMemberBinding
import com.ados.myfanclub.dialog.QuestionDialog
import com.ados.myfanclub.dialog.UserInfoDialog
import com.ados.myfanclub.model.*
import com.ados.myfanclub.repository.FirebaseRepository
import com.ados.myfanclub.viewmodel.FirebaseStorageViewModel
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFanClubMember.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFanClubMember : Fragment(), OnFanClubMemberItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentFanClubMemberBinding? = null
    private val binding get() = _binding!!

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterFanClubMember

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val firebaseStorageViewModel : FirebaseStorageViewModel by viewModels()

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null
    private var members : ArrayList<MemberDTO> = arrayListOf()

    private var questionDialog: QuestionDialog? = null
    private var userInfoDialog: UserInfoDialog? = null

    private var selectedMember: MemberDTO? = null
    private var selectedPosition: Int? = 0

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
        _binding = FragmentFanClubMemberBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        recyclerView = rootView.findViewById(R.id.rv_fan_club_member)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 기본 숨김 설정
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

        binding.buttonCancel.setOnClickListener {
            selectRecyclerView()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            refresh()
            refreshMembers()
        }

        binding.buttonDelegateMaster.setOnClickListener {
            // 관리자 권한이 없어졌는지 확인
            if (!(parentFragment as FragmentFanClubMain?)?.isRemoveAdmin()!!) {
                delegateMaster()
            }
        }

        binding.buttonAppointSubMaster.setOnClickListener {
            // 관리자 권한이 없어졌는지 확인
            if (!(parentFragment as FragmentFanClubMain?)?.isRemoveAdmin()!!) {
                if (fanClubDTO?.subMasterCount!! >= fanClubDTO?.getMaxSubMasterCount()!!) {
                    selectRecyclerView()
                    Toast.makeText(activity, "더 이상 부클럽장을 임명할 수 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    appointSubMaster()
                }
            }
        }

        binding.buttonFireSubMaster.setOnClickListener {
            // 관리자 권한이 없어졌는지 확인
            if (!(parentFragment as FragmentFanClubMain?)?.isRemoveAdmin()!!) {
                fireSubMaster()
            }
        }

        binding.buttonDeportation.setOnClickListener {
            // 관리자 권한이 없어졌는지 확인
            if (!(parentFragment as FragmentFanClubMain?)?.isRemoveAdmin()!!) {
                deportation()
            }
        }

        binding.buttonUserInfo.setOnClickListener {
            selectRecyclerView()
            firebaseViewModel.getUser(selectedMember?.userUid.toString()) { userDTO ->
                if (userDTO != null) {
                    var userEx = UserExDTO(userDTO)
                    if (userDTO.imgProfile != null) {
                        firebaseStorageViewModel.getUserProfileImage(selectedMember?.userUid.toString()) { uri ->
                            userEx.imgProfileUri = uri
                            onUserInfo(userEx)
                        }
                    } else {
                        onUserInfo(userEx)
                    }
                } else {
                    Toast.makeText(activity, "사용자 정보 획득에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun onUserInfo(userEx: UserExDTO) {
        if (userInfoDialog == null) {
            userInfoDialog = UserInfoDialog(requireContext(), selectedMember!!, userEx)
            userInfoDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            userInfoDialog?.setCanceledOnTouchOutside(false)
        } else {
            userInfoDialog?.member = selectedMember!!
            userInfoDialog?.user = userEx
        }
        userInfoDialog?.show()
        userInfoDialog?.setInfo()
        userInfoDialog?.binding?.buttonUserInfoClose?.setOnClickListener {
            userInfoDialog?.dismiss()
        }
    }

    private fun delegateMaster() {
        val question = QuestionDTO(
            QuestionDTO.Stat.ERROR,
            "클럽장 위임",
            "[${selectedMember?.userNickname}]님에게 클럽장을 위임하시겠습니까?\n클럽장을 위임하고나면 클럽원으로 강등됩니다."
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
            selectedMember?.position = MemberDTO.Position.MASTER
            firebaseViewModel.updateMemberPosition(fanClubDTO?.docName.toString(), selectedMember!!) { // 선택한 멤버 클럽장 임명
                currentMember?.position = MemberDTO.Position.MEMBER
                firebaseViewModel.updateMemberPosition(fanClubDTO?.docName.toString(), currentMember!!) { // 현재 사용자(클럽장) 클럽원으로 강등
                    // 팬클럽에 클럽장 아이디와 닉네임 새로운 클럽장 정보 적용
                    fanClubDTO?.masterUid = selectedMember?.userUid
                    fanClubDTO?.masterNickname = selectedMember?.userNickname
                    firebaseViewModel.updateFanClub(fanClubDTO!!) {
                        selectRecyclerView()
                        refreshMembers()
                        Toast.makeText(activity, "클럽장 위임 완료", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun appointSubMaster() {
        val question = QuestionDTO(
            QuestionDTO.Stat.INFO,
            "부클럽장 임명",
            "[${selectedMember?.userNickname}]님을 부클럽장으로 임명 하시겠습니까?"
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
            selectedMember?.position = MemberDTO.Position.SUB_MASTER
            //firebaseViewModel.updateMemberPosition(fanClubDTO?.docName.toString(), selectedMember!!) { // 선택한 멤버 부클럽장 임명
            firebaseViewModel.updateFanClubSubMaster(fanClubDTO?.docName.toString(), selectedMember!!) { fanClub -> // 선택한 멤버 부클럽장 임명
                if (fanClub != null) {
                    fanClubDTO = fanClub
                    members[selectedPosition!!].position = MemberDTO.Position.SUB_MASTER
                    recyclerViewAdapter.notifyDataSetChanged()
                    selectRecyclerView()
                    binding.textSubMasterCount.text = "${fanClubDTO?.subMasterCount}/${fanClubDTO?.getMaxSubMasterCount()}"
                    Toast.makeText(activity, "새로운 부클럽장 임명 완료", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fireSubMaster() {
        val question = QuestionDTO(
            QuestionDTO.Stat.WARNING,
            "부클럽장 해임",
            "[${selectedMember?.userNickname}]님을 부클럽장에서 해임 하시겠습니까?"
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
            selectedMember?.position = MemberDTO.Position.MEMBER
            //firebaseViewModel.updateMemberPosition(fanClubDTO?.docName.toString(), selectedMember!!) { // 선택한 멤버 부클럽장 해임
            firebaseViewModel.updateFanClubSubMaster(fanClubDTO?.docName.toString(), selectedMember!!, true) { fanClub -> // 선택한 멤버 부클럽장 해임
                if (fanClub != null) {
                    fanClubDTO = fanClub
                    members[selectedPosition!!].position = MemberDTO.Position.MEMBER
                    recyclerViewAdapter.notifyDataSetChanged()
                    selectRecyclerView()
                    binding.textSubMasterCount.text = "${fanClubDTO?.subMasterCount}/${fanClubDTO?.getMaxSubMasterCount()}"
                    Toast.makeText(activity, "부클럽장 해임 완료", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deportation() {
        val question = QuestionDTO(
            QuestionDTO.Stat.WARNING,
            "강제 추방",
            "[${selectedMember?.userNickname}]님을 강제 추방 하시겠습니까?"
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

            // 팬클럽 멤버에서 삭제
            (activity as MainActivity?)?.loading()
            val date = Date()
            firebaseViewModel.deleteMember(fanClubDTO?.docName.toString(), selectedMember!!) { fanClub ->
                if (fanClub != null) {
                    fanClubDTO = fanClub

                    var log = LogDTO("[클럽원 강제 추방] 추방된 클럽원 : ${selectedMember?.userNickname}(${selectedMember?.userUid.toString()}), 추방한 관리자 : ${currentMember?.userNickname}(${currentMember?.userUid.toString()})", date)
                    firebaseViewModel.writeFanClubLog(fanClubDTO?.docName.toString(), log) { }

                    firebaseViewModel.deleteUserFanClubId(selectedMember?.userUid.toString(), true) {
                        // 팬클럽 추방 우편으로 발송
                        val docName = "master${System.currentTimeMillis()}"
                        val calendar= Calendar.getInstance()
                        calendar.add(Calendar.DATE, 7)
                        var mail = MailDTO(docName,"팬클럽 추방", "죄송합니다. 가입하신 팬클럽 [${fanClubDTO?.name}]에서 추방 당하셨습니다.", "시스템", MailDTO.Item.NONE, 0, date, calendar.time)
                        firebaseViewModel.sendUserMail(selectedMember?.userUid.toString(), mail) {
                            var log2 = LogDTO("[팬클럽 추방됨] 추방된 팬클럽 : ${fanClubDTO?.name}(${fanClubDTO?.docName.toString()}), 추방한 관리자 : ${currentMember?.userNickname}(${currentMember?.userUid.toString()})", date)
                            firebaseViewModel.writeUserLog(selectedMember?.userUid.toString(), log2) { }
                        }

                        selectRecyclerView()
                        refreshMembers()
                        setFanClubInfo()

                        Toast.makeText(activity, "강제 추방 완료", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    (activity as MainActivity?)?.loadingEnd()
                }
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
        firebaseViewModel.getMembers(fanClubDTO?.docName.toString(), FirebaseRepository.MemberType.MEMBER_ONLY) {
            members = it
            recyclerViewAdapter = RecyclerViewAdapterFanClubMember(members, this)
            recyclerView.adapter = recyclerViewAdapter

            (activity as MainActivity?)?.loadingEnd()
        }
    }

    private fun setFanClubInfo() {
        binding.textSubMasterCount.text = "${fanClubDTO?.subMasterCount}/${fanClubDTO?.getMaxSubMasterCount()}"
        binding.textMemberCount.text = "${fanClubDTO?.memberCount}/${fanClubDTO?.getMaxMemberCount()}"

        visibleMenu()
    }

    private fun visibleMenu() {
        when {
            isMaster() -> { // 클럽장 메뉴 활성화
                binding.buttonDelegateMaster.visibility = View.VISIBLE
                binding.buttonAppointSubMaster.visibility = View.VISIBLE
                binding.buttonFireSubMaster.visibility = View.VISIBLE
                binding.buttonDeportation.visibility = View.VISIBLE
            }
            isAdministrator() -> { // 부클럽장 메뉴 활성화
                binding.buttonDelegateMaster.visibility = View.GONE
                binding.buttonAppointSubMaster.visibility = View.GONE
                binding.buttonFireSubMaster.visibility = View.GONE
                binding.buttonDeportation.visibility = View.VISIBLE
            }
            else -> { // 클럽원
                binding.buttonDelegateMaster.visibility = View.GONE
                binding.buttonAppointSubMaster.visibility = View.GONE
                binding.buttonFireSubMaster.visibility = View.GONE
                binding.buttonDeportation.visibility = View.GONE
            }
        }
    }

    private fun isMaster() : Boolean {
        return currentMember?.position == MemberDTO.Position.MASTER
    }

    private fun isAdministrator() : Boolean {
        return currentMember?.position == MemberDTO.Position.MASTER || currentMember?.position == MemberDTO.Position.SUB_MASTER
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentFanClubMember.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentFanClubMember().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun selectRecyclerView() {
        if (recyclerViewAdapter.selectItem(selectedPosition!!)) { // 선택 일 경우 메뉴 표시 및 레이아웃 어둡게
            val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
            binding.layoutMenu.visibility = View.VISIBLE
            binding.layoutMenu.startAnimation(translateUp)
            //recyclerView.smoothSnapToPosition(position)
        } else { // 해제 일 경우 메뉴 숨김 및 레이아웃 밝게
            val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
            binding.layoutMenu.visibility = View.GONE
            binding.layoutMenu.startAnimation(translateDown)
            //recyclerView.smoothSnapToPosition(position)
        }
    }

    override fun onItemClick(item: MemberDTO, position: Int) {
        val user = (activity as MainActivity?)?.getUser()

        // 본인 선택 이거나 클럽장 선택 했을경우 사용자 정보 보기만 가능
        if (user?.uid == item.userUid || item.position == MemberDTO.Position.MASTER) {
            binding.buttonDelegateMaster.visibility = View.GONE
            binding.buttonAppointSubMaster.visibility = View.GONE
            binding.buttonFireSubMaster.visibility = View.GONE
            binding.buttonDeportation.visibility = View.GONE
        } else {
            visibleMenu()

            // 클럽장이 부클럽장 선택 했을 경우 이미 부클럽장일 경우 임명 대신 해임 메뉴 보여줌
            if (isMaster()) {
                if (item.position == MemberDTO.Position.SUB_MASTER) {
                    binding.buttonAppointSubMaster.visibility = View.GONE
                    binding.buttonFireSubMaster.visibility = View.VISIBLE
                } else {
                    binding.buttonAppointSubMaster.visibility = View.VISIBLE
                    binding.buttonFireSubMaster.visibility = View.GONE
                }
            } else if (isAdministrator()) { // 부클럽장이 같은 부클럽장 선택 시 추방 메뉴 비활성
                if (item.position == MemberDTO.Position.SUB_MASTER) {
                    binding.buttonDeportation.visibility = View.GONE
                }
            }
        }

        selectedMember = item
        selectedPosition = position
        selectRecyclerView()

        // 본인 또는 클럽장이 아닐때만 메뉴 표시
        /*if (user?.uid != item.userUid && item.position != MemberDTO.Position.MASTER) {
            selectedMember = item
            selectedPosition = position
            selectRecyclerView()

            if (isMaster()) {
                if (item.position == MemberDTO.Position.SUB_MASTER) {
                    binding.buttonAppointSubMaster.visibility = View.GONE
                    binding.buttonFireSubMaster.visibility = View.VISIBLE
                } else {
                    binding.buttonAppointSubMaster.visibility = View.VISIBLE
                    binding.buttonFireSubMaster.visibility = View.GONE
                }
            }
        }*/
    }
}