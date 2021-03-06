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
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.database.DBHelperReport
import com.ados.myfanclub.databinding.FragmentAccountRankBinding
import com.ados.myfanclub.dialog.ImageViewDialog
import com.ados.myfanclub.dialog.ReportDialog
import com.ados.myfanclub.model.ReportDTO
import com.ados.myfanclub.model.UserDTO
import com.ados.myfanclub.model.UserExDTO
import com.ados.myfanclub.viewmodel.FirebaseStorageViewModel
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.bumptech.glide.Glide
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
 * Use the [FragmentAccountRank.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentAccountRank : Fragment(), OnUserRankItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentAccountRankBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val firebaseStorageViewModel : FirebaseStorageViewModel by viewModels()

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterUserRank

    private var imageViewDialog: ImageViewDialog? = null
    private var reportDialog : ReportDialog? = null
    lateinit var dbHandler : DBHelperReport

    private var selectedUserEx: UserExDTO? = null
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
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAccountRankBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        recyclerView = rootView.findViewById(R.id.rv_user_rank)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        dbHandler = DBHelperReport(requireContext())

        // ????????? ?????? ??????
        binding.layoutMenu.visibility = View.GONE

        refresh()
        observeUsers()

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editAboutMe.setOnTouchListener { _, _ ->
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
            false
        }

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            refresh()

            binding.swipeRefreshLayout.isRefreshing = false
            Toast.makeText(activity, "?????? ??????", Toast.LENGTH_SHORT).show()
        }

        binding.buttonClose.setOnClickListener {
            selectRecyclerView()
        }

        binding.buttonReport.setOnClickListener {
            val user = (activity as MainActivity?)?.getUser()!!

            if (reportDialog == null) {
                reportDialog = ReportDialog(requireContext())
                reportDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                reportDialog?.setCanceledOnTouchOutside(false)
            }
            reportDialog?.reportDTO = ReportDTO(user.uid, user.nickname, selectedUserEx?.userDTO?.uid, selectedUserEx?.userDTO?.nickname, selectedUserEx?.userDTO?.aboutMe, selectedUserEx?.userDTO?.uid, ReportDTO.Type.User)
            reportDialog?.show()
            reportDialog?.setInfo()

            reportDialog?.setOnDismissListener {
                if (!reportDialog?.reportDTO?.reason.isNullOrEmpty()) {
                    firebaseViewModel.sendReport(reportDialog?.reportDTO!!) {
                        if (!dbHandler.getBlock(reportDialog?.reportDTO?.contentDocName.toString())) {
                            dbHandler.updateBlock(reportDialog?.reportDTO?.contentDocName.toString(), 1)
                        } else {
                            dbHandler.updateBlock(reportDialog?.reportDTO?.contentDocName.toString(), 0)
                        }

                        selectedUserEx?.isBlocked = true
                        recyclerViewAdapter.notifyItemChanged(selectedPosition!!)
                        binding.buttonClose.performClick()
                        Toast.makeText(activity, "?????? ?????? ?????????????????????.", Toast.LENGTH_SHORT).show()
                    }
                }
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

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    private fun callBackPressed() {
        finishFragment()
    }

    private fun finishFragment() {
        val fragment = FragmentAccountInfo()
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
    }

    private fun refresh() {
        (activity as MainActivity?)?.loading()
        firebaseViewModel.getUsers()
        closeLayout()
    }

    private fun observeUsers() {
        firebaseViewModel.userDTOs.observe(viewLifecycleOwner) {
            setAdapter()
        }
    }

    private fun setAdapter() {
        var uidSet = hashSetOf<String>()
        val itemsEx: ArrayList<UserExDTO> = arrayListOf()
        for (user in firebaseViewModel.userDTOs.value!!) {
            itemsEx.add(UserExDTO(user, dbHandler.getBlock(user.uid.toString())))
            if (!user.uid.isNullOrEmpty()) {
                uidSet.add(user.uid.toString())
            }
        }

        var uriCheckIndex = 0
        for (uid in uidSet) {
            firebaseStorageViewModel.getUserProfileImage(uid) { uri ->
                if (uri != null) {
                    for (item in itemsEx) {
                        if (item.userDTO?.uid == uid) {
                            item.imgProfileUri = uri
                        }
                    }
                }
                uriCheckIndex++
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            while(true) {
                if (uriCheckIndex == uidSet.size) {
                    recyclerViewAdapter = RecyclerViewAdapterUserRank(itemsEx, this@FragmentAccountRank)
                    recyclerView.adapter = recyclerViewAdapter
                    (activity as MainActivity?)?.loadingEnd()
                    break
                }
                delay(100)
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
         * @return A new instance of fragment FragmentAccountRank.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentAccountRank().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setSelectUserInfo(item: UserExDTO) {
        if (selectedUserEx != null) {
            if (!selectedUserEx?.userDTO?.fanClubId.isNullOrEmpty()) {
                firebaseViewModel.getFanClub(selectedUserEx?.userDTO?.fanClubId.toString()) { fanClubDTO ->
                    if (fanClubDTO != null) {
                        binding.textFanClub.text = fanClubDTO.name
                        binding.imgFanClub.visibility = View.VISIBLE
                    }
                }
            } else {
                binding.textFanClub.text = "????????? ????????? ??????"
                binding.imgFanClub.visibility = View.GONE
            }

            if (item.imgProfileUri != null) {
                Glide.with(requireContext()).load(item.imgProfileUri).fitCenter().into(binding.imgProfile)
            } else {
                binding.imgProfile.setImageResource(R.drawable.profile)
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

            binding.textName.text = selectedUserEx?.userDTO?.nickname
            binding.textLevel.text = "Lv. ${selectedUserEx?.userDTO?.level}"
            binding.editAboutMe.setText(selectedUserEx?.userDTO?.aboutMe)

            binding.imgProfile.setOnClickListener {
                if (imageViewDialog == null) {
                    imageViewDialog = ImageViewDialog(requireContext())
                    imageViewDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    imageViewDialog?.setCanceledOnTouchOutside(false)
                }
                imageViewDialog?.imageUri = item.imgProfileUri
                imageViewDialog?.imageID = R.drawable.profile
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
        if (recyclerViewAdapter.selectItem(selectedPosition!!)) { // ?????? ??? ?????? ?????? ?????? ??? ???????????? ?????????
            openLayout()
        } else { // ?????? ??? ?????? ?????? ?????? ??? ???????????? ??????
            closeLayout()
        }
    }

    override fun onItemClick(item: UserExDTO, position: Int) {
        if (item.isBlocked) {
            Toast.makeText(activity, "????????? ????????? ?????????.", Toast.LENGTH_SHORT).show()
        } else {
            selectedUserEx = item
            selectedPosition = position
            setSelectUserInfo(item)
            selectRecyclerView()
        }
    }
}