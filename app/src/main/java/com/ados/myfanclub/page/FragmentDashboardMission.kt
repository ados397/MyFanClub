package com.ados.myfanclub.page

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.net.Uri
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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R.drawable.btn_round
import com.ados.myfanclub.SuccessCalendarWeek
import com.ados.myfanclub.ToggleAnimation
import com.ados.myfanclub.database.DBHelperReport
import com.ados.myfanclub.dialog.EditTextModifyDialog
import com.ados.myfanclub.dialog.MissionDialog
import com.ados.myfanclub.dialog.QuestionDialog
import com.ados.myfanclub.dialog.ReportDialog
import com.ados.myfanclub.model.*
import com.ados.myfanclub.repository.FirebaseStorageRepository
import com.ados.myfanclub.viewmodel.FirebaseStorageViewModel
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


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

    private lateinit var callback: OnBackPressedCallback

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val firebaseStorageViewModel : FirebaseStorageViewModel by viewModels()

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null

    lateinit var recyclerViewFanClub : RecyclerView
    lateinit var recyclerViewFanClubAdapter : RecyclerViewAdapterMission
    lateinit var recyclerViewPersonal : RecyclerView
    lateinit var recyclerViewPersonalAdapter : RecyclerViewAdapterMission

    private var isExpandClub: Boolean = true
    private var isExpandPersonal: Boolean = true

    private var selectedCycle : ScheduleDTO.Cycle = ScheduleDTO.Cycle.DAY

    private var missionDialog : MissionDialog? = null
    private var editTextModifyDialog : EditTextModifyDialog? = null
    private var reportDialog : ReportDialog? = null
    lateinit var dbHandler : DBHelperReport

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

        recyclerViewFanClub = rootView.findViewById(R.id.rv_mission_fan_club)as RecyclerView
        recyclerViewFanClub.layoutManager = LinearLayoutManager(requireContext())

        recyclerViewPersonal = rootView.findViewById(R.id.rv_mission_personal)as RecyclerView
        recyclerViewPersonal.layoutManager = LinearLayoutManager(requireContext())

        dbHandler = DBHelperReport(requireContext())

        selectedCycle = when (param2) {
            "week" -> ScheduleDTO.Cycle.WEEK
            "month" -> ScheduleDTO.Cycle.MONTH
            else -> ScheduleDTO.Cycle.DAY
        }
        if (selectedCycle != ScheduleDTO.Cycle.DAY) {
            changeTab()
        }
        getFanClubSchedule()
        getPersonalSchedule()
        observeFanClubSchedule()
        observePersonalSchedule()

        binding.buttonRefreshClub.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        binding.imgSuccessCalendarClub.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        binding.buttonHideClub.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        binding.buttonRefreshPersonal.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        binding.imgSuccessCalendarPersonal.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        binding.buttonHidePersonal.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)

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

        println("???????????? step - ${(activity as MainActivity?)?.getTutorialStep()}")
        observeTutorial()

        val user = (activity as MainActivity?)?.getUser()
        if (!user?.mainTitle.isNullOrEmpty()) {
            binding.textTitle.text = user?.mainTitle
        }

        if (fanClubDTO == null) {
            binding.textEmptyFanClub.text = "?????? ????????? ???????????? ????????????."
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            fanClubDTO = (activity as MainActivity?)?.getFanClub()
            currentMember = (activity as MainActivity?)?.getMember()
            getFanClubSchedule()
            getPersonalSchedule()

            binding.swipeRefreshLayout.isRefreshing = false
            Toast.makeText(activity, "?????? ??????", Toast.LENGTH_SHORT).show()
        }

        binding.buttonRefreshClub.setOnClickListener {
            refreshClub()
        }

        binding.buttonRefreshPersonal.setOnClickListener {
            refreshPersonal()
        }

        binding.textTitle.setOnClickListener {
            val userDTO = (activity as MainActivity?)?.getUser()!!
            val item = EditTextDTO("?????? ??????", userDTO.mainTitle, 30)
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
            }
            editTextModifyDialog?.binding?.buttonModifyOk?.setOnClickListener {
                editTextModifyDialog?.dismiss()

                val question = QuestionDTO(
                    QuestionDTO.Stat.WARNING,
                    "?????? ??????",
                    "????????? ?????? ???????????????????",
                )
                val questionDialog = QuestionDialog(requireContext(), question)
                questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                questionDialog.setCanceledOnTouchOutside(false)
                questionDialog.show()
                questionDialog.binding.buttonQuestionCancel.setOnClickListener { // No
                    questionDialog.dismiss()
                }
                questionDialog.binding.buttonQuestionOk.setOnClickListener {
                    questionDialog.dismiss()

                    userDTO.mainTitle = editTextModifyDialog?.binding?.editContent?.text.toString()
                    firebaseViewModel.updateUserMainTitle(user!!) {
                        Toast.makeText(activity, "?????? ?????? ??????!", Toast.LENGTH_SHORT).show()
                        binding.textTitle.text = editTextModifyDialog?.binding?.editContent?.text
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
                Toast.makeText(activity, "????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show()
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

        binding.textEmptyPersonal.setOnClickListener {
            if (firebaseViewModel.personalDashboardMissionDTOs.value!!.size == 0) {
                (activity as MainActivity?)?.moveScheduleTab()
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

    private fun refreshClub() {
        binding.buttonRefreshClub.animate().apply{
            rotationBy(360f)
            duration = 1000L
            start()
        }
        getFanClubSchedule()
        Toast.makeText(activity, "?????? ??????", Toast.LENGTH_SHORT).show()
    }

    private fun refreshPersonal() {
        binding.buttonRefreshPersonal.animate().apply{
            rotationBy(360f)
            duration = 1000L
            start()
        }
        getPersonalSchedule()
        Toast.makeText(activity, "?????? ??????", Toast.LENGTH_SHORT).show()
    }

    private fun observePersonalSchedule() {
        firebaseViewModel.personalDashboardMissionDTOs.observe(viewLifecycleOwner) {
            if (_binding != null) { // ?????? Destroy ?????? ?????? ??? ?????? ???????????? ????????? ?????? ????????? ?????? ??????
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
        firebaseViewModel.fanClubDashboardMissionDTOs.observe(viewLifecycleOwner) {
            if (_binding != null) { // ?????? Destroy ?????? ?????? ??? ?????? ???????????? ????????? ?????? ????????? ?????? ??????
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
        if (firebaseViewModel.personalDashboardMissionDTOs.value!!.size > 0) {
            binding.textEmptyPersonal.visibility = View.GONE
            for (mission in firebaseViewModel.personalDashboardMissionDTOs.value!!) {
                mission.isBlocked = dbHandler.getBlock(mission.scheduleDTO?.docName.toString())
            }
        } else {
            binding.textEmptyPersonal.visibility = View.VISIBLE
        }
        recyclerViewPersonalAdapter = RecyclerViewAdapterMission(firebaseViewModel.personalDashboardMissionDTOs.value!!, this)
        recyclerViewPersonal.adapter = recyclerViewPersonalAdapter
        recyclerViewPersonal.visibility = View.VISIBLE
    }

    private fun setAdapterFanClub() {
        recyclerViewFanClubAdapter = if (fanClubDTO == null) {
            val fanClubMissions: ArrayList<DashboardMissionDTO> = arrayListOf()
            RecyclerViewAdapterMission(fanClubMissions, this)
        } else {
            if (firebaseViewModel.fanClubDashboardMissionDTOs.value!!.size > 0) {
                binding.textEmptyFanClub.visibility = View.GONE
                for (mission in firebaseViewModel.fanClubDashboardMissionDTOs.value!!) {
                    mission.isBlocked = dbHandler.getBlock(mission.scheduleDTO?.docName.toString())
                }
            } else {
                binding.textEmptyFanClub.visibility = View.VISIBLE
            }
            RecyclerViewAdapterMission(firebaseViewModel.fanClubDashboardMissionDTOs.value!!, this)
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
            else -> {
                fieldName = SimpleDateFormat("yyyyMM").format(date)
                valueName = SimpleDateFormat("dd").format(date)
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
        val translateLeft = AnimationUtils.loadAnimation(context, R.anim.translate_page_left)
        binding.layoutSchedule.startAnimation(translateLeft)
    }

    private fun animRight() {
        val translateRight = AnimationUtils.loadAnimation(context, R.anim.translate_page_right)
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
        if (item.isBlocked) {
            Toast.makeText(activity, "????????? ????????? ?????????.", Toast.LENGTH_SHORT).show()
        } else {
            onMission(item, position)
        }
    }

    private fun getPhotoUri(item: DashboardMissionDTO, myCallback: (Uri?) -> Unit) {
        if (item.scheduleDTO?.isPhoto!!) {
            val uidAndType = if (item.type == DashboardMissionDTO.Type.PERSONAL) {
                Pair((activity as MainActivity?)?.getUser()?.uid.toString(), FirebaseStorageRepository.ScheduleType.PERSONAL)
            } else {
                Pair(fanClubDTO?.docName.toString(), FirebaseStorageRepository.ScheduleType.FAN_CLUB)
            }

            firebaseStorageViewModel.getScheduleImage(uidAndType.first, item.scheduleDTO?.docName.toString(), uidAndType.second) { uri ->
                myCallback(uri)
            }
        } else {
            myCallback(null)
        }
    }

    private fun onMission(item: DashboardMissionDTO, position: Int) {
        getPhotoUri(item) { uri ->
            if (missionDialog == null) {
                missionDialog = MissionDialog(requireContext())
                missionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                missionDialog?.setCanceledOnTouchOutside(false)
            }
            missionDialog?.mainActivity = (activity as MainActivity?)
            missionDialog?.dashboardMissionDTO = item
            missionDialog?.photoUri = uri
            missionDialog?.show()
            missionDialog?.setInfo()

            missionDialog?.binding?.buttonMissionCancel?.setOnClickListener { // No
                println("???????????? ?????????????")
                missionDialog?.dismiss()
                missionDialog = null
            }

            missionDialog?.binding?.buttonMissionOk?.setOnClickListener { // Ok
                val docName = when (item.scheduleDTO?.cycle) {
                    ScheduleDTO.Cycle.DAY -> "day"
                    ScheduleDTO.Cycle.WEEK -> "week"
                    ScheduleDTO.Cycle.MONTH -> "month"
                    else -> ""
                }
                val fieldValue = getStatisticsFieldValueName(item.scheduleDTO!!)

                item.scheduleProgressDTO?.count = missionDialog?.missionCount
                item.scheduleProgressDTO?.countMax = item.scheduleDTO?.count
                val user = (activity as MainActivity?)?.getUser()
                when (item.type) {
                    DashboardMissionDTO.Type.PERSONAL -> {
                        firebaseViewModel.updatePersonalMissionProgress(user?.uid.toString(), item) {
                            recyclerViewPersonalAdapter.notifyItemChanged(position)

                            // ?????? ?????? ??????
                            if (item.scheduleDTO?.cycle != ScheduleDTO.Cycle.PERIOD) { // ??????, ??????, ?????? ????????? ???
                                // ??????????????? ?????? ???????????? ????????? ??????
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

                            // ?????? ????????? - ?????? ?????? ????????? ?????? ??? ??????
                            if (!QuestDTO("?????? ?????? ?????????", "?????? ?????? ???????????? 1??? ?????? ?????? ?????????.", 1, user?.questSuccessTimes?.get("1"), user?.questGemGetTimes?.get("1")).isQuestSuccess()) { // ????????? ?????? ????????? ??? ??????
                                if (item.scheduleDTO?.cycle == ScheduleDTO.Cycle.DAY && item.scheduleDTO?.count == item.scheduleProgressDTO?.count) { // ?????? ???????????? ?????? ??????
                                    user?.questSuccessTimes?.set("1", Date())
                                    firebaseViewModel.updateUserQuestSuccessTimes(user!!) {
                                        Toast.makeText(activity, "?????? ?????? ??????! ????????? ???????????????!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(activity,"?????? ?????? ??????.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(activity,"?????? ?????? ??????.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    DashboardMissionDTO.Type.FAN_CLUB -> {
                        firebaseViewModel.updateFanClubMissionProgress(fanClubDTO?.docName.toString(), currentMember?.userUid.toString(), item) {
                            recyclerViewFanClubAdapter.notifyItemChanged(position)

                            // ?????? ?????? ??????
                            if (item.scheduleDTO?.cycle != ScheduleDTO.Cycle.PERIOD) { // ??????, ??????, ?????? ????????? ???
                                // ??????????????? ?????? ???????????? ????????? ??????
                                var totalPercent = 0
                                for (mission in firebaseViewModel.fanClubDashboardMissionDTOs.value!!) {
                                    if (mission.scheduleDTO?.cycle == item.scheduleDTO?.cycle) {
                                        var percent = ((mission.scheduleProgressDTO?.count?.toDouble()!! / mission.scheduleProgressDTO?.countMax!!) * 100).toInt()
                                        totalPercent = totalPercent.plus(percent)
                                    }
                                }

                                val averagePercent = totalPercent / firebaseViewModel.fanClubDashboardMissionDTOs.value!!.size
                                firebaseViewModel.updateFanClubScheduleStatistics(fanClubDTO?.docName.toString(), currentMember?.userUid.toString(), docName, fieldValue, averagePercent) {

                                }
                            }

                            // ?????? ????????? - ????????? ?????? ????????? ?????? ??? ??????
                            val userDTO = (activity as MainActivity?)?.getUser()!!
                            if (!QuestDTO("????????? ?????? ?????????", "????????? ?????? ???????????? 1??? ?????? ?????? ?????????.", 1, userDTO.questSuccessTimes.get("2"), userDTO.questGemGetTimes.get("2")).isQuestSuccess()) { // ????????? ?????? ????????? ??? ??????
                                if (item.scheduleDTO?.cycle == ScheduleDTO.Cycle.DAY && item.scheduleDTO?.count == item.scheduleProgressDTO?.count) { // ?????? ???????????? ?????? ??????
                                    userDTO.questSuccessTimes.set("2", Date())
                                    firebaseViewModel.updateUserQuestSuccessTimes(userDTO) {
                                        Toast.makeText(activity, "?????? ?????? ??????! ????????? ???????????????!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(activity,"????????? ?????? ?????? ??????.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(activity,"????????? ?????? ?????? ??????.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                missionDialog?.dismiss()
                missionDialog = null
            }

            missionDialog?.binding?.buttonReport?.setOnClickListener {
                val user = (activity as MainActivity?)?.getUser()!!

                if (reportDialog == null) {
                    reportDialog = ReportDialog(requireContext())
                    reportDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    reportDialog?.setCanceledOnTouchOutside(false)
                }
                reportDialog?.reportDTO = ReportDTO(user.uid, user.nickname, fanClubDTO?.docName, item.scheduleDTO?.title, item.scheduleDTO?.purpose, item.scheduleDTO?.docName, ReportDTO.Type.Schedule)
                reportDialog?.show()
                reportDialog?.setInfo()

                reportDialog?.setOnDismissListener {
                    if (!reportDialog?.reportDTO?.reason.isNullOrEmpty()) {
                        firebaseViewModel.sendReport(reportDialog?.reportDTO!!) {
                            if (!dbHandler.getBlock(reportDialog?.reportDTO?.contentDocName.toString())) {
                                dbHandler.updateBlock(reportDialog?.reportDTO?.contentDocName.toString(), 1)
                            } else {
                                dbHandler.updateBlock(reportDialog?.reportDTO?.contentDocName.toString(), 0)
                            }

                            item?.isBlocked = true
                            if (item.type == DashboardMissionDTO.Type.PERSONAL) {
                                recyclerViewPersonalAdapter.notifyItemChanged(position)
                            } else {
                                recyclerViewFanClubAdapter.notifyItemChanged(position)
                            }
                            missionDialog?.binding?.buttonMissionCancel?.performClick()
                            Toast.makeText(activity, "?????? ?????? ?????????????????????.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
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
            7 -> {
                println("???????????? Step - $step")
                TapTargetSequence(requireActivity())
                    .targets(
                        TapTarget.forBounds((activity as MainActivity?)?.getMainLayoutRect(),
                            "?????? ???????????? ????????? ???????????? ????????? ??? ????????????.",
                            "- OK ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(requireContext(), R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.layoutRefreshPersonal,
                            "????????? ???????????? ????????? ????????? ??????????????? ???????????????.(?????? ?????? ????????? ?????? ???????????? ???????????? ????????????)",
                            "- ???????????? ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .tintTarget(true)).listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            refreshPersonal()
                            (activity as MainActivity?)?.addTutorialStep()
                        }
                        override fun onSequenceStep(tutorialStep: TapTarget, targetClicked: Boolean) {

                        }
                        override fun onSequenceCanceled(lastTarget: TapTarget) {
                        }
                    }).start()

                /*TapTargetView.showFor(requireActivity(),
                    TapTarget.forView(binding.buttonRefreshClub,
                    //TapTarget.forBounds(Rect(70, rect.bottom.times(2), 100, 100),
                        "???????????? ??????????????? ????????? ????????? ??????????????? ???????????????. (???????????? ????????? ?????? ???????????? ???????????? ????????????.)",
                        "- ???????????? ????????? ???????????????.")
                        .cancelable(false)
                        .dimColor(R.color.black)
                        .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                        .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                        .titleTextSize(18) // Specify the size (in sp) of the title text
                        .transparentTarget(true)
                        .tintTarget(true),object : TapTargetView.Listener() {
                        // The listener can listen for regular clicks, long clicks or cancels
                        override fun onTargetClick(view: TapTargetView) {
                            super.onTargetClick(view) // This call is optional

                            refreshPersonal()
                            (activity as MainActivity?)?.addTutorialStep()
                        }
                    })*/
            }
            8 -> {
                println("???????????? Step - $step")
                val location = IntArray(2)
                val width = binding.layoutTitlePersonal.width.div(4)
                binding.layoutTitlePersonal.getLocationOnScreen(location)
                val rect = Rect(location[0], location[1], location[0] + width, location[1] + binding.layoutTitlePersonal.height.times(4))
                TapTargetView.showFor(requireActivity(),
                    TapTarget.forBounds(rect,
                        "?????? ????????? ???????????? ????????? ??????????????? ?????? ?????? ?????? ??? ???????????? ???????????? ??????????????????.",
                        "- ????????? ???????????? ????????? ?????????.")
                        .cancelable(false)
                        .dimColor(R.color.black)
                        .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                        .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                        .titleTextSize(18) // Specify the size (in sp) of the title text
                        .transparentTarget(true)
                        .tintTarget(true),object : TapTargetView.Listener() {
                        // The listener can listen for regular clicks, long clicks or cancels
                        override fun onTargetClick(view: TapTargetView) {
                            super.onTargetClick(view) // This call is optional

                            for (i in 0 until firebaseViewModel.personalDashboardMissionDTOs.value!!.size) {
                                if (isSampleData(firebaseViewModel.personalDashboardMissionDTOs.value!![i].scheduleDTO!!)) {
                                    onMission(firebaseViewModel.personalDashboardMissionDTOs.value!![i], i)
                                    break
                                }
                            }

                            (activity as MainActivity?)?.addTutorialStep()
                        }
                    })
            }
            11 -> {
                println("???????????? Step - $step")
                TapTargetSequence(requireActivity())
                    .targets(
                        TapTarget.forView(binding.layoutMain,
                            "???????????????! ????????? ???????????? ????????? ?????????????????????!",
                            "- OK ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(requireContext(), R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.layoutMain,
                            "????????? ????????? ????????????, ???????????? ???????????? ?????? ???????????? ???????????????!",
                            "- OK ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(requireContext(), R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.textTabDay,
                            "????????? ???????????? ????????? ????????? ?????? ???????????? ????????? ???????????????.",
                            "- ?????? ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .tintTarget(true),
                        TapTarget.forView(binding.textTabWeek,
                            "????????? ???????????? ????????? ????????? ?????? ???????????? ????????? ???????????????.",
                            "- ?????? ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .tintTarget(true),
                        TapTarget.forView(binding.textTabMonth,
                            "????????? ???????????? ????????? ????????? ?????? ???????????? ????????? ???????????????.",
                            "- ?????? ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .tintTarget(true),
                        TapTarget.forView(binding.textTabPeriod,
                            "????????? ???????????? ????????? ????????? ?????? ???????????? ????????? ???????????????.",
                            "- ????????? ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .tintTarget(true),
                        TapTarget.forView(binding.buttonSuccessCalendarPersonal,
                            "???????????? ??? ???????????? ????????? ???????????? ?????? ??? ??? ????????????.",
                            "- ?????? ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .tintTarget(true)).listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            (activity as MainActivity?)?.addTutorialStep()
                        }
                        override fun onSequenceStep(tutorialStep: TapTarget, targetClicked: Boolean) {

                        }
                        override fun onSequenceCanceled(lastTarget: TapTarget) {
                        }
                    }).start()
            }
        }
    }

    private fun isSampleData(item: ScheduleDTO) : Boolean {
        return item.title.equals("?????? ?????? ???????????? ??????!") &&
                item.purpose.equals("????????? 5??? ??? ?????? ???????????? ??????!\n\n??? ????????? ?????? ?????? ?????????!") &&
                item.count == 5L &&
                item.action == ScheduleDTO.Action.APP &&
                item.appDTO?.appName.equals("??????")
    }
}