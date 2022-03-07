package com.ados.myfanclub.page

import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.MySharedPreferences
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentFanClubInfoBinding
import com.ados.myfanclub.dialog.*
import com.ados.myfanclub.model.*
import com.ados.myfanclub.repository.FirebaseRepository
import com.ados.myfanclub.util.AdsRewardManager
import com.ados.myfanclub.viewmodel.FirebaseStorageViewModel
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.edit_text_modify_dialog.*
import kotlinx.android.synthetic.main.exp_up_fan_club_dialog.*
import kotlinx.android.synthetic.main.fan_club_reward_dialog.*
import kotlinx.android.synthetic.main.gem_question_dialog.*
import kotlinx.android.synthetic.main.get_item_dialog.*
import kotlinx.android.synthetic.main.level_up_action_fan_club_dialog.*
import kotlinx.android.synthetic.main.level_up_fan_club_dialog.*
import kotlinx.android.synthetic.main.question_dialog.*
import kotlinx.android.synthetic.main.send_notice_dialog.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import kotlin.concurrent.timer

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFanClubInfo.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFanClubInfo : Fragment(), OnFanClubMemberItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    private var _binding: FragmentFanClubInfoBinding? = null
    private val binding get() = _binding!!

    // 뷰모델 연결
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val firebaseStorageViewModel : FirebaseStorageViewModel by viewModels()

    // AD
    private var adsRewardManagerExp: AdsRewardManager? = null
    private var adsRewardManagerGem: AdsRewardManager? = null

    //lateinit var recyclerView : RecyclerView
    //lateinit var recyclerViewAdapter : RecyclerViewAdapterFanClubMember

    //private var fanClubDTO: FanClubDTO? = null
    private var fanClubExDTO: FanClubExDTO? = null
    private var currentMember: MemberDTO? = null

    private var isFirstDisplayChat = true // 최초에 채팅 표시를 하지 않기 위한 변수
    private var displayCount = 0 // 전광판 일정 시간 유지를 위한 변수
    private var displayChat = DisplayBoardDTO() // 표시할 가장 최근 채팅
    private var lastChat = DisplayBoardDTO() // 가장 마지막에 표시한 채팅

    private var toast : Toast? = null

    private val sharedPreferences: MySharedPreferences by lazy {
        MySharedPreferences(requireContext())
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            modifySymbolApply("add_image", uri)
        } else {
            Toast.makeText(context, "이미지를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private var isSettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        //fanClubDTO = (activity as MainActivity?)?.getFanClub()
        fanClubExDTO = (activity as MainActivity?)?.getFanClubEx()
        currentMember = (activity as MainActivity?)?.getMember()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFanClubInfoBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        val adPolicyDTO = (activity as MainActivity?)?.getAdPolicy()
        adsRewardManagerExp = AdsRewardManager(requireActivity(), adPolicyDTO!!, AdsRewardManager.RewardType.REWARD_FAN_CLUB_EXP)
        adsRewardManagerGem = AdsRewardManager(requireActivity(), adPolicyDTO!!, AdsRewardManager.RewardType.REWARD_FAN_CLUB_GEM)

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setInfo()

        binding.textNoticeContent.movementMethod = ScrollingMovementMethod.getInstance()
        binding.textNoticeContent.setOnTouchListener { view, motionEvent ->
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
            false
        }

        binding.editAboutMe.setOnTouchListener { view, motionEvent ->
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
            false
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            (activity as MainActivity?)?.loading()
            fanClubExDTO = (activity as MainActivity?)?.getFanClubEx()
            currentMember = (activity as MainActivity?)?.getMember()

            setInfo()
            (activity as MainActivity?)?.loadingEnd()

            Toast.makeText(context, "새로 고침", Toast.LENGTH_SHORT).show()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.buttonSettings.setOnClickListener {
            isSettings = !isSettings

            if (isSettings) {
                binding.imgSettings.setImageResource(R.drawable.settings_black)
                when {
                    currentMember?.isMaster()!! -> { // 클럽장 메뉴 활성화
                        binding.buttonModifyName.visibility = View.VISIBLE
                        binding.buttonModifySymbol.visibility = View.VISIBLE
                        binding.buttonModifyNotice.visibility = View.VISIBLE
                    }
                    currentMember?.isAdministrator()!! -> { // 부클럽장 메뉴 활성화
                        binding.buttonModifyName.visibility = View.VISIBLE
                        binding.buttonModifySymbol.visibility = View.VISIBLE
                        binding.buttonModifyNotice.visibility = View.VISIBLE
                    }
                }
            } else {
                binding.imgSettings.setImageResource(R.drawable.settings)
                binding.buttonModifyName.visibility = View.GONE
                binding.buttonModifySymbol.visibility = View.GONE
                binding.buttonModifyNotice.visibility = View.GONE
            }


        }

        binding.buttonCheckout.setOnClickListener {
            if (currentMember?.isCheckout()!!) {
                callToast("이미 출석체크 하였습니다.")
            } else {
                val question = QuestionDTO(
                    QuestionDTO.Stat.INFO,
                    "출석체크",
                    "오늘 출석체크를 하시겠습니까?",
                )
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
                    val preferencesDTO = (activity as MainActivity?)?.getPreferences()

                    // 오늘날짜 출석체크 등록
                    val date = Date()
                    val user = (activity as MainActivity?)?.getUser()!!
                    firebaseViewModel.updateFanClubCheckout(fanClubExDTO?.fanClubDTO?.docName.toString(), currentMember?.userUid.toString()) {
                        var exp = (activity as MainActivity?)?.getPreferences()?.rewardFanClubExp!!
                        if (user.isPremium()!!) { // 프리미엄 패키지 사용중이라면 경험치 두배
                            exp = exp.times(2)
                        }
                        exp = exp.plus(user.level!!) // 사용자 레벨만큼 추가 경험치

                        currentMember?.checkoutTime = date

                        // 경험치 추가 적용
                        applyExp(exp, 0, null)

                        // 다이아 추가
                        addGem(preferencesDTO?.rewardFanClubCheckoutGem!!)

                        // 일일 퀘스트 - 팬클럽 출석체크 완료 시 적용
                        if (!QuestDTO("팬클럽 출석체크", "팬클럽 출석체크를 완료 하세요.", 1, user.questSuccessTimes?.get("4"), user.questGemGetTimes?.get("4")).isQuestSuccess()) { // 퀘스트 완료 안했을 때 적용
                            user.questSuccessTimes?.set("4", date)
                            firebaseViewModel.updateUserQuestSuccessTimes(user) {
                                Toast.makeText(activity, "일일 과제 달성! 보상을 획득하세요!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

        binding.buttonModifyName.setOnClickListener {
            // 관리자 권한이 없어졌는지 확인
            if (!(parentFragment as FragmentFanClubMain?)?.isRemoveAdmin()!!) {
                modifyName()
            }
        }

        binding.buttonModifySymbol.setOnClickListener {
            // 관리자 권한이 없어졌는지 확인
            if (!(parentFragment as FragmentFanClubMain?)?.isRemoveAdmin()!!) {
                modifySymbol()
            }
        }

        binding.buttonModifyNotice.setOnClickListener {
            // 관리자 권한이 없어졌는지 확인
            if (!(parentFragment as FragmentFanClubMain?)?.isRemoveAdmin()!!) {
                modifyNotice()
            }
        }

        binding.buttonDonation.setOnClickListener {
            val userDTO = (activity as MainActivity?)?.getUser()
            val preferencesDTO = (activity as MainActivity?)?.getPreferences()

            val dialog = LevelUpActionFanClubDialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.preferencesDTO = preferencesDTO
            dialog.oldUserDTO = userDTO?.copy()
            dialog.newUserDTO = userDTO?.copy()
            dialog.oldFanClubDTO = fanClubExDTO?.fanClubDTO?.copy()
            dialog.newFanClubDTO = fanClubExDTO?.fanClubDTO?.copy()
            dialog.show()

            dialog.button_level_up_action_fan_club_cancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.button_up_exp_fan_club.setOnClickListener {
                val question = GemQuestionDTO("다이아를 사용해 경험치를 올립니다.", dialog.useGemCount)
                val questionDialog = GemQuestionDialog(requireContext(), question)
                questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                questionDialog.setCanceledOnTouchOutside(false)
                questionDialog.show()
                questionDialog.button_gem_question_cancel.setOnClickListener { // No
                    questionDialog.dismiss()
                }
                questionDialog.button_gem_question_ok.setOnClickListener { // Ok
                    questionDialog.dismiss()
                    dialog.dismiss()

                    (activity as MainActivity?)?.loading()
                    val availableExpGem = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_AVAILABLE_FAN_CLUB_EXP_GEM, preferencesDTO?.availableFanClubExpGem!!)
                    sharedPreferences.putAdCount(MySharedPreferences.PREF_KEY_AVAILABLE_FAN_CLUB_EXP_GEM, availableExpGem.minus(dialog.useGemCount))
                    applyExp(dialog.addExp, dialog.useGemCount, null)
                }
            }

            dialog.button_get_fan_club_exp_ad.setOnClickListener {
                val rewardExpCount = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_REWARD_FAN_CLUB_EXP_COUNT, preferencesDTO?.rewardFanClubExpCount!!)
                when {
                    rewardExpCount <= 0 -> {
                        callToast("오늘은 더 이상 광고를 시청할 수 없습니다.")
                    }
                    dialog.isRunTimerFanClubExp -> { // 타이머가 동작중이면 광고 시청 불가능
                        callToast("아직 광고를 시청할 수 없습니다.")
                    }
                    else -> {
                        if (adsRewardManagerExp != null) {
                            adsRewardManagerExp?.callReward {
                                if (it) {
                                    rewardExp(dialog)
                                } else {
                                    callToast("아직 광고를 시청할 수 없습니다.")
                                }

                            }
                        }
                    }
                }
            }

            dialog.button_get_fan_club_gem_ad.setOnClickListener {
                val rewardGemCount = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_REWARD_FAN_CLUB_GEM_COUNT, preferencesDTO?.rewardFanClubGemCount!!)
                when {
                    rewardGemCount <= 0 -> {
                        callToast("오늘은 더 이상 광고를 시청할 수 없습니다.")
                    }
                    dialog.isRunTimerFanClubGem -> { // 타이머가 동작중이면 광고 시청 불가능
                        callToast("아직 광고를 시청할 수 없습니다.")
                    }
                    else -> {
                        if (adsRewardManagerGem != null) {
                            adsRewardManagerGem?.callReward {
                                if (it) {
                                    rewardGem(dialog)
                                } else {
                                    callToast("아직 광고를 시청할 수 없습니다.")
                                }

                            }
                        }
                    }
                }
            }
        }

        binding.buttonReward.setOnClickListener {
            firebaseViewModel.getMemberCheckoutCount(fanClubExDTO?.fanClubDTO?.docName.toString()) { checkoutCount ->
                val dialog = FanClubRewardDialog(requireContext())
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCanceledOnTouchOutside(false)
                dialog.mainActivity = (activity as MainActivity?)
                dialog.fanClubDTO = fanClubExDTO?.fanClubDTO
                dialog.currentMember = currentMember
                dialog.fanClubCheckoutCount = checkoutCount
                dialog.show()

                dialog.button_fan_club_reward_cancel.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }

        binding.buttonSendNotice.setOnClickListener {
            val dialog = SendNoticeDialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            //dialog.mainActivity = (activity as MainActivity?)
            dialog.fanClubDTO = fanClubExDTO?.fanClubDTO
            //dialog.currentMember = currentMember
            //dialog.fanClubCheckoutCount = checkoutCount
            dialog.show()

            dialog.button_send_notice_cancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.button_send_notice_ok.setOnClickListener {
                val title = dialog.binding.editNoticeTitle.text.toString().trim()
                val content = dialog.binding.editNoticeContent.text.toString().trim()
                when {
                    title.isNullOrEmpty() -> {
                        Toast.makeText(context, "공지 제목을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    }
                    content.isNullOrEmpty() -> {
                        Toast.makeText(context, "공지 내용을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val preferencesDTO = (activity as MainActivity?)?.getPreferences()
                        val question = GemQuestionDTO("다이아를 사용해 모든 팬클럽 멤버에게 전체 공지를 발송합니다.", preferencesDTO?.priceFanClubNotice)
                        val questionDialog = GemQuestionDialog(requireContext(), question)
                        questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        questionDialog.setCanceledOnTouchOutside(false)
                        questionDialog.show()
                        questionDialog.button_gem_question_cancel.setOnClickListener { // No
                            questionDialog.dismiss()
                        }
                        questionDialog.button_gem_question_ok.setOnClickListener { // Ok
                            questionDialog.dismiss()
                            dialog.dismiss()

                            val user = (activity as MainActivity?)?.getUser()!!
                            if ((user.paidGem!! + user.freeGem!!) < preferencesDTO?.priceFanClubNotice!!) {
                                Toast.makeText(activity, "다이아가 부족합니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                (activity as MainActivity?)?.loading()
                                val date = Date()
                                val fromName = "보낸이 : ${user.nickname}"

                                firebaseViewModel.getMembers(fanClubExDTO?.fanClubDTO?.docName.toString(), FirebaseRepository.MemberType.MEMBER_ONLY) { members ->
                                    //println("공지 전송 호출")
                                    for (member in members) {
                                        if (!member.token.isNullOrEmpty()) {
                                            // FCM 전송하기
                                            val data = NotificationBody.NotificationData(
                                                title,
                                                fromName,
                                                content
                                            )
                                            val body = NotificationBody(member.token.toString(), data)
                                            firebaseViewModel.sendNotification(body)
                                            // 응답 여부
                                            firebaseViewModel.myResponse.observe(viewLifecycleOwner) {
                                                //Log.d(TAG, "onViewCreated: $it")
                                                //println("공지 전송 호출!!!")
                                            }

                                            // 전체 공지 우편으로도 발송
                                            val docName = "fanClub${System.currentTimeMillis()}"
                                            val calendar= Calendar.getInstance()
                                            calendar.add(Calendar.DATE, 7)
                                            var mail = MailDTO(docName, title, content, fromName, MailDTO.Item.NONE, 0, date, calendar.time)
                                            firebaseViewModel.sendUserMail(member?.userUid.toString(), mail) { }
                                        }
                                    }
                                    val oldPaidGemCount = user.paidGem!!
                                    val oldFreeGemCount = user.freeGem!!
                                    firebaseViewModel.useUserGem(user.uid.toString(), preferencesDTO?.priceFanClubNotice!!) { userDTO ->
                                        if (userDTO != null) {
                                            var logUser = LogDTO("[다이아 차감] 전체공지 전송으로 ${preferencesDTO?.priceFanClubNotice} 다이아 사용 (paidGem : $oldPaidGemCount -> ${userDTO?.paidGem}, freeGem : $oldFreeGemCount -> ${userDTO?.freeGem})", Date())
                                            firebaseViewModel.writeUserLog(user.uid.toString(), logUser) { }

                                            // 팬클럽 로그 기록
                                            var logFanClub = LogDTO("[팬클럽 전체 공지 발송] 발송자 (uid : ${user.uid}, nickname : ${user.nickname}), 공지 제목 : $title, 공지 내용 : $content", date)
                                            firebaseViewModel.writeFanClubLog(fanClubExDTO?.fanClubDTO?.docName.toString(), logFanClub) { }

                                            Toast.makeText(activity, "전체 공지 발송 완료!", Toast.LENGTH_SHORT).show()
                                            (activity as MainActivity?)?.loadingEnd()
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

    private fun rewardExp(dialog: LevelUpActionFanClubDialog) {
        (activity as MainActivity?)?.loading()
        val preferencesDTO = (activity as MainActivity?)?.getPreferences()
        //Toast.makeText(activity, "보상 $rewardAmount, $rewardType", Toast.LENGTH_SHORT).show()

        val rewardExpCount = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_REWARD_FAN_CLUB_EXP_COUNT, preferencesDTO?.rewardFanClubExpCount!!)
        sharedPreferences.putAdCount(MySharedPreferences.PREF_KEY_REWARD_FAN_CLUB_EXP_COUNT, rewardExpCount.minus(1))

        sharedPreferences.putLong(MySharedPreferences.PREF_KEY_REWARD_FAN_CLUB_EXP_TIME, System.currentTimeMillis())

        val user = (activity as MainActivity?)?.getUser()
        var exp = preferencesDTO?.rewardFanClubExp!!
        if (user?.isPremium()!!) { // 프리미엄 패키지 사용중이라면 경험치 두배
            exp = exp.times(2)
        }
        exp = exp.plus(user?.level!!) // 사용자 레벨만큼 추가 경험치

        applyExp(exp, 0, dialog)
        Toast.makeText(activity, "보상 획득 완료!", Toast.LENGTH_SHORT).show()

        // 일일 퀘스트 - 팬클럽 무료 경험치 광고 시청 시 적용
        if (!QuestDTO("팬클럽 무료 경험치 광고", "팬클럽 무료 경험치 광고를 1회 이상 시청 하세요.", 1, user?.questSuccessTimes?.get("7"), user?.questGemGetTimes?.get("7")).isQuestSuccess()) { // 퀘스트 완료 안했을 때 적용
            user?.questSuccessTimes?.set("7", Date())
            firebaseViewModel.updateUserQuestSuccessTimes(user!!) {
                Toast.makeText(activity, "일일 과제 달성! 보상을 획득하세요!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun rewardGem(dialog: LevelUpActionFanClubDialog) {
        val preferencesDTO = (activity as MainActivity?)?.getPreferences()

        val rewardGemCount = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_REWARD_FAN_CLUB_GEM_COUNT, preferencesDTO?.rewardFanClubGemCount!!)
        sharedPreferences.putAdCount(MySharedPreferences.PREF_KEY_REWARD_FAN_CLUB_GEM_COUNT, rewardGemCount.minus(1))

        sharedPreferences.putLong(MySharedPreferences.PREF_KEY_REWARD_FAN_CLUB_GEM_TIME, System.currentTimeMillis())

        addGem(preferencesDTO?.rewardFanClubGem!!, dialog)
        Toast.makeText(activity, "보상 획득 완료!", Toast.LENGTH_SHORT).show()

        // 일일 퀘스트 - 팬클럽 무료 다이아 광고 시청 시 적용
        val user = (activity as MainActivity?)?.getUser()
        if (!QuestDTO("팬클럽 무료 다이아 광고", "팬클럽 무료 다이아 광고를 1회 이상 시청 하세요.", 1, user?.questSuccessTimes?.get("8"), user?.questGemGetTimes?.get("8")).isQuestSuccess()) { // 퀘스트 완료 안했을 때 적용
            user?.questSuccessTimes?.set("8", Date())
            firebaseViewModel.updateUserQuestSuccessTimes(user!!) {
                Toast.makeText(activity, "일일 과제 달성! 보상을 획득하세요!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 경험치 적용
    private fun applyExp(exp: Long, gemCount: Int, dialog: LevelUpActionFanClubDialog?) {
        // 경험치 증가 적용
        //var currentUser = (activity as MainActivity?)?.getUser()
        val user = (activity as MainActivity?)?.getUser()!!
        val date = Date()
        val oldFanClub = fanClubExDTO?.fanClubDTO
        firebaseViewModel.addFanClubExp(fanClubExDTO?.fanClubDTO?.docName.toString(), exp) { fanClubDTO ->
            if (fanClubDTO != null) {
                // 다이아 사용일 경우 다이아 소모 적용
                if (gemCount > 0) {
                    val oldPaidGemCount = user.paidGem!!
                    val oldFreeGemCount = user.freeGem!!
                    firebaseViewModel.useUserGem(user.uid.toString(), gemCount) { userDTO ->
                        if (userDTO != null) {
                            var log = LogDTO("[다이아 차감] ($gemCount)다이아로 팬클럽 ($exp)경험치 기부 (paidGem : $oldPaidGemCount -> ${userDTO?.paidGem}, freeGem : $oldFreeGemCount -> ${userDTO?.freeGem})", date)
                            firebaseViewModel.writeUserLog(user.uid.toString(), log) { }
                        }
                    }
                }

                // 팬클럽 멤버 기여도 추가
                firebaseViewModel.addMemberContribution(fanClubDTO?.docName.toString(), currentMember?.userUid.toString(), exp) { memberDTO ->
                    if (memberDTO != null) {
                        // 기여도 추가 성공
                    }
                }

                var log = LogDTO("[경험치 증가] 경험치 [$exp] 증가 (exp : ${oldFanClub?.exp} -> ${fanClubDTO?.exp}), (level : ${oldFanClub?.level} -> ${fanClubDTO?.level}), 기여자 (uid : ${user?.uid}, nickname : ${user?.nickname})", date)
                firebaseViewModel.writeFanClubLog(fanClubDTO?.docName.toString(), log) { }

                (activity as MainActivity?)?.loadingEnd()

                val expUpDialog = ExpUpFanClubDialog(requireContext())
                expUpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                expUpDialog.setCanceledOnTouchOutside(false)
                expUpDialog.fanClubDTO = oldFanClub
                expUpDialog.userDTO = user
                expUpDialog.addExp = exp
                expUpDialog.gemCount = gemCount
                expUpDialog.show()

                thread(start = true) {
                    Thread.sleep(1300)

                    activity?.runOnUiThread {
                        setInfo()
                        if (fanClubDTO?.level!! > oldFanClub?.level!!) { // 레벨업 했다면 레벨업 대화상자 호출
                            expUpDialog.dismiss()
                            val levelDialog = LevelUpFanClubDialog(requireContext())
                            levelDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                            levelDialog.setCanceledOnTouchOutside(false)
                            levelDialog.oldFanClubDTO = oldFanClub
                            levelDialog.newFanClubDTO = fanClubDTO
                            levelDialog.show()

                            // 레벨업 보상 다이아 우편으로 지급
                            /*val levelUpGemCount = currentUser?.getLevelUpGemCount()
                            val docName = "master${System.currentTimeMillis()}"
                            val calendar= Calendar.getInstance()
                            calendar.add(Calendar.DATE, 7)
                            var mail = MailDTO(docName,"레벨업 보상", "${currentUser?.level} 레벨 달성 보상 다이아가 지급되었습니다.", "시스템", MailDTO.ITEM.FREE_GEM, levelUpGemCount, date, calendar.time)
                            firestore?.collection("user")?.document(currentUser?.uid.toString())?.collection("mail")?.document(docName)?.set(mail)?.addOnCompleteListener {
                                var log = LogDTO("[레벨업 보상] 레벨 ${currentUser?.level} 달성 보상 다이아 $levelUpGemCount 개 우편 발송, 유효기간 : ${SimpleDateFormat("yyyy.MM.dd HH:mm").format(calendar.time)}까지", date)
                                firestore?.collection("user")?.document(currentUser?.uid.toString())?.collection("log")?.document()?.set(log)
                            }*/

                            levelDialog.button_level_up_fan_club_ok.setOnClickListener {
                                levelDialog.dismiss()

                                if (dialog != null) {
                                    dialog.oldFanClubDTO = fanClubDTO?.copy()
                                    dialog.newFanClubDTO = fanClubDTO?.copy()
                                    dialog.setInfo()
                                }
                            }
                        } else {
                            expUpDialog.button_exp_up_fan_club_ok.visibility = View.VISIBLE
                        }
                    }
                }

                expUpDialog.button_exp_up_fan_club_ok.setOnClickListener {
                    expUpDialog.dismiss()

                    if (dialog != null) {
                        dialog.oldFanClubDTO = fanClubDTO?.copy()
                        dialog.newFanClubDTO = fanClubDTO?.copy()
                        dialog.setInfo()
                    }
                }
            }
        }
    }

    private fun addGem(gemCount: Int, dialog: LevelUpActionFanClubDialog? = null) {
        val user = (activity as MainActivity?)?.getUser()!!
        val oldFreeGemCount = user.freeGem!!
        firebaseViewModel.addUserGem(user.uid.toString(), 0, gemCount) { userDTO ->
            if (userDTO != null) {
                var log = LogDTO("[사용자 무료 다이아 획득] 다이아 $gemCount 획득 (freeGem : $oldFreeGemCount -> ${userDTO?.freeGem})", Date())
                firebaseViewModel.writeUserLog(user.uid.toString(), log) { }

                val getDialog = GetItemDialog(requireContext())
                getDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                getDialog.setCanceledOnTouchOutside(false)
                getDialog.mailDTO = MailDTO("", "", "", "", MailDTO.Item.FREE_GEM, gemCount)
                getDialog.show()

                getDialog.button_get_item_ok.setOnClickListener {
                    getDialog.dismiss()

                    if (dialog != null) {
                        dialog.oldUserDTO = userDTO?.copy()
                        dialog.newUserDTO = userDTO?.copy()
                        dialog.setInfo()
                    }
                }
            }
        }
    }

    private fun setInfo() {
        if (fanClubExDTO?.fanClubDTO != null) {
            setFanClubInfo()
            firebaseViewModel.getMemberCheckoutCount(fanClubExDTO?.fanClubDTO?.docName.toString()) { checkoutCount ->
                if (_binding != null) { // 메인 탭에서 바로 사용자 정보 탭 이동 시 팬클럽 뷰가 Destroy 되고 나서 뒤 늦게 들어오는 경우가 있기 때문에 예외 처리
                    binding.textCheckoutCount.text = "${checkoutCount}/${fanClubExDTO?.fanClubDTO?.memberCount}"
                }
            }
        }
        if (currentMember != null) {
            setCurrentMemberInfo()
        }
    }

    private fun setCurrentMemberInfo() {
        when (currentMember?.position) {
            MemberDTO.Position.MASTER -> binding.layoutPosition.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.master))
            MemberDTO.Position.SUB_MASTER -> binding.layoutPosition.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.sub_master))
            MemberDTO.Position.MEMBER -> binding.layoutPosition.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.member))
        }
        binding.imgPosition.setImageResource(currentMember?.getPositionImage()!!)
        binding.textPosition.text = currentMember?.getPositionString()

        binding.textMemberLevel.text = "Lv. ${currentMember?.userLevel}"
        binding.textMemberName.text = currentMember?.userNickname
        binding.textContribution.text = "기여도 : ${decimalFormat.format(currentMember?.contribution)}"
        binding.textResponseTime.text = SimpleDateFormat("yyyy.MM.dd HH:mm").format(currentMember?.responseTime)
        binding.editAboutMe.setText(currentMember?.userAboutMe)
        binding.imgCheckout.setImageResource(currentMember?.getCheckoutImage()!!)
    }

    private fun setFanClubInfo() {
        if (fanClubExDTO?.imgSymbolCustomUri != null) {
            Glide.with(requireContext()).load(fanClubExDTO?.imgSymbolCustomUri).fitCenter().into(binding.imgSymbol)
        } else {
            var imageID = requireContext().resources.getIdentifier(fanClubExDTO?.fanClubDTO?.imgSymbol, "drawable", requireContext().packageName)
            if (imageID > 0) {
                binding.imgSymbol.setImageResource(imageID)
            }
        }

        binding.textLevel.text = "Lv. ${fanClubExDTO?.fanClubDTO?.level}"
        binding.textName.text = fanClubExDTO?.fanClubDTO?.name
        binding.textNoticeContent.text = fanClubExDTO?.fanClubDTO?.notice
        binding.textExp.text = "${decimalFormat.format(fanClubExDTO?.fanClubDTO?.exp)}/${decimalFormat.format(fanClubExDTO?.fanClubDTO?.getNextLevelExp())}"
        var percent = ((fanClubExDTO?.fanClubDTO?.exp?.toDouble()!! / fanClubExDTO?.fanClubDTO?.getNextLevelExp()!!) * 100).toInt()
        binding.progressPercent.progress = percent

        binding.imgSettings.setImageResource(R.drawable.settings)
        binding.buttonModifyName.visibility = View.GONE
        binding.buttonModifySymbol.visibility = View.GONE
        binding.buttonModifyNotice.visibility = View.GONE

        when {
            currentMember?.isAdministrator()!! -> { // 운영진 메뉴 활성화
                binding.buttonSettings.visibility = View.VISIBLE
                binding.buttonSendNotice.visibility = View.VISIBLE
            }
            else -> {
                binding.buttonSettings.visibility = View.GONE
                binding.buttonSendNotice.visibility = View.GONE
            }
        }
    }

    private fun modifyName() {
        if (isBlockChangeName()) {
            val question = QuestionDTO(
                QuestionDTO.Stat.WARNING,
                "팬클럽 이름 변경",
                "아직 팬클럽 이름을 변경할 수 없습니다. 팬클럽 이름은 3일마다 변경 가능합니다.\n\n최종변경일 [${SimpleDateFormat("yyyy.MM.dd HH:mm").format(fanClubExDTO?.fanClubDTO?.nameChangeDate)}]",
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
            val item = EditTextDTO("팬클럽 이름 변경", fanClubExDTO?.fanClubDTO?.name, 30, "^[가-힣ㄱ-ㅎa-zA-Z0-9.~!@#\$%^&*\\[\\](){}|_ -]{1,15}\$", "사용할 수 없는 문자열이 포함되어 있습니다.")
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

                val question = GemQuestionDTO("다이아를 사용해 팬클럽 이름을 변경합니다.\n(3일마다 1회 변경 가능)", preferencesDTO?.priceFanClubName)
                val questionDialog = GemQuestionDialog(requireContext(), question)
                questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                questionDialog.setCanceledOnTouchOutside(false)
                questionDialog.show()
                questionDialog.button_gem_question_cancel.setOnClickListener { // No
                    questionDialog.dismiss()
                }
                questionDialog.button_gem_question_ok.setOnClickListener { // Ok
                    questionDialog.dismiss()

                    val user = (activity as MainActivity?)?.getUser()!!
                    if ((user.paidGem!! + user.freeGem!!) < preferencesDTO?.priceFanClubName!!) {
                        Toast.makeText(activity, "다이아가 부족합니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        val name = dialog.edit_content.text.toString().trim()
                        firebaseViewModel.isUsedFanClubName(name) { isUsed ->
                            if (isUsed) {
                                Toast.makeText(activity, "팬클럽 이름이 이미 존재합니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                dialog.dismiss()
                                if (fanClubExDTO?.fanClubDTO?.name != name) {
                                    (activity as MainActivity?)?.loading()

                                    val oldName = fanClubExDTO?.fanClubDTO?.name
                                    fanClubExDTO?.fanClubDTO?.name = name
                                    fanClubExDTO?.fanClubDTO?.nameChangeDate = Date()
                                    firebaseViewModel.updateFanClub(fanClubExDTO?.fanClubDTO!!) {
                                        // 다이아 차감
                                        val oldPaidGemCount = user.paidGem!!
                                        val oldFreeGemCount = user.freeGem!!
                                        firebaseViewModel.useUserGem(user.uid.toString(), preferencesDTO?.priceFanClubName!!) { userDTO ->
                                            if (userDTO != null) {
                                                var log = LogDTO("[다이아 차감] 팬클럽 이름 변경으로 ${preferencesDTO?.priceFanClubName} 다이아 사용 ($oldName -> ${fanClubExDTO?.fanClubDTO?.name}), (paidGem : $oldPaidGemCount -> ${userDTO?.paidGem}, freeGem : $oldFreeGemCount -> ${userDTO?.freeGem})", Date())
                                                firebaseViewModel.writeUserLog(user.uid.toString(), log) { }
                                                Toast.makeText(activity, "팬클럽 이름 변경 완료!", Toast.LENGTH_SHORT).show()
                                                binding.textName.text = name
                                            }
                                            (activity as MainActivity?)?.loadingEnd()
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

    private fun isBlockChangeName() : Boolean {
        // 팬클럽 명 변경 후 3일이 지나야 재 변경 가능
        var isBlock = false
        if (fanClubExDTO?.fanClubDTO?.nameChangeDate != null) {
            val calendar= Calendar.getInstance()
            calendar.time = fanClubExDTO?.fanClubDTO?.nameChangeDate
            calendar.add(Calendar.DATE, 3)

            if (Date() < calendar.time) {
                isBlock = true
            }
        }
        return isBlock
    }

    private fun modifySymbolApply(selectedSymbol: String, uri: Uri? = null) {
        val question = QuestionDTO(
            QuestionDTO.Stat.WARNING,
            "팬클럽 심볼 변경",
            "팬클럽 심볼을 변경하면 되돌릴 수 없습니다.\n정말 변경 하시겠습니까?",
            selectedSymbol
        )
        val questionDialog = QuestionDialog(requireContext(), question)
        questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        questionDialog.setCanceledOnTouchOutside(false)
        questionDialog.show()
        questionDialog.showImgOk(true)
        questionDialog.button_question_cancel.setOnClickListener { // No
            questionDialog.dismiss()
        }
        questionDialog.button_question_ok.setOnClickListener {
            questionDialog.dismiss()

            val preferencesDTO = (activity as MainActivity?)?.getPreferences()

            val gemQuestion = GemQuestionDTO("다이아를 사용해 팬클럽 심볼을 변경합니다.", preferencesDTO?.priceFanClubSymbol)
            val gemQuestionDialog = GemQuestionDialog(requireContext(), gemQuestion)
            gemQuestionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            gemQuestionDialog.setCanceledOnTouchOutside(false)
            gemQuestionDialog.show()
            gemQuestionDialog.button_gem_question_cancel.setOnClickListener { // No
                gemQuestionDialog.dismiss()
            }
            gemQuestionDialog.button_gem_question_ok.setOnClickListener { // Ok
                gemQuestionDialog.dismiss()

                val user = (activity as MainActivity?)?.getUser()!!
                if ((user.paidGem!! + user.freeGem!!) < preferencesDTO?.priceFanClubSymbol!!) {
                    Toast.makeText(activity, "다이아가 부족합니다.", Toast.LENGTH_SHORT).show()
                } else {
                    var imageID = requireContext().resources.getIdentifier(selectedSymbol, "drawable", requireContext().packageName)
                    if (imageID != null) {
                        (activity as MainActivity?)?.loading()

                        val oldImgSymbol = fanClubExDTO?.fanClubDTO?.imgSymbol
                        val oldImgSymbolCustom = fanClubExDTO?.fanClubDTO?.imgSymbolCustom
                        if (uri == null) { // 제공된 심볼 중에서 선택
                            fanClubExDTO?.fanClubDTO?.imgSymbol = selectedSymbol
                            fanClubExDTO?.fanClubDTO?.imgSymbolCustom = null
                            firebaseViewModel.updateFanClub(fanClubExDTO?.fanClubDTO!!) {
                                // 다이아 차감
                                val oldPaidGemCount = user.paidGem!!
                                val oldFreeGemCount = user.freeGem!!
                                firebaseViewModel.useUserGem(user.uid.toString(), preferencesDTO?.priceFanClubSymbol!!) { userDTO ->
                                    if (userDTO != null) {
                                        var log = LogDTO("[다이아 차감] 팬클럽 심볼 변경으로 ${preferencesDTO?.priceFanClubSymbol} 다이아 사용 ($oldImgSymbol -> ${fanClubExDTO?.fanClubDTO?.imgSymbol}), (paidGem : $oldPaidGemCount -> ${userDTO?.paidGem}, freeGem : $oldFreeGemCount -> ${userDTO?.freeGem})", Date())
                                        firebaseViewModel.writeUserLog(user.uid.toString(), log) { }
                                        Toast.makeText(activity, "심볼 변경 완료!", Toast.LENGTH_SHORT).show()
                                        binding.imgSymbol.setImageResource(imageID)
                                    }
                                    (activity as MainActivity?)?.loadingEnd()
                                }
                            }
                        } else { // 사용자 직접 이미지 업로드
                            var bitmap = (activity as MainActivity?)?.getBitmap(uri)
                            if (bitmap == null) {
                                Toast.makeText(activity, "이미지 업로드 실패 ", Toast.LENGTH_SHORT).show()
                                (activity as MainActivity?)?.loadingEnd()
                            } else {
                                firebaseStorageViewModel.setFanClubSymbol(fanClubExDTO?.fanClubDTO?.docName.toString(), bitmap!!) {
                                    if (!it) {
                                        Toast.makeText(activity, "이미지 업로드 실패 ", Toast.LENGTH_SHORT).show()
                                        (activity as MainActivity?)?.loadingEnd()
                                    } else {
                                        fanClubExDTO?.fanClubDTO?.imgSymbol = "reward_icon_25" // 기본 값
                                        fanClubExDTO?.fanClubDTO?.imgSymbolCustom = fanClubExDTO?.fanClubDTO?.getSymbolCustomImageName()
                                        firebaseViewModel.updateFanClub(fanClubExDTO?.fanClubDTO!!) {
                                            // 다이아 차감
                                            val oldPaidGemCount = user.paidGem!!
                                            val oldFreeGemCount = user.freeGem!!
                                            firebaseViewModel.useUserGem(user.uid.toString(), preferencesDTO?.priceFanClubSymbol!!) { userDTO ->
                                                if (userDTO != null) {
                                                    var log = LogDTO("[다이아 차감] 팬클럽 심볼(커스텀) 변경으로 ${preferencesDTO?.priceFanClubSymbol} 다이아 사용 ($oldImgSymbolCustom -> ${fanClubExDTO?.fanClubDTO?.imgSymbolCustom}), (paidGem : $oldPaidGemCount -> ${userDTO?.paidGem}, freeGem : $oldFreeGemCount -> ${userDTO?.freeGem})", Date())
                                                    firebaseViewModel.writeUserLog(user.uid.toString(), log) { }
                                                    Toast.makeText(activity, "심볼 변경 완료!", Toast.LENGTH_SHORT).show()
                                                    binding.imgSymbol.setImageBitmap(bitmap)
                                                }
                                                (activity as MainActivity?)?.loadingEnd()
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
    }

    private fun modifySymbol() {
        val dialog = SelectFanClubSymbolDialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.mainActivity = (activity as MainActivity?)
        dialog.show()

        dialog.setOnDismissListener {
            if (dialog.isOK && !dialog.selectedSymbol.isNullOrEmpty()) {
                if (dialog.isAddImage) {
                    resultLauncher.launch("image/*")
                } else {
                    modifySymbolApply(dialog.selectedSymbol)
                }
            }
        }
    }

    private fun modifyNotice() {
        val item = EditTextDTO("공지사항 변경", fanClubExDTO?.fanClubDTO?.notice, 600)
        val dialog = EditTextModifyDialog(requireContext(), item)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        dialog.button_modify_cancel.setOnClickListener { // No
            dialog.dismiss()
        }
        dialog.button_modify_ok.setOnClickListener {
            dialog.dismiss()

            val question = QuestionDTO(
                QuestionDTO.Stat.WARNING,
                "공지사항 변경",
                "공지사항을 변경하면 되돌릴 수 없습니다.\n정말 변경 하시겠습니까?",
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

                val oldNotice = fanClubExDTO?.fanClubDTO?.notice.toString()
                fanClubExDTO?.fanClubDTO?.notice = dialog.edit_content.text.toString()
                firebaseViewModel.updateFanClub(fanClubExDTO?.fanClubDTO!!) {
                    val user = (activity as MainActivity?)?.getUser()!!
                    var log = LogDTO("[팬클럽 공지사항 변경] 변경한 관리자 (uid : ${user.uid}, nickname : ${user.nickname}), 공지사항 변경 : $oldNotice -> ${fanClubExDTO?.fanClubDTO?.notice}", Date())
                    firebaseViewModel.writeFanClubLog(fanClubExDTO?.fanClubDTO?.docName.toString(), log) { }

                    Toast.makeText(activity, "공지사항 변경 완료!", Toast.LENGTH_SHORT).show()
                    binding.textNoticeContent.text = fanClubExDTO?.fanClubDTO?.notice
                }
            }
        }
    }

    private fun callToast(message: String) {
        if (toast == null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        } else {
            toast?.setText(message)
        }
        toast?.show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentFanClubInfo.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentFanClubInfo().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: MemberDTO, position: Int) {

    }
}