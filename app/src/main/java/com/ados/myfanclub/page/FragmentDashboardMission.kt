package com.ados.myfanclub.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentDashboardMissionBinding
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R.drawable.btn_round
import com.ados.myfanclub.SuccessCalendarWeek
import com.ados.myfanclub.ToggleAnimation
import com.ados.myfanclub.dialog.EditTextModifyDialog
import com.ados.myfanclub.dialog.MissionDialog
import com.ados.myfanclub.dialog.QuestionDialog
import com.ados.myfanclub.model.*
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import kotlinx.android.synthetic.main.edit_text_modify_dialog.*
import kotlinx.android.synthetic.main.mission_dialog.*
import kotlinx.android.synthetic.main.question_dialog.*
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
 * Use the [FragmentDashboardMission.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentDashboardMission : Fragment(), OnMissionItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentDashboardMissionBinding? = null
    private val binding get() = _binding!!

    private val firebaseViewModel : FirebaseViewModel by viewModels()

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null

    lateinit var recyclerViewFanClub : RecyclerView
    lateinit var recyclerViewFanClubAdapter : RecyclerViewAdapterMission
    lateinit var recyclerViewPersonal : RecyclerView
    lateinit var recyclerViewPersonalAdapter : RecyclerViewAdapterMission

    private var isExpandClub: Boolean = true
    private var isExpandPersonal: Boolean = true

    private var selectedCycle : ScheduleDTO.Cycle = ScheduleDTO.Cycle.DAY

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
        _binding = FragmentDashboardMissionBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        recyclerViewFanClub = rootView.findViewById(R.id.rv_mission_fan_club!!)as RecyclerView
        recyclerViewFanClub.layoutManager = LinearLayoutManager(requireContext())

        recyclerViewPersonal = rootView.findViewById(R.id.rv_mission_personal!!)as RecyclerView
        recyclerViewPersonal.layoutManager = LinearLayoutManager(requireContext())

        selectedCycle = when (param2) {
            "week" -> ScheduleDTO.Cycle.WEEK
            "month" -> ScheduleDTO.Cycle.MONTH
            else -> ScheduleDTO.Cycle.DAY
        }
        changeTab()
        getFanClubSchedule()
        getPersonalSchedule()
        observeFanClubSchedule()
        observePersonalSchedule()

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    //private fun toggleLayout(isExpanded: Boolean, view: View, fromRv: RecyclerView, toRv: RecyclerView): Boolean {
    private fun toggleLayout(isExpanded: Boolean, view: View, fromRv: RelativeLayout, toRv: RelativeLayout): Boolean {
        ToggleAnimation.toggleButton(view, isExpanded)
        if (isExpanded) {
            ToggleAnimation.expand(fromRv, toRv)
        } else {
            ToggleAnimation.close(fromRv, toRv)
        }
        return isExpanded
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = (activity as MainActivity?)?.getUser()
        if (!user?.mainTitle.isNullOrEmpty()) {
            binding.textTitle.text = user?.mainTitle
        }

        if (fanClubDTO == null) {
            binding.textEmptyFanClub.text = "아직 가입된 팬클럽이 없습니다."
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            fanClubDTO = (activity as MainActivity?)?.getFanClub()
            currentMember = (activity as MainActivity?)?.getMember()
            getFanClubSchedule()
            getPersonalSchedule()

            binding.swipeRefreshLayout.isRefreshing = false
            Toast.makeText(activity, "새로 고침", Toast.LENGTH_SHORT).show()
        }

        binding.buttonRefreshClub.setOnClickListener {
            binding.buttonRefreshClub.animate().apply{
                rotationBy(360f)
                duration = 1000L
                start()
            }
            getFanClubSchedule()
            Toast.makeText(activity, "새로 고침", Toast.LENGTH_SHORT).show()
        }

        binding.buttonRefreshPersonal.setOnClickListener {
            binding.buttonRefreshPersonal.animate().apply{
                rotationBy(360f)
                duration = 1000L
                start()
            }
            getPersonalSchedule()
            Toast.makeText(activity, "새로 고침", Toast.LENGTH_SHORT).show()
        }

        binding.textTitle.setOnClickListener {
            val user = (activity as MainActivity?)?.getUser()
            val item = EditTextDTO("제목 변경", user?.mainTitle, 30)
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
                    "제목 변경",
                    "제목을 변경 하시겠습니까?",
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

                    user?.mainTitle = dialog.edit_content.text.toString()
                    firebaseViewModel.updateUserMainTitle(user!!) {
                        Toast.makeText(activity, "제목 변경 완료!", Toast.LENGTH_SHORT).show()
                        binding.textTitle.text = dialog.edit_content.text
                    }
                }
            }
        }

        binding.buttonHideClub.setOnClickListener {
            if (isExpandClub) {
                isExpandClub = false
                binding.buttonHideClub.setImageResource(R.drawable.expend)
                //toggleLayout(isExpandClub, binding.buttonHideClub, binding.rvMissionFanClub, binding.rvMissionPersonal)
                toggleLayout(isExpandClub, binding.buttonHideClub, binding.layoutRvMissionFanClub, binding.layoutRvMissionPersonal)
            } else {
                isExpandClub = true
                binding.buttonHideClub.setImageResource(R.drawable.minimize)
                //toggleLayout(isExpandClub, binding.buttonHideClub, binding.rvMissionFanClub, binding.rvMissionPersonal)
                toggleLayout(isExpandClub, binding.buttonHideClub, binding.layoutRvMissionFanClub, binding.layoutRvMissionPersonal)
            }
        }

        binding.buttonHidePersonal.setOnClickListener {
            if (isExpandPersonal) {
                isExpandPersonal = false

                binding.buttonHidePersonal.setImageResource(R.drawable.expend)
                //binding.rvMissionPersonal.visibility = View.GONE
                //(binding.rvMissionFanClub.layoutParams as LinearLayout.LayoutParams).weight = 90F

                //toggleLayout(isExpandPersonal, binding.buttonHidePersonal, binding.rvMissionPersonal, binding.rvMissionFanClub)
                toggleLayout(isExpandPersonal, binding.buttonHidePersonal, binding.layoutRvMissionPersonal, binding.layoutRvMissionFanClub)
            } else {
                isExpandPersonal = true

                binding.buttonHidePersonal.setImageResource(R.drawable.minimize)
                //binding.rvMissionPersonal.visibility = View.VISIBLE
                //(binding.rvMissionFanClub.layoutParams as LinearLayout.LayoutParams).weight = 45F

                //toggleLayout(isExpandPersonal, binding.buttonHidePersonal, binding.rvMissionPersonal, binding.rvMissionFanClub)
                toggleLayout(isExpandPersonal, binding.buttonHidePersonal, binding.layoutRvMissionPersonal, binding.layoutRvMissionFanClub)
            }
        }

        binding.buttonSuccessCalendarClub.setOnClickListener {
            if (fanClubDTO == null) {
                Toast.makeText(activity, "가입된 팬클럽이 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                callSuccessCalendar("fanClub")
            }
        }

        binding.buttonSuccessCalendarPersonal.setOnClickListener {
            callSuccessCalendar("personal")
        }

        binding.textTabDay.setOnClickListener {
            if (binding.textTabDay.background == null) {
                selectedCycle = ScheduleDTO.Cycle.DAY
                changeTab()

                getFanClubSchedule()
                getPersonalSchedule()

                animLeft()
            }
        }
        binding.textTabWeek.setOnClickListener {
            if (binding.textTabWeek.background == null) {
                var oldSelectedCycle = selectedCycle

                selectedCycle = ScheduleDTO.Cycle.WEEK
                changeTab()

                getFanClubSchedule()
                getPersonalSchedule()

                if (oldSelectedCycle < ScheduleDTO.Cycle.WEEK) {
                    animRight()
                } else {
                    animLeft()
                }
            }
        }
        binding.textTabMonth.setOnClickListener {
            if (binding.textTabMonth.background == null) {
                var oldSelectedCycle = selectedCycle

                selectedCycle = ScheduleDTO.Cycle.MONTH
                changeTab()

                getFanClubSchedule()
                getPersonalSchedule()

                if (oldSelectedCycle < ScheduleDTO.Cycle.MONTH) {
                    animRight()
                } else {
                    animLeft()
                }
            }
        }
        binding.textTabPeriod.setOnClickListener {
            if (binding.textTabPeriod.background == null) {
                selectedCycle = ScheduleDTO.Cycle.PERIOD
                changeTab()

                getFanClubSchedule()
                getPersonalSchedule()

                animRight()
            }
        }
    }

    private fun callSuccessCalendar(type: String) {
        val cycle = when (selectedCycle) {
            ScheduleDTO.Cycle.DAY -> "day"
            ScheduleDTO.Cycle.WEEK -> "week"
            ScheduleDTO.Cycle.MONTH -> "month"
            else -> ""
        }
        val fragment = FragmentSuccessCalendarLayout.newInstance(type, cycle)
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            addToBackStack(null)
            commit()
        }
    }

    private fun observePersonalSchedule() {
        firebaseViewModel.personalDashboardMissionDTOs.observe(requireActivity()) {
            if (_binding != null) { // 뷰가 Destroy 되고 나서 뒤 늦게 들어오는 경우가 있기 때문에 예외 처리
                setAdapterPersonal()
            }
        }
    }

    private fun getPersonalSchedule() {
        recyclerViewPersonal.visibility = View.GONE

        val user = (activity as MainActivity?)?.getUser()
        firebaseViewModel.getPersonalDashboardMission(user?.uid.toString(), selectedCycle)
    }

    private fun observeFanClubSchedule() {
        firebaseViewModel.fanClubDashboardMissionDTOs.observe(requireActivity()) {
            if (_binding != null) { // 뷰가 Destroy 되고 나서 뒤 늦게 들어오는 경우가 있기 때문에 예외 처리
                setAdapterFanClub()
            }
        }
    }

    private fun getFanClubSchedule() {
        recyclerViewFanClub.visibility = View.GONE

        if (fanClubDTO == null) {
            setAdapterFanClub()
        } else {
            firebaseViewModel.getFanClubDashboardMission(fanClubDTO?.docName.toString(), currentMember?.userUid.toString(), selectedCycle)
        }
    }

    private fun setAdapterPersonal() {
        if (firebaseViewModel.personalDashboardMissionDTOs?.value!!.size > 0) {
            binding.textEmptyPersonal.visibility = View.GONE
        } else {
            binding.textEmptyPersonal.visibility = View.VISIBLE
        }
        recyclerViewPersonalAdapter = RecyclerViewAdapterMission(firebaseViewModel.personalDashboardMissionDTOs?.value!!, this)
        recyclerViewPersonal.adapter = recyclerViewPersonalAdapter
        recyclerViewPersonal.visibility = View.VISIBLE
    }

    private fun setAdapterFanClub() {
        recyclerViewFanClubAdapter = if (fanClubDTO == null) {
            val fanClubMissions: ArrayList<DashboardMissionDTO> = arrayListOf()
            RecyclerViewAdapterMission(fanClubMissions, this)
        } else {
            if (firebaseViewModel.fanClubDashboardMissionDTOs?.value!!.size > 0) {
                binding.textEmptyFanClub.visibility = View.GONE
            } else {
                binding.textEmptyFanClub.visibility = View.VISIBLE
            }
            RecyclerViewAdapterMission(firebaseViewModel.fanClubDashboardMissionDTOs?.value!!, this)
        }
        recyclerViewFanClub.adapter = recyclerViewFanClubAdapter
        recyclerViewFanClub.visibility = View.VISIBLE
    }

    private fun getStatisticsFieldValueName(schedule: ScheduleDTO) : Pair<String, String> {
        var fieldName = ""
        var valueName = ""
        val date = Date()
        when (schedule.cycle) {
            ScheduleDTO.Cycle.DAY -> {
                fieldName = SimpleDateFormat("yyyyMM").format(date)
                valueName = SimpleDateFormat("dd").format(date)
            }
            ScheduleDTO.Cycle.WEEK -> {
                var successCalendarWeek = SuccessCalendarWeek(date)
                successCalendarWeek.initBaseCalendar()
                var week = successCalendarWeek.getCurrentWeek()
                if (week != null) {
                    //fieldName = "${SimpleDateFormat("dd").format(week.startDate)}${SimpleDateFormat("dd").format(week.endDate)}"
                    fieldName = String.format("%04d%02d", week.year, week.month)
                    valueName = String.format("%02d", week.week)
                }
            }
            ScheduleDTO.Cycle.MONTH -> {
                fieldName = SimpleDateFormat("yyyy").format(Date())
                valueName = SimpleDateFormat("MM").format(date)
            }
        }
        return Pair(fieldName, valueName)
    }

    private fun changeTab() {
        releaseAllTabButton()
        binding.buttonSuccessCalendarClub.visibility = View.VISIBLE
        binding.buttonSuccessCalendarPersonal.visibility = View.VISIBLE
        when (selectedCycle) {
            ScheduleDTO.Cycle.DAY -> setTabButton(binding.textTabDay)
            ScheduleDTO.Cycle.WEEK -> setTabButton(binding.textTabWeek)
            ScheduleDTO.Cycle.MONTH -> setTabButton(binding.textTabMonth)
            ScheduleDTO.Cycle.PERIOD -> {
                setTabButton(binding.textTabPeriod)
                binding.buttonSuccessCalendarClub.visibility = View.GONE
                binding.buttonSuccessCalendarPersonal.visibility = View.GONE
            }
        }

    }

    private fun setTabButton(textView: TextView) {
        textView.background = getDrawable(requireContext(), btn_round)
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun releaseTabButton(textView: TextView) {
        textView.background = null
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }

    private fun releaseAllTabButton() {
        releaseTabButton(binding.textTabDay)
        releaseTabButton(binding.textTabWeek)
        releaseTabButton(binding.textTabMonth)
        releaseTabButton(binding.textTabPeriod)
    }

    private fun animLeft() {
        val translateLeft = AnimationUtils.loadAnimation(context, R.anim.translate_left)
        binding.layoutSchedule.startAnimation(translateLeft)
    }

    private fun animRight() {
        val translateRight = AnimationUtils.loadAnimation(context, R.anim.translate_right)
        binding.layoutSchedule.startAnimation(translateRight)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentDashboardMission.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentDashboardMission().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: DashboardMissionDTO, position: Int) {
        val dialog = MissionDialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.dashboardMissionDTO = item
        dialog.show()

        dialog.button_mission_cancel.setOnClickListener { // No
            dialog.dismiss()
        }

        dialog.button_mission_ok.setOnClickListener { // Ok
            val docName = when (item.scheduleDTO?.cycle) {
                ScheduleDTO.Cycle.DAY -> "day"
                ScheduleDTO.Cycle.WEEK -> "week"
                ScheduleDTO.Cycle.MONTH -> "month"
                else -> ""
            }
            val fieldValue = getStatisticsFieldValueName(item.scheduleDTO!!)

            item.scheduleProgressDTO?.count = dialog.missionCount
            item.scheduleProgressDTO?.countMax = item.scheduleDTO?.count
            val user = (activity as MainActivity?)?.getUser()
            when (item.type) {
                DashboardMissionDTO.Type.PERSONAL -> {
                    firebaseViewModel.updatePersonalMissionProgress(user?.uid.toString(), item) {
                        recyclerViewPersonalAdapter.notifyItemChanged(position)

                        // 통계 정보 기록
                        if (item.scheduleDTO?.cycle != ScheduleDTO.Cycle.PERIOD) { // 일일, 주간, 월간 통계만 냄
                            // 스케줄들의 모든 진행률을 통계로 계산
                            var totalPercent = 0
                            for (mission in firebaseViewModel.personalDashboardMissionDTOs.value!!) {
                                if (mission.scheduleDTO?.cycle == item.scheduleDTO?.cycle) {
                                    var percent = ((mission.scheduleProgressDTO?.count?.toDouble()!! / mission.scheduleProgressDTO?.countMax!!) * 100).toInt()
                                    totalPercent = totalPercent.plus(percent)
                                }
                            }

                            val averagePercent = totalPercent / firebaseViewModel.personalDashboardMissionDTOs.value!!.size
                            firebaseViewModel.updatePersonalScheduleStatistics(user?.uid.toString(), docName, fieldValue, averagePercent) {

                            }
                        }

                        // 일일 퀘스트 - 개인 일일 스케줄 완료 시 적용
                        if (!QuestDTO("개인 일일 스케줄", "개인 일일 스케줄을 1회 이상 완료 하세요.", 1, user?.questSuccessTimes?.get("1"), user?.questGemGetTimes?.get("1")).isQuestSuccess()) { // 퀘스트 완료 안했을 때 적용
                            if (item.scheduleDTO?.cycle == ScheduleDTO.Cycle.DAY && item.scheduleDTO?.count == item.scheduleProgressDTO?.count) { // 일일 미션이고 미션 완료
                                user?.questSuccessTimes?.set("1", Date())
                                firebaseViewModel.updateUserQuestSuccessTimes(user!!) {
                                    Toast.makeText(activity, "일일 과제 달성! 보상을 획득하세요!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(activity,"미션 적용 완료.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(activity,"미션 적용 완료.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                DashboardMissionDTO.Type.FAN_CLUB -> {
                    firebaseViewModel.updateFanClubMissionProgress(fanClubDTO?.docName.toString(), currentMember?.userUid.toString(), item) {
                            recyclerViewFanClubAdapter.notifyItemChanged(position)

                            // 통계 정보 기록
                            if (item.scheduleDTO?.cycle != ScheduleDTO.Cycle.PERIOD) { // 일일, 주간, 월간 통계만 냄
                                // 스케줄들의 모든 진행률을 통계로 계산
                                var totalPercent = 0
                                for (mission in firebaseViewModel.fanClubDashboardMissionDTOs?.value!!) {
                                    if (mission.scheduleDTO?.cycle == item.scheduleDTO?.cycle) {
                                        var percent = ((mission.scheduleProgressDTO?.count?.toDouble()!! / mission.scheduleProgressDTO?.countMax!!) * 100).toInt()
                                        totalPercent = totalPercent.plus(percent)
                                    }
                                }

                                val averagePercent = totalPercent / firebaseViewModel.fanClubDashboardMissionDTOs?.value!!.size
                                firebaseViewModel.updateFanClubScheduleStatistics(fanClubDTO?.docName.toString(), currentMember?.userUid.toString(), docName, fieldValue, averagePercent) {

                                }
                            }

                            // 일일 퀘스트 - 팬클럽 일일 스케줄 완료 시 적용
                            val user = (activity as MainActivity?)?.getUser()
                            if (!QuestDTO("팬클럽 일일 스케줄", "팬클럽 일일 스케줄을 1회 이상 완료 하세요.", 1, user?.questSuccessTimes?.get("2"), user?.questGemGetTimes?.get("2")).isQuestSuccess()) { // 퀘스트 완료 안했을 때 적용
                                if (item.scheduleDTO?.cycle == ScheduleDTO.Cycle.DAY && item.scheduleDTO?.count == item.scheduleProgressDTO?.count) { // 일일 미션이고 미션 완료
                                    user?.questSuccessTimes?.set("2", Date())
                                    firebaseViewModel.updateUserQuestSuccessTimes(user!!) {
                                        Toast.makeText(activity, "일일 과제 달성! 보상을 획득하세요!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(activity,"팬클럽 미션 적용 완료.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(activity,"팬클럽 미션 적용 완료.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            dialog.dismiss()
        }
    }
}