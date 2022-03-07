package com.ados.myfanclub.page

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.MySharedPreferences
import com.ados.myfanclub.R
import com.ados.myfanclub.database.DBHelperReport
import com.ados.myfanclub.databinding.FragmentChatBinding
import com.ados.myfanclub.dialog.ReportDialog
import com.ados.myfanclub.model.*
import com.ados.myfanclub.util.Utility
import com.ados.myfanclub.viewmodel.FirebaseStorageViewModel
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import kotlinx.android.synthetic.main.report_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentChat.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentChat : Fragment(), OnChatItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: Int? = null
    private var param2: String? = null

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val firebaseStorageViewModel : FirebaseStorageViewModel by viewModels()

    lateinit var recyclerViewAdapter : RecyclerViewAdapterChat

    private var fanClubDTO: FanClubDTO? = null
    private var currentMember: MemberDTO? = null
    private var toast : Toast? = null

    private var reportDialog : ReportDialog? = null
    lateinit var dbHandler : DBHelperReport

    private val sharedPreferences: MySharedPreferences by lazy {
        MySharedPreferences(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        fanClubDTO = (activity as MainActivity?)?.getFanClub()
        currentMember = (activity as MainActivity?)?.getMember()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        binding.rvChat.layoutManager = layoutManager

        dbHandler = DBHelperReport(requireContext())

        firebaseViewModel.getFanClubChatsListen(fanClubDTO?.docName.toString())
        firebaseViewModel.fanClubChatDTOs.observe(viewLifecycleOwner) {
            var uidSet = hashSetOf<String>()
            val itemsEx: ArrayList<DisplayBoardExDTO> = arrayListOf()
            for (chat in firebaseViewModel.fanClubChatDTOs.value!!) {
                itemsEx.add(DisplayBoardExDTO(chat, dbHandler?.getBlock(chat.docName.toString())))
                if (!chat.userUid.isNullOrEmpty()) {
                    uidSet.add(chat.userUid.toString())
                }
            }

            var uriCheckIndex = 0
            for (uid in uidSet) {
                firebaseStorageViewModel.getUserProfile(uid) { uri ->
                    if (uri != null) {
                        for (item in itemsEx) {
                            if (item.displayBoardDTO?.userUid == uid) {
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
                        recyclerViewAdapter = RecyclerViewAdapterChat(itemsEx, currentMember!!, this@FragmentChat)
                        binding.rvChat.adapter = recyclerViewAdapter
                        binding.rvChat.scrollToPosition(0)
                        break
                    }
                    delay(100)
                }
            }
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSend.isEnabled = false

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.buttonSend.setOnClickListener {
            val delay = (activity as MainActivity?)?.getPreferences()?.fanClubChatSendDelay!!
            val sendTime = sharedPreferences.getLong(MySharedPreferences.PREF_KEY_FAN_CLUB_CHAT_SEND_TIME, 0)
            if (TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis() - sendTime)) < delay) {
                callToast("메시지는 ${delay}초 마다 한번씩 보낼 수 있습니다.")
            } else {
                sharedPreferences.putLong(MySharedPreferences.PREF_KEY_FAN_CLUB_CHAT_SEND_TIME, System.currentTimeMillis())

                val user = (activity as MainActivity?)?.getUser()!!
                val docName = Utility.randomDocumentName()
                val chat = DisplayBoardDTO(docName, binding.editContent.text.toString(), user.uid.toString(), user.nickname.toString(), null, 0, Date())
                firebaseViewModel.sendFanClubChat(fanClubDTO?.docName.toString(), chat) {
                    binding.editContent.setText("")
                    binding.textSend.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_chat_disable))
                    binding.buttonSend.isEnabled = false
                }
            }
        }

        binding.editContent.doAfterTextChanged {
            if (binding.editContent.text.toString().isNullOrEmpty()) {
                binding.textSend.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_chat_disable))
                binding.buttonSend.isEnabled = false
            } else {
                binding.textSend.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
                binding.buttonSend.isEnabled = true
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
        val fragment = FragmentFanClubMain.newInstance(param1!!, param2!!)
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
    }

    private fun callToast(message: String) {
        if (toast == null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        } else {
            toast?.setText(message)
        }
        toast?.show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentChat.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Int, param2: String) =
            FragmentChat().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: DisplayBoardExDTO, position: Int) {

    }

    override fun onItemClickReport(item: DisplayBoardExDTO, position: Int) {
        val user = (activity as MainActivity?)?.getUser()!!

        if (reportDialog == null) {
            reportDialog = ReportDialog(requireContext())
            reportDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            reportDialog?.setCanceledOnTouchOutside(false)
        }
        reportDialog?.reportDTO = ReportDTO(user.uid, user.nickname, item.displayBoardDTO?.userUid, item.displayBoardDTO?.userNickname, item.displayBoardDTO?.displayText, item.displayBoardDTO?.docName, ReportDTO.Type.FanClubChat)
        reportDialog?.show()
        reportDialog?.setInfo()

        reportDialog?.setOnDismissListener {
            if (!reportDialog?.reportDTO?.reason.isNullOrEmpty()) {
                firebaseViewModel.sendReport(reportDialog?.reportDTO!!) {
                    if (!dbHandler?.getBlock(reportDialog?.reportDTO?.contentDocName.toString())) {
                        dbHandler?.updateBlock(reportDialog?.reportDTO?.contentDocName.toString(), 1)
                    } else {
                        dbHandler?.updateBlock(reportDialog?.reportDTO?.contentDocName.toString(), 0)
                    }

                    item.isBlocked = true
                    recyclerViewAdapter.notifyItemChanged(position)
                    callToast("신고 처리 완료되었습니다.")
                }
            }
        }
    }
}