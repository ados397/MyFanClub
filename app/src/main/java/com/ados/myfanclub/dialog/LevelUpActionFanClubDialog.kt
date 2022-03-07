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
import com.ados.myfanclub.MySharedPreferences
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.LevelUpActionFanClubDialogBinding
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.PreferencesDTO
import com.ados.myfanclub.model.UserDTO
import java.text.DecimalFormat
import kotlin.math.ceil

class LevelUpActionFanClubDialog(context: Context) : Dialog(context), View.OnClickListener {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    lateinit var binding: LevelUpActionFanClubDialogBinding

    private val layout = R.layout.level_up_action_fan_club_dialog
    private var gemExp = 100L
    private var rewardGem = 1
    private var availableExpGem = 0
    private var rewardExpCount = 0
    private var rewardGemCount = 0
    var useGemCount = 0
    var addExp = 0L

    var preferencesDTO : PreferencesDTO? = null
    var oldUserDTO : UserDTO? = null
    var newUserDTO : UserDTO? = null
    var oldFanClubDTO : FanClubDTO? = null
    var newFanClubDTO : FanClubDTO? = null

    private var toast : Toast? = null

    private lateinit var rewardFanClubExpTimer : CountDownTimer
    var isRunTimerFanClubExp = false
    private lateinit var rewardFanClubGemTimer : CountDownTimer
    var isRunTimerFanClubGem = false

    private val sharedPreferences: MySharedPreferences by lazy {
        MySharedPreferences(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LevelUpActionFanClubDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

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
        val totalGem = newUserDTO?.getTotalGem()!!

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

        val nextLevelExp = newFanClubDTO?.getNextLevelExp()!!
        val totalExp = newFanClubDTO?.exp?.plus(gemExp.times(gemCount))!!
        val remindExp = nextLevelExp.minus(newFanClubDTO?.exp!!) // 레벨업 까지 남은 경험치

        // 최대 경험치를 초과할 경우 최대 경험치 만큼만 추가
        if (totalExp > nextLevelExp) {
            newFanClubDTO?.exp = nextLevelExp
            //gemCount = ceil(remindExp.div(gemExp).toDouble()).toInt() // 레벨업 까지 필요한 다이아 계산 (올림 연산)
            gemCount = ceil(remindExp.toDouble().div(gemExp.toDouble())).toInt() // 레벨업 까지 필요한 다이아 계산 (올림 연산)
        } else {
            newFanClubDTO?.exp = totalExp
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
        newFanClubDTO = oldFanClubDTO?.copy()
        useGemCount = 0
        addExp = 0L
        binding.textUseGemCount.text = decimalFormat.format(useGemCount)
        availableExpGem = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_AVAILABLE_FAN_CLUB_EXP_GEM, preferencesDTO?.availableFanClubExpGem!!)
        binding.textUpExpCount.text = "오늘 사용 가능한 다이아 : ${decimalFormat.format(availableExpGem)}"
    }

    fun setInfo() {
        gemExp = preferencesDTO?.rewardFanClubExp!!
        rewardGem = preferencesDTO?.rewardFanClubGem!!

        availableExpGem = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_AVAILABLE_FAN_CLUB_EXP_GEM, preferencesDTO?.availableFanClubExpGem!!)
        rewardExpCount = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_REWARD_FAN_CLUB_EXP_COUNT, preferencesDTO?.rewardFanClubExpCount!!)
        rewardGemCount = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_REWARD_FAN_CLUB_GEM_COUNT, preferencesDTO?.rewardFanClubGemCount!!)

        if (oldUserDTO?.isPremium()!!) {
            gemExp = gemExp.times(2)
            binding.layoutPremiumUpExp.visibility = View.VISIBLE
            binding.layoutPremiumGetExp.visibility = View.VISIBLE
            binding.layoutPremiumGetGem.visibility = View.VISIBLE
        } else {
            binding.layoutPremiumUpExp.visibility = View.GONE
            binding.layoutPremiumGetExp.visibility = View.GONE
            binding.layoutPremiumGetGem.visibility = View.GONE
        }
        gemExp = gemExp.plus(oldUserDTO?.level!!)

        resetExp()
        setProgress()
        setRewardFanClubExpTimer()
        setRewardFanClubGemTimer()

        val gemExpString = decimalFormat.format(gemExp)
        val ssb1 = SpannableStringBuilder("1 다이아 당 ${gemExpString}의 경험치 획득!")
        ssb1.apply {
            setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.text_red)), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.text_red)), 8, 8 + gemExpString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.textUseGem.text = ssb1
        binding.textUpExpCount.text = "오늘 사용 가능한 다이아 : ${decimalFormat.format(availableExpGem)}"

        val ssb2 = SpannableStringBuilder("광고를 시청하고 무료 경험치 ${gemExpString}을 받으세요!")
        ssb2.apply {
            setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.text_red)), 16, 16 + gemExpString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.textGetExp.text = ssb2
        binding.textGetExpCount.text = "오늘 받을 수 있는 횟수 : ${decimalFormat.format(rewardExpCount)}"

        val rewardGemString = decimalFormat.format(rewardGem)
        val ssb3 = SpannableStringBuilder("광고를 시청하고 무료 다이아 1개를 받으세요!")
        ssb3.apply {
            setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.text_red)), 16, 16 + rewardGemString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.textGetGem.text = ssb3
        binding.textGetGemCount.text = "오늘 받을 수 있는 횟수 : ${decimalFormat.format(rewardGemCount)}"
    }

    private fun setProgress() {
        println("올드 ${oldFanClubDTO?.exp}, 뉴 ${newFanClubDTO?.exp}")
        val nextLevelExp = newFanClubDTO?.getNextLevelExp()!!
        var percent = ((newFanClubDTO?.exp?.toDouble()!! / nextLevelExp) * 100).toInt()
        binding.progressPercent.progress = percent

        binding.textExp.text = "${decimalFormat.format(newFanClubDTO?.exp)}(+${decimalFormat.format(addExp)})"
        binding.textExpTotal.text = "/${decimalFormat.format(nextLevelExp)}"

        if (oldFanClubDTO?.exp!! < newFanClubDTO?.exp!!) {
            binding.textExp.setTextColor(ContextCompat.getColor(context!!, R.color.text_red))
            //binding.progressPercent.progressDrawable.setColorFilter(ContextCompat.getColor(context!!, R.color.progress_0), PorterDuff.Mode.SRC_IN)
            //binding.progressPercent.progressBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.progress_background_0))
            binding.progressPercent.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.progress_background_0))
            binding.buttonUpExpFanClub.isEnabled = true
            binding.buttonUpExpFanClub.background = AppCompatResources.getDrawable(context, R.drawable.btn_round8)
            binding.buttonUpExpFanClub.setTextColor(ContextCompat.getColor(context!!, R.color.text_gold))
        } else {
            binding.textExp.setTextColor(ContextCompat.getColor(context!!, R.color.text))
            //binding.progressPercent.progressDrawable.colorFilter = null
            binding.progressPercent.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.progress_70))
            binding.buttonUpExpFanClub.isEnabled = false
            binding.buttonUpExpFanClub.background = AppCompatResources.getDrawable(context, R.drawable.btn_round2)
            binding.buttonUpExpFanClub.setTextColor(ContextCompat.getColor(context!!, R.color.text_disable))
        }
    }

    private fun setRewardFanClubExpTimer() {
        //isReward = false

        var rewardTime = sharedPreferences.getLong(MySharedPreferences.PREF_KEY_REWARD_FAN_CLUB_EXP_TIME, 0L)

        // 인터벌 시간하고, 최대개수 디비로 설정하여 핫타임때 적용하도록
        var intervalMillis = 1000 * preferencesDTO?.rewardFanClubExpTime!!.toLong()
        if (oldUserDTO?.isPremium()!!) { // 프리미엄 패키지는 광고 충전시간 절반
            intervalMillis = intervalMillis.div(2)
        }

        var interval = 0L
        if (rewardTime != 0L) {
            interval = (rewardTime + intervalMillis) - System.currentTimeMillis()
        }

        println("데이터 확인 rewardTime = $rewardTime, intervalMillis = $intervalMillis, interval = $interval ")

        // 타이머가 동작중이면 종료 후 다시 실행
        if (isRunTimerFanClubExp)
            rewardFanClubExpTimer.cancel()

        isRunTimerFanClubExp = true
        rewardFanClubExpTimer = object : CountDownTimer(interval, 1000) {
            override fun onFinish() {
                isRunTimerFanClubExp = false
                //isReward = true

                binding.buttonGetFanClubExpAd.text = "광고보기"
            }

            override fun onTick(millisUntilFinished: Long) {
                var totalsec = millisUntilFinished / 1000
                var min = (totalsec % 3600) / 60
                var sec = totalsec % 60

                binding.buttonGetFanClubExpAd.text = "${String.format("%02d", min)}:${String.format("%02d", sec)}"
            }

        }.start()
    }

    private fun setRewardFanClubGemTimer() {
        //isReward = false

        var rewardTime = sharedPreferences.getLong(MySharedPreferences.PREF_KEY_REWARD_FAN_CLUB_GEM_TIME, 0L)

        // 인터벌 시간하고, 최대개수 디비로 설정하여 핫타임때 적용하도록
        var intervalMillis = 1000 * preferencesDTO?.rewardFanClubGemTime!!.toLong()
        if (oldUserDTO?.isPremium()!!) { // 프리미엄 패키지는 광고 충전시간 절반
            intervalMillis = intervalMillis.div(2)
        }

        var interval = 0L
        if (rewardTime != 0L) {
            interval = (rewardTime + intervalMillis) - System.currentTimeMillis()
        }

        println("데이터 확인 rewardTime = $rewardTime, intervalMillis = $intervalMillis, interval = $interval ")

        // 타이머가 동작중이면 종료 후 다시 실행
        if (isRunTimerFanClubGem)
            rewardFanClubGemTimer.cancel()

        isRunTimerFanClubGem = true
        rewardFanClubGemTimer = object : CountDownTimer(interval, 1000) {
            override fun onFinish() {
                isRunTimerFanClubGem = false
                //isReward = true

                binding.buttonGetFanClubGemAd.text = "광고보기"
            }

            override fun onTick(millisUntilFinished: Long) {
                var totalsec = millisUntilFinished / 1000
                var min = (totalsec % 3600) / 60
                var sec = totalsec % 60

                binding.buttonGetFanClubGemAd.text = "${String.format("%02d", min)}:${String.format("%02d", sec)}"
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
}