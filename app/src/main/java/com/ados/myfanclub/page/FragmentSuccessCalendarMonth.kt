package com.ados.myfanclub.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentSuccessCalendarBinding
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentSuccessCalendarMonth.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentSuccessCalendarMonth : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentSuccessCalendarBinding? = null
    private val binding get() = _binding!!

    var pageIndex = Int.MAX_VALUE
    lateinit var currentDate: Date

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterSuccessCalendarMonth

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
        recyclerView.layoutManager = GridLayoutManager(activity, 3)

        binding.calendarHeader.visibility = View.GONE
        binding.layoutTotal.visibility = View.GONE

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
            add(Calendar.YEAR, pageIndex)
            time
        }

        recyclerViewAdapter = RecyclerViewAdapterSuccessCalendarMonth(binding.layoutSuccessCalendar, date)
        recyclerView.adapter = recyclerViewAdapter

        currentDate = date

        binding.textYearMonth.text = SimpleDateFormat("yyyy년").format(date.time)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentSuccessCalendarMonth.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentSuccessCalendarMonth().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}