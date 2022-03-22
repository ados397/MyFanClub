package com.ados.myfanclub.page

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentFanClubJoinBinding
import com.ados.myfanclub.dialog.QuestionDialog
import com.ados.myfanclub.model.*
import com.ados.myfanclub.viewmodel.FirebaseStorageViewModel
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFanClubJoin.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFanClubJoin : Fragment(), OnFanClubItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentFanClubJoinBinding? = null
    private val binding get() = _binding!!

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val firebaseStorageViewModel : FirebaseStorageViewModel by viewModels()

    private lateinit var callback: OnBackPressedCallback

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterFanClub

    private var questionDialog: QuestionDialog? = null

    private var selectedFanClub: FanClubDTO? = null
    private var selectedPosition: Int? = 0

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
        _binding = FragmentFanClubJoinBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        recyclerView = rootView.findViewById(R.id.rv_fan_club)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 메뉴는 기본 숨김
        binding.layoutMenu.visibility = View.GONE

        searchFanClub()
        firebaseViewModel.fanClubDTOs.observe(viewLifecycleOwner) {
            setAdapter()
        }

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SearchView 폰트 변경
        val typeface = ResourcesCompat.getFont(requireContext(), R.font.uhbee_zziba_regular)
        val editText = binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        editText.typeface = typeface
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20F)

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.buttonSearch.setOnClickListener {
            searchFanClub()
            closeLayout()
        }

        binding.buttonCancel.setOnClickListener {
            selectRecyclerView()
        }

        binding.buttonRequest.setOnClickListener {
            val user = (activity as MainActivity?)?.getUser()!!
            if (isBlockFanClubJoin(user)) {
                val question = QuestionDTO(
                    QuestionDTO.Stat.WARNING,
                    "팬클럽 가입",
                    "팬클럽 탈퇴 혹은 추방 시, 24시간이 지나야 팬클럽에 가입 요청할 수 있습니다.\n\n탈퇴일 [${SimpleDateFormat("yyyy.MM.dd HH:mm").format(user.fanClubQuitDate!!)}]",
                )
                if (questionDialog == null) {
                    questionDialog = QuestionDialog(requireContext(), question)
                    questionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    questionDialog?.setCanceledOnTouchOutside(false)
                } else {
                    questionDialog?.question = question
                }
                questionDialog?.show()
                questionDialog?.setInfo()
                questionDialog?.showButtonOk(false)
                questionDialog?.setButtonCancel("확인")
                questionDialog?.binding?.buttonQuestionCancel?.setOnClickListener { // No
                    questionDialog?.dismiss()
                }
            } else if (user.fanClubRequestId.size > 100) {
                Toast.makeText(activity, "더 이상 팬클럽 가입 신청을 할 수 없습니다. 가입 승인을 기다려 주세요.", Toast.LENGTH_SHORT).show()
            } else { // 팬클럽 탈퇴 후 24시간이 지나야 재 가입이 가능
                firebaseViewModel.getMember(selectedFanClub?.docName.toString(), user.uid.toString()) { memberDTO ->
                    if (memberDTO != null) {
                        Toast.makeText(activity, "이미 가입 요청을 한 팬클럽 입니다!", Toast.LENGTH_SHORT).show()
                    } else {
                        val member = MemberDTO(false, user.uid, user.nickname, user.level, user.aboutMe, 0, MemberDTO.Position.GUEST, Date(), null, null, user.token)
                        firebaseViewModel.updateMember(selectedFanClub?.docName.toString(), member) {
                            user.fanClubRequestId.add(selectedFanClub?.docName.toString())
                            firebaseViewModel.updateUserFanClubRequestId(user) {
                                Toast.makeText(activity, "가입 요청이 정상적으로 되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    selectRecyclerView()
                }
            }
        }
    }

    private fun setAdapter() {
        var uidSet = hashSetOf<String>()
        val itemsEx: ArrayList<FanClubExDTO> = arrayListOf()
        for (fanClub in firebaseViewModel.fanClubDTOs.value!!) {
            itemsEx.add(FanClubExDTO(fanClub))
            if (!fanClub.docName.isNullOrEmpty()) {
                uidSet.add(fanClub.docName.toString())
            }
        }

        var uriCheckIndex = 0
        for (uid in uidSet) {
            firebaseStorageViewModel.getFanClubSymbolImage(uid) { uri ->
                if (uri != null) {
                    for (item in itemsEx) {
                        if (item.fanClubDTO?.docName == uid) {
                            item.imgSymbolCustomUri = uri
                        }
                    }
                }
                uriCheckIndex++
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            while(true) {
                if (uriCheckIndex == uidSet.size) {
                    recyclerViewAdapter = RecyclerViewAdapterFanClub(itemsEx, this@FragmentFanClubJoin)
                    recyclerView.adapter = recyclerViewAdapter
                    (activity as MainActivity?)?.loadingEnd()
                    break
                }
                delay(100)
            }
        }
    }

    private fun isBlockFanClubJoin(user: UserDTO) : Boolean {
        // 팬클럽 탈퇴 후 24시간이 지나야 재 가입이 가능
        var isBlock = false
        if (user.fanClubQuitDate != null) {
            val calendar= Calendar.getInstance()
            calendar.time = user.fanClubQuitDate!!
            calendar.add(Calendar.DATE, 1)

            if (Date() < calendar.time) {
                isBlock = true
            }
        }
        return isBlock
    }

    private fun searchFanClub() {
        var query = binding.searchView.query.trim()
        firebaseViewModel.getFanClubsSearch(query.toString())
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
        val fragment = FragmentFanClubInitalize()
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
    }

    private fun setSelectFanClubInfo(item: FanClubExDTO) {
        if (selectedFanClub != null) {
            if (item.imgSymbolCustomUri != null) {
                Glide.with(requireContext()).load(item.imgSymbolCustomUri).fitCenter().into(binding.imgSymbol)
            } else {
                var imageID = requireContext().resources.getIdentifier(selectedFanClub?.imgSymbol, "drawable", requireContext().packageName)
                if (imageID > 0) {
                    binding.imgSymbol.setImageResource(imageID)
                }
            }

            binding.textName.text = selectedFanClub?.name
            binding.textLevel.text = "Lv. ${selectedFanClub?.level}"
            binding.textMaster.text = selectedFanClub?.masterNickname
            binding.textCount.text = "${selectedFanClub?.memberCount}/${selectedFanClub?.getMaxMemberCount()}"
            binding.editDescription.setText(selectedFanClub?.description)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentFanClubJoin.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentFanClubJoin().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun openLayout() {
        if (binding.layoutMenu.visibility == View.GONE) {
            val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
            binding.layoutMenu.startAnimation(translateUp)
        }
        binding.layoutMenu.visibility = View.VISIBLE
        //recyclerView.smoothSnapToPosition(position)
    }

    private fun closeLayout() {
        if (binding.layoutMenu.visibility == View.VISIBLE) {
            val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
            binding.layoutMenu.startAnimation(translateDown)
        }
        binding.layoutMenu.visibility = View.GONE
        //recyclerView.smoothSnapToPosition(position)
    }

    private fun selectRecyclerView() {
        if (recyclerViewAdapter.selectItem(selectedPosition!!)) { // 선택 일 경우 메뉴 표시 및 레이아웃 어둡게
            openLayout()
        } else { // 해제 일 경우 메뉴 숨김 및 레이아웃 밝게
            closeLayout()
        }
    }

    override fun onItemClick(item: FanClubExDTO, position: Int) {
        selectedFanClub = item.fanClubDTO
        selectedPosition = position
        setSelectFanClubInfo(item)
        selectRecyclerView()
    }
}