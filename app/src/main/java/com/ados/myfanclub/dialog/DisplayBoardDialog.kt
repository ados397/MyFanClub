package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.DisplayBoardDialogBinding
import com.ados.myfanclub.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.gem_question_dialog.*
import java.util.*
import kotlin.collections.ArrayList


class DisplayBoardDialog(context: Context) : Dialog(context) {

    lateinit var binding: DisplayBoardDialogBinding
    private val layout = R.layout.display_board_dialog
    private var firestore : FirebaseFirestore? = null
    lateinit var recyclerViewAdapter : RecyclerViewAdapterDisplayBoard

    private var displayBoards : ArrayList<DisplayBoardDTO> = arrayListOf()
    var preferencesDTO : PreferencesDTO? = null
    var currentUser: UserDTO? = null

    val anim = AlphaAnimation(0.1f, 1.0f)

    private var toast : Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DisplayBoardDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        //window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        firestore = FirebaseFirestore.getInstance()

        val layoutManager = LinearLayoutManager(context)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        binding.rvDisplayBoard.layoutManager = layoutManager

        anim.duration = 800
        anim.startOffset = 20
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        binding.layoutTitle.textDisplayBoard.startAnimation(anim)
        binding.layoutTitle.textDisplayBoard.text = "🎆 전광판 🎉 광고 📣"
        binding.layoutDisplayBoardTest.textDisplayBoard.text = "전광판 테스트"
        binding.layoutDisplayBoardTest.textDisplayBoard.setTextColor(ContextCompat.getColor(context, R.color.display_board_1))

        //firestore?.collection("displayBoard")?.orderBy("order", Query.Direction.DESCENDING)?.limit(20)?.get()?.addOnCompleteListener { task ->


        firestore?.collection("displayBoard")?.orderBy("order", Query.Direction.DESCENDING)?.limit(15)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if(querySnapshot == null)return@addSnapshotListener
            displayBoards.clear()
            for(snapshot in querySnapshot){
                var displayBoardDTO = snapshot.toObject(DisplayBoardDTO::class.java)!!
                displayBoards.add(displayBoardDTO)
            }
            if (displayBoards.size < 15) {
                for (i in displayBoards.size..15) {
                    displayBoards.add(DisplayBoardDTO(""))
                }
            }

            recyclerViewAdapter = RecyclerViewAdapterDisplayBoard(displayBoards)
            binding.rvDisplayBoard.adapter = recyclerViewAdapter
            binding.rvDisplayBoard.scrollToPosition(0)
        }

        binding.layoutAdd.visibility = View.GONE

        binding.buttonAddAds.setOnClickListener {
            if (currentUser?.level!! < 5) {
                Toast.makeText(context, "레벨 [ 5 ] 달성 시 등록 가능합니다.", Toast.LENGTH_SHORT).show()
            } else {
                binding.layoutAdd.visibility = View.VISIBLE
                val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
                binding.layoutAdd.startAnimation(translateUp)

                binding.layoutTitle.textDisplayBoard.clearFocus()
                binding.layoutTitle.textDisplayBoard.clearAnimation()
                binding.layoutDisplayBoardTest.textDisplayBoard.requestFocus()
            }
        }

        binding.editDisplayBoard.doAfterTextChanged {
            binding.layoutDisplayBoardTest.textDisplayBoard.text = binding.editDisplayBoard.text
            binding.textDisplayBoardLen.text = "${binding.editDisplayBoard.text.length}/40"
        }

        binding.layoutDisplayBoardTest.textDisplayBoard.setOnFocusChangeListener { view, b ->
            if (!b) {
                binding.layoutDisplayBoardTest.textDisplayBoard.clearAnimation()
            }
        }

        binding.buttonPreview.setOnClickListener {
            binding.layoutDisplayBoardTest.textDisplayBoard.requestFocus()
            binding.layoutDisplayBoardTest.textDisplayBoard.startAnimation(anim)
        }

        binding.buttonAddOk.setOnClickListener {
            val displayText = binding.editDisplayBoard.text.toString().trim()
            if (displayText.isNullOrEmpty()) {
                Toast.makeText(context, "내용을 입력 하세요.", Toast.LENGTH_SHORT).show()
            } else {
                val question = GemQuestionDTO("다이아를 사용해 전광판을 등록합니다.", preferencesDTO?.priceDisplayBoard)
                val questionDialog = GemQuestionDialog(context, question)
                questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                questionDialog.setCanceledOnTouchOutside(false)
                questionDialog.show()
                questionDialog.button_gem_question_cancel.setOnClickListener { // No
                    questionDialog.dismiss()
                }
                questionDialog.button_gem_question_ok.setOnClickListener { // Ok
                    questionDialog.dismiss()

                    firestore?.collection("displayBoard")?.orderBy("order", Query.Direction.DESCENDING)?.limit(1)?.get()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result) {
                                var displayBoardDTO = document.toObject(DisplayBoardDTO::class.java)!!

                                var newDisplayBoard = DisplayBoardDTO()
                                newDisplayBoard.displayText = displayText
                                newDisplayBoard.userUid = currentUser?.uid
                                newDisplayBoard.userNickname = currentUser?.nickname
                                newDisplayBoard.color = binding.layoutDisplayBoardTest.textDisplayBoard.currentTextColor
                                newDisplayBoard.order = displayBoardDTO.order?.plus(1)
                                newDisplayBoard.createTime = Date()

                                firestore?.collection("displayBoard")?.document()?.set(newDisplayBoard)?.addOnCompleteListener {
                                    // 다이아 차감
                                    var oldPaidGemCount = 0
                                    var oldFreeGemCount = 0
                                    var tsDoc = firestore?.collection("user")?.document(currentUser?.uid.toString())
                                    firestore?.runTransaction { transaction ->
                                        val user = transaction.get(tsDoc!!).toObject(UserDTO::class.java)
                                        oldPaidGemCount = user?.paidGem!!
                                        oldFreeGemCount = user?.freeGem!!

                                        user?.useGem(preferencesDTO?.priceDisplayBoard!!)

                                        currentUser = user

                                        transaction.set(tsDoc, user!!)
                                    }?.addOnSuccessListener { result ->
                                        var log = LogDTO("[다이아 차감] 전광판 등록으로 ${preferencesDTO?.priceDisplayBoard} 다이아 사용 (전광판 내용 -> \"displayText\"), (paidGem : $oldPaidGemCount -> ${currentUser?.paidGem}, freeGem : $oldFreeGemCount -> ${currentUser?.freeGem})", Date())
                                        firestore?.collection("user")?.document(currentUser?.uid.toString())?.collection("log")?.document()?.set(log)

                                        Toast.makeText(context, "전광판 등록 완료!", Toast.LENGTH_SHORT).show()
                                    }?.addOnFailureListener { e ->
                                        //(activity as MainActivity?)?.loadingEnd()
                                    }
                                }
                            }
                        }
                    }
                    closeAddLayout()
                }
            }
        }

        binding.buttonAddCancel.setOnClickListener {
            closeAddLayout()
        }

        if (currentUser?.level!! >= 5) { binding.buttonColor1.setCompoundDrawables(null, null, null, null) }
        if (currentUser?.level!! >= 15) { binding.buttonColor2.setCompoundDrawables(null, null, null, null) }
        if (currentUser?.level!! >= 25) { binding.buttonColor3.setCompoundDrawables(null, null, null, null) }
        if (currentUser?.level!! >= 35) { binding.buttonColor4.setCompoundDrawables(null, null, null, null) }
        if (currentUser?.level!! >= 45) { binding.buttonColor5.setCompoundDrawables(null, null, null, null) }
        if (currentUser?.level!! >= 55) { binding.buttonColor6.setCompoundDrawables(null, null, null, null) }
        if (currentUser?.level!! >= 65) { binding.buttonColor7.setCompoundDrawables(null, null, null, null) }
        if (currentUser?.level!! >= 75) { binding.buttonColor8.setCompoundDrawables(null, null, null, null) }
        if (currentUser?.level!! >= 85) { binding.buttonColor9.setCompoundDrawables(null, null, null, null) }
        if (currentUser?.level!! >= 95) { binding.buttonColor10.setCompoundDrawables(null, null, null, null) }

        binding.buttonColor1.setOnClickListener { clickColorButton(5, ContextCompat.getColor(context, R.color.display_board_1)) }
        binding.buttonColor2.setOnClickListener { clickColorButton(15, ContextCompat.getColor(context, R.color.display_board_2)) }
        binding.buttonColor3.setOnClickListener { clickColorButton(25, ContextCompat.getColor(context, R.color.display_board_3)) }
        binding.buttonColor4.setOnClickListener { clickColorButton(35, ContextCompat.getColor(context, R.color.display_board_4)) }
        binding.buttonColor5.setOnClickListener { clickColorButton(45, ContextCompat.getColor(context, R.color.display_board_5)) }
        binding.buttonColor6.setOnClickListener { clickColorButton(55, ContextCompat.getColor(context, R.color.display_board_6)) }
        binding.buttonColor7.setOnClickListener { clickColorButton(65, ContextCompat.getColor(context, R.color.display_board_7)) }
        binding.buttonColor8.setOnClickListener { clickColorButton(75, ContextCompat.getColor(context, R.color.display_board_8)) }
        binding.buttonColor9.setOnClickListener { clickColorButton(85, ContextCompat.getColor(context, R.color.display_board_9)) }
        binding.buttonColor10.setOnClickListener { clickColorButton(95, ContextCompat.getColor(context, R.color.display_board_10)) }
    }

    private fun clickColorButton(level: Int, color: Int) {
        if (currentUser?.level!! >= level) {
            binding.layoutDisplayBoardTest.textDisplayBoard.setTextColor(color)
        } else {
            var toastText = "레벨 [ ${level} ] 달성 시 사용 가능합니다."
            if (toast == null) {
                toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT)
            } else {
                toast?.setText(toastText)
            }
            toast?.show()
        }
    }

    private fun closeAddLayout() {
        binding.layoutAdd.visibility = View.GONE
        val translateDown = AnimationUtils.loadAnimation(context, R.anim.translate_down)
        binding.layoutAdd.startAnimation(translateDown)

        binding.layoutDisplayBoardTest.textDisplayBoard.clearFocus()
        binding.layoutDisplayBoardTest.textDisplayBoard.clearAnimation()
        binding.layoutTitle.textDisplayBoard.requestFocus()
        binding.layoutTitle.textDisplayBoard.startAnimation(anim)
    }

    private fun init() {
        //button_ok.setOnClickListener(this)
    }

}