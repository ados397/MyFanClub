package com.ados.myfanclub.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentAccountInfoBinding
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.MemberDTO
import kotlinx.android.synthetic.main.question_dialog.*
import java.text.SimpleDateFormat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentAccountInfo.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentAccountInfo : Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentAccountInfoBinding? = null
    private val binding get() = _binding!!

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            fanClubDTO = it.getParcelable(ARG_PARAM1)
            currentMember = it.getParcelable(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAccountInfoBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView


        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = (activity as MainActivity?)?.getUser()
        binding.textNickname.text = user?.nickname
        binding.textUserId.text = "(${user?.userId})"
        binding.textCreateTime.text = "가입 ${SimpleDateFormat("yyyy.MM.dd").format(user?.createTime)}"
        binding.textLevel.text = "Lv. ${user?.level}"
        binding.editAboutMe.setText(user?.aboutMe)

        binding.buttonSettings.setOnClickListener {
            val fragment = FragmentAccountSettings.newInstance(fanClubDTO, currentMember)
            //fragment.scheduleDTO = selectedSchedule!!
            parentFragmentManager.beginTransaction().apply{
                replace(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
                commit()
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
         * @return A new instance of fragment FragmentAccountInfo.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: FanClubDTO?, param2: MemberDTO?) =
            FragmentAccountInfo().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putParcelable(ARG_PARAM2, param2)
                }
            }
    }
}