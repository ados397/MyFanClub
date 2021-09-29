package com.ados.myfanclub.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.ados.myfanclub.MyPagerAdapterFanClub
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentFanClubMainBinding
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.MemberDTO
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFanClubMain.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFanClubMain : Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentFanClubMainBinding? = null
    private val binding get() = _binding!!

    private var firestore : FirebaseFirestore? = null

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
        _binding = FragmentFanClubMainBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        firestore = FirebaseFirestore.getInstance()

        setFanClubInfo()

        binding.viewpager.isUserInputEnabled = false // 좌우 터치 스와이프 금지
        binding.viewpager.apply {
            //adapter = MyPagerAdapterFanClub(context as FragmentActivity, fanClubDTO!!, currentMember!!)
            adapter = MyPagerAdapterFanClub(childFragmentManager, viewLifecycleOwner.lifecycle, fanClubDTO!!, currentMember!!)
            setPageTransformer(ZoomOutPageTransformer())
        }
        /*val user = (activity as MainActivity?)?.getUser()
        firestore?.collection("fanClub")?.document(user?.fanClubId!!)?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.result.exists()) { // document 있음
                    fanClubDTO = task.result.toObject(FanClubDTO::class.java)!!
                    firestore?.collection("fanClub")?.document(user?.fanClubId!!)?.collection("member")?.document(user?.uid!!)?.get()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (task.result.exists()) { // document 있음
                                currentMember = task.result.toObject(MemberDTO::class.java)!!

                                when {
                                    isAdministrator() -> { // 클럽장, 부클럽장 메뉴 활성화
                                        binding.textTabSchedule.visibility = View.VISIBLE
                                    }
                                    else -> {
                                        binding.textTabSchedule.visibility = View.GONE
                                    }
                                }
                                binding.viewpager.isUserInputEnabled = false // 좌우 터치 스와이프 금지
                                binding.viewpager.apply {
                                    //adapter = MyPagerAdapterFanClub(context as FragmentActivity, fanClubDTO!!, currentMember!!)
                                    adapter = MyPagerAdapterFanClub(childFragmentManager, viewLifecycleOwner.lifecycle, fanClubDTO!!, currentMember!!)
                                    setPageTransformer(ZoomOutPageTransformer())
                                }
                            }
                        }
                    }
                } else {
                    // 팬클럽 정보 가져오기 실패
                }
            }
        }*/

        binding.textTabInfo.setOnClickListener {
            binding.viewpager.currentItem = 0
            releaseAllTabButton()
            setTabButton(binding.textTabInfo)
        }
        binding.textTabMember.setOnClickListener {
            binding.viewpager.currentItem = 1
            releaseAllTabButton()
            setTabButton(binding.textTabMember)
        }
        binding.textTabRank.setOnClickListener {
            binding.viewpager.currentItem = 2
            releaseAllTabButton()
            setTabButton(binding.textTabRank)
        }
        binding.textTabManagement.setOnClickListener {
            binding.viewpager.currentItem = 3
            releaseAllTabButton()
            setTabButton(binding.textTabManagement)
        }
        binding.textTabSchedule.setOnClickListener {
            binding.viewpager.currentItem = 4
            releaseAllTabButton()
            setTabButton(binding.textTabSchedule)
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

    fun setFanClubInfo() {
        when {
            isAdministrator() -> { // 클럽장, 부클럽장 메뉴 활성화
                binding.textTabSchedule.visibility = View.VISIBLE
            }
            else -> {
                binding.textTabSchedule.visibility = View.GONE
            }
        }
    }

    fun setFanClub(fanClub: FanClubDTO) {
        fanClubDTO = fanClub
    }

    fun setMember(member: MemberDTO) {
        currentMember = member
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
        releaseTabButton(binding.textTabInfo)
        releaseTabButton(binding.textTabMember)
        releaseTabButton(binding.textTabRank)
        releaseTabButton(binding.textTabManagement)
        releaseTabButton(binding.textTabSchedule)
    }

    private fun isMaster() : Boolean {
        return currentMember?.position == MemberDTO.POSITION.MASTER
    }

    private fun isAdministrator() : Boolean {
        return currentMember?.position == MemberDTO.POSITION.MASTER || currentMember?.position == MemberDTO.POSITION.SUB_MASTER
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentFanClubMain.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: FanClubDTO?, param2: MemberDTO?) =
            FragmentFanClubMain().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putParcelable(ARG_PARAM2, param2)
                }
            }
    }

}