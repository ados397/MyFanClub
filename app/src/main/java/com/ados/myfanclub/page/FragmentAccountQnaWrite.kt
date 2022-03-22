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
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentAccountQnaWriteBinding
import com.ados.myfanclub.dialog.QuestionDialog
import com.ados.myfanclub.model.LogDTO
import com.ados.myfanclub.model.QnaDTO
import com.ados.myfanclub.model.QuestionDTO
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentAccountQnaWrite.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentAccountQnaWrite : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentAccountQnaWriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var callback: OnBackPressedCallback

    private val firebaseViewModel : FirebaseViewModel by viewModels()

    private var questionDialog : QuestionDialog? = null

    private var titleOK: Boolean = false
    private var contentOK: Boolean = false

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
        _binding = FragmentAccountQnaWriteBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.buttonOk.setOnClickListener {
            val question = QuestionDTO(
                QuestionDTO.Stat.WARNING,
                "1:1 문의하기",
                "작성하신 내용으로 문의하시겠습니까?",
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
            }

            questionDialog?.binding?.buttonQuestionOk?.setOnClickListener { // Ok
                questionDialog?.dismiss()

                val user = (activity as MainActivity?)?.getUser()!!
                var qnaDTO = QnaDTO()
                qnaDTO.userUid = user.uid
                qnaDTO.userNickname = user.nickname
                qnaDTO.title = binding.editTitle.text.toString()
                qnaDTO.content = binding.editContent.text.toString()
                qnaDTO.createTime = Date()
                firebaseViewModel.sendQna(qnaDTO) {
                    var log = LogDTO("[1:1 문의하기] 제목 : ${qnaDTO.title}, 사용자 : ${user.nickname}(${user.uid})", Date())
                    firebaseViewModel.writeUserLog(user.uid.toString(), log) { }

                    Toast.makeText(context, "정상적으로 문의되었습니다.", Toast.LENGTH_SHORT).show()
                    callBackPressed()
                }
            }

        }

        binding.editTitle.doAfterTextChanged {
            if (binding.editTitle.text.toString().isNullOrEmpty()) {
                binding.textTitleError.text = "제목을 입력해 주세요."
                binding.editTitle.setBackgroundResource(R.drawable.edit_rectangle_red)
                titleOK = false
            } else {
                binding.textTitleError.text = ""
                binding.editTitle.setBackgroundResource(R.drawable.edit_rectangle)
                titleOK = true
            }

            binding.textTitleLen.text = "${binding.editTitle.text.length}/60"

            visibleOkButton()
        }

        binding.editContent.doAfterTextChanged {
            if (binding.editContent.text.toString().isNullOrEmpty()) {
                binding.textContentError.text = "내용을 입력해 주세요."
                binding.editContent.setBackgroundResource(R.drawable.edit_rectangle_red)
                contentOK = false
            } else {
                binding.textContentError.text = ""
                binding.editContent.setBackgroundResource(R.drawable.edit_rectangle)
                contentOK = true
            }

            binding.textContentLen.text = "${binding.editContent.text.length}/600"

            visibleOkButton()
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
        val fragment = FragmentAccountQna()
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
    }

    // 유효성 체크
    private fun visibleOkButton() {
        binding.buttonOk.isEnabled = titleOK && contentOK
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentAccountQnaWrite.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentAccountQnaWrite().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}