package com.ados.myfanclub.page

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentFanClubInitalizeBinding
import com.ados.myfanclub.dialog.GetItemDialog
import com.ados.myfanclub.dialog.LevelUpActionFanClubDialog
import com.ados.myfanclub.model.LogDTO
import com.ados.myfanclub.model.MailDTO
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.snackbar.Snackbar
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFanClubInitalize.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFanClubInitalize : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentFanClubInitalizeBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

    private val firebaseViewModel : FirebaseViewModel by viewModels()

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
        _binding = FragmentFanClubInitalizeBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

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

        binding.buttonCreate.setOnClickListener {
            val user = (activity as MainActivity?)?.getUser()
            if (user?.level!! < 7) {
                Toast.makeText(context, "????????? ????????? ?????? [ 7 ] ?????? ???????????????.", Toast.LENGTH_SHORT).show()
            } else {
                val fragment = FragmentFanClubCreate()
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.layout_fragment, fragment)
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    addToBackStack(null)
                    commit()
                }
            }
        }

        binding.buttonJoin.setOnClickListener {
            val user = (activity as MainActivity?)?.getUser()
            if (user?.level!! < 3) {
                Toast.makeText(context, "????????? ????????? ?????? [ 3 ] ?????? ???????????????.", Toast.LENGTH_SHORT).show()
            } else {
                val fragment = FragmentFanClubJoin()
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.layout_fragment, fragment)
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    addToBackStack(null)
                    commit()
                }
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
         * @return A new instance of fragment FragmentFanClubInitalize.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentFanClubInitalize().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun observeTutorial() {
        (activity as MainActivity?)?.getTutorialStep()?.observe(viewLifecycleOwner) {
            onTutorial((activity as MainActivity?)?.getTutorialStep()?.value!!)
        }
    }

    private fun onTutorial(step: Int) {
        when (step) {
            21 -> {
                println("???????????? Step - $step")
                TapTargetSequence(requireActivity())
                    .targets(
                        TapTarget.forBounds((activity as MainActivity?)?.getMainLayoutRect(),
                            "???????????? ????????? ?????? ??? ????????? ????????? ???????????????.",
                            "- OK ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(requireContext(), R.drawable.ok))
                            .targetRadius(65)
                            .tintTarget(true),
                        TapTarget.forView(binding.buttonJoin,
                            "????????? ????????? ?????? [ 3 ] ?????? ??? ???????????????.",
                            "- ?????? ????????? ???????????? ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .targetRadius(100)
                            .tintTarget(true),
                        TapTarget.forView(binding.buttonCreate,
                            "????????? ????????? ?????? [ 7 ] ?????? ??? ???????????????.",
                            "- ????????? ????????? ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .targetRadius(100)
                            .tintTarget(true),
                        TapTarget.forView(binding.layoutMain,
                            "?????? ????????? ???????????? ?????????????????? ????????? ??? ????????????.",
                            "- OK ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(requireContext(), R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.layoutMain,
                            "?????? ?????? ?????????????????? ?????? ????????? ???????????? [ ??????????????? ]??? ???????????????!!",
                            "- OK ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(requireContext(), R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.layoutMain,
                            "??????????????? ?????????????????????! ???????????? ????????? ?????? ?????????!",
                            "- OK ????????? ???????????????.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(requireContext(), R.drawable.ok))
                            .tintTarget(true)).listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            (activity as MainActivity?)?.finishTutorialStep(true) // ???????????? ??????
                            rewardTutorialGem()
                        }
                        override fun onSequenceStep(tutorialStep: TapTarget, targetClicked: Boolean) {

                        }
                        override fun onSequenceCanceled(lastTarget: TapTarget) {

                        }
                    }).start()
            }
        }
    }

    private fun rewardTutorialGem() {
        val user = (activity as MainActivity?)?.getUser()!!
        val gemCount = (activity as MainActivity?)?.getPreferences()?.rewardTutorialGem!!
        val oldFreeGemCount = user.freeGem!!
        firebaseViewModel.addUserGem(user.uid.toString(), 0, gemCount) { userDTO ->
            if (userDTO != null) {
                var log = LogDTO("[???????????? ?????? ????????? ??????] ????????? $gemCount ?????? (freeGem : $oldFreeGemCount -> ${userDTO.freeGem})", Date())
                firebaseViewModel.writeUserLog(user.uid.toString(), log) { }

                val getDialog = GetItemDialog(requireContext())
                getDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                getDialog.setCanceledOnTouchOutside(false)
                getDialog.mailDTO = MailDTO("", "", "", "", MailDTO.Item.FREE_GEM, gemCount)
                getDialog.show()

                getDialog.binding.buttonGetItemOk.setOnClickListener {
                    getDialog.dismiss()

                }
            }
        }
    }
}