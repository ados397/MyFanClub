package com.ados.myfanclub.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentPageFanClubBinding
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.MemberDTO

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentPageFanClub.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentPageFanClub : Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentPageFanClubBinding? = null
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
        _binding = FragmentPageFanClubBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        val user = (activity as MainActivity?)?.getUser()
        if (user?.fanClubId.isNullOrEmpty()) { // 가입된 팬클럽이 없으면 창설/가입 페이지로
            val fragment = FragmentFanClubInitalize()
            childFragmentManager.beginTransaction().replace(R.id.layout_fragment, fragment).commit()
        } else { // 가입된 팬클럽이 있으면 팬클럽 정보 페이지로
            val fragment = FragmentFanClubMain.newInstance(fanClubDTO, currentMember)
            childFragmentManager.beginTransaction().replace(R.id.layout_fragment, fragment).commit()
        }

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentPageFanClub.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: FanClubDTO?, param2: MemberDTO?) =
            FragmentPageFanClub().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putParcelable(ARG_PARAM2, param2)
                }
            }
    }
}