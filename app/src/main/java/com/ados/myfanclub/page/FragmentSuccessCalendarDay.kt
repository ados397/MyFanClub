package com.ados.myfanclub.page

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.SuccessCalendar
import com.ados.myfanclub.databinding.FragmentSuccessCalendarBinding
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentSuccessCalendarDay.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentSuccessCalendarDay : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentSuccessCalendarBinding? = null
    private val binding get() = _binding!!

    private val firebaseViewModel : FirebaseViewModel by viewModels()

    var pageIndex = Int.MAX_VALUE
    lateinit var currentDate: Date

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapterDay : RecyclerViewAdapterSuccessCalendarDay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSuccessCalendarBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        recyclerView = rootView.findViewById(R.id.rv_success_calendar!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = GridLayoutManager(activity, 7)

        initView(rootView)

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun initView(view: View) {
        pageIndex -= (Int.MAX_VALUE / 2)

        // 날짜 적용
        val date = Calendar.getInstance().run {
            add(Calendar.MONTH, pageIndex)
            time
        }

        var successCalendar = SuccessCalendar(date)
        successCalendar.initBaseCalendar()

        val fieldName = SimpleDateFormat("yyyyMM").format(date)

        if (param1.equals("fanClub")) {
            val fanClubDTO = (activity as MainActivity?)?.getFanClub()
            val currentMember = (activity as MainActivity?)?.getMember()
            firebaseViewModel.getFanClubScheduleStatistics(fanClubDTO?.docName.toString(), currentMember?.userUid.toString(), "day", fieldName)
        } else if (param1.equals("personal")) {
            val user = (activity as MainActivity?)?.getUser()
            firebaseViewModel.getPersonalScheduleStatistics(user?.uid.toString(), "day", fieldName)
        }

        firebaseViewModel.scheduleStatistics.observe(viewLifecycleOwner) {
            recyclerViewAdapterDay = RecyclerViewAdapterSuccessCalendarDay(binding.layoutSuccessCalendar, date, firebaseViewModel.scheduleStatistics.value!!, successCalendar)
            recyclerView.adapter = recyclerViewAdapterDay

            var total = 0
            for (it in firebaseViewModel.scheduleStatistics.value!!) {
                total = total.plus(it.value)
            }
            val percent = total.toDouble() / (successCalendar.dateList.size - successCalendar.prevTail - successCalendar.nextHead).toDouble()
            binding.progressPercent.progress = percent.toInt()
            binding.textPercent.text = "${String.format("%.1f", percent)}%"

            if (binding.progressPercent.progress < 100) {
                binding.imgComplete.visibility = View.GONE
                when {
                    binding.progressPercent.progress < 40 -> {
                        binding.progressPercent.progressBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.progress_background_0))
                        binding.progressPercent.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.progress_0))
                        binding.textPercent.setTextColor(ContextCompat.getColor(requireContext(), R.color.progress_0))
                    }
                    binding.progressPercent.progress < 70 -> {
                        binding.progressPercent.progressBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.progress_background_40))
                        binding.progressPercent.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.progress_40))
                        binding.textPercent.setTextColor(ContextCompat.getColor(requireContext(), R.color.progress_40))
                    }
                    else -> {
                        binding.progressPercent.progressBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.progress_background_70))
                        binding.progressPercent.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.progress_70))
                        binding.textPercent.setTextColor(ContextCompat.getColor(requireContext(), R.color.progress_70))
                    }
                }
            } else {
                binding.imgComplete.visibility = View.VISIBLE
                binding.progressPercent.progressBackgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.progress_background_100))
                binding.progressPercent.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.progress_100))
                binding.textPercent.setTextColor(ContextCompat.getColor(requireContext(), R.color.progress_100))
            }
        }

        currentDate = date

        binding.textYearMonth.text = SimpleDateFormat("yyyy년 MM월").format(date.time)
        binding.textTotal.text = SimpleDateFormat("MM월 달성률").format(date.time)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentSuccessCalendar.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentSuccessCalendarDay().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}