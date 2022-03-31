package com.ados.myfanclub.page

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.MySharedPreferences
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentAccountInfoBinding
import com.ados.myfanclub.dialog.*
import com.ados.myfanclub.model.*
import com.ados.myfanclub.util.AdsRewardManager
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.bumptech.glide.Glide
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
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
 * Use the [FragmentAccountInfo.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentAccountInfo : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    private var _binding: FragmentAccountInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

    // 뷰모델 연결
    private val firebaseViewModel : FirebaseViewModel by viewModels()

    // AD
    private var adsRewardManagerExp: AdsRewardManager? = null
    private var adsRewardManagerGem: AdsRewardManager? = null

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null
    private var currentUserEx: UserExDTO? = null
    private var toast : Toast? = null

    private var questionDialog: QuestionDialog? = null
    private var levelUpActionUserDialog: LevelUpActionUserDialog? = null
    private var gemQuestionDialog: GemQuestionDialog? = null
    private var expUpUserDialog: ExpUpUserDialog? = null
    private var levelUpUserDialog: LevelUpUserDialog? = null
    private var imageViewDialog: ImageViewDialog? = null

    private val sharedPreferences: MySharedPreferences by lazy {
        MySharedPreferences(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        currentUserEx = (activity as MainActivity?)?.getUserEx()
        fanClubDTO = (activity as MainActivity?)?.getFanClub()
        currentMember = (activity as MainActivity?)?.getMember()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAccountInfoBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        val adPolicyDTO = (activity as MainActivity?)?.getAdPolicy()!!
        adsRewardManagerExp = AdsRewardManager(requireActivity(), adPolicyDTO, AdsRewardManager.RewardType.REWARD_USER_EXP)
        adsRewardManagerGem = AdsRewardManager(requireActivity(), adPolicyDTO, AdsRewardManager.RewardType.REWARD_USER_GEM)

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

        observeTutorial()

        setUserInfo()

        binding.swipeRefreshLayout.setOnRefreshListener {
            (activity as MainActivity?)?.loading()

            currentUserEx = (activity as MainActivity?)?.getUserEx()
            setUserInfo()
            binding.swipeRefreshLayout.isRefreshing = false

            (activity as MainActivity?)?.loadingEnd()
        }

        binding.editAboutMe.setOnTouchListener { _, _ ->
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
            false
        }

        binding.buttonProfileSettings.setOnClickListener {
            val fragment = FragmentAccountProfileSettings()
            parentFragmentManager.beginTransaction().apply{
                replace(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
                commit()
            }
        }

        binding.buttonRank.setOnClickListener {
            val fragment = FragmentAccountRank()
            parentFragmentManager.beginTransaction().apply{
                replace(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
                commit()
            }
        }

        binding.buttonSettings.setOnClickListener {
            val fragment = FragmentAccountSettings()
            parentFragmentManager.beginTransaction().apply{
                replace(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
                commit()
            }
        }

        binding.buttonGamble.setOnClickListener {
            val fragment = FragmentGamble()
            parentFragmentManager.beginTransaction().apply{
                replace(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
                commit()
            }
        }

        binding.buttonCheckout.setOnClickListener {
            if (currentUserEx?.userDTO?.isCheckout()!!) {
                Toast.makeText(activity, "이미 출석체크 하였습니다.", Toast.LENGTH_SHORT).show()
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
                    val preferencesDTO = (activity as MainActivity?)?.getPreferences()

                    // 오늘날짜 출석체크 등록
                    val date = Date()
                    currentUserEx?.userDTO?.checkoutTime = date
                    firebaseViewModel.updateUserCheckout(currentUserEx?.userDTO?.uid.toString()) {
                        var exp = (activity as MainActivity?)?.getPreferences()?.rewardUserExp!!
                        if (currentUserEx?.userDTO?.isPremium()!!) { // 프리미엄 패키지 사용중이라면 경험치 두배
                            exp = exp.times(2)
                        }

                        // 경험치 추가 적용
                        applyExp(exp, 0, null)

                        // 다이아 추가
                        addGem(preferencesDTO?.rewardUserCheckoutGem!!)

                        // 일일 퀘스트 - 팬클럽 출석체크 완료 시 적용
                        if (!QuestDTO("개인 출석체크", "개인 출석체크를 완료 하세요.", 1, currentUserEx?.userDTO?.questSuccessTimes?.get("3"), currentUserEx?.userDTO?.questGemGetTimes?.get("3")).isQuestSuccess()) { // 퀘스트 완료 안했을 때 적용
                            currentUserEx?.userDTO?.questSuccessTimes?.set("3", date)
                            firebaseViewModel.updateUserQuestSuccessTimes(currentUserEx?.userDTO!!) {
                                Toast.makeText(activity, "일일 과제 달성! 보상을 획득하세요!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

        binding.buttonLevelUp.setOnClickListener {
            onLevelUp(false)
        }

        binding.imgProfile.setOnClickListener {
            if (imageViewDialog == null) {
                imageViewDialog = ImageViewDialog(requireContext())
                imageViewDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                imageViewDialog?.setCanceledOnTouchOutside(false)
            }
            imageViewDialog?.imageUri = currentUserEx?.imgProfileUri
            imageViewDialog?.imageID = R.drawable.profile
            imageViewDialog?.show()
            imageViewDialog?.setInfo()
            imageViewDialog?.binding?.buttonCancel?.setOnClickListener { // No
                imageViewDialog?.dismiss()
                imageViewDialog = null
            }
        }
    }

    private fun onLevelUp(isTutorial: Boolean) {
        // 디이아 소모 작업 전에는 사용자 최신 정보로 갱신
        // 갱신 안하면 우편에서 받은 다이아 적용이 안됨
        currentUserEx = (activity as MainActivity?)?.getUserEx()
        val preferencesDTO = (activity as MainActivity?)?.getPreferences()

        if (levelUpActionUserDialog == null) {
            levelUpActionUserDialog = LevelUpActionUserDialog(requireContext())
            levelUpActionUserDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            levelUpActionUserDialog?.setCanceledOnTouchOutside(false)
        }
        levelUpActionUserDialog?.mainActivity = (activity as MainActivity?)
        levelUpActionUserDialog?.preferencesDTO = preferencesDTO
        levelUpActionUserDialog?.oldUserDTO = currentUserEx?.userDTO?.copy()
        levelUpActionUserDialog?.newUserDTO = currentUserEx?.userDTO?.copy()
        levelUpActionUserDialog?.isTutorial = isTutorial
        levelUpActionUserDialog?.show()
        levelUpActionUserDialog?.setInfo()

        levelUpActionUserDialog?.binding?.buttonLevelUpActionUserCancel?.setOnClickListener {
            levelUpActionUserDialog?.dismiss()
        }

        levelUpActionUserDialog?.binding?.buttonUpExpUser?.setOnClickListener {
            val question = if (isTutorial) { // 튜토리얼 진행 시 다이아 소모가 없음
                levelUpActionUserDialog?.useGemCount = 0
                GemQuestionDTO("튜토리얼 진행 시 다이아 소모가 없습니다.", levelUpActionUserDialog?.useGemCount)
            } else {
                GemQuestionDTO("다이아를 사용해 경험치를 올립니다.", levelUpActionUserDialog?.useGemCount)
            }

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
            }
            gemQuestionDialog?.binding?.buttonGemQuestionOk?.setOnClickListener { // Ok
                gemQuestionDialog?.dismiss()
                levelUpActionUserDialog?.dismiss()

                (activity as MainActivity?)?.loading()
                val availableExpGem = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_AVAILABLE_USER_EXP_GEM, preferencesDTO?.availableUserExpGem!!)
                sharedPreferences.putAdCount(MySharedPreferences.PREF_KEY_AVAILABLE_USER_EXP_GEM, availableExpGem.minus(levelUpActionUserDialog?.useGemCount!!))
                applyExp(levelUpActionUserDialog?.addExp!!, levelUpActionUserDialog?.useGemCount!!, null)
            }
        }

        levelUpActionUserDialog?.binding?.buttonGetUserExpAd?.setOnClickListener {
            val rewardExpCount = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_REWARD_USER_EXP_COUNT, preferencesDTO?.rewardUserExpCount!!)
            when {
                rewardExpCount <= 0 -> {
                    callToast("오늘은 더 이상 광고를 시청할 수 없습니다.")
                }
                levelUpActionUserDialog?.isRunTimerUserExp!! -> { // 타이머가 동작중이면 광고 시청 불가능
                    callToast("아직 광고를 시청할 수 없습니다.")
                }
                else -> {
                    if (adsRewardManagerExp != null) {
                        adsRewardManagerExp?.callReward {
                            if (it) {
                                rewardExp(levelUpActionUserDialog!!)
                            } else {
                                callToast("아직 광고를 시청할 수 없습니다.")
                            }

                        }
                    }
                }
            }
        }

        levelUpActionUserDialog?.binding?.buttonGetUserGemAd?.setOnClickListener {
            val rewardGemCount = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_REWARD_USER_GEM_COUNT, preferencesDTO?.rewardUserGemCount!!)
            when {
                rewardGemCount <= 0 -> {
                    callToast("오늘은 더 이상 광고를 시청할 수 없습니다.")
                }
                levelUpActionUserDialog?.isRunTimerUserGem!! -> { // 타이머가 동작중이면 광고 시청 불가능
                    callToast("아직 광고를 시청할 수 없습니다.")
                }
                else -> {
                    if (adsRewardManagerGem != null) {
                        adsRewardManagerGem?.callReward {
                            if (it) {
                                rewardGem(levelUpActionUserDialog!!)
                            } else {
                                callToast("아직 광고를 시청할 수 없습니다.")
                            }

                        }
                    }
                }
            }
        }
    }

    private fun rewardExp(dialog: LevelUpActionUserDialog) {
        (activity as MainActivity?)?.loading()
        //Toast.makeText(activity, "보상 $rewardAmount, $rewardType", Toast.LENGTH_SHORT).show()

        //val rewardExpCount = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_REWARD_USER_EXP_COUNT, preferencesDTO?.rewardUserExpCount!!)
        val preferencesDTO = (activity as MainActivity?)?.getPreferences()!!
        val rewardExpCount = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_REWARD_USER_EXP_COUNT, preferencesDTO.rewardUserExpCount!!)
        sharedPreferences.putAdCount(MySharedPreferences.PREF_KEY_REWARD_USER_EXP_COUNT, rewardExpCount.minus(1))

        sharedPreferences.putLong(MySharedPreferences.PREF_KEY_REWARD_USER_EXP_TIME, System.currentTimeMillis())

        //var exp = preferencesDTO?.rewardUserExp!!
        var exp = preferencesDTO.rewardUserExp!!
        if (currentUserEx?.userDTO?.isPremium()!!) { // 프리미엄 패키지 사용중이라면 경험치 두배
            exp = exp.times(2)
        }
        applyExp(exp, 0, dialog)
        Toast.makeText(activity, "보상 획득 완료!", Toast.LENGTH_SHORT).show()

        // 일일 퀘스트 - 개인 무료 경험치 광고 시청 시 적용
        if (!QuestDTO("개인 무료 경험치 광고", "개인 무료 경험치 광고를 1회 이상 시청 하세요.", 1, currentUserEx?.userDTO?.questSuccessTimes?.get("5"), currentUserEx?.userDTO?.questGemGetTimes?.get("5")).isQuestSuccess()) { // 퀘스트 완료 안했을 때 적용
            currentUserEx?.userDTO?.questSuccessTimes?.set("5", Date())
            firebaseViewModel.updateUserQuestSuccessTimes(currentUserEx?.userDTO!!) {
                Toast.makeText(activity, "일일 과제 달성! 보상을 획득하세요!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun rewardGem(dialog: LevelUpActionUserDialog) {
        //val rewardGemCount = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_REWARD_USER_GEM_COUNT, preferencesDTO?.rewardUserGemCount!!)
        val preferencesDTO = (activity as MainActivity?)?.getPreferences()!!
        val rewardGemCount = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_REWARD_USER_GEM_COUNT, preferencesDTO.rewardUserGemCount!!)
        sharedPreferences.putAdCount(MySharedPreferences.PREF_KEY_REWARD_USER_GEM_COUNT, rewardGemCount.minus(1))

        sharedPreferences.putLong(MySharedPreferences.PREF_KEY_REWARD_USER_GEM_TIME, System.currentTimeMillis())

        //addGem(preferencesDTO?.rewardUserGem!!, dialog)
        addGem(preferencesDTO.rewardUserGem!!, dialog)
        Toast.makeText(activity, "보상 획득 완료!", Toast.LENGTH_SHORT).show()

        // 일일 퀘스트 - 개인 무료 다이아 광고 시청 시 적용
        if (!QuestDTO("개인 무료 다이아 광고", "개인 무료 다이아 광고를 1회 이상 시청 하세요.", 1, currentUserEx?.userDTO?.questSuccessTimes?.get("6"), currentUserEx?.userDTO?.questGemGetTimes?.get("6")).isQuestSuccess()) { // 퀘스트 완료 안했을 때 적용
            currentUserEx?.userDTO?.questSuccessTimes?.set("6", Date())
            firebaseViewModel.updateUserQuestSuccessTimes(currentUserEx?.userDTO!!) {
                Toast.makeText(activity, "일일 과제 달성! 보상을 획득하세요!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUserInfo() {
        if (currentUserEx?.imgProfileUri != null) {
            Glide.with(requireContext()).load(currentUserEx?.imgProfileUri).fitCenter().into(binding.imgProfile)
        } else {
            binding.imgProfile.setImageResource(R.drawable.profile)
        }

        binding.textNickname.text = currentUserEx?.userDTO?.nickname
        binding.textUserId.text = "(${currentUserEx?.userDTO?.userId})"
        binding.textCreateTime.text = "가입 ${SimpleDateFormat("yyyy.MM.dd").format(currentUserEx?.userDTO?.createTime!!)}"
        binding.textLevel.text = "Lv. ${currentUserEx?.userDTO?.level}"
        binding.textExp.text = "${decimalFormat.format(currentUserEx?.userDTO?.exp)}/${decimalFormat.format(currentUserEx?.userDTO?.getNextLevelExp())}"
        binding.editAboutMe.setText(currentUserEx?.userDTO?.aboutMe)

        var percent = ((currentUserEx?.userDTO?.exp?.toDouble()!! / currentUserEx?.userDTO?.getNextLevelExp()!!) * 100).toInt()
        binding.progressPercent.progress = percent

        binding.imgCheckout.setImageResource(currentUserEx?.userDTO?.getCheckoutImage()!!)
    }

    // 경험치 적용
    private fun applyExp(exp: Long, gemCount: Int, dialog: LevelUpActionUserDialog?) {
        // 경험치 증가 적용
        val date = Date()
        val oldUser = currentUserEx?.userDTO?.copy()
        firebaseViewModel.addUserExp(currentUserEx?.userDTO?.uid.toString(), exp, gemCount) { userDTO ->
            if (userDTO != null) {
                currentUserEx?.userDTO = userDTO

                // 레벨업 했다면 팬클럽에도 적용
                if (currentUserEx?.userDTO?.level!! > oldUser?.level!!) {
                    // 가입한 팬클럽이 있을 경우 레벨업 적용
                    if (fanClubDTO != null && currentMember != null) {
                        currentMember?.userLevel = currentUserEx?.userDTO?.level
                        firebaseViewModel.updateMemberLevel(fanClubDTO?.docName.toString(), currentMember!!) {
                        }
                    }
                }
                var log = LogDTO("[경험치 증가] 경험치 [$exp] 증가 (exp : ${oldUser.exp} -> ${currentUserEx?.userDTO?.exp}), (level : ${oldUser.level} -> ${currentUserEx?.userDTO?.level})", date)
                firebaseViewModel.writeUserLog(currentUserEx?.userDTO?.uid.toString(), log) { }

                if (gemCount > 0) {
                    log.log = "[다이아 차감] ($gemCount)다이아로 ($exp)경험치 구매 (paidGem : ${oldUser.paidGem} -> ${currentUserEx?.userDTO?.paidGem}, freeGem : ${oldUser.freeGem} -> ${currentUserEx?.userDTO?.freeGem})"
                    firebaseViewModel.writeUserLog(currentUserEx?.userDTO?.uid.toString(), log) { }
                }

                (activity as MainActivity?)?.loadingEnd()

                if (expUpUserDialog == null) {
                    expUpUserDialog = ExpUpUserDialog(requireContext())
                    expUpUserDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    expUpUserDialog?.setCanceledOnTouchOutside(false)
                }
                expUpUserDialog?.userDTO = oldUser
                expUpUserDialog?.addExp = exp
                expUpUserDialog?.gemCount = gemCount
                expUpUserDialog?.show()
                expUpUserDialog?.setInfo()

                thread(start = true) {
                    Thread.sleep(1300)

                    activity?.runOnUiThread {
                        setUserInfo()
                        if (currentUserEx?.userDTO?.level!! > oldUser.level!!) { // 레벨업 했다면 레벨업 대화상자 호출
                            expUpUserDialog?.dismiss()

                            if (levelUpUserDialog == null) {
                                levelUpUserDialog = LevelUpUserDialog(requireContext())
                                levelUpUserDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                                levelUpUserDialog?.setCanceledOnTouchOutside(false)
                            }
                            levelUpUserDialog?.mainActivity = (activity as MainActivity?)
                            levelUpUserDialog?.oldUserDTO = oldUser
                            levelUpUserDialog?.newUserDTO = currentUserEx?.userDTO
                            levelUpUserDialog?.show()
                            levelUpUserDialog?.setInfo()

                            // 레벨업 보상 다이아 우편으로 지급
                            val levelUpGemCount = currentUserEx?.userDTO?.getLevelUpGemCount()
                            val docName = "master${System.currentTimeMillis()}"
                            val calendar= Calendar.getInstance()
                            calendar.add(Calendar.DATE, 7)
                            var mail = MailDTO(docName,"레벨업 보상", "${currentUserEx?.userDTO?.level} 레벨 달성 보상 다이아가 지급되었습니다.", "시스템", MailDTO.Item.FREE_GEM, levelUpGemCount, date, calendar.time)
                            firebaseViewModel.sendUserMail(currentUserEx?.userDTO?.uid.toString(), mail) {
                                var log2 = LogDTO("[레벨업 보상] 레벨 ${currentUserEx?.userDTO?.level} 달성 보상 다이아 $levelUpGemCount 개 우편 발송, 유효기간 : ${SimpleDateFormat("yyyy.MM.dd HH:mm").format(calendar.time)}까지", date)
                                firebaseViewModel.writeUserLog(currentUserEx?.userDTO?.uid.toString(), log2) { }
                            }

                            levelUpUserDialog?.binding?.buttonLevelUpUserOk?.setOnClickListener {
                                levelUpUserDialog?.dismiss()

                                if (dialog != null) {
                                    dialog.oldUserDTO = currentUserEx?.userDTO?.copy()
                                    dialog.newUserDTO = currentUserEx?.userDTO?.copy()
                                    dialog.setInfo()
                                }
                            }
                        } else {
                            expUpUserDialog?.binding?.buttonExpUpUserOk?.visibility = View.VISIBLE
                        }
                    }
                }

                expUpUserDialog?.binding?.buttonExpUpUserOk?.setOnClickListener {
                    expUpUserDialog?.dismiss()

                    if (dialog != null) {
                        dialog.oldUserDTO = currentUserEx?.userDTO?.copy()
                        dialog.newUserDTO = currentUserEx?.userDTO?.copy()
                        dialog.setInfo()
                    }
                }

                (activity as MainActivity?)?.loadingEnd()
            }
        }
    }

    private fun addGem(gemCount: Int, dialog: LevelUpActionUserDialog? = null) {
        currentUserEx = (activity as MainActivity?)?.getUserEx()
        val oldFreeGemCount = currentUserEx?.userDTO?.freeGem!!
        firebaseViewModel.addUserGem(currentUserEx?.userDTO?.uid.toString(), 0, gemCount) { userDTO ->
            if (userDTO != null) {
                currentUserEx?.userDTO = userDTO
                var log = LogDTO("[사용자 무료 다이아 획득] 다이아 $gemCount 획득 (freeGem : $oldFreeGemCount -> ${currentUserEx?.userDTO?.freeGem})", Date())
                firebaseViewModel.writeUserLog(currentUserEx?.userDTO?.uid.toString(), log) { }

                val getDialog = GetItemDialog(requireContext())
                getDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                getDialog.setCanceledOnTouchOutside(false)
                getDialog.mailDTO = MailDTO("", "", "", "", MailDTO.Item.FREE_GEM, gemCount)
                getDialog.show()

                getDialog.binding.buttonGetItemOk.setOnClickListener {
                    getDialog.dismiss()

                    if (dialog != null) {
                        dialog.oldUserDTO = currentUserEx?.userDTO?.copy()
                        dialog.newUserDTO = currentUserEx?.userDTO?.copy()
                        dialog.setInfo()
                    }
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

    private fun observeTutorial() {
        (activity as MainActivity?)?.getTutorialStep()?.observe(viewLifecycleOwner) {
            onTutorial((activity as MainActivity?)?.getTutorialStep()?.value!!)
        }
    }

    private fun onTutorial(step: Int) {
        when (step) {
            13 -> {
                println("튜토리얼 Step - $step")
                TapTargetSequence(requireActivity())
                    .targets(
                        TapTarget.forBounds((activity as MainActivity?)?.getMainLayoutRect(),
                            "여기에서 나의 레벨 및 정보를 확인할 수 있으며 프로필 설정도 가능합니다.",
                            "- OK 버튼을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(requireContext(), R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.buttonLevelUp,
                            "이제 레벨업을 해보겠습니다.",
                            "- 사용자 레벨업 버튼을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .targetRadius(100)
                            .tintTarget(true)).listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            onLevelUp(true)
                            (activity as MainActivity?)?.addTutorialStep()
                        }
                        override fun onSequenceStep(tutorialStep: TapTarget, targetClicked: Boolean) {
                            //Toast.makeText(secondActivity.this,"GREAT!",Toast.LENGTH_SHORT).show();

                        }
                        override fun onSequenceCanceled(lastTarget: TapTarget) {

                        }
                    }).start()
            }
            19 -> {
                println("튜토리얼 Step - $step")
                TapTargetSequence(requireActivity())
                    .targets(
                        TapTarget.forBounds((activity as MainActivity?)?.getMainLayoutRect(),
                            "하루 한번 출석체크도 잊지말고 하세요!",
                            "- OK 버튼을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(requireContext(), R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.buttonCheckout,
                            "경험치와 다이아의 보상이 무료로 주어지니 꼭 챙기세요!",
                            "- 출석체크는 매일 1회 가능 합니다.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .targetRadius(100)
                            .tintTarget(true)).listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            (activity as MainActivity?)?.addTutorialStep()
                        }
                        override fun onSequenceStep(tutorialStep: TapTarget, targetClicked: Boolean) {
                            //Toast.makeText(secondActivity.this,"GREAT!",Toast.LENGTH_SHORT).show();

                        }
                        override fun onSequenceCanceled(lastTarget: TapTarget) {

                        }
                    }).start()

                /*TapTargetView.showFor(requireActivity(),
                    //TapTarget.forView(binding.buttonCheckout,
                    TapTarget.forBounds((activity as MainActivity?)?.getMainLayoutRect(),
                        "그리고 하루 한번 경험치와 다이아 획득이 가능한 출석체크도 잊지말고 꼭 하세요!",
                        "- 출석체크는 매일 1회 가능 합니다.") // All options below are optional
                        .cancelable(false)
                        .dimColor(R.color.black)
                        .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                        .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                        .titleTextSize(18) // Specify the size (in sp) of the title text
                        .transparentTarget(true)
                        .targetRadius(80)
                        .tintTarget(true),object : TapTargetView.Listener() {
                        // The listener can listen for regular clicks, long clicks or cancels
                        override fun onTargetClick(view: TapTargetView) {
                            super.onTargetClick(view) // This call is optional

                            (activity as MainActivity?)?.addTutorialStep()
                        }
                    })*/
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
         * @return A new instance of fragment FragmentAccountInfo.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentAccountInfo().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}