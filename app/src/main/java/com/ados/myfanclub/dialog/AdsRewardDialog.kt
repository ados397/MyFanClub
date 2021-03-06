package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.MySharedPreferences
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.AdsRewardDialogBinding
import com.ados.myfanclub.model.PreferencesDTO
import com.ados.myfanclub.model.UserDTO
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
import java.text.DecimalFormat

class AdsRewardDialog(context: Context) : Dialog(context), View.OnClickListener {

    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    lateinit var binding: AdsRewardDialogBinding
    var mainActivity: MainActivity? = null

    private val layout = R.layout.ads_reward_dialog
    private var rewardItem = 1
    private var rewardGambleCountCount = 0

    var preferencesDTO : PreferencesDTO? = null
    var userDTO : UserDTO? = null

    private var toast : Toast? = null

    private lateinit var rewardTimer : CountDownTimer
    var isRunTimer = false

    private val sharedPreferences: MySharedPreferences by lazy {
        MySharedPreferences(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdsRewardDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setInfo()
    }

    fun setInfo() {
        rewardItem = preferencesDTO?.rewardGambleCount!!
        rewardGambleCountCount = sharedPreferences.getAdCount(MySharedPreferences.PREF_KEY_REWARD_GAMBLE_COUNT_COUNT, preferencesDTO?.rewardGambleCountCount!!)

        if (userDTO?.isPremium()!!) {
            binding.layoutPremium.visibility = View.VISIBLE
        } else {
            binding.layoutPremium.visibility = View.GONE
        }
        binding.layoutPremium.visibility = View.GONE

        //setRewardTimer()

        val rewardString = decimalFormat.format(rewardItem)
        val ssb = SpannableStringBuilder("????????? ???????????? ?????? ????????? ${rewardString}??? ?????? ?????? ???????????????????")
        ssb.apply {
            setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_red)), 16, 16 + rewardString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.textGet.text = ssb
        binding.textGetCount.text = "?????? ?????? ??? ?????? ?????? : ${decimalFormat.format(rewardGambleCountCount)}"
    }

    private fun setRewardTimer() {
        var rewardTime = sharedPreferences.getLong(MySharedPreferences.PREF_KEY_REWARD_GAMBLE_COUNT_TIME, 0L)

        // ????????? ????????????, ???????????? ????????? ???????????? ???????????? ???????????????
        var intervalMillis = 1000 * preferencesDTO?.rewardGambleCountTime!!.toLong()
        if (userDTO?.isPremium()!!) { // ???????????? ???????????? ?????? ???????????? ??????
            intervalMillis = intervalMillis.div(2)
        }

        var interval = 0L
        if (rewardTime != 0L) {
            interval = (rewardTime + intervalMillis) - System.
            currentTimeMillis()
        }

        println("????????? ?????? rewardTime = $rewardTime, intervalMillis = $intervalMillis, interval = $interval ")

        // ???????????? ??????????????? ?????? ??? ?????? ??????
        if (isRunTimer)
            rewardTimer.cancel()

        isRunTimer = true
        rewardTimer = object : CountDownTimer(interval, 1000) {
            override fun onFinish() {
                isRunTimer = false
                //isReward = true

                binding.buttonGet.text = "????????????"
            }

            override fun onTick(millisUntilFinished: Long) {
                var totalsec = millisUntilFinished / 1000
                var min = (totalsec % 3600) / 60
                var sec = totalsec % 60

                binding.buttonGet.text = "${String.format("%02d", min)}:${String.format("%02d", sec)}"
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