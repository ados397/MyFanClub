package com.ados.myfanclub.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.MyPagerAdapterFanClub
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentFanClubMainBinding
import com.ados.myfanclub.dialog.QuestionDialog
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.MemberDTO
import com.ados.myfanclub.model.QuestionDTO
import kotlinx.android.synthetic.main.question_dialog.*

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
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentFanClubMainBinding? = null
    private val binding get() = _binding!!

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null

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
        println("언제 들어오나?? 이게 문제인가??????????????")
        // Inflate the layout for this fragment
        _binding = FragmentFanClubMainBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        setFanClubInfo()

        binding.viewpager.isUserInputEnabled = false // 좌우 터치 스와이프 금지
        binding.viewpager.apply {
            //adapter = MyPagerAdapterFanClub(context as FragmentActivity, fanClubDTO!!, currentMember!!)
            adapter = MyPagerAdapterFanClub(childFragmentManager, viewLifecycleOwner.lifecycle)
            setPageTransformer(ZoomOutPageTransformer())
        }

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
            // 관리자 권한이 없어졌다면 스케줄 탭 삭제를 위해 갱신, 아니라면 탭 이동
            if (!isRemoveAdmin()) {
                binding.viewpager.currentItem = 4
                releaseAllTabButton()
                setTabButton(binding.textTabSchedule)
            }

            /*(activity as MainActivity?)?.refreshFanClubDTO { fanClub ->
                if (fanClubDTO != fanClub) { // 팬클럽 정보가 변경 되었으면 작업을 진행하지 않고 새로고침
                    fanClubDTO = fanClub
                    fanClubInfoChange()
                } else {
                    (activity as MainActivity?)?.refreshMemberDTO { member ->
                        if (currentMember != member) { // 팬클럽 정보가 변경 되었으면 작업을 진행하지 않고 새로고침
                            currentMember = member
                            fanClubInfoChange()
                        } else {
                            println("언제 들어오나?? ㅠㅠㅠㅠㅠㅠㅠㅠㅠㅠㅠ")
                            binding.viewpager.setCurrentItem(4, false)
                            //binding.viewpager.currentItem = 4
                            releaseAllTabButton()
                            setTabButton(binding.textTabSchedule)
                        }
                    }
                }
            }*/
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

    // 관리자 없다면 삭제되었다면 새로고침
    fun isRemoveAdmin() : Boolean {
        fanClubDTO = (activity as MainActivity?)?.getFanClub()
        currentMember = (activity as MainActivity?)?.getMember()
        //println("관리자 권한 체크 new: ${member.position}, old: ${currentMember?.position}")
        // 관리자 권한이 삭제됨
        //if (currentMember?.isAdministrator()!! && !member.isAdministrator()) {
        if (!currentMember?.isAdministrator()!!) {
            fanClubDTO = (activity as MainActivity?)?.getFanClub()
            currentMember = (activity as MainActivity?)?.getMember()

            val question = QuestionDTO(
                QuestionDTO.Stat.WARNING,
                "팬클럽 정보 변경",
                "팬클럽 정보가 변경되어 새로고침 합니다.",
            )
            val questionDialog = QuestionDialog(requireContext(), question)
            questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            questionDialog.setCanceledOnTouchOutside(false)
            questionDialog.show()
            questionDialog.showButtonOk(false)
            questionDialog.setButtonCancel("확인")
            questionDialog.button_question_cancel.setOnClickListener { // No
                questionDialog.dismiss()
                (activity as MainActivity?)?.loading()
                binding.viewpager.apply {
                    adapter = MyPagerAdapterFanClub(childFragmentManager, viewLifecycleOwner.lifecycle)
                    setPageTransformer(ZoomOutPageTransformer())
                    moveInfoPage()
                }
                setFanClubInfo()
                (activity as MainActivity?)?.loadingEnd()
            }
            return true
        } else {
            return false
        }
    }

    private fun setFanClubInfo() {
        when {
            currentMember?.isAdministrator()!! -> { // 클럽장, 부클럽장 메뉴 활성화
                binding.textTabSchedule.visibility = View.VISIBLE
            }
            else -> {
                binding.textTabSchedule.visibility = View.GONE
            }
        }
    }

    private fun moveInfoPage() {
        binding.viewpager.currentItem = 0
        releaseAllTabButton()
        setTabButton(binding.textTabInfo)
    }

    fun getFanClub() : FanClubDTO? {
        return fanClubDTO
    }

    fun getMember() : MemberDTO? {
        return currentMember
    }

    fun setFanClub(fanClub: FanClubDTO?) {
        fanClubDTO = fanClub
    }

    fun setMember(member: MemberDTO?) {
        currentMember = member
    }

    fun moveFanClubInitalize() {
        val fragment = FragmentFanClubInitalize()
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
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
        fun newInstance(param1: String, param2: String) =
            FragmentFanClubMain().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}