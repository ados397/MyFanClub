package com.ados.myfanclub.page

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.widget.ViewPager2
import com.ados.myfanclub.MyPagerAdapterSuccessCalendar
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentSuccessCalendarLayoutBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentSuccessCalendarLayout.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentSuccessCalendarLayout : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentSuccessCalendarLayoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

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
        _binding = FragmentSuccessCalendarLayoutBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView


        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val firstFragmentStateAdapter = MyPagerAdapterSuccessCalendar(requireActivity())
        //binding.viewpager.adapter = firstFragmentStateAdapter
        if (param1.equals("fanClub")) {
            binding.textTitle.text = "팬클럽 목표달성 통계"
        } else if (param1.equals("personal")) {
            binding.textTitle.text = "개인 목표달성 통계"
        }

        binding.viewpager.orientation = ViewPager2.ORIENTATION_VERTICAL

        binding.viewpager.apply {
            releaseAllTabButton()
            when {
                param2.equals("week") -> {
                    setPagerAdapter(1)
                    setTabButton(binding.textTabWeek)
                }
                param2.equals("month") -> {
                    setPagerAdapter(2)
                    setTabButton(binding.textTabMonth)
                }
                else -> {
                    setPagerAdapter(0)
                    setTabButton(binding.textTabDay)
                }
            }
            //setPageTransformer(ZoomOutPageTransformer())
        }

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.textTabDay.setOnClickListener {
            setPagerAdapter(0)
            releaseAllTabButton()
            setTabButton(binding.textTabDay)
        }
        binding.textTabWeek.setOnClickListener {
            setPagerAdapter(1)
            releaseAllTabButton()
            setTabButton(binding.textTabWeek)
        }
        binding.textTabMonth.setOnClickListener {
            setPagerAdapter(2)
            releaseAllTabButton()
            setTabButton(binding.textTabMonth)
        }
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

    private fun callBackPressed() {
        val fragment = FragmentDashboardMission.newInstance("", param2!!)
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
    }

    private fun setPagerAdapter(pageIndex: Int) {
        //binding.viewpager.adapter = MyPagerAdapterSuccessCalendar(context as FragmentActivity, pageIndex)
        binding.viewpager.adapter = MyPagerAdapterSuccessCalendar(childFragmentManager, viewLifecycleOwner.lifecycle, pageIndex, param1!!)
        binding.viewpager.currentItem = Int.MAX_VALUE / 2
    }

    private fun setTabButton(textView: TextView) {
        textView.background = AppCompatResources.getDrawable(requireContext(), R.drawable.btn_round)
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
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentSuccessCalendarLayout.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentSuccessCalendarLayout().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}