package com.ados.myfanclub.page

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentFanClubRankBinding
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.MemberDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFanClubRank.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFanClubRank : Fragment(), OnFanClubRankItemClickListener {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentFanClubRankBinding? = null
    private val binding get() = _binding!!

    private var firestore : FirebaseFirestore? = null

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterFanClubRank

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null
    private var fanClubs : ArrayList<FanClubDTO> = arrayListOf()

    private var selectedFanClub: FanClubDTO? = null
    private var selectedPosition: Int? = 0

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
        _binding = FragmentFanClubRankBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        firestore = FirebaseFirestore.getInstance()

        recyclerView = rootView.findViewById(R.id.rv_fan_club_rank!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 메뉴는 기본 숨김
        binding.layoutMenu.visibility = View.GONE

        refresh()

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonClose.setOnClickListener {
            selectRecyclerView()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            refresh()

            binding.swipeRefreshLayout.isRefreshing = false
            Toast.makeText(activity, "새로 고침", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentFanClubRank.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: FanClubDTO?, param2: MemberDTO?) =
            FragmentFanClubRank().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putParcelable(ARG_PARAM2, param2)
                }
            }
    }

    private fun refresh() {
        firestore?.collection("fanClub")?.orderBy("exp", Query.Direction.DESCENDING)?.get()?.addOnCompleteListener { task ->
            fanClubs.clear()
            if (task.isSuccessful) {
                for (document in task.result) {
                    var fanClub = document.toObject(FanClubDTO::class.java)!!
                    fanClubs.add(fanClub)
                }
                recyclerViewAdapter = RecyclerViewAdapterFanClubRank(fanClubs, this)
                recyclerView.adapter = recyclerViewAdapter
            }
        }
    }

    private fun setSelectFanClubInfo() {
        if (selectedFanClub != null) {
            var imageID = requireContext().resources.getIdentifier(selectedFanClub?.imgSymbol, "drawable", requireContext().packageName)
            if (imageID > 0) {
                binding.imgSymbol.setImageResource(imageID)
            }

            when (selectedPosition) {
                0 -> {
                    binding.imgRank.visibility = View.VISIBLE
                    binding.textRank.visibility = View.GONE
                    binding.imgRank.setImageResource(R.drawable.award_01)
                    binding.cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.rank1))
                }
                1 -> {
                    binding.imgRank.visibility = View.VISIBLE
                    binding.textRank.visibility = View.GONE
                    binding.imgRank.setImageResource(R.drawable.award_02)
                    binding.cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.rank2))
                }
                2 -> {
                    binding.imgRank.visibility = View.VISIBLE
                    binding.textRank.visibility = View.GONE
                    binding.imgRank.setImageResource(R.drawable.award_03)
                    binding.cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.rank3))
                }
                else -> {
                    binding.imgRank.visibility = View.GONE
                    binding.textRank.visibility = View.VISIBLE
                    binding.textRank.text = "${selectedPosition!!+1}"
                    binding.cardView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }

            binding.textName.text = selectedFanClub?.name
            binding.textLevel.text = "Lv. ${selectedFanClub?.level}"
            binding.textMaster.text = selectedFanClub?.masterNickname
            binding.textCount.text = "${selectedFanClub?.count}/${selectedFanClub?.countMax}"
            binding.editDescription.setText(selectedFanClub?.description)
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