package com.ados.myfanclub.page

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentScheduleListBinding
import com.ados.myfanclub.dialog.QuestionDialog
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.MemberDTO
import com.ados.myfanclub.model.QuestionDTO
import com.ados.myfanclub.model.ScheduleDTO
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.material.snackbar.Snackbar

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
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentScheduleListBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

    private val firebaseViewModel : FirebaseViewModel by viewModels()

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterSchedule
    lateinit var itemTouchHelper : ItemTouchHelper

    private var questionDialog: QuestionDialog? = null

    private var schedulesBackup : ArrayList<ScheduleDTO> = arrayListOf()
    private var selectedSchedule: ScheduleDTO? = null
    private var selectedPosition: Int? = 0
    private var isAddedTutorialSampleData = false // ???????????? ?????? ???????????? ?????? ???????????? ????????? ??????
    private var isReorder = false // ????????? ?????? ?????? ????????? ??????

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)

            if (param1.equals("fanClub")) {
                fanClubDTO = (activity as MainActivity?)?.getFanClub()
                currentMember = (activity as MainActivity?)?.getMember()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentScheduleListBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        recyclerView = rootView.findViewById(R.id.rv_schedule)as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // ????????? ?????? ??????
        binding.layoutMenu.visibility = View.GONE
        binding.layoutMenuReorder.visibility = View.GONE

        if (fanClubDTO == null) {
            getPersonalSchedule()
        } else {
            getFanClubSchedule()
        }
        observeSchedules()

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

        observeTutorial()

        if (fanClubDTO != null) {
            binding.textPageTitle.text = ""
        }

        binding.buttonAddSchedule.setOnClickListener {
            if (binding.layoutMenu.visibility == View.GONE) { // ?????? ????????? ????????? ????????????
                addSchedule()
            }
        }

        binding.buttonReorder.setOnClickListener {
            if (binding.layoutMenu.visibility == View.GONE) { // ?????? ????????? ????????? ????????????
                visibleReorder()

                // ?????? ???????????? ?????? ????????? ?????? ????????? ??????
                schedulesBackup.clear()
                schedulesBackup.addAll(firebaseViewModel.scheduleDTOs.value!!)

                recyclerViewAdapter.showReorderIcon = true
                recyclerViewAdapter.notifyDataSetChanged()
            }
        }

        binding.buttonModify.setOnClickListener {
            moveScheduleAdd(true)
        }

        binding.buttonDelete.setOnClickListener {
            val question = QuestionDTO(
                QuestionDTO.Stat.WARNING,
                "????????? ??????",
                "???????????? ???????????? ????????? ??? ????????????.\n?????? ?????? ???????????????????",
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
            questionDialog?.binding?.buttonQuestionCancel?.setOnClickListener { // No
                questionDialog?.dismiss()
                questionDialog = null
            }
            questionDialog?.binding?.buttonQuestionOk?.setOnClickListener { // Ok
                questionDialog?.dismiss()
                questionDialog = null

                if (fanClubDTO == null) {
                    setPersonalScheduleDelete()
                } else {
                    setFanClubScheduleDelete()
                }
            }
        }

        binding.buttonMenuCancel.setOnClickListener {
            selectRecyclerView()
        }

        binding.buttonReorderOk.setOnClickListener {
            disableReorder()

            recyclerViewAdapter.showReorderIcon = false
            recyclerViewAdapter.notifyDataSetChanged()


            if (fanClubDTO == null) {
                setPersonalScheduleOrder()
            } else {
                setFanClubScheduleOrder()
            }
        }

        binding.buttonReorderCancel.setOnClickListener {
            disableReorder()

            // ?????? ????????? ??????
            firebaseViewModel.scheduleDTOs.value!!.clear()
            firebaseViewModel.scheduleDTOs.value!!.addAll(schedulesBackup)
            setAdapter()
        }
    }

    private fun getPersonalSchedule() {
        val user = (activity as MainActivity?)?.getUser()!!
        firebaseViewModel.getPersonalSchedules(user.uid.toString())
    }

    private fun getFanClubSchedule() {
        firebaseViewModel.getFanClubSchedulesListen(fanClubDTO?.docName.toString())
    }

    private fun observeSchedules() {
        firebaseViewModel.scheduleDTOs.observe(viewLifecycleOwner) {
            setAdapter()
        }
    }

    private fun setPersonalScheduleOrder() {
        val user = (activity as MainActivity?)?.getUser()!!
        for (schedule in firebaseViewModel.scheduleDTOs.value!!) {
            println("????????? ?????? $schedule")
            firebaseViewModel.updatePersonalScheduleOrder(user.uid.toString(), schedule) { }
        }
    }

    private fun setFanClubScheduleOrder() {
        for (schedule in firebaseViewModel.scheduleDTOs.value!!) {
            println("????????? ????????? ?????? $schedule")
            firebaseViewModel.updateFanClubScheduleOrder(fanClubDTO?.docName.toString(), schedule) { }
        }
    }

    private fun setPersonalScheduleDelete() {
        val user = (activity as MainActivity?)?.getUser()!!
        firebaseViewModel.deletePersonalSchedule(user.uid.toString(), selectedSchedule?.docName.toString()) {
            firebaseViewModel.scheduleDTOs.value!!.remove(selectedSchedule)
            setAdapter()
            hideMenu()
            // ?????? ??? ?????? ??????
        }
    }

    private fun setFanClubScheduleDelete() {
        firebaseViewModel.deleteFanClubSchedule(fanClubDTO?.docName.toString(), selectedSchedule?.docName.toString()) {
            firebaseViewModel.scheduleDTOs.value!!.remove(selectedSchedule)
            setAdapter()
            hideMenu()
            // ?????? ??? ?????? ??????
        }
    }

    private fun setAdapter() {
        if (_binding != null) {
            recyclerViewAdapter = RecyclerViewAdapterSchedule(firebaseViewModel.scheduleDTOs.value!!, this, this)
            recyclerView.adapter = recyclerViewAdapter

            val swipeHelperCallback = SwipeHelperCallback(recyclerViewAdapter)
            itemTouchHelper = ItemTouchHelper(swipeHelperCallback)
            itemTouchHelper.attachToRecyclerView(recyclerView)

            if (fanClubDTO == null) {
                val user = (activity as MainActivity?)?.getUser()!!
                binding.textScheduleCount.text = "${firebaseViewModel.scheduleDTOs.value!!.size}/${user.getScheduleCount()}"
            } else {
                binding.textScheduleCount.text = "${firebaseViewModel.scheduleDTOs.value!!.size}/${fanClubDTO?.getScheduleCount()}"
            }
        }
    }

    private fun visibleReorder() {
        isReorder = true
        binding.buttonAddSchedule.visibility = View.GONE
        binding.buttonReorder.visibility = View.GONE
        binding.layoutMenuModify.visibility = View.GONE

        val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
        binding.layoutMenu.visibility = View.VISIBLE
        binding.layoutMenuReorder.visibility = View.VISIBLE
        binding.layoutMenu.startAnimation(translateUp)
    }

    private fun disableReorder() {
        isReorder = false
        binding.buttonAddSchedule.visibility = View.VISIBLE
        binding.buttonReorder.visibility = View.VISIBLE
        binding.layoutMenuModify.visibility = View.VISIBLE

        val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
        binding.layoutMenu.visibility = View.GONE
        binding.layoutMenuReorder.visibility = View.GONE
        binding.layoutMenu.startAnimation(translateDown)
    }

    private fun moveScheduleAdd(isModify: Boolean = false) {
        val fragment = FragmentScheduleAdd.newInstance(param1!!, param2!!)
        if (isModify) {
            fragment.scheduleDTO = selectedSchedule!!
            fragment.isAddedTutorialSampleData = isAddedTutorialSampleData
        }

        //parentFragmentManager.popBackStackImmediate("scheduleAdd", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        /*parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            addToBackStack("scheduleAdd")
            commit()
        }*/
        parentFragmentManager.beginTransaction().replace(R.id.layout_fragment, fragment).commit()
    }

    private fun addSchedule(isTutorial: Boolean = false) {
        if (fanClubDTO != null) { // ????????? ????????? ????????? ????????? ??????????????? ??????
            if (!(parentFragment as FragmentPageSchedule?)?.isRemoveAdmin()!!) {
                if (firebaseViewModel.scheduleDTOs.value!!.size >= fanClubDTO?.getScheduleCount()!!) {
                    Toast.makeText(activity, "???????????? ??? ?????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show()
                } else {
                    firebaseViewModel.stopFanClubSchedulesListen() // ???????????? listen ?????? ????????? ?????? listen stop ??????
                    moveScheduleAdd()
                }
            }
        } else {
            val user = (activity as MainActivity?)?.getUser()!!
            // ???????????? ????????? ????????? ?????? ?????? ??????
            if (!isTutorial && firebaseViewModel.scheduleDTOs.value!!.size >= user.getScheduleCount()) {
                Toast.makeText(activity, "???????????? ??? ?????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show()
            } else {
                moveScheduleAdd()
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
         * @return A new instance of fragment FragmentScheduleList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentScheduleList().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun hideMenu() {
        val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
        binding.layoutMenu.visibility = View.GONE
        binding.layoutMenu.startAnimation(translateDown)
        //recyclerView.smoothSnapToPosition(position)
    }

    private fun selectRecyclerView() {
        if (recyclerViewAdapter.selectItem(selectedPosition!!)) { // ?????? ??? ?????? ?????? ?????? ??? ???????????? ?????????
            val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
            binding.layoutMenu.visibility = View.VISIBLE
            binding.layoutMenuModify.visibility = View.VISIBLE
            binding.layoutMenu.startAnimation(translateUp)
            //recyclerView.smoothSnapToPosition(position)
        } else { // ?????? ??? ?????? ?????? ?????? ??? ???????????? ??????
            hideMenu()
        }
    }

    override fun onItemClick(item: ScheduleDTO, position: Int) {
        if (!isReorder) { // ?????? ?????? ????????? ???????????? ??????
            if (fanClubDTO != null) { // ????????? ????????? ????????? ????????? ??????????????? ??????
                if (!(parentFragment as FragmentPageSchedule?)?.isRemoveAdmin()!!) {
                    selectedSchedule = item
                    selectedPosition = position
                    selectRecyclerView()
                }
            } else {
                selectedSchedule = item
                selectedPosition = position
                selectRecyclerView()
            }
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

    private fun observeTutorial() {
        (activity as MainActivity?)?.getTutorialStep()?.observe(viewLifecycleOwner) {
            onTutorial((activity as MainActivity?)?.getTutorialStep()?.value!!)
        }
    }

    private fun onTutorial(step: Int) {
        when (step) {
            2 -> {
                println("???????????? Step - $step")
                TapTargetSequence(requireActivity())
                    .targets(
                        TapTarget.forBounds((activity as MainActivity?)?.getMainLayoutRect(),
                            "???????????? ?????? ????????? ?????? ??? ??????, ????????? ???????????????.",
                            "- OK ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(requireContext(), R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.buttonAddSchedule,
                            "?????? ?????? ???????????? ????????? ???????????????.",
                            "- ????????? ?????? ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .tintTarget(true)).listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            for (i in 0 until firebaseViewModel.scheduleDTOs.value!!.size) {
                                if (isSampleData(firebaseViewModel.scheduleDTOs.value!![i])) {
                                    isAddedTutorialSampleData = true
                                    break
                                }
                            }

                            addSchedule(true)
                            (activity as MainActivity?)?.addTutorialStep()
                        }
                        override fun onSequenceStep(tutorialStep: TapTarget, targetClicked: Boolean) {
                            //Toast.makeText(secondActivity.this,"GREAT!",Toast.LENGTH_SHORT).show();

                        }
                        override fun onSequenceCanceled(lastTarget: TapTarget) {

                        }
                    }).start()
            }
            5-> {
                println("???????????? Step - $step")
                val location = IntArray(2)
                val width = binding.layoutCount.width.div(3)
                binding.layoutCount.getLocationOnScreen(location)
                val rect = Rect(location[0], location[1], location[0] + width, location[1] + binding.layoutCount.height.times(4))
                TapTargetView.showFor(requireActivity(),
                    TapTarget.forBounds(rect,
                            "?????? ???????????? ?????????????????????.",
                            "- ????????? ???????????? ??????????????? ??????, ??????, ??????, ????????? ???????????? ????????? ??? ????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .tintTarget(true),object : TapTargetView.Listener() {
                        // The listener can listen for regular clicks, long clicks or cancels
                        override fun onTargetClick(view: TapTargetView) {
                            super.onTargetClick(view) // This call is optional

                            (activity as MainActivity?)?.addTutorialStep()
                        }
                    })
            }
        }
    }

    private fun isSampleData(item: ScheduleDTO) : Boolean {
        return item.title.equals("?????? ?????? ???????????? ??????!") &&
                item.purpose.equals("????????? 5??? ??? ?????? ???????????? ??????!\n\n??? ????????? ?????? ?????? ?????????!") &&
                item.count == 5L &&
                item.action == ScheduleDTO.Action.APP &&
                item.appDTO?.appName.equals("??????")
    }
}