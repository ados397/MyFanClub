package com.ados.myfanclub.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ados.myfanclub.databinding.FragmentPageScheduleBinding

import com.ados.myfanclub.R
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.MemberDTO


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentPageSchedule.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentPageSchedule : Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentPageScheduleBinding? = null
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
        _binding = FragmentPageScheduleBinding.inflate(inflater, container, false)

        var rootView = binding.root.rootView

        val fragment = FragmentScheduleList.newInstance(fanClubDTO, currentMember)
        childFragmentManager.beginTransaction().replace(R.id.layout_fragment, fragment).commit()

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //binding.buttonAddSchedule.setOnClickListener {

            //(activity as MainActivity?)!!.changeFregment()
            //val transaction = childFragmentManager.beginTransaction()
            //val transaction = requireFragmentManager().beginTransaction()
            val fragment = FragmentScheduleAdd()
            //transaction.replace(R.id.layout_main, fragment)
            //transaction.addToBackStack(null)
            //transaction.commit()
            /*childFragmentManager.beginTransaction()
                .replace(R.id., fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()*/
        //}
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentPageSchedule.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: FanClubDTO?, param2: MemberDTO?) =
            FragmentPageSchedule().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putParcelable(ARG_PARAM2, param2)
                }
            }
    }


}