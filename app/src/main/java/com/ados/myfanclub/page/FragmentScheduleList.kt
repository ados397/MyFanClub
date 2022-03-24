package com.ados.myfanclub.page

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
    private var isAddedTutorialSampleData = true // 튜토리얼 샘플 데이터가 이미 추가되어 있는지 확인

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

        // 메뉴는 기본 숨김
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeTutorial()

        if (fanClubDTO != null) {
            binding.textPageTitle.text = ""
        }

        binding.buttonAddSchedule.setOnClickListener {
            addSchedule()
        }

        binding.buttonReorder.setOnClickListener {
            visibleReorder()

            // 순서 편집하기 전에 복원을 위해 데이터 백업
            schedulesBackup.clear()
            schedulesBackup.addAll(firebaseViewModel.scheduleDTOs.value!!)

            recyclerViewAdapter.showReorderIcon = true
            recyclerViewAdapter.notifyDataSetChanged()
        }

        binding.buttonModify.setOnClickListener {
            moveScheduleAdd(true)
        }

        binding.buttonDelete.setOnClickListener {
            val question = QuestionDTO(
                QuestionDTO.Stat.WARNING,
                "스케줄 삭제",
                "스케줄을 삭제하면 되돌릴 수 없습니다.\n정말 삭제 하시겠습니까?",
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

            // 원본 데이터 복원
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
            println("스케줄 오더 $schedule")
            firebaseViewModel.updatePersonalScheduleOrder(user.uid.toString(), schedule) { }
        }
    }

    private fun setFanClubScheduleOrder() {
        for (schedule in firebaseViewModel.scheduleDTOs.value!!) {
            println("팬클럽 스케줄 오더 $schedule")
            firebaseViewModel.updateFanClubScheduleOrder(fanClubDTO?.docName.toString(), schedule) { }
        }
    }

    private fun setPersonalScheduleDelete() {
        val user = (activity as MainActivity?)?.getUser()!!
        firebaseViewModel.deletePersonalSchedule(user.uid.toString(), selectedSchedule?.docName.toString()) {
            firebaseViewModel.scheduleDTOs.value!!.remove(selectedSchedule)
            setAdapter()
            hideMenu()
            // 필요 시 로그 추가
        }
    }

    private fun setFanClubScheduleDelete() {
        firebaseViewModel.deleteFanClubSchedule(fanClubDTO?.docName.toString(), selectedSchedule?.docName.toString()) {
            firebaseViewModel.scheduleDTOs.value!!.remove(selectedSchedule)
            setAdapter()
            hideMenu()
            // 필요 시 로그 추가
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
        if (fanClubDTO != null) { // 팬클럽 일때는 관리자 권한이 없어졌는지 확인
            if (!(parentFragment as FragmentPageSchedule?)?.isRemoveAdmin()!!) {
                if (firebaseViewModel.scheduleDTOs.value!!.size >= fanClubDTO?.getScheduleCount()!!) {
                    Toast.makeText(activity, "스케줄을 더 이상 추가할 수 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    firebaseViewModel.stopFanClubSchedulesListen() // 중복으로 listen 하지 않도록 기존 listen stop 처리
                    moveScheduleAdd()
                }
            }
        } else {
            val user = (activity as MainActivity?)?.getUser()!!
            // 튜토리얼 일때는 스케줄 제한 없이 추가
            if (!isTutorial && firebaseViewModel.scheduleDTOs.value!!.size >= user.getScheduleCount()) {
                Toast.makeText(activity, "스케줄을 더 이상 추가할 수 없습니다.", Toast.LENGTH_SHORT).show()
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
        if (recyclerViewAdapter.selectItem(selectedPosition!!)) { // 선택 일 경우 메뉴 표시 및 레이아웃 어둡게
            val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
            binding.layoutMenu.visibility = View.VISIBLE
            binding.layoutMenuModify.visibility = View.VISIBLE
            binding.layoutMenu.startAnimation(translateUp)
            //recyclerView.smoothSnapToPosition(position)
        } else { // 해제 일 경우 메뉴 숨김 및 레이아웃 밝게
            hideMenu()
        }
    }

    override fun onItemClick(item: ScheduleDTO, position: Int) {
        if (fanClubDTO != null) { // 팬클럽 일때는 관리자 권한이 없어졌는지 확인
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
                println("튜토리얼 Step - $step")
                TapTargetSequence(requireActivity())
                    .targets(
                        TapTarget.forBounds((activity as MainActivity?)?.getMainLayoutRect(),
                            "여기에서 개인 스케줄 등록 및 수정, 삭제가 가능합니다.",
                            "- OK 버튼을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(requireContext(), R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.buttonAddSchedule,
                            "그럼 개인 스케줄을 등록해 보겠습니다.",
                            "- 스케줄 등록 버튼을 눌러주세요.") // All options below are optional
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
                println("튜토리얼 Step - $step")
                val location = IntArray(2)
                val width = binding.layoutCount.width.div(3)
                binding.layoutCount.getLocationOnScreen(location)
                val rect = Rect(location[0], location[1], location[0] + width, location[1] + binding.layoutCount.height.times(4))
                TapTargetView.showFor(requireActivity(),
                    TapTarget.forBounds(rect,
                            "일일 스케줄이 등록되었습니다.",
                            "- 등록된 스케줄의 아이콘으로 일일, 주간, 월간, 기간내 스케줄을 구분할 수 있습니다.") // All options below are optional
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
        return item.title.equals("멜론 노래 스트리밍 하기!") &&
                item.purpose.equals("하루에 5번 씩 멜론 스트리밍 하기!\n\n내 가수를 위해 꼭꼭 지키기!") &&
                item.count == 5L &&
                item.action == ScheduleDTO.Action.APP &&
                item.appDTO?.appName.equals("멜론")
    }
}