package com.ados.myfanclub.page

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentScheduleAddBinding
import com.ados.myfanclub.dialog.SelectAppDialog
import com.ados.myfanclub.model.*
import com.ados.myfanclub.repository.FirebaseStorageRepository
import com.ados.myfanclub.util.Utility
import com.ados.myfanclub.util.Utility.Companion.randomDocumentName
import com.ados.myfanclub.viewmodel.FirebaseStorageViewModel
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.applikeysolutions.cosmocalendar.selection.OnDaySelectedListener
import com.applikeysolutions.cosmocalendar.selection.RangeSelectionManager
import com.applikeysolutions.cosmocalendar.utils.SelectionType
import com.bumptech.glide.Glide
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentScheduleAdd.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentScheduleAdd : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentScheduleAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val firebaseStorageViewModel : FirebaseStorageViewModel by viewModels()

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null

    private var selectAppDialog: SelectAppDialog? = null

    var scheduleDTO = ScheduleDTO()
    private var titleOK: Boolean = false
    private var rangeOK: Boolean = false
    private var purposeOK: Boolean = false
    private var actionOK: Boolean = false
    private var cycleOK: Boolean = false
    private var countOK: Boolean = false
    private var photoBitmap: Bitmap? = null
    var isAddedTutorialSampleData = false // ???????????? ?????? ???????????? ?????? ???????????? ????????? ??????

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            photoBitmap = (activity as MainActivity?)?.getBitmap(uri)
            if (photoBitmap == null) {
                Toast.makeText(activity, "?????? ???????????? ??????. ?????? ??? ?????? ????????? ?????????.", Toast.LENGTH_SHORT).show()
            } else {
                scheduleDTO.isPhoto = true
                binding.imgPhoto.setImageBitmap(photoBitmap)
                binding.layoutPhoto.visibility = View.VISIBLE
                binding.layoutLoadPhoto.visibility = View.GONE
            }
        } else {
            Toast.makeText(context, "?????? ???????????? ??????. ?????? ??? ?????? ????????? ?????????.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)

            if (param1.equals("fanClub")) {
                fanClubDTO = (activity as MainActivity?)?.getFanClub()
                currentMember = (activity as MainActivity?)?.getMember()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentScheduleAddBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        binding.layoutPhoto.visibility = View.GONE
        binding.layoutSelectApp.visibility = View.GONE
        binding.editUrl.visibility = View.GONE
        binding.layoutAlarm.visibility = View.GONE

        val c = Calendar.getInstance()

        val disabledDaysSet: MutableSet<Long> = HashSet()
        for (day in c.get(Calendar.DAY_OF_MONTH) downTo 1){
            c.add(Calendar.DATE, -1)
            disabledDaysSet.add(c.timeInMillis)
        }
        binding.calendarView.disabledDays = disabledDaysSet

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeTutorial()

        if (!scheduleDTO.docName.isNullOrEmpty()) {
            titleOK = true
            rangeOK = true
            purposeOK = true
            actionOK = true
            cycleOK = true
            countOK = true

            if (scheduleDTO.isPhoto) {
                val uidAndType = if (fanClubDTO == null) {
                    Pair((activity as MainActivity?)?.getUser()?.uid.toString(), FirebaseStorageRepository.ScheduleType.PERSONAL)
                } else {
                    Pair(fanClubDTO?.docName.toString(), FirebaseStorageRepository.ScheduleType.FAN_CLUB)
                }

                firebaseStorageViewModel.getScheduleImage(uidAndType.first, scheduleDTO.docName.toString(), uidAndType.second) { uri ->
                    if (uri != null) {
                        Glide.with(requireContext()).load(uri).fitCenter().into(binding.imgPhoto)
                        binding.layoutPhoto.visibility = View.VISIBLE
                        binding.layoutLoadPhoto.visibility = View.GONE
                    } else {
                        Toast.makeText(activity, "????????? ?????? ???????????? ??????. ????????? ?????? ????????? ?????????.", Toast.LENGTH_SHORT).show()
                        scheduleDTO.isPhoto = false
                        binding.layoutPhoto.visibility = View.GONE
                        binding.layoutLoadPhoto.visibility = View.VISIBLE
                    }
                }
            } else {
                binding.layoutPhoto.visibility = View.GONE
                binding.layoutLoadPhoto.visibility = View.VISIBLE
            }

            binding.textPageTitle.text = "????????? ??????"
            binding.buttonOk.text = "????????? ??????"
            binding.editTitle.setText(scheduleDTO.title)
            binding.textTitleLen.text = "${binding.editTitle.text.length}/30"
            setStartEndDate()
            binding.editPurpose.setText(scheduleDTO.purpose)
            binding.textPurposeLen.text = "${binding.editPurpose.text.length}/300"
            when (scheduleDTO.action) {
                ScheduleDTO.Action.APP -> {
                    binding.textSelectedApp.text = scheduleDTO.appDTO?.appName
                    binding.radioApp.isChecked = true
                    selectActionApp()
                }
                ScheduleDTO.Action.URL -> {
                    binding.editUrl.setText(scheduleDTO.url)
                    binding.radioUrl.isChecked = true
                    selectActionUrl()
                }
                ScheduleDTO.Action.ETC -> {
                    binding.radioEtc.isChecked = true
                    selectActionNone()
                }
                else -> {
                    binding.textSelectedApp.text = scheduleDTO.appDTO?.appName
                    binding.radioApp.isChecked = true
                    selectActionApp()
                }
            }
            when (scheduleDTO.cycle) {
                ScheduleDTO.Cycle.DAY -> binding.radioDay.isChecked = true
                ScheduleDTO.Cycle.WEEK -> binding.radioWeek.isChecked = true
                ScheduleDTO.Cycle.MONTH -> binding.radioMonth.isChecked = true
                ScheduleDTO.Cycle.PERIOD -> binding.radioPeriod.isChecked = true
                else -> binding.radioDay.isChecked = true
            }
            binding.editCount.setText(scheduleDTO.count.toString())

            if (scheduleDTO.isAlarm == true) {
                binding.layoutAlarm.visibility = View.VISIBLE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    binding.timepickerAlarm.hour = scheduleDTO.alarmDTO.alarmHour!!
                    binding.timepickerAlarm.minute = scheduleDTO.alarmDTO.alarmMinute!!
                } else {
                    binding.timepickerAlarm.currentHour = scheduleDTO.alarmDTO.alarmHour!!
                    binding.timepickerAlarm.currentMinute = scheduleDTO.alarmDTO.alarmMinute!!
                }

                if (scheduleDTO.alarmDTO.alarmDate != null) {
                    setCalendarSingleSubExecute()
                } else {
                    binding.daySun.isChecked = scheduleDTO.alarmDTO.dayOfWeek["1"] == true
                    binding.dayMon.isChecked = scheduleDTO.alarmDTO.dayOfWeek["2"] == true
                    binding.dayTues.isChecked = scheduleDTO.alarmDTO.dayOfWeek["3"] == true
                    binding.dayWed.isChecked = scheduleDTO.alarmDTO.dayOfWeek["4"] == true
                    binding.dayThurs.isChecked = scheduleDTO.alarmDTO.dayOfWeek["5"] == true
                    binding.dayFri.isChecked = scheduleDTO.alarmDTO.dayOfWeek["6"] == true
                    binding.daySat.isChecked = scheduleDTO.alarmDTO.dayOfWeek["7"] == true
                    setWeekString()
                }
                binding.switchAlarm.isChecked = true
            }

            visibleOkButton()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getAlarmDateText(binding.timepickerAlarm.hour, binding.timepickerAlarm.minute)
            } else {
                getAlarmDateText(binding.timepickerAlarm.currentHour, binding.timepickerAlarm.currentMinute)
            }

        }

        binding.editPurpose.setOnTouchListener { _, _ ->
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
            false
        }

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.layoutLoadPhoto.setOnClickListener {
            resultLauncher.launch("image/*")
        }

        binding.imgDeletePhoto.setOnClickListener {
            binding.layoutPhoto.visibility = View.GONE
            binding.layoutLoadPhoto.visibility = View.VISIBLE
            scheduleDTO.isPhoto = false
            photoBitmap = null
        }

        binding.buttonSelectApp.setOnClickListener {
            if (selectAppDialog == null) {
                selectAppDialog = SelectAppDialog(requireContext())
                selectAppDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                selectAppDialog?.setCanceledOnTouchOutside(false)
            }
            selectAppDialog?.show()
            selectAppDialog?.setInfo()

            selectAppDialog?.binding?.buttonAppCancel?.setOnClickListener { // No
                selectAppDialog?.dismiss()
            }

            selectAppDialog?.binding?.buttonAppOk?.setOnClickListener { // Ok
                if (selectAppDialog?.selectedApp == null) {
                    Toast.makeText(activity,"????????? ?????? ????????????.", Toast.LENGTH_SHORT).show()
                } else {
                    scheduleDTO.appDTO = selectAppDialog?.selectedApp
                    binding.textSelectedApp.text = scheduleDTO.appDTO?.appName
                    selectAppDialog?.dismiss()
                    actionOK = true
                    visibleOkButton()
                }
            }
        }

        binding.editStartDate.setOnClickListener {
            setCalendarRange()
        }
        binding.editEndDate.setOnClickListener {
            setCalendarRange()
        }

        binding.radioGroupAction.setOnCheckedChangeListener { _, i ->
            when(i) {
                R.id.radio_app -> {
                    selectActionApp()
                }
                R.id.radio_url -> {
                    selectActionUrl()
                }
                R.id.radio_etc -> {
                    selectActionNone()
                }
            }
        }

        binding.switchAlarm.setOnCheckedChangeListener { _, b ->
            if (b) {
                binding.layoutAlarm.visibility = View.VISIBLE
                scheduleDTO.isAlarm = true
            } else {
                binding.layoutAlarm.visibility = View.GONE
                scheduleDTO.isAlarm = false
            }
        }

        binding.buttonAlarmCalendar.setOnClickListener {
            setCalendarSingle()
        }

        binding.timepickerAlarm.setOnTimeChangedListener { _, i, i2 ->
            if (binding.weekGroup.checkedIds.size == 0) {
                getAlarmDateText(i, i2)
            }
        }

        binding.weekGroup.setOnCheckedChangeListener { _, _, _ ->
            setWeekString()
        }

        binding.editTitle.doAfterTextChanged {
            if (binding.editTitle.text.toString().isNullOrEmpty()) {
                binding.textTitleError.text = "????????? ????????? ?????????."
                binding.editTitle.setBackgroundResource(R.drawable.edit_rectangle_red)
                titleOK = false
            } else {
                binding.textTitleError.text = ""
                binding.editTitle.setBackgroundResource(R.drawable.edit_rectangle)
                titleOK = true
            }

            binding.textTitleLen.text = "${binding.editTitle.text.length}/30"

            visibleOkButton()
        }

        binding.editPurpose.doAfterTextChanged {
            if (binding.editPurpose.text.toString().isNullOrEmpty()) {
                binding.textPurposeError.text = "????????? ????????? ?????????."
                binding.editPurpose.setBackgroundResource(R.drawable.edit_rectangle_red)
                purposeOK = false
            } else {
                binding.textPurposeError.text = ""
                binding.editPurpose.setBackgroundResource(R.drawable.edit_rectangle)
                purposeOK = true
            }

            binding.textPurposeLen.text = "${binding.editPurpose.text.length}/300"

            visibleOkButton()
        }

        binding.editUrl.doAfterTextChanged {
            isValidUrlEdit()
        }

        binding.editCount.doAfterTextChanged {
            var countStr = binding.editCount.text.toString()
            var count = 0
            if (!countStr.isNullOrEmpty()) {
                count = countStr.toInt()
            }

            if (countStr.isNullOrEmpty() || count <= 0) {
                binding.textCountError.text = "?????? ????????? 1 ?????? ????????? ?????????."
                binding.editCount.setBackgroundResource(R.drawable.edit_rectangle_red)
                countOK = false
            } else {
                binding.textCountError.text = ""
                binding.editCount.setBackgroundResource(R.drawable.edit_rectangle)
                countOK = true
            }
            visibleOkButton()
        }

        binding.radioGroupCycle.setOnCheckedChangeListener { _, i ->
            when(i) {
                R.id.radio_day -> {
                    scheduleDTO.cycle = ScheduleDTO.Cycle.DAY
                    cycleOK = true
                }
                R.id.radio_week -> {
                    scheduleDTO.cycle = ScheduleDTO.Cycle.WEEK
                    cycleOK = true
                }
                R.id.radio_month -> {
                    scheduleDTO.cycle = ScheduleDTO.Cycle.MONTH
                    cycleOK = true
                }
                R.id.radio_period -> {
                    scheduleDTO.cycle = ScheduleDTO.Cycle.PERIOD
                    cycleOK = true
                }
            }
            visibleOkButton()
        }

        binding.buttonOk.setOnClickListener {
            applySchedule()
        }


        /*binding.buttonExecuteApp.setOnClickListener {
            val linePackage = "com.iloen.melon"

            val intentLine = requireActivity().packageManager.getLaunchIntentForPackage(linePackage) // ???????????? ????????? ?????? ??????

            try {
                startActivity(intentLine) // ?????? ?????? ???????????????.
            } catch (e: Exception) {  // ?????? ????????? ???????????? (?????? ?????????)
                val intentPlayStore = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + linePackage)) // ?????? ????????? ???????????? ??????
                startActivity(intentPlayStore) // ????????????????????? ??????
            }

        }*/
    }

    private fun setPersonalSchedule() {
        val user = (activity as MainActivity?)?.getUser()!!
        firebaseViewModel.updatePersonalSchedule(user.uid.toString(), scheduleDTO) {
            if (scheduleDTO.isPhoto) {
                if (photoBitmap != null) { // isPhoto ??? true ?????? photoBitmap ??? null ????????? ????????? ???????????? ???????????? ???????????? ????????? ?????????. ????????? ???????????? ????????? ????????? ??????????????? ????????? ?????? ??????.
                    firebaseStorageViewModel.setScheduleImage(user.uid.toString(), scheduleDTO.docName.toString(), FirebaseStorageRepository.ScheduleType.PERSONAL, photoBitmap!!) {
                        Toast.makeText(activity,"????????? ?????? ??????", Toast.LENGTH_SHORT).show()
                        (activity as MainActivity?)?.loadingEnd()
                        finishFragment()
                    }
                } else {
                    Toast.makeText(activity,"????????? ?????? ??????", Toast.LENGTH_SHORT).show()
                    (activity as MainActivity?)?.loadingEnd()
                    finishFragment()
                }
            } else {
                Toast.makeText(activity,"????????? ?????? ??????", Toast.LENGTH_SHORT).show()
                (activity as MainActivity?)?.loadingEnd()
                finishFragment()
            }
        }
    }

    private fun setFanClubSchedule() {
        firebaseViewModel.updateFanClubSchedule(fanClubDTO?.docName.toString(), scheduleDTO) {
            if (scheduleDTO.isPhoto) {
                if (photoBitmap != null) { // isPhoto ??? true ?????? photoBitmap ??? null ????????? ????????? ???????????? ???????????? ???????????? ????????? ?????????. ????????? ???????????? ????????? ????????? ??????????????? ????????? ?????? ??????.
                    firebaseStorageViewModel.setScheduleImage(fanClubDTO?.docName.toString(), scheduleDTO.docName.toString(), FirebaseStorageRepository.ScheduleType.FAN_CLUB, photoBitmap!!) {
                        Toast.makeText(activity,"????????? ????????? ?????? ??????", Toast.LENGTH_SHORT).show()
                        (activity as MainActivity?)?.loadingEnd()
                        finishFragment()
                    }
                } else {
                    Toast.makeText(activity,"????????? ????????? ?????? ??????", Toast.LENGTH_SHORT).show()
                    (activity as MainActivity?)?.loadingEnd()
                    finishFragment()
                }
            } else {
                Toast.makeText(activity,"????????? ????????? ?????? ??????", Toast.LENGTH_SHORT).show()
                (activity as MainActivity?)?.loadingEnd()
                finishFragment()
            }
        }
    }

    private fun selectActionApp() {
        binding.layoutSelectApp.visibility = View.VISIBLE
        binding.editUrl.visibility = View.GONE
        binding.textUrlError.visibility = View.GONE

        scheduleDTO.action = ScheduleDTO.Action.APP
        actionOK = scheduleDTO.appDTO != null
        visibleOkButton()
    }

    private fun selectActionUrl() {
        binding.layoutSelectApp.visibility = View.GONE
        binding.editUrl.visibility = View.VISIBLE
        binding.textUrlError.visibility = View.VISIBLE

        scheduleDTO.action = ScheduleDTO.Action.URL
        isValidUrlEdit()
    }

    private fun selectActionNone() {
        binding.layoutSelectApp.visibility = View.GONE
        binding.editUrl.visibility = View.GONE
        binding.textUrlError.visibility = View.GONE

        scheduleDTO.action = ScheduleDTO.Action.ETC
        actionOK = true
        visibleOkButton()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                callBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    private fun callBackPressed() {
        finishFragment()
    }

    private fun finishFragment() {
        val fragment = FragmentScheduleList.newInstance(param1!!, param2!!)
        /*parentFragmentManager.popBackStackImmediate("scheduleList", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack("scheduleList")
            commit()
        }*/
        parentFragmentManager.beginTransaction().replace(R.id.layout_fragment, fragment).commit()
    }

    private fun getAlarmDateText(hour: Int, min: Int) {
        val alarmTime = "${hour}${min}".toInt()
        val nowTime = SimpleDateFormat("HHmm").format(Date()).toInt()
        if (alarmTime > nowTime) {
            // ?????? ???????????? ?????? ??????
            var today = SimpleDateFormat("MM??? dd??? (E)", Locale("ko", "KR")).format(Date())
            binding.textAlarmDate.text = "??????-$today"
        } else {
            // ?????? ???????????? ????????? ????????? ??????
            val cal = Calendar.getInstance()
            cal.time = Date()
            cal.add(Calendar.DATE, 1)
            var tomorrow = SimpleDateFormat("MM??? dd??? (E)", Locale("ko", "KR")).format(cal.time)

            // ????????? ????????? ????????? ?????? ??????
            val alarmYear = SimpleDateFormat("yyyy").format(cal.time).toInt()
            val todayYear = SimpleDateFormat("yyyy").format(Date()).toInt()
            if (alarmYear > todayYear) { // ????????? ????????? ?????? ??????
                tomorrow = "${alarmYear}??? $tomorrow"
            }

            binding.textAlarmDate.text = "??????-$tomorrow"
        }
    }

    private fun setWeekString() {
        scheduleDTO.alarmDTO.alarmDate = null
        scheduleDTO.alarmDTO.clearDayOfWeek()
        when {
            binding.weekGroup.checkedIds.size == 0 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getAlarmDateText(binding.timepickerAlarm.hour, binding.timepickerAlarm.minute)
                } else {
                    getAlarmDateText(binding.timepickerAlarm.currentHour, binding.timepickerAlarm.currentMinute)
                }
            }
            binding.weekGroup.checkedIds.size >= binding.weekGroup.size -> {
                binding.textAlarmDate.text = "??????"
                scheduleDTO.alarmDTO.everyDayOfWeek()
            }
            else -> {
                var weekSize = 0
                var weekString = ""

                if (binding.daySun.isChecked) {
                    weekString += "???"
                    weekSize++

                    scheduleDTO.alarmDTO.dayOfWeek["1"] = true
                }
                if (binding.dayMon.isChecked) {
                    if (weekSize > 0)
                        weekString += ", "
                    weekString += "???"
                    weekSize++

                    scheduleDTO.alarmDTO.dayOfWeek["2"] = true
                }
                if (binding.dayTues.isChecked) {
                    if (weekSize > 0)
                        weekString += ", "
                    weekString += "???"
                    weekSize++

                    scheduleDTO.alarmDTO.dayOfWeek["3"] = true
                }
                if (binding.dayWed.isChecked) {
                    if (weekSize > 0)
                        weekString += ", "
                    weekString += "???"
                    weekSize++

                    scheduleDTO.alarmDTO.dayOfWeek["4"] = true
                }
                if (binding.dayThurs.isChecked) {
                    if (weekSize > 0)
                        weekString += ", "
                    weekString += "???"
                    weekSize++

                    scheduleDTO.alarmDTO.dayOfWeek["5"] = true
                }
                if (binding.dayFri.isChecked) {
                    if (weekSize > 0)
                        weekString += ", "
                    weekString += "???"
                    weekSize++

                    scheduleDTO.alarmDTO.dayOfWeek["6"] = true
                }
                if (binding.daySat.isChecked) {
                    if (weekSize > 0)
                        weekString += ", "
                    weekString += "???"
                    //weekSize++

                    scheduleDTO.alarmDTO.dayOfWeek["7"] = true
                }
                binding.textAlarmDate.text = "?????? $weekString"
            }
        }
        println("?????? ?????? ${scheduleDTO.alarmDTO.dayOfWeek}")
    }

    private fun showCalendar() {
        binding.scrollView.visibility = View.GONE
        binding.layoutOk.visibility = View.GONE
        binding.scrollViewCalendar.visibility = View.VISIBLE
        binding.layoutCalendar.visibility = View.VISIBLE
    }

    private fun hideCalendar() {
        binding.scrollView.visibility = View.VISIBLE
        binding.layoutOk.visibility = View.VISIBLE
        binding.scrollViewCalendar.visibility = View.GONE
        binding.layoutCalendar.visibility = View.GONE
    }

    private fun setCalendarRange() {
        showCalendar()
        binding.calendarView.selectedDayBackgroundColor = Color.parseColor("#FFEACA")
        binding.calendarView.selectionType = SelectionType.RANGE
        binding.calendarView.selectionManager = RangeSelectionManager(OnDaySelectedListener {
            /*println("========== setSelectionManager ==========")
            println("Selected Dates : " + binding.calendarView.selectedDates.size)
            if (binding.calendarView.selectedDates.size <= 0) return@OnDaySelectedListener
            println("Selected Days : " + binding.calendarView.selectedDays)
            println("Start Day : " + binding.calendarView.selectedDays[0])
            println("End Day : " + binding.calendarView.selectedDays[binding.calendarView.selectedDays.lastIndex])*/
        })

        binding.buttonCalOk.setOnClickListener {
            hideCalendar()
            if (binding.calendarView.selectedDates.size > 0) {
                scheduleDTO.startDate = binding.calendarView.selectedDays[0].calendar.time
                scheduleDTO.endDate = binding.calendarView.selectedDays[binding.calendarView.selectedDays.lastIndex].calendar.time

                setStartEndDate()
            }
            isValidCalendarRange()
        }

        binding.buttonCalCancel.setOnClickListener {
            hideCalendar()
            isValidCalendarRange()
        }
    }

    private fun setStartEndDate() {
        var startDate = SimpleDateFormat("yyyy.MM.dd").format(scheduleDTO.startDate!!)
        var endDate = SimpleDateFormat("yyyy.MM.dd").format(scheduleDTO.endDate!!)

        println("????????? ${scheduleDTO.startDate}, ?????? ${scheduleDTO.endDate}")

        binding.editStartDate.setText(startDate)
        binding.editEndDate.setText(endDate)
    }

    private fun isValidCalendarRange() {
        if (binding.editStartDate.text.isNullOrEmpty() || binding.editEndDate.text.isNullOrEmpty()) {
            binding.textRangeError.text = "????????? ????????? ?????????."
            binding.editStartDate.setBackgroundResource(R.drawable.edit_rectangle_red)
            binding.editEndDate.setBackgroundResource(R.drawable.edit_rectangle_red)
            rangeOK = false
        } else {
            binding.textRangeError.text = ""
            binding.editStartDate.setBackgroundResource(R.drawable.edit_rectangle)
            binding.editEndDate.setBackgroundResource(R.drawable.edit_rectangle)
            rangeOK = true
        }
        visibleOkButton()
    }

    private fun isValidUrl(url: String) : Boolean {
        return url.matches("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]".toRegex())
    }

    private fun isValidUrlEdit() {
        //if (binding.editUrl.text.toString().isNullOrEmpty()) {
        if (!isValidUrl(binding.editUrl.text.toString())) {
            binding.textUrlError.text = "????????? URL??? ????????????."
            binding.editUrl.setBackgroundResource(R.drawable.edit_rectangle_red)
            actionOK = false
        } else {
            binding.textUrlError.text = ""
            binding.editUrl.setBackgroundResource(R.drawable.edit_rectangle)
            actionOK = true
        }

        visibleOkButton()
    }

    private fun setCalendarSingleSubExecute() {
        val alarmDate = SimpleDateFormat("yyyyMMdd").format(scheduleDTO.alarmDTO.alarmDate!!).toInt()
        val todayDate = SimpleDateFormat("yyyyMMdd").format(Date()).toInt()

        var alarmDateFormat = SimpleDateFormat("MM??? dd??? (E)", Locale("ko", "KR")).format(scheduleDTO.alarmDTO.alarmDate!!)
        val alarmYear = SimpleDateFormat("yyyy").format(scheduleDTO.alarmDTO.alarmDate!!).toInt()
        val todayYear = SimpleDateFormat("yyyy").format(Date()).toInt()
        if (alarmYear > todayYear) { // ????????? ????????? ?????? ??????
            alarmDateFormat = "${alarmYear}??? $alarmDateFormat"
        }

        when (alarmDate) {
            todayDate -> { // ??????
                val alarmTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    "${binding.timepickerAlarm.hour}${binding.timepickerAlarm.minute}".toInt()
                } else {
                    "${binding.timepickerAlarm.currentHour}${binding.timepickerAlarm.currentMinute}".toInt()
                }
                val nowTime = SimpleDateFormat("HHmm").format(Date()).toInt()

                //println("${binding.timepickerAlarm.hour}")
                if (alarmTime > nowTime) {
                    binding.textAlarmDate.text = "??????-$alarmDateFormat"
                } else { // ?????? ?????? ????????? ?????? ??????
                    Toast.makeText(activity,"?????? ?????? ????????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show()
                }
            }
            (todayDate + 1) -> { // ??????
                binding.textAlarmDate.text = "??????-$alarmDateFormat"
            }
            else -> {
                binding.textAlarmDate.text = "$alarmDateFormat"
            }
        }
    }

    private fun setCalendarSingle() {
        showCalendar()
        binding.calendarView.selectionType = SelectionType.SINGLE
        binding.calendarView.selectedDayBackgroundColor = Color.parseColor("#F79256")

        binding.buttonCalOk.setOnClickListener {
            hideCalendar()
            if (binding.calendarView.selectedDates.size > 0) {
                scheduleDTO.alarmDTO.alarmDate = binding.calendarView.selectedDays[0].calendar.time
                if (binding.weekGroup.checkedIds.size > 0) {
                    binding.weekGroup.clearCheck()
                    Handler(Looper.getMainLooper()).postDelayed({ // weekGroup ?????? ??????????????? ????????? ????????? ????????? ??????
                        setCalendarSingleSubExecute()
                    }, 50L)
                } else {
                    setCalendarSingleSubExecute()
                }


                //binding.weekGroup.isEnabled = true

            }
        }

        binding.buttonCalCancel.setOnClickListener {
            hideCalendar()
        }
    }

    // ????????? ??????
    private fun visibleOkButton() {
        binding.buttonOk.isEnabled = titleOK && rangeOK && purposeOK && actionOK && cycleOK && countOK
    }

    private fun applySchedule() {
        (activity as MainActivity?)?.loading()
        if (scheduleDTO.docName.isNullOrEmpty()) {
            scheduleDTO.docName = Utility.randomDocumentName()
            scheduleDTO.order = System.currentTimeMillis()
        }

        scheduleDTO.isSelected = false
        scheduleDTO.title = binding.editTitle.text.toString()
        scheduleDTO.purpose = binding.editPurpose.text.toString()
        scheduleDTO.url = binding.editUrl.text.toString()

        // ????????? ????????? ?????? ???????????? ????????????
        when (scheduleDTO.action) {
            ScheduleDTO.Action.APP -> {
                scheduleDTO.url = ""
            }
            ScheduleDTO.Action.URL -> {
                scheduleDTO.appDTO = null
            }
            ScheduleDTO.Action.ETC -> {
                scheduleDTO.url = ""
                scheduleDTO.appDTO = null
            }
            else -> {
                scheduleDTO.url = ""
            }
        }

        scheduleDTO.count = binding.editCount.text.toString().toLong()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scheduleDTO.alarmDTO.alarmHour = binding.timepickerAlarm.hour
            scheduleDTO.alarmDTO.alarmMinute = binding.timepickerAlarm.minute
        } else {
            scheduleDTO.alarmDTO.alarmHour = binding.timepickerAlarm.currentHour
            scheduleDTO.alarmDTO.alarmMinute = binding.timepickerAlarm.currentMinute
        }

        if (fanClubDTO == null) {
            setPersonalSchedule()
        } else {
            setFanClubSchedule()
        }
    }

    private fun addSampleData() {
        titleOK = true
        rangeOK = true
        purposeOK = true
        actionOK = true
        cycleOK = true
        countOK = true

        binding.editTitle.setText("?????? ?????? ???????????? ??????!")

        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.DATE, 30)
        scheduleDTO.startDate = Date()
        scheduleDTO.endDate = cal.time
        setStartEndDate()

        binding.editPurpose.setText("????????? 5??? ??? ?????? ???????????? ??????!\n\n??? ????????? ?????? ?????? ?????????!")

        scheduleDTO.action = ScheduleDTO.Action.APP
        scheduleDTO.appDTO = AppDTO(false, "com.iloen.melon", "??????", "https://play-lh.googleusercontent.com/GweSpOJ7p8RZ0lzMDr7sU0x5EtvbsAubkVjLY-chdyV6exnSUfl99Am0g8X0w_a2Qo4=s180-rw", 1)
        binding.textSelectedApp.text = scheduleDTO.appDTO?.appName
        binding.radioApp.isChecked = true
        selectActionApp()

        scheduleDTO.cycle = ScheduleDTO.Cycle.DAY
        binding.radioDay.isChecked = true

        binding.editCount.setText("5")

        binding.buttonOk.isEnabled = true
    }

    private fun observeTutorial() {
        (activity as MainActivity?)?.getTutorialStep()?.observe(viewLifecycleOwner) {
            onTutorial((activity as MainActivity?)?.getTutorialStep()?.value!!)
        }
    }

    private fun onTutorial(step: Int) {
        when (step) {
            3 -> {
                println("???????????? Step - $step")
                TapTargetSequence(requireActivity())
                    .targets(
                        TapTarget.forView(binding.layoutMain,
                            "???????????? ?????? ???????????? ????????? ??? ????????????.",
                            "- OK ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(requireContext(), R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.layoutMain,
                            "???????????? ???, ???, ??? ????????? ???????????? ??? ????????????.",
                            "- OK ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(requireContext(), R.drawable.ok))
                            .tintTarget(true)).listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            addSampleData()
                            (activity as MainActivity?)?.addTutorialStep()
                        }
                        override fun onSequenceStep(tutorialStep: TapTarget, targetClicked: Boolean) {
                            //Toast.makeText(secondActivity.this,"GREAT!",Toast.LENGTH_SHORT).show();

                        }
                        override fun onSequenceCanceled(lastTarget: TapTarget) {

                        }
                    }).start()
            }
            4 -> {
                println("???????????? Step - $step")
                //var rect = Rect()
                //binding.layoutOk.getGlobalVisibleRect(rect)
                val location = IntArray(2)
                binding.layoutOk.getLocationOnScreen(location)
                val rect = Rect(location[0], location[1], location[0] + binding.layoutOk.width, location[1] + binding.layoutOk.height)
                TapTargetSequence(requireActivity())
                    .targets(
                        TapTarget.forBounds(rect,
                            "'?????? ???????????? 5?????? ?????? ??????'??? ????????? ????????????????????????.",
                            "- ?????? ?????? ????????? ?????? ???????????? ??????????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .tintTarget(true)).listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            if (isAddedTutorialSampleData) { // ?????? ?????? ???????????? ????????? ???????????? ????????????
                                finishFragment()
                            } else {
                                applySchedule()
                            }
                            (activity as MainActivity?)?.addTutorialStep()
                        }
                        override fun onSequenceStep(tutorialStep: TapTarget, targetClicked: Boolean) {
                            //Toast.makeText(secondActivity.this,"GREAT!",Toast.LENGTH_SHORT).show();

                        }
                        override fun onSequenceCanceled(lastTarget: TapTarget) {

                        }
                    }).start()
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
         * @return A new instance of fragment FragmentScheduleAdd.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentScheduleAdd().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}