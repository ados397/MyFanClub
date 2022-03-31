package com.ados.myfanclub.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.MyPagerAdapterFanClub
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentFanClubMainBinding
import com.ados.myfanclub.dialog.GetItemDialog
import com.ados.myfanclub.dialog.QuestionDialog
import com.ados.myfanclub.model.*
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import java.util.*
import kotlin.concurrent.timer

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFanClubMain.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFanClubMain : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: Int? = 0
    private var param2: String? = null

    private var _binding: FragmentFanClubMainBinding? = null
    private val binding get() = _binding!!

    private val firebaseViewModel : FirebaseViewModel by viewModels()

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null

    private var lastChatDate = Date()
    private var displayCount = 0 // 전광판 일정 시간 유지를 위한 변수
    private var displayChat = DisplayBoardDTO() // 표시할 가장 최근 채팅
    private var chatTimer : Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
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
        _binding = FragmentFanClubMainBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        binding.textChat.visibility = View.GONE

        chatTimer()
        firebaseViewModel.getFanClubChatListen(fanClubDTO?.docName.toString())
        firebaseViewModel.fanClubChatDTO.observe(viewLifecycleOwner) {
            displayChat = firebaseViewModel.fanClubChatDTO.value!!
        }

        return rootView
    }

    override fun onDestroyView() {
        chatTimer?.cancel()
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeTutorial()

        setFanClubInfo()

        binding.viewpager.isUserInputEnabled = false // 좌우 터치 스와이프 금지
        binding.viewpager.apply {
            println("탭 : 이쪽인가?")
            //adapter = MyPagerAdapterFanClub(context as FragmentActivity, fanClubDTO!!, currentMember!!)
            adapter = MyPagerAdapterFanClub(childFragmentManager, viewLifecycleOwner.lifecycle)
            setPageTransformer(ZoomOutPageTransformer())

        }

        binding.viewpager.post {
            if (param1 != 0) {
                println("탭 : 이게 들어오나?")
                changeTab(param1!!)
            }
        }

        binding.buttonChat.setOnClickListener {
            if (chatTimer != null) {
                chatTimer?.cancel()
            }

            val fragment = FragmentChat.newInstance(binding.viewpager.currentItem, "")
            parentFragmentManager.beginTransaction().apply{
                replace(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
                commit()
            }
        }

        binding.textTabInfo.setOnClickListener {
            changeTab(0)
        }
        binding.textTabMember.setOnClickListener {
            changeTab(1)
        }
        binding.textTabRank.setOnClickListener {
            changeTab(2)
        }
        binding.textTabManagement.setOnClickListener {
            changeTab(3)
        }
        binding.textTabSchedule.setOnClickListener {
            // 관리자 권한이 없어졌다면 스케줄 탭 삭제를 위해 갱신, 아니라면 탭 이동
            if (!isRemoveAdmin()) {
                changeTab(4)
            }
        }
    }

    // 채팅 타이머
    private fun chatTimer() {
        val preferencesDTO = (activity as MainActivity?)?.getPreferences()!!
        chatTimer = timer(period = 1000)
        {
            // 새로운 채팅 표시
            if (displayChat.createTime != null && displayChat.createTime!! > lastChatDate) {
                activity?.runOnUiThread {
                    lastChatDate = displayChat.createTime!!
                    displayCount = 0
                    if (displayChat.userNickname.isNullOrEmpty()) {
                        binding.textChat.text = "${displayChat.displayText}"
                    } else {
                        binding.textChat.text = "${displayChat.userNickname} : ${displayChat.displayText}"
                    }
                    openChat()
                }
            } else if (displayCount >= preferencesDTO.fanClubChatDisplayPeriod!!) { // 표시 시간이 지났다면 채팅창 닫음
                activity?.runOnUiThread {
                    closeChat()
                }
            }
            displayCount++
        }
    }

    private fun openChat() {
        if (binding.textChat.visibility == View.GONE) {
            val translateLeft = AnimationUtils.loadAnimation(context, R.anim.translate_left)
            binding.textChat.startAnimation(translateLeft)
            binding.textChat.visibility = View.VISIBLE
            binding.textChat.isSelected = true
            binding.textChat.requestFocus()
        }
    }

    private fun closeChat() {
        if (binding.textChat.visibility == View.VISIBLE) {
            val translateRight = AnimationUtils.loadAnimation(context, R.anim.translate_right)
            binding.textChat.startAnimation(translateRight)
            binding.textChat.visibility = View.GONE
        }
    }

    // 관리자 없다면 삭제되었다면 새로고침
    fun isRemoveAdmin() : Boolean {
        fanClubDTO = (activity as MainActivity?)?.getFanClub()
        currentMember = (activity as MainActivity?)?.getMember()
        //println("관리자 권한 체크 new: ${member.position}, old: ${currentMember?.position}")
        // 관리자 권한이 삭제됨
        //if (currentMember?.isAdministrator()!! && !member.isAdministrator()) {
        if (!currentMember?.isAdministrator()!!) {
            fanClubDTO = (activity as MainActivity?)?.getFanClub()
            currentMember = (activity as MainActivity?)?.getMember()

            val question = QuestionDTO(
                QuestionDTO.Stat.WARNING,
                "팬클럽 정보 변경",
                "팬클럽 정보가 변경되어 새로고침 합니다.",
            )
            val questionDialog = QuestionDialog(requireContext(), question)
            questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            questionDialog.setCanceledOnTouchOutside(false)
            questionDialog.show()
            questionDialog.showButtonOk(false)
            questionDialog.setButtonCancel("확인")
            questionDialog.binding.buttonQuestionCancel.setOnClickListener { // No
                questionDialog.dismiss()
                (activity as MainActivity?)?.loading()
                binding.viewpager.apply {
                    adapter = MyPagerAdapterFanClub(childFragmentManager, viewLifecycleOwner.lifecycle)
                    setPageTransformer(ZoomOutPageTransformer())
                    moveInfoPage()
                }
                setFanClubInfo()
                (activity as MainActivity?)?.loadingEnd()
            }
            return true
        } else {
            return false
        }
    }

    private fun setFanClubInfo() {
        when {
            currentMember?.isAdministrator()!! -> { // 클럽장, 부클럽장 메뉴 활성화
                binding.textTabSchedule.visibility = View.VISIBLE
            }
            else -> {
                binding.textTabSchedule.visibility = View.GONE
            }
        }
    }

    private fun moveInfoPage() {
        binding.viewpager.currentItem = 0
        releaseAllTabButton()
        setTabButton(binding.textTabInfo)
    }

    fun getFanClub() : FanClubDTO? {
        return fanClubDTO
    }

    fun getMember() : MemberDTO? {
        return currentMember
    }

    fun setFanClub(fanClub: FanClubDTO?) {
        fanClubDTO = fanClub
    }

    fun setMember(member: MemberDTO?) {
        currentMember = member
    }

    fun moveFanClubInitalize() {
        val fragment = FragmentFanClubInitalize()
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
    }

    private fun setTabButton(textView: TextView) {
        textView.background = AppCompatResources.getDrawable(requireContext(), R.drawable.btn_round)
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun releaseTabButton(textView: TextView) {
        textView.background = null
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }

    private fun releaseAllTabButton() {
        releaseTabButton(binding.textTabInfo)
        releaseTabButton(binding.textTabMember)
        releaseTabButton(binding.textTabRank)
        releaseTabButton(binding.textTabManagement)
        releaseTabButton(binding.textTabSchedule)
    }

    private fun changeTab(tab: Int) {
        println("탭 : $tab, 사이즈 : ${binding.viewpager.childCount}")
        //binding.viewpager.adapter?.notifyItemChanged(tab)
        binding.viewpager.currentItem = tab
        //binding.viewpager.currentItem = tab
        releaseAllTabButton()
        when (tab) {
            0 -> setTabButton(binding.textTabInfo)
            1 -> setTabButton(binding.textTabMember)
            2 -> setTabButton(binding.textTabRank)
            3 -> setTabButton(binding.textTabManagement)
            4 -> setTabButton(binding.textTabSchedule)
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentFanClubMain.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Int, param2: String) =
            FragmentFanClubMain().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun observeTutorial() {
        (activity as MainActivity?)?.getTutorialStep()?.observe(viewLifecycleOwner) {
            onTutorial((activity as MainActivity?)?.getTutorialStep()?.value!!)
        }
    }

    private fun onTutorial(step: Int) {
        when (step) {
            21 -> {
                println("튜토리얼 Step - $step")
                TapTargetSequence(requireActivity())
                    .targets(
                        TapTarget.forBounds((activity as MainActivity?)?.getMainLayoutRect(),
                            "같은 팬클럽에 속한 멤버들은 스케줄 공유, 채팅, 푸시메시지 발송 등 꼭 필요한 다양한 기능이 가능합니다.",
                            "- OK 버튼을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(requireContext(), R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.layoutMain,
                            "마음 맞는 팬클럽원들과 함께 즐겁고 스마트한 [ 마이팬클럽 ]을 즐겨보세요!!",
                            "- OK 버튼을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(requireContext(), R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.layoutMain,
                            "튜토리얼이 완료되었습니다! 튜토리얼 보상이 주어 집니다!",
                            "- OK 버튼을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(requireContext(), R.drawable.ok))
                            .tintTarget(true)).listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            (activity as MainActivity?)?.finishTutorialStep(true) // 튜토리얼 완료
                            rewardTutorialGem()
                        }
                        override fun onSequenceStep(tutorialStep: TapTarget, targetClicked: Boolean) {

                        }
                        override fun onSequenceCanceled(lastTarget: TapTarget) {

                        }
                    }).start()
            }
        }
    }

    private fun rewardTutorialGem() {
        val user = (activity as MainActivity?)?.getUser()!!
        val gemCount = (activity as MainActivity?)?.getPreferences()?.rewardTutorialGem!!
        val oldFreeGemCount = user.freeGem!!
        firebaseViewModel.addUserGem(user.uid.toString(), 0, gemCount) { userDTO ->
            if (userDTO != null) {
                var log = LogDTO("[튜토리얼 완료 다이아 획득] 다이아 $gemCount 획득 (freeGem : $oldFreeGemCount -> ${userDTO.freeGem})", Date())
                firebaseViewModel.writeUserLog(user.uid.toString(), log) { }

                val getDialog = GetItemDialog(requireContext())
                getDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                getDialog.setCanceledOnTouchOutside(false)
                getDialog.mailDTO = MailDTO("", "", "", "", MailDTO.Item.FREE_GEM, gemCount)
                getDialog.show()

                getDialog.binding.buttonGetItemOk.setOnClickListener {
                    getDialog.dismiss()

                }
            }
        }
    }

}