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
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentFanClubCreateBinding
import com.ados.myfanclub.dialog.SelectFanClubSymbolDialog
import com.ados.myfanclub.model.FanClubDTO
import com.ados.myfanclub.model.MemberDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.question_dialog.*
import kotlinx.android.synthetic.main.select_fan_club_symbol_dialog.*
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

    private var firebaseAuth : FirebaseAuth? = null
    private var firestore : FirebaseFirestore? = null

    private lateinit var callback: OnBackPressedCallback

    private var symbolImage: String = "reward_icon_01"

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

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.imgSymbol.setOnClickListener{
            val dialog = SelectFanClubSymbolDialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

            dialog.setOnDismissListener {
                if (dialog.isOK && !dialog.selectedSymbol.isNullOrEmpty()) {
                    symbolImage = dialog.selectedSymbol
                    var imageID = requireContext().resources.getIdentifier(symbolImage, "drawable", requireContext().packageName)
                    if (imageID != null) {
                        binding.imgSymbol.setImageResource(imageID)
                    }
                }
            }
        }

        binding.buttonOk.setOnClickListener {
            var name = binding.editName.text.toString()
            firestore?.collection("fanClub")?.get()?.addOnCompleteListener { task ->
                if(task.isSuccessful){
                    for (document in task.result) {
                        var fanclub = document.toObject(FanClubDTO::class.java)!!
                        when {
                            fanclub.name!! == name -> {
                                Toast.makeText(activity, "팬클럽이 이미 존재합니다.", Toast.LENGTH_SHORT).show()
                                return@addOnCompleteListener
                            }
                        }
                    }

                    val alphabets = ('a'..'z').toMutableList()
                    val docName = "${alphabets[Random().nextInt(alphabets.size)]}${System.currentTimeMillis()}"
                    val user = (activity as MainActivity?)?.getUser()
                    // 팬클럽 창설 시 회원이 1명이기 때문에 count 는 1로 설정
                    var fanClubDTO = FanClubDTO(false, docName, name, binding.editDescription.text.toString(), "", symbolImage, 1, 0.0, user?.uid, user?.nickname, 1, 10, Date())
                    firestore?.collection("fanClub")?.document(docName)?.set(fanClubDTO)?.addOnCompleteListener {
                        user?.fanClubId = docName
                        firestore?.collection("user")?.document(user?.uid!!)?.set(user)?.addOnCompleteListener {
                            //Toast.makeText(activity, "팬클럽 창설 완료!", Toast.LENGTH_SHORT).show()
                            //moveFanClubMain()
                            (activity as MainActivity?)?.setUser(user)
                        }
                        val member = MemberDTO(false, user?.uid, user?.nickname, user?.level, user?.aboutMe, 0, MemberDTO.POSITION.MASTER, Date(), Date(), false)
                        firestore?.collection("fanClub")?.document(docName)?.collection("member")?.document(user?.uid.toString())?.set(member)?.addOnCompleteListener {
                            Toast.makeText(activity, "팬클럽 창설 완료!", Toast.LENGTH_SHORT).show()
                            moveFanClubMain()
                        }
                    }

                    /*if (userDTO != null) { // null 이 아니라면 소셜 로그인, 이미 로그인 처리는 되어 있음, firestore에 데이터 기록 후 메인페이지 이동
                        writeFirestoreAndFinish(UserDTO(firebaseAuth?.currentUser?.uid, email, userDTO?.loginType, nickname, null))
                    } else {
                        firebaseAuth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                writeFirestoreAndFinish(UserDTO(firebaseAuth?.currentUser?.uid, email, UserDTO.LoginType.EMAIL, nickname, null))
                            } else if (!task.exception?.message.isNullOrEmpty()) {
                                Toast.makeText(this, "회원가입에 실패하였습니다. 잠시 후 다시 시도해 보세요.", Toast.LENGTH_SHORT).show()
                                //Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }*/
                }
            }
        }

        binding.editName.doAfterTextChanged {
            binding.textNameLen.text = "${binding.editName.text.length}/30"
        }

        binding.editDescription.doAfterTextChanged {
            binding.textDescriptionLen.text = "${binding.editDescription.text.length}/30"
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
        val fragment = FragmentFanClubMain()
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
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