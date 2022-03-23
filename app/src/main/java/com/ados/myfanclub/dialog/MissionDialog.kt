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
            when (dashboardMissionDTO?.scheduleDTO?.action) { // 앱 실행
                ScheduleDTO.Action.APP -> {
                    val linePackage = dashboardMissionDTO?.scheduleDTO?.appDTO?.packageName.toString()
                    val intentLine = context.packageManager.getLaunchIntentForPackage(linePackage) // 인텐트에 패키지 주소 저장

                    try {
                        context.startActivity(intentLine) // 라인 앱을 실행해본다.
                    } catch (e: Exception) {  // 만약 실행이 안된다면 (앱이 없다면)
                        val intentPlayStore = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$linePackage")) // 설치 링크를 인텐트에 담아
                        context.startActivity(intentPlayStore) // 플레이스토어로 이동
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

        when (dashboardMissionDTO?.scheduleDTO?.action) { // 앱 실행
            ScheduleDTO.Action.APP -> {
                binding.layoutQuickMenu.visibility = View.VISIBLE
                binding.textExecute.text = "앱 실행"
                binding.imgIcon.setImageResource(R.drawable.app)
                binding.textAppName.visibility = View.VISIBLE
                binding.textAppName.text = "[${dashboardMissionDTO?.scheduleDTO?.appDTO?.appName}]"
            }
            ScheduleDTO.Action.URL -> {
                binding.layoutQuickMenu.visibility = View.VISIBLE
                binding.textExecute.text = "링크 실행"
                binding.imgIcon.setImageResource(R.drawable.link)
                binding.textAppName.visibility = View.GONE
            }
            ScheduleDTO.Action.ETC -> {
                binding.layoutQuickMenu.visibility = View.GONE
            }
            else-> binding.layoutQuickMenu.visibility = View.GONE
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
                println("튜토리얼 Step - $step")
                TapTargetSequence(this)
                    .targets(
                        TapTarget.forView(binding.layoutMain,
                            "여기에서 '멜론' 실행 및 진행도를 업데이트 할 수 있습니다.",
                            "- OK 버튼을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(context, R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.buttonExecute,
                            "이 버튼을 누르면 '멜론'이 바로 실행되며 간편하게 이용 가능 합니다!",
                            "- 그 외에도 수 많은 앱들과 링크를 등록하여 한 번의 클릭만으로 간편하게 실행 가능합니다.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .targetRadius(80)
                            .tintTarget(true),
                        TapTarget.forView(binding.buttonMax,
                            "스트리밍이 끝났다면 스스로 진행도를 업데이트 하여 내가 수행한 일정을 기록합니다.",
                            "- MAX 버튼을 눌러 진행도를 100% 상태로 만들어 볼게요.") // All options below are optional
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
                println("튜토리얼 Step - $step")
                TapTargetView.showFor(this,
                    TapTarget.forView(binding.buttonMissionOk,
                        "진행도를 업데이트 하였으면 확인 버튼을 눌러 적용 합니다.",
                        "- 확인 버튼을 눌러주세요.")
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