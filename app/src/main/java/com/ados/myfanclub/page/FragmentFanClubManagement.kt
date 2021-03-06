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
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.MySharedPreferences
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentFanClubManagementBinding
import com.ados.myfanclub.dialog.EditTextModifyDialog
import com.ados.myfanclub.dialog.QuestionDialog
import com.ados.myfanclub.model.*
import com.ados.myfanclub.repository.FirebaseRepository
import com.ados.myfanclub.util.Utility
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.google.android.material.snackbar.Snackbar
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

    private lateinit var callback: OnBackPressedCallback

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterFanClubSignUp

    private val firebaseViewModel : FirebaseViewModel by viewModels()

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null
    private var membersEx : ArrayList<MemberExDTO> = arrayListOf()

    private var questionDialog: QuestionDialog? = null
    private var editTextModifyDialog: EditTextModifyDialog? = null

    private val sharedPreferences: MySharedPreferences by lazy {
        MySharedPreferences(requireContext())
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
        _binding = FragmentFanClubManagementBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        recyclerView = rootView.findViewById(R.id.rv_fan_club_sign_up)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // ????????? ?????? ??????
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

        println("??? : ??????????????????????")

        binding.swipeRefreshLayout.setOnRefreshListener {
            refresh()
            refreshMembers()
        }

        binding.buttonModifyDescription.setOnClickListener {
            // ????????? ????????? ??????????????? ??????
            if (!(parentFragment as FragmentFanClubMain?)?.isRemoveAdmin()!!) {
                modifyDescription()
            }
        }

        binding.buttonApproval.setOnClickListener {
            // ????????? ????????? ??????????????? ??????
            if (!(parentFragment as FragmentFanClubMain?)?.isRemoveAdmin()!!) {
                hideMenu()

                (activity as MainActivity?)?.loading()
                fanClubDTO = (activity as MainActivity?)?.getFanClub()
                if (fanClubDTO?.memberCount!! >= fanClubDTO?.getMaxMemberCount()!!) {
                    Toast.makeText(activity, "??? ?????? ??????????????? ?????? ??? ????????????. ????????? ????????? ?????? ?????????.", Toast.LENGTH_SHORT).show()
                    refreshMembers()
                    (activity as MainActivity?)?.loadingEnd()
                } else {
                    var jobCount = 0
                    var successCount = 0
                    val date = Date()
                    val calendar= Calendar.getInstance()
                    calendar.add(Calendar.DATE, 7)
                    var currentUser = (activity as MainActivity?)?.getUser()
                    for (member in membersEx) {
                        if (!member.isSelected) {
                            continue
                        }

                        jobCount++
                        member.isSelected = false
                        member.memberDTO?.position = MemberDTO.Position.MEMBER
                        member.memberDTO?.responseTime = date

                        // ?????? ?????? ????????? ????????? ???????????? ????????? ??????
                        firebaseViewModel.getHaveFanClub(member.memberDTO?.userUid.toString()) { userDTO ->
                            if (userDTO == null) {
                                Toast.makeText(activity, "?????? ?????? ???????????? ????????? ????????? ?????????.", Toast.LENGTH_SHORT).show()
                                successCount++
                            } else {
                                // ????????? ????????? ??????
                                firebaseViewModel.updateMember(fanClubDTO?.docName.toString(), member.memberDTO!!) { // ????????? ?????? ??????
                                    firebaseViewModel.addFanClubMemberCount(fanClubDTO?.docName.toString(), 1) { fanClub -> // ????????? ????????? 1 ??????
                                        if (fanClub != null) {
                                            // ????????? ????????? ????????? ID ?????? ??? ????????? ?????? ?????? ??????
                                            userDTO.fanClubId = fanClubDTO?.docName
                                            firebaseViewModel.updateUserFanClubApproval(userDTO) {
                                                successCount++
                                            }

                                            // ????????? ?????? ?????? ????????? ???????????? ??????
                                            val displayText = "* ${member.memberDTO?.userNickname}?????? ???????????? ?????????????????????! ??????????????? ???????????????."
                                            val chat = DisplayBoardDTO(Utility.randomDocumentName(), displayText, null, null, null, 0, Date())
                                            firebaseViewModel.sendFanClubChat(fanClubDTO?.docName.toString(), chat) { }

                                            // ????????? ?????? ?????? ???????????? ??????
                                            val docName = "master${System.currentTimeMillis()}"
                                            var mail = MailDTO(docName,"????????? ?????? ??????", "???????????????! ????????? [${fanClubDTO?.name}]??? ?????? ???????????????! ?????? ????????? ???????????? ?????? ???????????? ????????? ????????? ????????? ??????????????????!", "?????????", MailDTO.Item.NONE, 0, date, calendar.time)
                                            firebaseViewModel.sendUserMail(member.memberDTO?.userUid.toString(), mail) {
                                                var log = LogDTO("[????????? ?????? ??????] ????????? ?????? - Name : ${fanClubDTO?.name}, docName : ${fanClubDTO?.docName} ?????? ??????, ???????????? : ${SimpleDateFormat("yyyy.MM.dd HH:mm").format(calendar.time)}??????", date)
                                                firebaseViewModel.writeUserLog(member.memberDTO?.userUid.toString(), log) { }
                                            }

                                            // ????????? ?????? ??????
                                            var log = LogDTO("[????????? ?????? ??????] ????????? ?????? (uid : ${member.memberDTO?.userUid}, nickname : ${member.memberDTO?.userNickname}), ????????? ????????? ?????? (uid : ${currentUser?.uid}, nickname : ${currentUser?.nickname})", date)
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
                                Toast.makeText(activity, "?????? ?????? ??????!", Toast.LENGTH_SHORT).show()
                                refreshMembers()
                            }
                        }
                    }
                }
            }
        }

        binding.buttonReject.setOnClickListener {
            // ????????? ????????? ??????????????? ??????
            if (!(parentFragment as FragmentFanClubMain?)?.isRemoveAdmin()!!) {
                hideMenu()
                (activity as MainActivity?)?.loading()
                var jobCount = 0
                var successCount = 0
                val date = Date()
                val calendar= Calendar.getInstance()
                calendar.add(Calendar.DATE, 7)
                for (member in membersEx) {
                    if (member.isSelected) {
                        jobCount++
                        firebaseViewModel.updateUserFanClubReject(fanClubDTO?.docName.toString(), member.memberDTO?.userUid.toString()) {
                            successCount++

                            // ????????? ?????? ?????? ???????????? ??????
                            val docName = "master${System.currentTimeMillis()}"
                            var mail = MailDTO(docName,"????????? ?????? ??????", "???????????????. ????????? [${fanClubDTO?.name}]??? ????????? ?????????????????????.", "?????????", MailDTO.Item.NONE, 0, date, calendar.time)
                            firebaseViewModel.sendUserMail(member.memberDTO?.userUid.toString(), mail) {
                                var log = LogDTO("[????????? ?????? ??????] ????????? ?????? - Name : ${fanClubDTO?.name}, docName : ${fanClubDTO?.docName} ?????? ??????, ???????????? : ${SimpleDateFormat("yyyy.MM.dd HH:mm").format(calendar.time)}??????", date)
                                firebaseViewModel.writeUserLog(member.memberDTO?.userUid.toString(), log) { }
                            }
                        }
                    }
                }

                timer(period = 100)
                {
                    if (jobCount == successCount) {
                        cancel()
                        activity?.runOnUiThread {
                            Toast.makeText(activity, "?????? ?????? ??????!", Toast.LENGTH_SHORT).show()
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
            isAdministrator() -> { // ?????????, ???????????? ?????? ?????????
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

        Toast.makeText(context, "?????? ??????", Toast.LENGTH_SHORT).show()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun refreshMembers() {
        firebaseViewModel.getMembers(fanClubDTO?.docName.toString(), FirebaseRepository.MemberType.GUEST_ONLY) {
            membersEx.clear()
            for (member in it) {
                membersEx.add(MemberExDTO(member))
            }

            recyclerViewAdapter = RecyclerViewAdapterFanClubSignUp(membersEx, this)
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
        val item = EditTextDTO("????????? ?????? ??????", fanClubDTO?.description, 600)
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
                "????????? ?????? ??????",
                "????????? ?????? ???????????? ????????? ??? ????????????.\n?????? ?????? ???????????????????",
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

                fanClubDTO?.description = editTextModifyDialog?.binding?.editContent?.text.toString()
                firebaseViewModel.updateFanClubDescription(fanClubDTO!!) {
                    Toast.makeText(activity, "????????? ?????? ?????? ??????!", Toast.LENGTH_SHORT).show()
                    binding.editDescription.setText(fanClubDTO?.description)
                }
            }
        }
    }

    private fun fanClubQuit() {
        // ???????????? ????????? ?????? ?????? ????????? ????????? ????????? ????????? ????????? ??????????????? ?????? ??????
        if (currentMember?.position == MemberDTO.Position.MASTER && fanClubDTO?.memberCount!! > 1) {
            val question = QuestionDTO(
                QuestionDTO.Stat.ERROR,
                "????????? ??????",
                "???????????? ????????? ????????? ??? ??? ????????????. ???????????? ????????? ??? ????????? ???????????????.",
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
            questionDialog?.setButtonCancel("??????")
            questionDialog?.showButtonOk(false)
            questionDialog?.binding?.buttonQuestionCancel?.setOnClickListener { // No
                questionDialog?.dismiss()
                questionDialog = null
            }
        } else {
            val question = QuestionDTO(
                QuestionDTO.Stat.ERROR,
                "????????? ??????",
                "????????? ?????? ??? 24?????? ?????? ????????? ????????? ???????????? ????????? ??? ?????? ????????? ????????? ?????????. ?????? ?????? ???????????????????",
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

                // ????????? ???????????? ??????
                firebaseViewModel.deleteMember(fanClubDTO?.docName.toString(), currentMember!!) { fanClub ->
                    if (fanClub != null) {
                        // ????????? ???????????? ????????? ?????? ??????
                        val user = (activity as MainActivity?)?.getUser()!!
                        firebaseViewModel.deleteUserFanClubId(user.uid.toString()) { }

                        if (fanClub.memberCount!! <= 0) { // ????????? ????????? ???????????? ????????? ????????? ??????
                            firebaseViewModel.deleteFanClub(fanClub.docName.toString()) {
                                var log = LogDTO("[????????? ??????] ????????? ??? : ${fanClubDTO?.name}(${fanClubDTO?.docName.toString()}), ????????? ????????? : ${currentMember?.userNickname}(${currentMember?.userUid.toString()})", Date())
                                firebaseViewModel.writeAdminLog(log) { }
                            }
                        } else {
                            val displayText = "* ${currentMember?.userNickname}?????? ???????????? ?????????????????????."
                            val chat = DisplayBoardDTO(Utility.randomDocumentName(), displayText, null, null, null, 0, Date())
                            firebaseViewModel.sendFanClubChat(fanClubDTO?.docName.toString(), chat) { }

                            sharedPreferences.putInt(MySharedPreferences.PREF_KEY_LAST_FAN_CLUB_LEVEL, 0)
                            sharedPreferences.putString(MySharedPreferences.PREF_KEY_LAST_MEMBER_POSITION, "")

                            var log = LogDTO("[????????? ??????] ${currentMember?.userNickname}(${currentMember?.userUid.toString()})", Date())
                            firebaseViewModel.writeFanClubLog(fanClub.docName.toString(), log) { }
                        }

                        var log = LogDTO("[????????? ??????] ????????? ??? : ${fanClubDTO?.name}(${fanClubDTO?.docName.toString()})", Date())
                        firebaseViewModel.writeUserLog(currentMember?.userUid.toString(), log) { }
                    }
                    (activity as MainActivity?)?.loadingEnd()
                }
            }
        }
    }

    private fun selectRecyclerView() {
        if (recyclerViewAdapter.isChecked()) { // ????????? ????????? ???????????? ????????? ??????
            if (!binding.layoutMenu.isVisible) { // ?????? ???????????? ?????? ?????? ?????? ?????? ???????????? ??????
                val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
                binding.layoutMenu.visibility = View.VISIBLE
                binding.layoutMenu.startAnimation(translateUp)
                //recyclerView.smoothSnapToPosition(position)
            }
        } else { // ?????? ?????? ???????????? ?????? ??????
            hideMenu()
        }
    }

    override fun onItemClick(item: MemberExDTO, position: Int) {
        selectRecyclerView()
    }
}