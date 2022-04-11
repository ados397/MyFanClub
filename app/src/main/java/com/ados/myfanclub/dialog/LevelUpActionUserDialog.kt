package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.MySharedPreferences
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.LevelUpActionUserDialogBinding
import com.ados.myfanclub.model.PreferencesDTO
import com.ados.myfanclub.model.UserDTO
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
import java.text.DecimalFormat
import kotlin.math.ceil

class LevelUpActionUserDialog(context: Context) : Dialog(context), View.OnClickListener {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    lateinit var binding: LevelUpActionUserDialogBinding
    var mainActivity: MainActivity? = null

    private val layout = R.layout.level_up_action_user_dialog
    var isTutorial = false
    private var gemExp = 20L
    private var rewardGem = 1
    private var availableExpGem = 0
    private var rewardExpCount = 0
    private var rewardGemCount = 0
    var useGemCount = 0
    var addExp = 0L

    var preferencesDTO : PreferencesDTO? = null
    var oldUserDTO : UserDTO? = null
    var newUserDTO : UserDTO? = null

    private var toast : Toast? = null

    private lateinit var rewardUserExpTimer : CountDownTimer
    var isRunTimerUserExp = false
    private lateinit var rewardUserGemTimer : CountDownTimer
    var isRunTimerUserGem = false

    private val sharedPreferences: MySharedPreferences by lazy {
        MySharedPreferences(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LevelUpActionUserDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        observeTutorial()

        setInfo()

        binding.buttonReset.setOnClickListener {
            resetExp()
            setProgress()
        }
        binding.buttonPlus.setOnClickListener {
            plusExp(1)
        }
        binding.buttonPlus10.setOnClickListener {
            plusExp(10)
        }
        binding.buttonPlus100.setOnClickListener {
            plusExp(100)
        }
        binding.buttonMax.setOnClickListener {
            plusExp(Int.MAX_VALUE)
        }

    }

    private fun plusExp(count: Int) {
        var gemCount = count
        //val totalGem = newUserDTO?.getTotalGem()!!
        val totalGem = if (isTutorial) { // 튜토리얼 진행중에는 다이아 소모 없음.
            count
        } else { // 튜토리얼이 아닐때는 정상적으로 적용
            newUserDTO?.getTotalGem()!!
        }

        // 가진 다이아를 초과하였을 경우 가진 다이아 만큼만 추가
        if (totalGem < gemCount) {
            gemCount = totalGem
        }

        // 하루 사용 가능 다이아를 초과하였을 경우 사용 가능 다이아 만큼만 추가
        if (gemCount > availableExpGem) {
            gemCount = availableExpGem
        }

        if (totalGem <= 0) {
            if (toast == null) {
                toast = Toast.makeText(context, "다이아가 부족합니다.", Toast.LENGTH_SHORT)
            }
            toast?.show()
        }

        if (availableExpGem <= 0) {
            if (toast == null) {
                toast = Toast.makeText(context, "오늘은 더 이상 다이아를 사용할 수 없습니다. 내일 다시 사용해 주세요.", Toast.LENGTH_SHORT)
            }
            toast?.show()
        }

        val nextLevelExp = newUserDTO?.getNextLevelExp()!!
        val totalExp = newUserDTO?.exp?.plus(gemExp.times(gemCount))!!
        val remindExp = nextLevelExp.minus(newUserDTO?.exp!!) // 레벨업 까지 남은 경험치

        // 최대 경험치를 초과할 경우 최대 경험치 만큼만 추가
        if (totalExp > nextLevelExp) {
            newUserDTO?.exp = nextLevelExp
            //gemCount = ceil(remindExp.div(gemExp).toDouble()).toInt() // 레벨업 까지 필요한 다이아 계산 (올림 연산)
            gemCount = ceil(remindExp.toDouble().div(gemExp.toDouble())).toInt() // 레벨업 까지 필요한 다이아 계산 (올림 연산)
        } else {
            newUserDTO?.exp = totalExp
        }

        newUserDTO?.useGem(gemCount)

        useGemCount = useGemCount.plus(gemCount)
        addExp = gemExp.times(useGemCount)
        binding.textUseGemCount.text = decimalFormat.format(useGemCount)
        availableExpGem = availableExpGem.minus(gemCount)
        binding.textUpExpCount.text = "오늘 사용 가능한 다이아 : ${decimalFormat.format(availableExpGem)}"
        setProgress()
    }

    /*private fun plusExp(exp: Long) {
        val nextLevelExp = newUserDTO?.getNextLevelExp()!!
        val totalExp = newUserDTO?.exp?.plus(exp)!!
        if (totalExp > nextLevelExp) {
            newUserDTO?.exp = nextLevelExp
        } else {
            newUserDTO?.exp = totalExp
        }

        // 소모 다이아 계산
        totalExp / gemExp
    }*/

    private fun resetExp() {
        newUserDTO = oldUserDTO?.copy()
        useGemCount = 0
        addExp = 0L
        binding.textUseGemCount.text = decimalFormat.format(useGemCount)
        availableExpGem = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_AVAILABLE_USER_EXP_GEM, preferencesDTO?.availableUserExpGem!!)
        binding.textUpExpCount.text = "오늘 사용 가능한 다이아 : ${decimalFormat.format(availableExpGem)}"
    }

    fun setInfo() {
        gemExp = preferencesDTO?.rewardUserExp!!
        rewardGem = preferencesDTO?.rewardUserGem!!

        availableExpGem = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_AVAILABLE_USER_EXP_GEM, preferencesDTO?.availableUserExpGem!!)
        rewardExpCount = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_REWARD_USER_EXP_COUNT, preferencesDTO?.rewardUserExpCount!!)
        rewardGemCount = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_REWARD_USER_GEM_COUNT, preferencesDTO?.rewardUserGemCount!!)

        var gemExpString = decimalFormat.format(gemExp)
        if (oldUserDTO?.isPremium()!!) {
            gemExpString = "${decimalFormat.format(gemExp.times(2))}(${decimalFormat.format(gemExp)}+${decimalFormat.format(gemExp)})"
            gemExp = gemExp.times(2)
            binding.layoutPremiumUpExp.visibility = View.VISIBLE
            binding.layoutPremiumGetExp.visibility = View.VISIBLE
            binding.layoutPremiumGetGem.visibility = View.VISIBLE
        } else {
            binding.layoutPremiumUpExp.visibility = View.GONE
            binding.layoutPremiumGetExp.visibility = View.GONE
            binding.layoutPremiumGetGem.visibility = View.GONE
        }

        resetExp()
        setProgress()
        setRewardUserExpTimer()
        setRewardUserGemTimer()

        val ssb1 = SpannableStringBuilder("1 다이아 당 ${gemExpString}의 경험치 획득!")
        ssb1.apply {
            setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_red)), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_red)), 8, 8 + gemExpString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.textUseGem.text = ssb1
        binding.textUpExpCount.text = "오늘 사용 가능한 다이아 : ${decimalFormat.format(availableExpGem)}"

        val ssb2 = SpannableStringBuilder("광고를 시청하고 무료 경험치 ${gemExpString}을 받으세요!")
        ssb2.apply {
            setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_red)), 16, 16 + gemExpString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.textGetExp.text = ssb2
        binding.textGetExpCount.text = "오늘 받을 수 있는 횟수 : ${decimalFormat.format(rewardExpCount)}"

        val rewardGemString = decimalFormat.format(rewardGem)
        val ssb3 = SpannableStringBuilder("광고를 시청하고 무료 다이아 ${rewardGemString}개를 받으세요!")
        ssb3.apply {
            setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_red)), 16, 16 + rewardGemString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.textGetGem.text = ssb3
        binding.textGetGemCount.text = "오늘 받을 수 있는 횟수 : ${decimalFormat.format(rewardGemCount)}"
    }

    private fun setProgress() {
        println("올드 ${oldUserDTO?.exp}, 뉴 ${newUserDTO?.exp}")
        val nextLevelExp = newUserDTO?.getNextLevelExp()!!
        var percent = ((newUserDTO?.exp?.toDouble()!! / nextLevelExp) * 100).toInt()
        binding.progressPercent.progress = percent

        binding.textExp.text = "${decimalFormat.format(newUserDTO?.exp)}(+${decimalFormat.format(addExp)})"
        binding.textExpTotal.text = "/${decimalFormat.format(nextLevelExp)}"

        if (oldUserDTO?.exp!! < newUserDTO?.exp!!) {
            binding.textExp.setTextColor(ContextCompat.getColor(context, R.color.text_red))
            //binding.progressPercent.progressDrawable.setColorFilter(ContextCompat.getColor(context!!, R.color.progress_0), PorterDuff.Mode.SRC_IN)
            //binding.progressPercent.progressBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.progress_background_0))
            binding.progressPercent.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.progress_background_0))
            binding.buttonUpExpUser.isEnabled = true
            binding.buttonUpExpUser.background = AppCompatResources.getDrawable(context, R.drawable.btn_round8)
            binding.buttonUpExpUser.setTextColor(ContextCompat.getColor(context, R.color.text_gold))
        } else {
            binding.textExp.setTextColor(ContextCompat.getColor(context, R.color.text))
            //binding.progressPercent.progressDrawable.colorFilter = null
            binding.progressPercent.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.progress_70))
            binding.buttonUpExpUser.isEnabled = false
            binding.buttonUpExpUser.background = AppCompatResources.getDrawable(context, R.drawable.btn_round2)
            binding.buttonUpExpUser.setTextColor(ContextCompat.getColor(context, R.color.text_disable))
        }
    }

    private fun setRewardUserExpTimer() {
        //isReward = false

        var rewardTime = sharedPreferences.getLong(MySharedPreferences.PREF_KEY_REWARD_USER_EXP_TIME, 0L)

        // 인터벌 시간하고, 최대개수 디비로 설정하여 핫타임때 적용하도록
        var intervalMillis = 1000 * preferencesDTO?.rewardUserExpTime!!.toLong()
        if (oldUserDTO?.isPremium()!!) { // 프리미엄 패키지는 광고 충전시간 절반
            intervalMillis = intervalMillis.div(2)
        }

        var interval = 0L
        if (rewardTime != 0L) {
            interval = (rewardTime + intervalMillis) - System.
            currentTimeMillis()
        }

        println("데이터 확인 rewardTime = $rewardTime, intervalMillis = $intervalMillis, interval = $interval ")

        // 타이머가 동작중이면 종료 후 다시 실행
        if (isRunTimerUserExp)
            rewardUserExpTimer.cancel()

        isRunTimerUserExp = true
        rewardUserExpTimer = object : CountDownTimer(interval, 1000) {
            override fun onFinish() {
                isRunTimerUserExp = false
                //isReward = true

                binding.buttonGetUserExpAd.text = "광고보기"
            }

            override fun onTick(millisUntilFinished: Long) {
                var totalsec = millisUntilFinished / 1000
                var min = (totalsec % 3600) / 60
                var sec = totalsec % 60

                binding.buttonGetUserExpAd.text = "${String.format("%02d", min)}:${String.format("%02d", sec)}"
            }

        }.start()
    }

    private fun setRewardUserGemTimer() {
        //isReward = false

        var rewardTime = sharedPreferences.getLong(MySharedPreferences.PREF_KEY_REWARD_USER_GEM_TIME, 0L)

        // 인터벌 시간하고, 최대개수 디비로 설정하여 핫타임때 적용하도록
        var intervalMillis = 1000 * preferencesDTO?.rewardUserGemTime!!.toLong()
        if (oldUserDTO?.isPremium()!!) { // 프리미엄 패키지는 광고 충전시간 절반
            intervalMillis = intervalMillis.div(2)
        }

        var interval = 0L
        if (rewardTime != 0L) {
            interval = (rewardTime + intervalMillis) - System.currentTimeMillis()
        }

        println("데이터 확인 rewardTime = $rewardTime, intervalMillis = $intervalMillis, interval = $interval ")

        // 타이머가 동작중이면 종료 후 다시 실행
        if (isRunTimerUserGem)
            rewardUserGemTimer.cancel()

        isRunTimerUserGem = true
        rewardUserGemTimer = object : CountDownTimer(interval, 1000) {
            override fun onFinish() {
                isRunTimerUserGem = false
                //isReward = true

                binding.buttonGetUserGemAd.text = "광고보기"
            }

            override fun onTick(millisUntilFinished: Long) {
                var totalsec = millisUntilFinished / 1000
                var min = (totalsec % 3600) / 60
                var sec = totalsec % 60

                binding.buttonGetUserGemAd.text = "${String.format("%02d", min)}:${String.format("%02d", sec)}"
            }

        }.start()
    }

    private fun init() {
        //button_ok.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        /*when (v.id) {
            R.id.button_ok -> {
                dismiss()
            }
        }*/
    }

    private fun observeTutorial() {
        mainActivity?.getTutorialStep()?.observe(mainActivity!!) {
            onTutorial(mainActivity?.getTutorialStep()?.value!!)
        }
    }

    private fun onTutorial(step: Int) {
        when (step) {
            14 -> {
                println("튜토리얼 Step - $step")
                TapTargetSequence(this)
                    .targets(
                        TapTarget.forView(binding.layoutMain,
                            "레벨업을 위한 경험치는 다이아를 사용하거나 무료 경험치 획득 광고를 통해서 얻을 수 있습니다.",
                            "- OK 버튼을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(context, R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.buttonMax,
                            "그럼 다이아를 통해서 레벨업을 진행해 보겠습니다.\n(튜토리얼 진행을 위해 실제 다이아가 소모되지 않습니다.)",
                            "- MAX 버튼을 눌러 경험치를 최대로 채워 볼게요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .tintTarget(true)).listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            plusExp(Int.MAX_VALUE)
                            mainActivity?.addTutorialStep()
                        }
                        override fun onSequenceStep(tutorialStep: TapTarget, targetClicked: Boolean) {

                        }
                        override fun onSequenceCanceled(lastTarget: TapTarget) {
                        }
                    }).start()
            }
            15 -> {
                println("튜토리얼 Step - $step")
                TapTargetView.showFor(this,
                    TapTarget.forView(binding.buttonUpExpUser,
                        "사용하기 버튼을 눌러 레벨업을 해볼게요.",
                        "- 튜토리얼 레벨업은 레벨3 이하에서만 적용됩니다.")
                        .cancelable(false)
                        .dimColor(R.color.black)
                        .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                        .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                        .titleTextSize(18) // Specify the size (in sp) of the title text
                        .transparentTarget(true)
                        .targetRadius(85)
                        .tintTarget(true),object : TapTargetView.Listener() {
                        // The listener can listen for regular clicks, long clicks or cancels
                        override fun onTargetClick(view: TapTargetView) {
                            super.onTargetClick(view) // This call is optional
                            if (newUserDTO?.level!! >= 3) { // 이미 레벨 2 이상이라면 레벨업 적용 안함
                                binding.buttonLevelUpActionUserCancel.performClick()
                                mainActivity?.addTutorialStep(3) // Step 18로 바로 이동 ( 15 + 3 = 18)
                            } else {
                                binding.buttonUpExpUser.performClick()
                                mainActivity?.addTutorialStep()
                            }
                        }
                    })
            }
        }
    }
}