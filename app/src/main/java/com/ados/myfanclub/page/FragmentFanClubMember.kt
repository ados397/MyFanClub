package com.ados.myfanclub.page

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.database.DBHelperReport
import com.ados.myfanclub.databinding.FragmentFanClubMemberBinding
import com.ados.myfanclub.dialog.QuestionDialog
import com.ados.myfanclub.dialog.ReportDialog
import com.ados.myfanclub.dialog.UserInfoDialog
import com.ados.myfanclub.model.*
import com.ados.myfanclub.repository.FirebaseRepository
import com.ados.myfanclub.util.Utility
import com.ados.myfanclub.viewmodel.FirebaseStorageViewModel
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.google.android.material.snackbar.Snackbar
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

    private lateinit var callback: OnBackPressedCallback

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterFanClubMember

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val firebaseStorageViewModel : FirebaseStorageViewModel by viewModels()

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null
    private var membersEx : ArrayList<MemberExDTO> = arrayListOf()

    private var questionDialog: QuestionDialog? = null
    private var userInfoDialog: UserInfoDialog? = null
    private var reportDialog : ReportDialog? = null
    lateinit var dbHandler : DBHelperReport

    private var selectedMemberEx: MemberExDTO? = null
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

        dbHandler = DBHelperReport(requireContext())

        // ?????? ?????? ??????
        binding.layoutMenu.visibility = View.GONE

        setFanClubInfo()

        refreshMembers()

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (activity as MainActivity?)?.backPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
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
            // ????????? ????????? ??????????????? ??????
            if (!(parentFragment as FragmentFanClubMain?)?.isRemoveAdmin()!!) {
                delegateMaster()
            }
        }

        binding.buttonAppointSubMaster.setOnClickListener {
            // ????????? ????????? ??????????????? ??????
            if (!(parentFragment as FragmentFanClubMain?)?.isRemoveAdmin()!!) {
                if (fanClubDTO?.subMasterCount!! >= fanClubDTO?.getMaxSubMasterCount()!!) {
                    selectRecyclerView()
                    Toast.makeText(activity, "??? ?????? ??????????????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show()
                } else {
                    appointSubMaster()
                }
            }
        }

        binding.buttonFireSubMaster.setOnClickListener {
            // ????????? ????????? ??????????????? ??????
            if (!(parentFragment as FragmentFanClubMain?)?.isRemoveAdmin()!!) {
                fireSubMaster()
            }
        }

        binding.buttonDeportation.setOnClickListener {
            // ????????? ????????? ??????????????? ??????
            if (!(parentFragment as FragmentFanClubMain?)?.isRemoveAdmin()!!) {
                deportation()
            }
        }

        binding.buttonUserInfo.setOnClickListener {
            selectRecyclerView()
            firebaseViewModel.getUser(selectedMemberEx?.memberDTO?.userUid.toString()) { userDTO ->
                if (userDTO != null) {
                    var userEx = UserExDTO(userDTO)
                    if (userDTO.imgProfile != null) {
                        firebaseStorageViewModel.getUserProfileImage(selectedMemberEx?.memberDTO?.userUid.toString()) { uri ->
                            userEx.imgProfileUri = uri
                            onUserInfo(userEx)
                        }
                    } else {
                        onUserInfo(userEx)
                    }
                } else {
                    Toast.makeText(activity, "????????? ?????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun onUserInfo(userEx: UserExDTO) {
        if (userInfoDialog == null) {
            userInfoDialog = UserInfoDialog(requireContext(), selectedMemberEx?.memberDTO!!, userEx)
            userInfoDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            userInfoDialog?.setCanceledOnTouchOutside(false)
        } else {
            userInfoDialog?.member = selectedMemberEx?.memberDTO!!
            userInfoDialog?.user = userEx
        }
        userInfoDialog?.show()
        userInfoDialog?.setInfo()
        userInfoDialog?.binding?.buttonUserInfoClose?.setOnClickListener {
            userInfoDialog?.dismiss()
        }

        userInfoDialog?.binding?.buttonReport?.setOnClickListener {
            val user = (activity as MainActivity?)?.getUser()!!

            if (reportDialog == null) {
                reportDialog = ReportDialog(requireContext())
                reportDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                reportDialog?.setCanceledOnTouchOutside(false)
            }
            reportDialog?.reportDTO = ReportDTO(user.uid, user.nickname, selectedMemberEx?.memberDTO?.userUid, selectedMemberEx?.memberDTO?.userNickname, selectedMemberEx?.memberDTO?.userAboutMe, selectedMemberEx?.memberDTO?.userUid, ReportDTO.Type.User)
            reportDialog?.show()
            reportDialog?.setInfo()

            reportDialog?.setOnDismissListener {
                if (!reportDialog?.reportDTO?.reason.isNullOrEmpty()) {
                    firebaseViewModel.sendReport(reportDialog?.reportDTO!!) {
                        if (!dbHandler.getBlock(reportDialog?.reportDTO?.contentDocName.toString())) {
                            dbHandler.updateBlock(reportDialog?.reportDTO?.contentDocName.toString(), 1)
                        } else {
                            dbHandler.updateBlock(reportDialog?.reportDTO?.contentDocName.toString(), 0)
                        }

                        selectedMemberEx?.isBlocked = true
                        recyclerViewAdapter.notifyItemChanged(selectedPosition!!)
                        userInfoDialog?.binding?.buttonUserInfoClose?.performClick()
                        Toast.makeText(activity, "?????? ?????? ?????????????????????.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun delegateMaster() {
        val question = QuestionDTO(
            QuestionDTO.Stat.ERROR,
            "????????? ??????",
            "[${selectedMemberEx?.memberDTO?.userNickname}]????????? ???????????? ?????????????????????????\n???????????? ?????????????????? ??????????????? ???????????????."
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
        questionDialog?.binding?.buttonQuestionOk?.setOnClickListener { // Ok
            questionDialog?.dismiss()
            questionDialog = null
            (activity as MainActivity?)?.loading()
            selectedMemberEx?.memberDTO?.position = MemberDTO.Position.MASTER
            firebaseViewModel.updateMemberPosition(fanClubDTO?.docName.toString(), selectedMemberEx?.memberDTO!!) { // ????????? ?????? ????????? ??????
                currentMember?.position = MemberDTO.Position.MEMBER
                firebaseViewModel.updateMemberPosition(fanClubDTO?.docName.toString(), currentMember!!) { // ?????? ?????????(?????????) ??????????????? ??????
                    // ???????????? ????????? ???????????? ????????? ????????? ????????? ?????? ??????
                    fanClubDTO?.masterUid = selectedMemberEx?.memberDTO?.userUid
                    fanClubDTO?.masterNickname = selectedMemberEx?.memberDTO?.userNickname
                    firebaseViewModel.updateFanClub(fanClubDTO!!) {
                        val displayText = "* ${selectedMemberEx?.memberDTO?.userNickname}?????? ????????? ???????????? ???????????????!"
                        val chat = DisplayBoardDTO(Utility.randomDocumentName(), displayText, null, null, null, 0, Date())
                        firebaseViewModel.sendFanClubChat(fanClubDTO?.docName.toString(), chat) { }

                        selectRecyclerView()
                        refreshMembers()
                        Toast.makeText(activity, "????????? ?????? ??????", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun appointSubMaster() {
        val question = QuestionDTO(
            QuestionDTO.Stat.INFO,
            "???????????? ??????",
            "[${selectedMemberEx?.memberDTO?.userNickname}]?????? ?????????????????? ?????? ???????????????????"
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
        questionDialog?.binding?.buttonQuestionOk?.setOnClickListener { // Ok
            questionDialog?.dismiss()
            questionDialog = null
            selectedMemberEx?.memberDTO?.position = MemberDTO.Position.SUB_MASTER
            //firebaseViewModel.updateMemberPosition(fanClubDTO?.docName.toString(), selectedMemberEx?.memberDTO!!) { // ????????? ?????? ???????????? ??????
            firebaseViewModel.updateFanClubSubMaster(fanClubDTO?.docName.toString(), selectedMemberEx?.memberDTO!!) { fanClub -> // ????????? ?????? ???????????? ??????
                if (fanClub != null) {
                    fanClubDTO = fanClub

                    val displayText = "* ${selectedMemberEx?.memberDTO?.userNickname}?????? ????????? ??????????????? ???????????????!"
                    val chat = DisplayBoardDTO(Utility.randomDocumentName(), displayText, null, null, null, 0, Date())
                    firebaseViewModel.sendFanClubChat(fanClubDTO?.docName.toString(), chat) { }

                    membersEx[selectedPosition!!].memberDTO?.position = MemberDTO.Position.SUB_MASTER
                    recyclerViewAdapter.notifyDataSetChanged()
                    selectRecyclerView()
                    binding.textSubMasterCount.text = "${fanClubDTO?.subMasterCount}/${fanClubDTO?.getMaxSubMasterCount()}"
                    Toast.makeText(activity, "????????? ???????????? ?????? ??????", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fireSubMaster() {
        val question = QuestionDTO(
            QuestionDTO.Stat.WARNING,
            "???????????? ??????",
            "[${selectedMemberEx?.memberDTO?.userNickname}]?????? ?????????????????? ?????? ???????????????????"
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
        questionDialog?.binding?.buttonQuestionOk?.setOnClickListener { // Ok
            questionDialog?.dismiss()
            questionDialog = null
            selectedMemberEx?.memberDTO?.position = MemberDTO.Position.MEMBER
            //firebaseViewModel.updateMemberPosition(fanClubDTO?.docName.toString(), selectedMemberEx?.memberDTO!!) { // ????????? ?????? ???????????? ??????
            firebaseViewModel.updateFanClubSubMaster(fanClubDTO?.docName.toString(), selectedMemberEx?.memberDTO!!, true) { fanClub -> // ????????? ?????? ???????????? ??????
                if (fanClub != null) {
                    fanClubDTO = fanClub
                    membersEx[selectedPosition!!].memberDTO?.position = MemberDTO.Position.MEMBER
                    recyclerViewAdapter.notifyDataSetChanged()
                    selectRecyclerView()
                    binding.textSubMasterCount.text = "${fanClubDTO?.subMasterCount}/${fanClubDTO?.getMaxSubMasterCount()}"
                    Toast.makeText(activity, "???????????? ?????? ??????", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deportation() {
        val question = QuestionDTO(
            QuestionDTO.Stat.WARNING,
            "?????? ??????",
            "[${selectedMemberEx?.memberDTO?.userNickname}]?????? ?????? ?????? ???????????????????"
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
        questionDialog?.binding?.buttonQuestionOk?.setOnClickListener { // Ok
            questionDialog?.dismiss()
            questionDialog = null

            // ????????? ???????????? ??????
            (activity as MainActivity?)?.loading()
            val date = Date()
            firebaseViewModel.deleteMember(fanClubDTO?.docName.toString(), selectedMemberEx?.memberDTO!!) { fanClub ->
                if (fanClub != null) {
                    fanClubDTO = fanClub

                    var log = LogDTO("[????????? ?????? ??????] ????????? ????????? : ${selectedMemberEx?.memberDTO?.userNickname}(${selectedMemberEx?.memberDTO?.userUid.toString()}), ????????? ????????? : ${currentMember?.userNickname}(${currentMember?.userUid.toString()})", date)
                    firebaseViewModel.writeFanClubLog(fanClubDTO?.docName.toString(), log) { }

                    firebaseViewModel.deleteUserFanClubId(selectedMemberEx?.memberDTO?.userUid.toString(), true) {
                        val displayText = "* ${selectedMemberEx?.memberDTO?.userNickname}?????? ???????????? ?????????????????????."
                        val chat = DisplayBoardDTO(Utility.randomDocumentName(), displayText, null, null, null, 0, Date())
                        firebaseViewModel.sendFanClubChat(fanClubDTO?.docName.toString(), chat) { }

                        // ????????? ?????? ???????????? ??????
                        val docName = "master${System.currentTimeMillis()}"
                        val calendar= Calendar.getInstance()
                        calendar.add(Calendar.DATE, 7)
                        var mail = MailDTO(docName,"????????? ??????", "???????????????. ???????????? ????????? [${fanClubDTO?.name}]?????? ?????? ??????????????????.", "?????????", MailDTO.Item.NONE, 0, date, calendar.time)
                        firebaseViewModel.sendUserMail(selectedMemberEx?.memberDTO?.userUid.toString(), mail) {
                            var log2 = LogDTO("[????????? ?????????] ????????? ????????? : ${fanClubDTO?.name}(${fanClubDTO?.docName.toString()}), ????????? ????????? : ${currentMember?.userNickname}(${currentMember?.userUid.toString()})", date)
                            firebaseViewModel.writeUserLog(selectedMemberEx?.memberDTO?.userUid.toString(), log2) { }
                        }

                        selectRecyclerView()
                        refreshMembers()
                        setFanClubInfo()

                        Toast.makeText(activity, "?????? ?????? ??????", Toast.LENGTH_SHORT).show()
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

        Toast.makeText(context, "?????? ??????", Toast.LENGTH_SHORT).show()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun refreshMembers() {
        firebaseViewModel.getMembers(fanClubDTO?.docName.toString(), FirebaseRepository.MemberType.MEMBER_ONLY) {
            membersEx.clear()
            for (member in it) {
                membersEx.add(MemberExDTO(member, dbHandler.getBlock(member.userUid.toString())))
            }
            recyclerViewAdapter = RecyclerViewAdapterFanClubMember(membersEx, this)
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
            isMaster() -> { // ????????? ?????? ?????????
                binding.buttonDelegateMaster.visibility = View.VISIBLE
                binding.buttonAppointSubMaster.visibility = View.VISIBLE
                binding.buttonFireSubMaster.visibility = View.VISIBLE
                binding.buttonDeportation.visibility = View.VISIBLE
            }
            isAdministrator() -> { // ???????????? ?????? ?????????
                binding.buttonDelegateMaster.visibility = View.GONE
                binding.buttonAppointSubMaster.visibility = View.GONE
                binding.buttonFireSubMaster.visibility = View.GONE
                binding.buttonDeportation.visibility = View.VISIBLE
            }
            else -> { // ?????????
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
        if (recyclerViewAdapter.selectItem(selectedPosition!!)) { // ?????? ??? ?????? ?????? ?????? ??? ???????????? ?????????
            val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
            binding.layoutMenu.visibility = View.VISIBLE
            binding.layoutMenu.startAnimation(translateUp)
            //recyclerView.smoothSnapToPosition(position)
        } else { // ?????? ??? ?????? ?????? ?????? ??? ???????????? ??????
            val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
            binding.layoutMenu.visibility = View.GONE
            binding.layoutMenu.startAnimation(translateDown)
            //recyclerView.smoothSnapToPosition(position)
        }
    }

    override fun onItemClick(item: MemberExDTO, position: Int) {
        val user = (activity as MainActivity?)?.getUser()

        if (item.isBlocked) {
            Toast.makeText(activity, "????????? ????????? ?????????.", Toast.LENGTH_SHORT).show()
        } else if (user?.uid != item.memberDTO?.userUid) { // ?????? ????????? ?????? ??????
            // ????????? ?????? ???????????? ????????? ?????? ????????? ??????
            if (item.memberDTO?.position == MemberDTO.Position.MASTER) {
                binding.buttonDelegateMaster.visibility = View.GONE
                binding.buttonAppointSubMaster.visibility = View.GONE
                binding.buttonFireSubMaster.visibility = View.GONE
                binding.buttonDeportation.visibility = View.GONE
            } else {
                visibleMenu()

                // ???????????? ???????????? ?????? ?????? ?????? ?????? ??????????????? ?????? ?????? ?????? ?????? ?????? ?????????
                if (isMaster()) {
                    if (item.memberDTO?.position == MemberDTO.Position.SUB_MASTER) {
                        binding.buttonAppointSubMaster.visibility = View.GONE
                        binding.buttonFireSubMaster.visibility = View.VISIBLE
                    } else {
                        binding.buttonAppointSubMaster.visibility = View.VISIBLE
                        binding.buttonFireSubMaster.visibility = View.GONE
                    }
                } else if (isAdministrator()) { // ??????????????? ?????? ???????????? ?????? ??? ?????? ?????? ?????????
                    if (item.memberDTO?.position == MemberDTO.Position.SUB_MASTER) {
                        binding.buttonDeportation.visibility = View.GONE
                    }
                }
            }

            selectedMemberEx = item
            selectedPosition = position
            selectRecyclerView()
        }

        // ?????? ?????? ???????????? ???????????? ?????? ??????
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