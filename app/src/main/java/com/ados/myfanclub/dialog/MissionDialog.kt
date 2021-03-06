package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.MissionDialogBinding
import com.ados.myfanclub.model.DashboardMissionDTO
import com.ados.myfanclub.model.DashboardMissionDTO.*
import com.ados.myfanclub.model.ScheduleDTO
import com.bumptech.glide.Glide
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
import java.text.SimpleDateFormat


class MissionDialog(context: Context) : Dialog(context), View.OnClickListener {

    lateinit var binding: MissionDialogBinding
    var mainActivity: MainActivity? = null

    private val layout = R.layout.mission_dialog

    var dashboardMissionDTO: DashboardMissionDTO? = null

    var missionCount: Long = 0L
    var missionCountMax: Long = 0L
    var missionPercent: Int = 0
    var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MissionDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        observeTutorial()

        setInfo()

        binding.buttonMinus100.setOnClickListener {
            minusCount(100)
        }
        binding.buttonMinus10.setOnClickListener {
            minusCount(10)
        }
        binding.buttonMinus.setOnClickListener {
            minusCount(1)
        }
        binding.buttonPlus.setOnClickListener {
            plusCount(1)
        }
        binding.buttonPlus10.setOnClickListener {
            plusCount(10)
        }
        binding.buttonPlus100.setOnClickListener {
            plusCount(100)
        }
        binding.buttonMax.setOnClickListener {
            missionCount = missionCountMax
            refreshRate()
        }

        binding.buttonExecute.setOnClickListener {
            when (dashboardMissionDTO?.scheduleDTO?.action) { // ??? ??????
                ScheduleDTO.Action.APP -> {
                    val linePackage = dashboardMissionDTO?.scheduleDTO?.appDTO?.packageName.toString()
                    val intentLine = context.packageManager.getLaunchIntentForPackage(linePackage) // ???????????? ????????? ?????? ??????

                    try {
                        context.startActivity(intentLine) // ?????? ?????? ???????????????.
                    } catch (e: Exception) {  // ?????? ????????? ???????????? (?????? ?????????)
                        val intentPlayStore = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$linePackage")) // ?????? ????????? ???????????? ??????
                        context.startActivity(intentPlayStore) // ????????????????????? ??????
                    }
                }
                ScheduleDTO.Action.URL -> {
                    val address = dashboardMissionDTO?.scheduleDTO?.url
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(address))
                    context.startActivity(intent)
                }
                else -> return@setOnClickListener
            }
        }

        binding.textPurpose.movementMethod = ScrollingMovementMethod.getInstance()
        binding.textPurpose.setOnTouchListener { _, _ ->
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    fun setInfo() {
        binding.buttonReport.visibility = if (dashboardMissionDTO?.type == Type.FAN_CLUB) View.VISIBLE else View.GONE

        when (dashboardMissionDTO?.scheduleDTO?.cycle) {
            ScheduleDTO.Cycle.DAY -> binding.imgScheduleType.setImageResource(R.drawable.schedule_day)
            ScheduleDTO.Cycle.WEEK -> binding.imgScheduleType.setImageResource(R.drawable.schedule_week)
            ScheduleDTO.Cycle.MONTH -> binding.imgScheduleType.setImageResource(R.drawable.schedule_month)
            ScheduleDTO.Cycle.PERIOD -> binding.imgScheduleType.setImageResource(R.drawable.schedule_period)
            else -> binding.imgScheduleType.setImageResource(R.drawable.schedule_day)
        }

        binding.textTitle.text = dashboardMissionDTO?.scheduleDTO?.title
        binding.textPurpose.text = dashboardMissionDTO?.scheduleDTO?.purpose
        binding.textRange.text = "${SimpleDateFormat("yyyy.MM.dd").format(dashboardMissionDTO?.scheduleDTO?.startDate!!)} ~ ${SimpleDateFormat("yyyy.MM.dd").format(dashboardMissionDTO?.scheduleDTO?.endDate!!)}"

        if (photoUri != null) {
            binding.imgPhoto.visibility = View.VISIBLE
            Glide.with(context).load(photoUri).fitCenter().into(binding.imgPhoto)
        } else {
            binding.imgPhoto.visibility = View.GONE
        }

        missionCount = dashboardMissionDTO?.scheduleProgressDTO?.count!!
        missionCountMax = dashboardMissionDTO?.scheduleDTO?.count!!

        refreshRate()

        when (dashboardMissionDTO?.scheduleDTO?.action) { // ??? ??????
            ScheduleDTO.Action.APP -> {
                binding.buttonExecute.visibility = View.VISIBLE
                binding.textExecute.text = "??? ??????"
                binding.imgIcon.setImageResource(R.drawable.app)
                binding.textAppName.visibility = View.VISIBLE
                binding.textAppName.text = "[${dashboardMissionDTO?.scheduleDTO?.appDTO?.appName}]"
            }
            ScheduleDTO.Action.URL -> {
                binding.buttonExecute.visibility = View.VISIBLE
                binding.textExecute.text = "?????? ??????"
                binding.imgIcon.setImageResource(R.drawable.link)
                binding.textAppName.visibility = View.GONE
            }
            ScheduleDTO.Action.ETC -> {
                binding.buttonExecute.visibility = View.GONE
            }
            else-> binding.buttonExecute.visibility = View.GONE
        }
    }

    private fun plusCount(count: Int) {
        if (missionCount < missionCountMax) {
            missionCount = if (missionCount + count > missionCountMax) {
                missionCountMax
            } else {
                missionCount.plus(count)
            }
            refreshRate()
        }
    }

    private fun minusCount(count: Int) {
        if (missionCount > 0) {
            missionCount = if (missionCount - count < 0) {
                0
            } else {
                missionCount.minus(count)
            }
            refreshRate()
        }
    }

    private fun refreshRate() {
        binding.textRate.text = "${missionCount}/${missionCountMax}"
        getPercent()
    }

    private fun getPercent() {
        missionPercent = ((missionCount.toDouble() / missionCountMax) * 100).toInt()
        binding.progressPercent.progress = missionPercent

        binding.textPercent.text = "${missionPercent}%"

        if (missionPercent < 100) {
            binding.imgComplete.visibility = View.GONE
            when {
                missionPercent < 40 -> {
                    setPercent(ContextCompat.getColor(context, R.color.progress_0))
                }
                missionPercent < 70 -> {
                    setPercent(ContextCompat.getColor(context, R.color.progress_40))
                }
                else -> {
                    setPercent(ContextCompat.getColor(context, R.color.progress_70))
                }
            }
        } else {
            binding.imgComplete.visibility = View.VISIBLE
            setPercent(ContextCompat.getColor(context, R.color.progress_100))
        }
    }

    private fun setPercent(color: Int) {
        binding.textPercent.setTextColor(color)
        //binding.progressPercent.progressDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        binding.progressPercent.progressDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_ATOP)
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
            9 -> {
                println("???????????? Step - $step")
                TapTargetSequence(this)
                    .targets(
                        TapTarget.forView(binding.layoutMain,
                            "???????????? ?????? ?????? ??? ???????????? ???????????? ??? ??? ????????????",
                            "- OK ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(context, R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.buttonExecute,
                            "??? ????????? ????????? ????????? ?????? ???????????? ???????????? ?????? ?????? ?????????!",
                            "- ??? ????????? ????????? ????????? ????????? ???????????? ??? ?????? ??????????????? ???????????? ?????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .targetRadius(80)
                            .tintTarget(true),
                        TapTarget.forView(binding.buttonMax,
                            "??????????????? ???????????? ????????? ???????????? ???????????? ?????? ?????? ????????? ????????? ???????????????.",
                            "- MAX ????????? ?????? ???????????? 100% ????????? ????????? ?????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .tintTarget(true)).listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            missionCount = missionCountMax
                            refreshRate()
                            mainActivity?.addTutorialStep()
                        }
                        override fun onSequenceStep(tutorialStep: TapTarget, targetClicked: Boolean) {

                        }
                        override fun onSequenceCanceled(lastTarget: TapTarget) {
                        }
                    }).start()
            }
            10 -> {
                println("???????????? Step - $step")
                TapTargetView.showFor(this,
                    TapTarget.forView(binding.buttonMissionOk,
                        "???????????? ???????????? ???????????? ?????? ????????? ?????? ?????? ?????????.",
                        "- ?????? ????????? ???????????????.")
                        .cancelable(false)
                        .dimColor(R.color.black)
                        .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                        .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                        .titleTextSize(18) // Specify the size (in sp) of the title text
                        .transparentTarget(true)
                        .targetRadius(65)
                        .tintTarget(true),object : TapTargetView.Listener() {
                        // The listener can listen for regular clicks, long clicks or cancels
                        override fun onTargetClick(view: TapTargetView) {
                            super.onTargetClick(view) // This call is optional

                            binding.buttonMissionOk.performClick()

                            mainActivity?.addTutorialStep()
                        }
                    })
            }
        }
    }
}