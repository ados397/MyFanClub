package com.ados.myfanclub.page

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentFanClubRankBinding
import com.ados.myfanclub.dialog.ImageViewDialog
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.FanClubExDTO
import com.ados.myfanclub.model.MemberDTO
import com.ados.myfanclub.viewmodel.FirebaseStorageViewModel
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentFanClubRankBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val firebaseStorageViewModel : FirebaseStorageViewModel by viewModels()

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterFanClubRank

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null

    private var imageViewDialog: ImageViewDialog? = null

    private var selectedFanClub: FanClubDTO? = null
    private var selectedPosition: Int? = 0

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
        // Inflate the layout for this fragment
        _binding = FragmentFanClubRankBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        recyclerView = rootView.findViewById(R.id.rv_fan_club_rank)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 메뉴는 기본 숨김
        binding.layoutMenu.visibility = View.GONE

        refresh()
        observeFanClubs()

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (activity as MainActivity?)?.backPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editDescription.setOnTouchListener { _, _ ->
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
            false
        }

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
        fun newInstance(param1: String, param2: String) =
            FragmentFanClubRank().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
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
                    recyclerViewAdapter = RecyclerViewAdapterFanClubRank(itemsEx, this@FragmentFanClubRank)
                    recyclerView.adapter = recyclerViewAdapter
                    (activity as MainActivity?)?.loadingEnd()
                    break
                }
                delay(100)
            }
        }
    }

    private fun refresh() {
        (activity as MainActivity?)?.loading()
        firebaseViewModel.getFanClubs()
        closeLayout()
    }

    private fun observeFanClubs() {
        firebaseViewModel.fanClubDTOs.observe(viewLifecycleOwner) {
            if (_binding != null) { // 메인 탭에서 바로 사용자 정보 탭 이동 시 팬클럽 뷰가 Destroy 되고 나서 뒤 늦게 들어오는 경우가 있기 때문에 예외 처리
                setAdapter()
            }
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
            binding.textCount.text = "${selectedFanClub?.memberCount}/${selectedFanClub?.getMaxMemberCount()}"
            binding.editDescription.setText(selectedFanClub?.description)

            binding.imgSymbol.setOnClickListener {
                if (imageViewDialog == null) {
                    imageViewDialog = ImageViewDialog(requireContext())
                    imageViewDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    imageViewDialog?.setCanceledOnTouchOutside(false)
                }
                imageViewDialog?.imageUri = item.imgSymbolCustomUri
                imageViewDialog?.imageID = requireContext().resources.getIdentifier(selectedFanClub?.imgSymbol, "drawable", requireContext().packageName)
                imageViewDialog?.show()
                imageViewDialog?.setInfo()
                imageViewDialog?.binding?.buttonCancel?.setOnClickListener { // No
                    imageViewDialog?.dismiss()
                    imageViewDialog = null
                }
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