package com.ados.myfanclub.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentScheduleListBinding
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.MemberDTO
import com.ados.myfanclub.model.ScheduleDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentScheduleList.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentScheduleList : Fragment(), OnScheduleItemClickListener, OnStartDragListener {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentScheduleListBinding? = null
    private val binding get() = _binding!!

    private var firebaseAuth : FirebaseAuth? = null
    private var firestore : FirebaseFirestore? = null

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterSchedule
    lateinit var itemTouchHelper : ItemTouchHelper

    private var schedules : ArrayList<ScheduleDTO> = arrayListOf()
    private var schedulesBackup : ArrayList<ScheduleDTO> = arrayListOf()
    private var selectedSchedule: ScheduleDTO? = null

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
        _binding = FragmentScheduleListBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        recyclerView = rootView.findViewById(R.id.rv_schedule!!)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 메뉴는 기본 숨김
        binding.layoutMenu.visibility = View.GONE
        binding.layoutMenuReorder.visibility = View.GONE

        if (fanClubDTO == null) {
            getPersonalSchedule()
        } else {
            getFanClubSchedule()
        }

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (fanClubDTO != null) {
            binding.textPageTitle.text = ""
        }

        binding.buttonAddSchedule.setOnClickListener {
            val fragment = FragmentScheduleAdd.newInstance(fanClubDTO, currentMember)
            parentFragmentManager.beginTransaction().apply{
                replace(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
                commit()
            }
        }

        binding.buttonReorder.setOnClickListener {
            visibleReorder()

            // 순서 편집하기 전에 복원을 위해 데이터 백업
            schedulesBackup.clear()
            schedulesBackup.addAll(schedules)

            recyclerViewAdapter.showReorderIcon = true
            recyclerViewAdapter.notifyDataSetChanged()
        }

        binding.buttonModify.setOnClickListener {
            val fragment = FragmentScheduleAdd.newInstance(fanClubDTO, currentMember)
            fragment.scheduleDTO = selectedSchedule!!
            parentFragmentManager.beginTransaction().apply{
                replace(R.id.layout_fragment, fragment)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                addToBackStack(null)
                commit()
            }
        }

        binding.buttonDelete.setOnClickListener {

        }

        binding.buttonOk.setOnClickListener {
            disableReorder()

            recyclerViewAdapter.showReorderIcon = false
            recyclerViewAdapter.notifyDataSetChanged()


            if (fanClubDTO == null) {
                setPersonalScheduleOrder()
            } else {
                setFanClubScheduleOrder()
            }
        }

        binding.buttonCancel.setOnClickListener {
            disableReorder()

            // 원본 데이터 복원
            schedules.clear()
            schedules.addAll(schedulesBackup)
            setAdapter()
        }
    }

    private fun getPersonalSchedule() {
        firestore?.collection("user")?.document(firebaseAuth?.currentUser?.uid.toString())?.collection("schedule")?.orderBy("order", Query.Direction.ASCENDING)?.get()?.addOnSuccessListener { result ->
            println("스케줄 호출")
            schedules.clear()
            for (document in result) {
                var schedule = document.toObject(ScheduleDTO::class.java)!!
                schedules.add(schedule)
            }
            setAdapter()
        }?.addOnFailureListener { exception ->

        }
    }

    private fun getFanClubSchedule() {
        firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.collection("schedule")?.orderBy("order", Query.Direction.ASCENDING)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            println("팬클럽 스케줄 호출")
            schedules.clear()
            if(querySnapshot == null)return@addSnapshotListener
            for(snapshot in querySnapshot){
                var schedule = snapshot.toObject(ScheduleDTO::class.java)!!
                schedules.add(schedule)
            }
            setAdapter()
        }
        /*firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.collection("schedule")?.orderBy("order", Query.Direction.ASCENDING)?.get()?.addOnSuccessListener { result ->
            println("팬클럽 스케줄 호출")
            schedules.clear()
            for (document in result) {
                var schedule = document.toObject(ScheduleDTO::class.java)!!
                schedules.add(schedule)
            }
            setAdapter()
        }?.addOnFailureListener { exception ->

        }*/
    }

    private fun setPersonalScheduleOrder() {
        for (schedule in schedules) {
            println("스케줄 오더 $schedule")
            firestore?.collection("user")?.document(firebaseAuth?.currentUser?.uid.toString())?.collection("schedule")?.document(schedule.docName.toString())?.set(schedule)?.addOnCompleteListener {

            }
        }
    }

    private fun setFanClubScheduleOrder() {
        for (schedule in schedules) {
            println("팬클럽 스케줄 오더 $schedule")
            firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.collection("schedule")?.document(schedule.docName.toString())?.set(schedule)?.addOnCompleteListener {

            }
        }
    }

    private fun setAdapter() {
        recyclerViewAdapter = RecyclerViewAdapterSchedule(schedules, this, this)
        recyclerView.adapter = recyclerViewAdapter

        val swipeHelperCallback = SwipeHelperCallback(recyclerViewAdapter)
        itemTouchHelper = ItemTouchHelper(swipeHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun visibleReorder() {
        binding.buttonAddSchedule.visibility = View.GONE
        binding.buttonReorder.visibility = View.GONE
        binding.layoutMenuModify.visibility = View.GONE

        val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
        binding.layoutMenu.visibility = View.VISIBLE
        binding.layoutMenuReorder.visibility = View.VISIBLE
        binding.layoutMenu.startAnimation(translateUp)
    }

    private fun disableReorder() {
        binding.buttonAddSchedule.visibility = View.VISIBLE
        binding.buttonReorder.visibility = View.VISIBLE
        binding.layoutMenuModify.visibility = View.VISIBLE

        val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
        binding.layoutMenu.visibility = View.GONE
        binding.layoutMenuReorder.visibility = View.GONE
        binding.layoutMenu.startAnimation(translateDown)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentScheduleList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: FanClubDTO?, param2: MemberDTO?) =
            FragmentScheduleList().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putParcelable(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: ScheduleDTO, position: Int) {
        if (recyclerViewAdapter?.selectItem(position)) { // 선택 일 경우 메뉴 표시 및 레이아웃 어둡게
            val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
            binding.layoutMenu.visibility = View.VISIBLE
            binding.layoutMenuModify.visibility = View.VISIBLE
            binding.layoutMenu.startAnimation(translateUp)
            //recyclerView.smoothSnapToPosition(position)
            selectedSchedule = item
        } else { // 해제 일 경우 메뉴 숨김 및 레이아웃 밝게
            val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
            binding.layoutMenu.visibility = View.GONE
            binding.layoutMenu.startAnimation(translateDown)
            //recyclerView.smoothSnapToPosition(position)
        }

    }

    fun RecyclerView.smoothSnapToPosition(position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_START) {
        val smoothScroller = object : LinearSmoothScroller(this.context) {
            override fun getVerticalSnapPreference(): Int = snapMode
            override fun getHorizontalSnapPreference(): Int = snapMode
        }
        smoothScroller.targetPosition = position
        layoutManager?.startSmoothScroll(smoothScroller)
    }

    override fun onStartDrag(holder: RecyclerViewAdapterSchedule.ViewHolder) {
        itemTouchHelper.startDrag(holder)
    }
}