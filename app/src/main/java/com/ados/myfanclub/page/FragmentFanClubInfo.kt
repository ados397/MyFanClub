package com.ados.myfanclub.page

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.google.android.material.snackbar.Snackbar
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

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

    private lateinit var callback: OnBackPressedCallback

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

    private var toast : Toast? = null

    private var questionDialog: QuestionDialog? = null
    private var gemQuestionDialog: GemQuestionDialog? = null
    private var levelUpActionFanClubDialog: LevelUpActionFanClubDialog? = null
    private var expUpFanClubDialog: ExpUpFanClubDialog? = null
    private var levelUpFanClubDialog: LevelUpFanClubDialog? = null
    private var editTextModifyDialog: EditTextModifyDialog? = null
    private var sendNoticeDialog: SendNoticeDialog? = null
    private var fanClubRewardDialog: FanClubRewardDialog? = null
    private var selectFanClubSymbolDialog: SelectFanClubSymbolDialog? = null
    private var imageViewDialog: ImageViewDialog? = null

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

        val adPolicyDTO = (activity as MainActivity?)?.getAdPolicy()!!
        adsRewardManagerExp = AdsRewardManager(requireActivity(), adPolicyDTO, AdsRewardManager.RewardType.REWARD_FAN_CLUB_EXP)
        adsRewardManagerGem = AdsRewardManager(requireActivity(), adPolicyDTO, AdsRewardManager.RewardType.REWARD_FAN_CLUB_GEM)

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

        setInfo()

        binding.textNoticeContent.movementMethod = ScrollingMovementMethod.getInstance()
        binding.textNoticeContent.setOnTouchListener { _, _ ->
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
            false
        }

        binding.editAboutMe.setOnTouchListener { _, _ ->
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
                        binding.buttonModifyName.visibility = View.GONE // 팬클럽 이름 변경 불가
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
                    val preferencesDTO = (activity as MainActivity?)?.getPreferences()!!

                    // 오늘날짜 출석체크 등록
                    val date = Date()
                    val user = (activity as MainActivity?)?.getUser()!!
                    firebaseViewModel.updateFanClubCheckout(fanClubExDTO?.fanClubDTO?.docName.toString(), currentMember?.userUid.toString()) {
                        var exp = (activity as MainActivity?)?.getPreferences()?.rewardFanClubExp!!
                        if (user.isPremium()) { // 프리미엄 패키지 사용중이라면 경험치 두배
                            exp = exp.times(2)
                        }
                        exp = exp.plus(user.level!!) // 사용자 레벨만큼 추가 경험치

                        currentMember?.checkoutTime = date

                        // 경험치 추가 적용
                        applyExp(exp, 0, null)

                        // 다이아 추가
                        addGem(preferencesDTO.rewardFanClubCheckoutGem!!)

                        // 일일 퀘스트 - 팬클럽 출석체크 완료 시 적용
                        if (!QuestDTO("팬클럽 출석체크", "팬클럽 출석체크를 완료 하세요.", 1, user.questSuccessTimes.get("4"), user.questGemGetTimes.get("4")).isQuestSuccess()) { // 퀘스트 완료 안했을 때 적용
                            user.questSuccessTimes.set("4", date)
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
            val preferencesDTO = (activity as MainActivity?)?.getPreferences()!!

            if (levelUpActionFanClubDialog == null) {
                levelUpActionFanClubDialog = LevelUpActionFanClubDialog(requireContext())
                levelUpActionFanClubDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                levelUpActionFanClubDialog?.setCanceledOnTouchOutside(false)
            }
            levelUpActionFanClubDialog?.preferencesDTO = preferencesDTO
            levelUpActionFanClubDialog?.oldUserDTO = userDTO?.copy()
            levelUpActionFanClubDialog?.newUserDTO = userDTO?.copy()
            levelUpActionFanClubDialog?.oldFanClubDTO = fanClubExDTO?.fanClubDTO?.copy()
            levelUpActionFanClubDialog?.newFanClubDTO = fanClubExDTO?.fanClubDTO?.copy()
            levelUpActionFanClubDialog?.show()

            levelUpActionFanClubDialog?.binding?.buttonLevelUpActionFanClubCancel?.setOnClickListener {
                levelUpActionFanClubDialog?.dismiss()
                levelUpActionFanClubDialog = null
            }

            levelUpActionFanClubDialog?.binding?.buttonUpExpFanClub?.setOnClickListener {
                val question = GemQuestionDTO("다이아를 사용해 경험치를 올립니다.", levelUpActionFanClubDialog?.useGemCount)
                if (gemQuestionDialog == null) {
                    gemQuestionDialog = GemQuestionDialog(requireContext(), question)
                    gemQuestionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    gemQuestionDialog?.setCanceledOnTouchOutside(false)
                } else {
                    gemQuestionDialog?.question = question
                }
                gemQuestionDialog?.mainActivity = (activity as MainActivity?)
                gemQuestionDialog?.show()
                gemQuestionDialog?.setInfo()
                gemQuestionDialog?.binding?.buttonGemQuestionCancel?.setOnClickListener { // No
                    gemQuestionDialog?.dismiss()
                    gemQuestionDialog = null
                }
                gemQuestionDialog?.binding?.buttonGemQuestionOk?.setOnClickListener { // Ok
                    gemQuestionDialog?.dismiss()
                    gemQuestionDialog = null
                    levelUpActionFanClubDialog?.dismiss()

                    (activity as MainActivity?)?.loading()
                    val availableExpGem = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_AVAILABLE_FAN_CLUB_EXP_GEM, preferencesDTO.availableFanClubExpGem!!)
                    sharedPreferences.putAdCount(MySharedPreferences.PREF_KEY_AVAILABLE_FAN_CLUB_EXP_GEM, availableExpGem.minus(levelUpActionFanClubDialog?.useGemCount!!))
                    applyExp(levelUpActionFanClubDialog?.addExp!!, levelUpActionFanClubDialog?.useGemCount!!, null)
                    levelUpActionFanClubDialog = null
                }
            }

            levelUpActionFanClubDialog?.binding?.buttonGetFanClubExpAd?.setOnClickListener {
                val rewardExpCount = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_REWARD_FAN_CLUB_EXP_COUNT, preferencesDTO.rewardFanClubExpCount!!)
                when {
                    rewardExpCount <= 0 -> {
                        callToast("오늘은 더 이상 광고를 시청할 수 없습니다.")
                    }
                    levelUpActionFanClubDialog?.isRunTimerFanClubExp!! -> { // 타이머가 동작중이면 광고 시청 불가능
                        callToast("아직 광고를 시청할 수 없습니다.")
                    }
                    else -> {
                        if (adsRewardManagerExp != null) {
                            adsRewardManagerExp?.callReward {
                                if (it) {
                                    rewardExp(levelUpActionFanClubDialog!!)
                                } else {
                                    callToast("아직 광고를 시청할 수 없습니다.")
                                }

                            }
                        }
                    }
                }
            }

            levelUpActionFanClubDialog?.binding?.buttonGetFanClubGemAd?.setOnClickListener {
                val rewardGemCount = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_REWARD_FAN_CLUB_GEM_COUNT, preferencesDTO.rewardFanClubGemCount!!)
                when {
                    rewardGemCount <= 0 -> {
                        callToast("오늘은 더 이상 광고를 시청할 수 없습니다.")
                    }
                    levelUpActionFanClubDialog?.isRunTimerFanClubGem!! -> { // 타이머가 동작중이면 광고 시청 불가능
                        callToast("아직 광고를 시청할 수 없습니다.")
                    }
                    else -> {
                        if (adsRewardManagerGem != null) {
                            adsRewardManagerGem?.callReward {
                                if (it) {
                                    rewardGem(levelUpActionFanClubDialog!!)
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
                if (fanClubRewardDialog == null) {
                    fanClubRewardDialog = FanClubRewardDialog(requireContext())
                    fanClubRewardDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    fanClubRewardDialog?.setCanceledOnTouchOutside(false)
                }
                fanClubRewardDialog?.mainActivity = (activity as MainActivity?)
                fanClubRewardDialog?.fanClubDTO = fanClubExDTO?.fanClubDTO
                fanClubRewardDialog?.currentMember = currentMember
                fanClubRewardDialog?.fanClubCheckoutCount = checkoutCount
                fanClubRewardDialog?.show()
                fanClubRewardDialog?.setInfo()

                fanClubRewardDialog?.binding?.buttonFanClubRewardCancel?.setOnClickListener {
                    fanClubRewardDialog?.dismiss()
                    fanClubRewardDialog = null
                }
            }
        }

        binding.buttonSendNotice.setOnClickListener {
            if (sendNoticeDialog == null) {
                sendNoticeDialog = SendNoticeDialog(requireContext())
                sendNoticeDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                sendNoticeDialog?.setCanceledOnTouchOutside(false)
            }
            sendNoticeDialog?.fanClubDTO = fanClubExDTO?.fanClubDTO
            sendNoticeDialog?.show()
            sendNoticeDialog?.setInfo()

            sendNoticeDialog?.binding?.buttonSendNoticeCancel?.setOnClickListener {
                sendNoticeDialog?.dismiss()
                sendNoticeDialog = null
            }

            sendNoticeDialog?.binding?.buttonSendNoticeOk?.setOnClickListener {
                val title = sendNoticeDialog?.binding?.editNoticeTitle?.text.toString().trim()
                val content = sendNoticeDialog?.binding?.editNoticeContent?.text.toString().trim()
                when {
                    title.isNullOrEmpty() -> {
                        Toast.makeText(context, "공지 제목을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    }
                    content.isNullOrEmpty() -> {
                        Toast.makeText(context, "공지 내용을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val preferencesDTO = (activity as MainActivity?)?.getPreferences()!!
                        val question = GemQuestionDTO("다이아를 사용해 모든 팬클럽 멤버에게 전체 공지를 발송합니다.", preferencesDTO.priceFanClubNotice)
                        if (gemQuestionDialog == null) {
                            gemQuestionDialog = GemQuestionDialog(requireContext(), question)
                            gemQuestionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                            gemQuestionDialog?.setCanceledOnTouchOutside(false)
                        } else {
                            gemQuestionDialog?.question = question
                        }
                        gemQuestionDialog?.mainActivity = (activity as MainActivity?)
                        gemQuestionDialog?.show()
                        gemQuestionDialog?.setInfo()

                        gemQuestionDialog?.binding?.buttonGemQuestionCancel?.setOnClickListener { // No
                            gemQuestionDialog?.dismiss()
                            gemQuestionDialog = null
                        }
                        gemQuestionDialog?.binding?.buttonGemQuestionOk?.setOnClickListener { // Ok
                            gemQuestionDialog?.dismiss()
                            gemQuestionDialog = null

                            val user = (activity as MainActivity?)?.getUser()!!
                            if ((user.paidGem!! + user.freeGem!!) < preferencesDTO.priceFanClubNotice!!) {
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
                                            var mail = MailDTO(docName, title, content, user.nickname, MailDTO.Item.NONE, 0, date, calendar.time)
                                            firebaseViewModel.sendUserMail(member.userUid.toString(), mail) { }
                                        }
                                    }
                                    val oldPaidGemCount = user.paidGem!!
                                    val oldFreeGemCount = user.freeGem!!
                                    firebaseViewModel.useUserGem(user.uid.toString(), preferencesDTO.priceFanClubNotice) { userDTO ->
                                        if (userDTO != null) {
                                            var logUser = LogDTO("[다이아 차감] 전체공지 전송으로 ${preferencesDTO.priceFanClubNotice} 다이아 사용 (paidGem : $oldPaidGemCount -> ${userDTO.paidGem}, freeGem : $oldFreeGemCount -> ${userDTO.freeGem})", Date())
                                            firebaseViewModel.writeUserLog(user.uid.toString(), logUser) { }

                                            // 팬클럽 로그 기록
                                            var logFanClub = LogDTO("[팬클럽 전체 공지 발송] 발송자 (uid : ${user.uid}, nickname : ${user.nickname}), 공지 제목 : $title, 공지 내용 : $content", date)
                                            firebaseViewModel.writeFanClubLog(fanClubExDTO?.fanClubDTO?.docName.toString(), logFanClub) { }

                                            Toast.makeText(activity, "전체 공지 발송 완료!", Toast.LENGTH_SHORT).show()
                                        }
                                        sendNoticeDialog?.dismiss()
                                        sendNoticeDialog = null
                                        (activity as MainActivity?)?.loadingEnd()
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }

        binding.imgSymbol.setOnClickListener {
            if (imageViewDialog == null) {
                imageViewDialog = ImageViewDialog(requireContext())
                imageViewDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                imageViewDialog?.setCanceledOnTouchOutside(false)
            }
            imageViewDialog?.imageUri = fanClubExDTO?.imgSymbolCustomUri
            imageViewDialog?.imageID = requireContext().resources.getIdentifier(fanClubExDTO?.fanClubDTO?.imgSymbol, "drawable", requireContext().packageName)
            imageViewDialog?.show()
            imageViewDialog?.setInfo()
            imageViewDialog?.binding?.buttonCancel?.setOnClickListener { // No
                imageViewDialog?.dismiss()
                imageViewDialog = null
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
        var exp = preferencesDTO.rewardFanClubExp!!
        if (user?.isPremium()!!) { // 프리미엄 패키지 사용중이라면 경험치 두배
            exp = exp.times(2)
        }
        exp = exp.plus(user.level!!) // 사용자 레벨만큼 추가 경험치

        applyExp(exp, 0, dialog)
        Toast.makeText(activity, "보상 획득 완료!", Toast.LENGTH_SHORT).show()

        // 일일 퀘스트 - 팬클럽 무료 경험치 광고 시청 시 적용
        if (!QuestDTO("팬클럽 무료 경험치 광고", "팬클럽 무료 경험치 광고를 1회 이상 시청 하세요.", 1, user.questSuccessTimes.get("7"), user.questGemGetTimes.get("7")).isQuestSuccess()) { // 퀘스트 완료 안했을 때 적용
            user.questSuccessTimes.set("7", Date())
            firebaseViewModel.updateUserQuestSuccessTimes(user) {
                Toast.makeText(activity, "일일 과제 달성! 보상을 획득하세요!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun rewardGem(dialog: LevelUpActionFanClubDialog) {
        val preferencesDTO = (activity as MainActivity?)?.getPreferences()

        val rewardGemCount = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_REWARD_FAN_CLUB_GEM_COUNT, preferencesDTO?.rewardFanClubGemCount!!)
        sharedPreferences.putAdCount(MySharedPreferences.PREF_KEY_REWARD_FAN_CLUB_GEM_COUNT, rewardGemCount.minus(1))

        sharedPreferences.putLong(MySharedPreferences.PREF_KEY_REWARD_FAN_CLUB_GEM_TIME, System.currentTimeMillis())

        addGem(preferencesDTO.rewardFanClubGem!!, dialog)
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
                            var log = LogDTO("[다이아 차감] ($gemCount)다이아로 팬클럽 ($exp)경험치 기부 (paidGem : $oldPaidGemCount -> ${userDTO.paidGem}, freeGem : $oldFreeGemCount -> ${userDTO.freeGem})", date)
                            firebaseViewModel.writeUserLog(user.uid.toString(), log) { }
                        }
                    }
                }

                // 팬클럽 멤버 기여도 추가
                firebaseViewModel.addMemberContribution(fanClubDTO.docName.toString(), currentMember?.userUid.toString(), exp) { memberDTO ->
                    if (memberDTO != null) {
                        // 기여도 추가 성공
                    }
                }

                var log = LogDTO("[경험치 증가] 경험치 [$exp] 증가 (exp : ${oldFanClub?.exp} -> ${fanClubDTO.exp}), (level : ${oldFanClub?.level} -> ${fanClubDTO.level}), 기여자 (uid : ${user.uid}, nickname : ${user.nickname})", date)
                firebaseViewModel.writeFanClubLog(fanClubDTO.docName.toString(), log) { }

                (activity as MainActivity?)?.loadingEnd()

                if (expUpFanClubDialog == null) {
                    expUpFanClubDialog = ExpUpFanClubDialog(requireContext())
                    expUpFanClubDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    expUpFanClubDialog?.setCanceledOnTouchOutside(false)
                }
                expUpFanClubDialog?.fanClubDTO = oldFanClub
                expUpFanClubDialog?.userDTO = user
                expUpFanClubDialog?.addExp = exp
                expUpFanClubDialog?.gemCount = gemCount
                expUpFanClubDialog?.show()

                thread(start = true) {
                    Thread.sleep(1300)

                    activity?.runOnUiThread {
                        setInfo()
                        if (fanClubDTO.level!! > oldFanClub?.level!!) { // 레벨업 했다면 레벨업 대화상자 호출
                            expUpFanClubDialog?.dismiss()
                            expUpFanClubDialog = null

                            if (levelUpFanClubDialog == null) {
                                levelUpFanClubDialog = LevelUpFanClubDialog(requireContext())
                                levelUpFanClubDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                                levelUpFanClubDialog?.setCanceledOnTouchOutside(false)
                            }
                            levelUpFanClubDialog?.oldFanClubDTO = oldFanClub
                            levelUpFanClubDialog?.newFanClubDTO = fanClubDTO
                            levelUpFanClubDialog?.show()

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

                            levelUpFanClubDialog?.binding?.buttonLevelUpFanClubOk?.setOnClickListener {
                                levelUpFanClubDialog?.dismiss()
                                levelUpFanClubDialog = null

                                if (dialog != null) {
                                    dialog.oldFanClubDTO = fanClubDTO.copy()
                                    dialog.newFanClubDTO = fanClubDTO.copy()
                                    dialog.setInfo()
                                }
                            }
                        } else {
                            expUpFanClubDialog?.binding?.buttonExpUpFanClubOk?.visibility = View.VISIBLE
                        }
                    }
                }

                expUpFanClubDialog?.binding?.buttonExpUpFanClubOk?.setOnClickListener {
                    expUpFanClubDialog?.dismiss()
                    expUpFanClubDialog = null

                    if (dialog != null) {
                        dialog.oldFanClubDTO = fanClubDTO.copy()
                        dialog.newFanClubDTO = fanClubDTO.copy()
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
                var log = LogDTO("[사용자 무료 다이아 획득] 다이아 $gemCount 획득 (freeGem : $oldFreeGemCount -> ${userDTO.freeGem})", Date())
                firebaseViewModel.writeUserLog(user.uid.toString(), log) { }

                val getDialog = GetItemDialog(requireContext())
                getDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                getDialog.setCanceledOnTouchOutside(false)
                getDialog.mailDTO = MailDTO("", "", "", "", MailDTO.Item.FREE_GEM, gemCount)
                getDialog.show()

                getDialog.binding.buttonGetItemOk.setOnClickListener {
                    getDialog.dismiss()

                    if (dialog != null) {
                        dialog.oldUserDTO = userDTO.copy()
                        dialog.newUserDTO = userDTO.copy()
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
            else -> binding.layoutPosition.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.member))
        }
        binding.imgPosition.setImageResource(currentMember?.getPositionImage()!!)
        binding.textPosition.text = currentMember?.getPositionString()

        binding.textMemberLevel.text = "Lv. ${currentMember?.userLevel}"
        binding.textMemberName.text = currentMember?.userNickname
        binding.textContribution.text = "기여도 : ${decimalFormat.format(currentMember?.contribution!!)}"
        binding.textResponseTime.text = SimpleDateFormat("yyyy.MM.dd HH:mm").format(currentMember?.responseTime!!)
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
                "아직 팬클럽 이름을 변경할 수 없습니다. 팬클럽 이름은 3일마다 변경 가능합니다.\n\n최종변경일 [${SimpleDateFormat("yyyy.MM.dd HH:mm").format(fanClubExDTO?.fanClubDTO?.nameChangeDate!!)}]",
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
            questionDialog?.showButtonOk(false)
            questionDialog?.setButtonCancel("확인")
            questionDialog?.binding?.buttonQuestionCancel?.setOnClickListener { // No
                questionDialog?.dismiss()
                questionDialog = null
            }
        } else {
            val item = EditTextDTO("팬클럽 이름 변경", fanClubExDTO?.fanClubDTO?.name, 30, "^[가-힣ㄱ-ㅎa-zA-Z0-9.~!@#\$%^&*\\[\\](){}|_ -]{1,15}\$", "사용할 수 없는 문자열이 포함되어 있습니다.")
            if (editTextModifyDialog == null) {
                editTextModifyDialog = EditTextModifyDialog(requireContext(), item)
                editTextModifyDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                editTextModifyDialog?.setCanceledOnTouchOutside(false)
            } else {
                editTextModifyDialog?.item = item
            }
            editTextModifyDialog?.show()
            editTextModifyDialog?.setInfo()
            editTextModifyDialog?.showImgOk(true)
            editTextModifyDialog?.binding?.buttonModifyCancel?.setOnClickListener { // No
                editTextModifyDialog?.dismiss()
                editTextModifyDialog = null
            }
            editTextModifyDialog?.binding?.buttonModifyOk?.setOnClickListener { // Ok
                val preferencesDTO = (activity as MainActivity?)?.getPreferences()!!

                val question = GemQuestionDTO("다이아를 사용해 팬클럽 이름을 변경합니다.\n(3일마다 1회 변경 가능)", preferencesDTO.priceFanClubName)
                if (gemQuestionDialog == null) {
                    gemQuestionDialog = GemQuestionDialog(requireContext(), question)
                    gemQuestionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    gemQuestionDialog?.setCanceledOnTouchOutside(false)
                } else {
                    gemQuestionDialog?.question = question
                }
                gemQuestionDialog?.mainActivity = (activity as MainActivity?)
                gemQuestionDialog?.show()
                gemQuestionDialog?.setInfo()

                gemQuestionDialog?.binding?.buttonGemQuestionCancel?.setOnClickListener { // No
                    gemQuestionDialog?.dismiss()
                    gemQuestionDialog = null
                }
                gemQuestionDialog?.binding?.buttonGemQuestionOk?.setOnClickListener { // Ok
                    gemQuestionDialog?.dismiss()
                    gemQuestionDialog = null

                    val user = (activity as MainActivity?)?.getUser()!!
                    if ((user.paidGem!! + user.freeGem!!) < preferencesDTO.priceFanClubName!!) {
                        Toast.makeText(activity, "다이아가 부족합니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        val name = editTextModifyDialog?.binding?.editContent?.text.toString().trim()
                        firebaseViewModel.isUsedFanClubName(name) { isUsed ->
                            if (isUsed) {
                                Toast.makeText(activity, "팬클럽 이름이 이미 존재합니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                editTextModifyDialog?.dismiss()
                                editTextModifyDialog = null
                                if (fanClubExDTO?.fanClubDTO?.name != name) {
                                    (activity as MainActivity?)?.loading()

                                    val oldName = fanClubExDTO?.fanClubDTO?.name
                                    fanClubExDTO?.fanClubDTO?.name = name
                                    fanClubExDTO?.fanClubDTO?.nameChangeDate = Date()
                                    firebaseViewModel.updateFanClub(fanClubExDTO?.fanClubDTO!!) {
                                        // 다이아 차감
                                        val oldPaidGemCount = user.paidGem!!
                                        val oldFreeGemCount = user.freeGem!!
                                        firebaseViewModel.useUserGem(user.uid.toString(), preferencesDTO.priceFanClubName) { userDTO ->
                                            if (userDTO != null) {
                                                var log = LogDTO("[다이아 차감] 팬클럽 이름 변경으로 ${preferencesDTO.priceFanClubName} 다이아 사용 ($oldName -> ${fanClubExDTO?.fanClubDTO?.name}), (paidGem : $oldPaidGemCount -> ${userDTO.paidGem}, freeGem : $oldFreeGemCount -> ${userDTO.freeGem})", Date())
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
            calendar.time = fanClubExDTO?.fanClubDTO?.nameChangeDate!!
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
        if (questionDialog == null) {
            questionDialog = QuestionDialog(requireContext(), question)
            questionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            questionDialog?.setCanceledOnTouchOutside(false)
        } else {
            questionDialog?.question = question
        }
        questionDialog?.show()
        questionDialog?.setInfo()
        questionDialog?.showImgOk(true)
        questionDialog?.binding?.buttonQuestionCancel?.setOnClickListener { // No
            questionDialog?.dismiss()
            questionDialog = null
        }
        questionDialog?.binding?.buttonQuestionOk?.setOnClickListener { // Ok
            questionDialog?.dismiss()
            questionDialog = null

            val preferencesDTO = (activity as MainActivity?)?.getPreferences()!!

            val gemQuestion = GemQuestionDTO("다이아를 사용해 팬클럽 심볼을 변경합니다.", preferencesDTO.priceFanClubSymbol)
            if (gemQuestionDialog == null) {
                gemQuestionDialog = GemQuestionDialog(requireContext(), gemQuestion)
                gemQuestionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                gemQuestionDialog?.setCanceledOnTouchOutside(false)
            } else {
                gemQuestionDialog?.question = gemQuestion
            }
            gemQuestionDialog?.mainActivity = (activity as MainActivity?)
            gemQuestionDialog?.show()
            gemQuestionDialog?.setInfo()

            gemQuestionDialog?.binding?.buttonGemQuestionCancel?.setOnClickListener { // No
                gemQuestionDialog?.dismiss()
                gemQuestionDialog = null
            }
            gemQuestionDialog?.binding?.buttonGemQuestionOk?.setOnClickListener { // Ok
                gemQuestionDialog?.dismiss()
                gemQuestionDialog = null

                val user = (activity as MainActivity?)?.getUser()!!
                if ((user.paidGem!! + user.freeGem!!) < preferencesDTO.priceFanClubSymbol!!) {
                    Toast.makeText(activity, "다이아가 부족합니다.", Toast.LENGTH_SHORT).show()
                } else {
                    var imageID = requireContext().resources.getIdentifier(selectedSymbol, "drawable", requireContext().packageName)
                    (activity as MainActivity?)?.loading()

                    val oldImgSymbol = fanClubExDTO?.fanClubDTO?.imgSymbol
                    val oldImgSymbolCustom = fanClubExDTO?.fanClubDTO?.imgSymbolCustom
                    if (uri == null) { // 제공된 심볼 중에서 선택
                        fanClubExDTO?.fanClubDTO?.imgSymbol = selectedSymbol
                        fanClubExDTO?.fanClubDTO?.imgSymbolCustom = null
                        fanClubExDTO?.fanClubDTO?.imgSymbolUpdateTime = Date()
                        fanClubExDTO?.imgSymbolCustomUri = null
                        firebaseViewModel.updateFanClub(fanClubExDTO?.fanClubDTO!!) {
                            // 다이아 차감
                            val oldPaidGemCount = user.paidGem!!
                            val oldFreeGemCount = user.freeGem!!
                            firebaseViewModel.useUserGem(user.uid.toString(), preferencesDTO.priceFanClubSymbol) { userDTO ->
                                if (userDTO != null) {
                                    var log = LogDTO("[다이아 차감] 팬클럽 심볼 변경으로 ${preferencesDTO.priceFanClubSymbol} 다이아 사용 ($oldImgSymbol -> ${fanClubExDTO?.fanClubDTO?.imgSymbol}), (paidGem : $oldPaidGemCount -> ${userDTO.paidGem}, freeGem : $oldFreeGemCount -> ${userDTO.freeGem})", Date())
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
                            firebaseStorageViewModel.setFanClubSymbolImage(fanClubExDTO?.fanClubDTO?.docName.toString(), bitmap) {
                                if (!it) {
                                    Toast.makeText(activity, "이미지 업로드 실패 ", Toast.LENGTH_SHORT).show()
                                    (activity as MainActivity?)?.loadingEnd()
                                } else {
                                    fanClubExDTO?.fanClubDTO?.imgSymbol = "reward_icon_25" // 기본 값
                                    fanClubExDTO?.fanClubDTO?.imgSymbolCustom = fanClubExDTO?.fanClubDTO?.getSymbolCustomImageName()
                                    fanClubExDTO?.fanClubDTO?.imgSymbolUpdateTime = Date()
                                    firebaseViewModel.updateFanClub(fanClubExDTO?.fanClubDTO!!) {
                                        // 다이아 차감
                                        val oldPaidGemCount = user.paidGem!!
                                        val oldFreeGemCount = user.freeGem!!
                                        firebaseViewModel.useUserGem(user.uid.toString(), preferencesDTO.priceFanClubSymbol) { userDTO ->
                                            if (userDTO != null) {
                                                var log = LogDTO("[다이아 차감] 팬클럽 심볼(커스텀) 변경으로 ${preferencesDTO.priceFanClubSymbol} 다이아 사용 ($oldImgSymbolCustom -> ${fanClubExDTO?.fanClubDTO?.imgSymbolCustom}), (paidGem : $oldPaidGemCount -> ${userDTO.paidGem}, freeGem : $oldFreeGemCount -> ${userDTO.freeGem})", Date())
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

    private fun modifySymbol() {
        if (selectFanClubSymbolDialog == null) {
            selectFanClubSymbolDialog = SelectFanClubSymbolDialog(requireContext())
            selectFanClubSymbolDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            selectFanClubSymbolDialog?.setCanceledOnTouchOutside(false)
        }
        selectFanClubSymbolDialog?.mainActivity = (activity as MainActivity?)
        selectFanClubSymbolDialog?.show()
        selectFanClubSymbolDialog?.setInfo()

        selectFanClubSymbolDialog?.setOnDismissListener {
            if (selectFanClubSymbolDialog?.isOK!! && !selectFanClubSymbolDialog?.selectedSymbol.isNullOrEmpty()) {
                if (selectFanClubSymbolDialog?.isAddImage!!) {
                    resultLauncher.launch("image/*")
                } else {
                    modifySymbolApply(selectFanClubSymbolDialog?.selectedSymbol!!)
                }
            }
            selectFanClubSymbolDialog = null
        }
    }

    private fun modifyNotice() {
        val item = EditTextDTO("공지사항 변경", fanClubExDTO?.fanClubDTO?.notice, 600)
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
            editTextModifyDialog = null
        }
        editTextModifyDialog?.binding?.buttonModifyOk?.setOnClickListener { // Ok
            editTextModifyDialog?.dismiss()
            editTextModifyDialog = null

            val question = QuestionDTO(
                QuestionDTO.Stat.WARNING,
                "공지사항 변경",
                "공지사항을 변경하면 되돌릴 수 없습니다.\n정말 변경 하시겠습니까?",
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

                val oldNotice = fanClubExDTO?.fanClubDTO?.notice.toString()
                fanClubExDTO?.fanClubDTO?.notice = editTextModifyDialog?.binding?.editContent?.text.toString()
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