package com.ados.myfanclub.page

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentFanClubCreateBinding
import com.ados.myfanclub.dialog.GemQuestionDialog
import com.ados.myfanclub.dialog.SelectFanClubSymbolDialog
import com.ados.myfanclub.model.*
import com.ados.myfanclub.util.Utility
import com.ados.myfanclub.viewmodel.FirebaseStorageViewModel
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentFanClubCreate.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFanClubCreate : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentFanClubCreateBinding? = null
    private val binding get() = _binding!!

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val firebaseStorageViewModel : FirebaseStorageViewModel by viewModels()

    private lateinit var callback: OnBackPressedCallback

    private var gemQuestionDialog: GemQuestionDialog? = null
    private var selectFanClubSymbolDialog: SelectFanClubSymbolDialog? = null

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null

    private var symbolImage: String = "reward_icon_25"
    private var symbolImageCustomBitmap: Bitmap? = null

    private var nameOK: Boolean = false
    private var descriptionOK: Boolean = false

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            symbolImageCustomBitmap = (activity as MainActivity?)?.getBitmap(uri)
            if (symbolImageCustomBitmap == null) {
                Toast.makeText(context, "???????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show()
            } else {
                binding.imgSymbol.setImageBitmap(symbolImageCustomBitmap)
            }
        } else {
            symbolImageCustomBitmap = null
            Toast.makeText(context, "???????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show()
        }
    }

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
        _binding = FragmentFanClubCreateBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editDescription.setOnTouchListener { _, _ ->
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
            false
        }

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.imgSymbol.setOnClickListener{
            if (selectFanClubSymbolDialog == null) {
                selectFanClubSymbolDialog = SelectFanClubSymbolDialog(requireContext())
                selectFanClubSymbolDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                selectFanClubSymbolDialog?.setCanceledOnTouchOutside(false)
            }
            selectFanClubSymbolDialog?.show()
            selectFanClubSymbolDialog?.setInfo()

            selectFanClubSymbolDialog?.setOnDismissListener {
                if (selectFanClubSymbolDialog?.isOK!! && !selectFanClubSymbolDialog?.selectedSymbol.isNullOrEmpty()) {
                    if (selectFanClubSymbolDialog?.isAddImage!!) {
                        resultLauncher.launch("image/*")
                    } else {
                        symbolImage = selectFanClubSymbolDialog?.selectedSymbol!!
                        symbolImageCustomBitmap = null
                        var imageID = requireContext().resources.getIdentifier(symbolImage, "drawable", requireContext().packageName)
                        binding.imgSymbol.setImageResource(imageID)
                    }
                }
            }
        }

        binding.buttonOk.setOnClickListener {
            val preferencesDTO = (activity as MainActivity?)?.getPreferences()!!
            val question = GemQuestionDTO("???????????? ????????? ???????????? ?????? ?????????.", preferencesDTO.priceFanClubCreate)
            if (gemQuestionDialog == null) {
                gemQuestionDialog = GemQuestionDialog(requireContext(), question)
                gemQuestionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                gemQuestionDialog?.setCanceledOnTouchOutside(false)
            } else {
                gemQuestionDialog?.question = question
            }
            gemQuestionDialog?.mainActivity = (activity as MainActivity?)
            gemQuestionDialog?.show()
            gemQuestionDialog?.setInfo()

            gemQuestionDialog?.binding?.buttonGemQuestionCancel?.setOnClickListener { // No
                gemQuestionDialog?.dismiss()
            }
            gemQuestionDialog?.binding?.buttonGemQuestionOk?.setOnClickListener { // Ok
                gemQuestionDialog?.dismiss()

                var user = (activity as MainActivity?)?.getUser()!!

                if ((user.paidGem!! + user.freeGem!!) < preferencesDTO.priceFanClubCreate!!) {
                    Toast.makeText(activity, "???????????? ???????????????.", Toast.LENGTH_SHORT).show()
                } else {
                    var name = binding.editName.text.toString().trim()
                    var description = binding.editDescription.text.toString().trim()

                    firebaseViewModel.isUsedFanClubName(name) { isUsed ->
                        if (isUsed) {
                            Toast.makeText(activity, "???????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show()
                        } else {
                            (activity as MainActivity?)?.loading()
                            val date = Date()
                            val docName = Utility.randomDocumentName()
                            // ????????? ?????? ??? ????????? 1????????? ????????? count ??? 1??? ??????
                            fanClubDTO = FanClubDTO(docName, name, null, description, "", symbolImage, null, null, 1, 0L, 0L, user.uid, user.nickname, 1, 0, date)
                            if (symbolImageCustomBitmap != null) {
                                fanClubDTO?.imgSymbolCustom = fanClubDTO?.getSymbolCustomImageName()
                            }

                            firebaseViewModel.updateFanClub(fanClubDTO!!) {
                                if (symbolImageCustomBitmap != null) { // ????????? ?????? ????????? ?????????
                                    firebaseStorageViewModel.setFanClubSymbolImage(docName, symbolImageCustomBitmap!!) {
                                        if (!it) {
                                            Toast.makeText(activity, "????????? ????????? ??????????????????. ?????? ??? ?????? ????????? ?????????.", Toast.LENGTH_SHORT).show()
                                        } else { // ????????? ????????? ??????
                                            // ???????????? ????????? ?????? ??? 100,000??? ???????????? ??????
                                            val member = MemberDTO(user.uid, user.nickname, user.level, user.aboutMe, 100000, MemberDTO.Position.MASTER, date, date, null, user.token)
                                            firebaseViewModel.updateMember(docName, member) {
                                                // ????????? ID ?????? ??? ?????? ?????? ????????? ?????????
                                                user.fanClubId = docName
                                                firebaseViewModel.updateUserFanClubApproval(user) {
                                                    // ????????? ??????
                                                    val oldPaidGemCount = user.paidGem!!
                                                    val oldFreeGemCount = user.freeGem!!
                                                    firebaseViewModel.useUserGem(user.uid.toString(), preferencesDTO.priceFanClubCreate) { userDTO->
                                                        if (userDTO != null) {
                                                            var log = LogDTO("[????????? ??????] ????????? ????????? ${preferencesDTO.priceFanClubCreate} ????????? ??????, ????????? ??? : $name($docName), (paidGem : $oldPaidGemCount -> ${userDTO.paidGem}, freeGem : $oldFreeGemCount -> ${userDTO.freeGem})", Date())
                                                            firebaseViewModel.writeUserLog(user.uid.toString(), log) { }

                                                            (activity as MainActivity?)?.loadingEnd()
                                                            Toast.makeText(activity, "????????? ?????? ??????!", Toast.LENGTH_SHORT).show()
                                                            //moveFanClubMain()
                                                        }
                                                    }
                                                }
                                            }
                                            (activity as MainActivity?)?.loadingEnd()
                                        }
                                    }
                                } else { // ????????? ?????? ????????? ??????
                                    // ???????????? ????????? ?????? ??? 100,000??? ???????????? ??????
                                    val member = MemberDTO(user.uid, user.nickname, user.level, user.aboutMe, 100000, MemberDTO.Position.MASTER, date, date, null, user.token)
                                    firebaseViewModel.updateMember(docName, member) {
                                        // ????????? ID ?????? ??? ?????? ?????? ????????? ?????????
                                        user.fanClubId = docName
                                        firebaseViewModel.updateUserFanClubApproval(user) {
                                            // ????????? ??????
                                            val oldPaidGemCount = user.paidGem!!
                                            val oldFreeGemCount = user.freeGem!!
                                            firebaseViewModel.useUserGem(user.uid.toString(), preferencesDTO.priceFanClubCreate) { userDTO->
                                                if (userDTO != null) {
                                                    var log = LogDTO("[????????? ??????] ????????? ????????? ${preferencesDTO.priceFanClubCreate} ????????? ??????, ????????? ??? : $name($docName), (paidGem : $oldPaidGemCount -> ${userDTO.paidGem}, freeGem : $oldFreeGemCount -> ${userDTO.freeGem})", Date())
                                                    firebaseViewModel.writeUserLog(user.uid.toString(), log) { }

                                                    (activity as MainActivity?)?.loadingEnd()
                                                    Toast.makeText(activity, "????????? ?????? ??????!", Toast.LENGTH_SHORT).show()
                                                    //moveFanClubMain()
                                                }
                                            }
                                        }
                                    }
                                    (activity as MainActivity?)?.loadingEnd()
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.editName.doAfterTextChanged {
            var name = binding.editName.text.toString().trim()
            if (!isValidName(name)) {
                binding.textNameError.text = "????????? ??? ?????? ???????????? ???????????? ????????????."
                binding.editName.setBackgroundResource(R.drawable.edit_rectangle_red)
                nameOK = false
            } else {
                binding.textNameError.text = ""
                binding.editName.setBackgroundResource(R.drawable.edit_rectangle)
                nameOK = true
            }
            binding.textNameLen.text = "${binding.editName.text.length}/30"
            visibleOkButton()
        }

        binding.editDescription.doAfterTextChanged {
            var description = binding.editDescription.text.toString().trim()
            descriptionOK = if (description.isNullOrEmpty()) {
                binding.editDescription.setBackgroundResource(R.drawable.edit_rectangle_red)
                false
            } else {
                binding.editDescription.setBackgroundResource(R.drawable.edit_rectangle)
                true
            }
            binding.textDescriptionLen.text = "${binding.editDescription.text.length}/600"
            visibleOkButton()
        }
    }

    private fun createFanClub() {

    }

    private fun visibleOkButton() {
        binding.buttonOk.isEnabled = nameOK && descriptionOK
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

    private fun moveFanClubMain() {
        //val fragment = FragmentFanClubMain()
        val fragment = FragmentFanClubMain()
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
    }

    private fun isValidName(name: String) : Boolean {
        val exp = Regex("^[???-??????-???a-zA-Z0-9.~!@#\$%^&*\\[\\](){}|_ -]{1,30}\$")
        return !name.isNullOrEmpty() && exp.matches(name)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentFanClubCreate.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentFanClubCreate().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}