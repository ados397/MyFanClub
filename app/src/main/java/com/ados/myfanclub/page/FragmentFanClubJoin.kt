package com.ados.myfanclub.page

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentFanClubJoinBinding
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.MemberDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

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

    private var firestore : FirebaseFirestore? = null

    private lateinit var callback: OnBackPressedCallback

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterFanClub

    private var fanClubs : ArrayList<FanClubDTO> = arrayListOf()

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

        firestore = FirebaseFirestore.getInstance()

        recyclerView = rootView.findViewById(R.id.rv_fan_club!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 메뉴는 기본 숨김
        binding.layoutMenu.visibility = View.GONE

        searchFanClub()




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
            val user = (activity as MainActivity?)?.getUser()
            firestore?.collection("fanClub")?.document(selectedFanClub?.docName.toString())?.collection("member")?.document(user?.uid.toString())?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result.exists()) { // document 있음
                        Toast.makeText(activity, "이미 가입 요청을 한 팬클럽 입니다!", Toast.LENGTH_SHORT).show()
                    } else { // document 없으면 회원 가입 페이지로 이동
                        val member = MemberDTO(false, user?.uid, user?.nickname, user?.level, user?.aboutMe, 0, MemberDTO.POSITION.GUEST, Date(), null, false)
                        firestore?.collection("fanClub")?.document(selectedFanClub?.docName.toString())?.collection("member")?.document(user?.uid.toString())?.set(member)?.addOnCompleteListener {
                            Toast.makeText(activity, "팬클럽 가입 요청 완료!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                selectRecyclerView()
            }
        }
    }

    private fun searchFanClub() {
        var query = binding.searchView.query
        var collection: Query? = null

        collection = if (query.isNullOrEmpty()) { // 검색어가 없을 경우 랜덤으로 팬클럽 표시
            // 팬클럽 생성 시간이 특정날짜 기준 이하인 팬클럽을 랜덤으로 획득
            // 오늘날짜 - 팬클럽 15개 이상 생성된 날짜 중 랜덤 값
            val startDate = SimpleDateFormat("yyyyMMdd").parse("20210905").time
            val calendar= Calendar.getInstance()
            val range = ((calendar.time.time - startDate) / (24 * 60 * 60 * 1000)).toInt()
            val random = Random.nextInt(0, range)
            calendar.add(Calendar.DATE, -random)

            println("랜덤 : $random, 레인지 : $range, ${calendar.time}, $calendar")

            firestore?.collection("fanClub")?.whereLessThan("createTime", calendar.time)?.limit(15)
        } else { // 검색어에 해당하는 팬클럽 표시
            firestore?.collection("fanClub")
        }
        //firestore?.collection("fanClub")?.get()?.addOnCompleteListener { task ->
        collection?.get()?.addOnCompleteListener { task ->
            fanClubs.clear()
            if (task.isSuccessful) {
                for (document in task.result) {
                    var fanClub = document.toObject(FanClubDTO::class.java)!!
                    if (!query.isNullOrEmpty()) {
                        if (fanClub.name!!.contains(query)) {
                            fanClubs.add(fanClub)
                        }
                    } else {
                        fanClubs.add(fanClub)
                    }
                }
                recyclerViewAdapter = RecyclerViewAdapterFanClub(fanClubs, this)
                recyclerView.adapter = recyclerViewAdapter
            }
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
        val fragment = FragmentFanClubInitalize()
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
    }

    private fun setSelectFanClubInfo() {
        if (selectedFanClub != null) {
            var imageID = requireContext().resources.getIdentifier(selectedFanClub?.imgSymbol, "drawable", requireContext().packageName)
            if (imageID > 0) {
                binding.imgSymbol.setImageResource(imageID)
            }

            binding.textName.text = selectedFanClub?.name
            binding.textLevel.text = "Lv. ${selectedFanClub?.level}"
            binding.textMaster.text = selectedFanClub?.masterNickname
            binding.textCount.text = "${selectedFanClub?.count}/${selectedFanClub?.countMax}"
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
        if (recyclerViewAdapter?.selectItem(selectedPosition!!)) { // 선택 일 경우 메뉴 표시 및 레이아웃 어둡게
            openLayout()
        } else { // 해제 일 경우 메뉴 숨김 및 레이아웃 밝게
            closeLayout()
        }
    }

    override fun onItemClick(item: FanClubDTO, position: Int) {
        selectedFanClub = item
        selectedPosition = position
        setSelectFanClubInfo()
        selectRecyclerView()
    }
}