package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.DisplayBoardDialogBinding
import com.ados.myfanclub.model.DisplayBoardDTO
import com.ados.myfanclub.model.UserDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*
import kotlin.collections.ArrayList


class DisplayBoardDialog(context: Context) : Dialog(context) {

    lateinit var binding: DisplayBoardDialogBinding
    private val layout = R.layout.display_board_dialog
    private var firestore : FirebaseFirestore? = null
    lateinit var recyclerViewAdapter : RecyclerViewAdapterDisplayBoard

    private var displayBoards : ArrayList<DisplayBoardDTO> = arrayListOf()
    var currentUser: UserDTO? = null

    val anim = AlphaAnimation(0.1f, 1.0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DisplayBoardDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

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
            binding.layoutAdd.visibility = View.VISIBLE
            val translateUp = AnimationUtils.loadAnimation(context, R.anim.translate_up)
            binding.layoutAdd.startAnimation(translateUp)

            binding.layoutTitle.textDisplayBoard.clearFocus()
            binding.layoutTitle.textDisplayBoard.clearAnimation()
            binding.layoutDisplayBoardTest.textDisplayBoard.requestFocus()
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
            val displayText = binding.editDisplayBoard.text.trim()
            if (displayText.isNullOrEmpty()) {
                Toast.makeText(context, "내용을 입력 하세요.", Toast.LENGTH_SHORT).show()
            } else {
                firestore?.collection("displayBoard")?.orderBy("order", Query.Direction.DESCENDING)?.limit(1)?.get()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            var displayBoardDTO = document.toObject(DisplayBoardDTO::class.java)!!

                            var newDisplayBoard = DisplayBoardDTO()
                            newDisplayBoard.displayText = displayText.toString()
                            newDisplayBoard.userUid = currentUser?.uid
                            newDisplayBoard.userNickname = currentUser?.nickname
                            newDisplayBoard.color = binding.layoutDisplayBoardTest.textDisplayBoard.currentTextColor
                            newDisplayBoard.order = displayBoardDTO.order?.plus(1)
                            newDisplayBoard.createTime = Date()

                            firestore?.collection("displayBoard")?.document()?.set(newDisplayBoard)?.addOnCompleteListener {
                                Toast.makeText(context, "전광판 등록 완료!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                closeAddLayout()
            }
        }

        binding.buttonAddCancel.setOnClickListener {
            closeAddLayout()
        }

        binding.buttonColor1.setOnClickListener { binding.layoutDisplayBoardTest.textDisplayBoard.setTextColor(ContextCompat.getColor(context, R.color.display_board_1)) }
        binding.buttonColor2.setOnClickListener { binding.layoutDisplayBoardTest.textDisplayBoard.setTextColor(ContextCompat.getColor(context, R.color.display_board_2)) }
        binding.buttonColor3.setOnClickListener { binding.layoutDisplayBoardTest.textDisplayBoard.setTextColor(ContextCompat.getColor(context, R.color.display_board_3)) }
        binding.buttonColor4.setOnClickListener { binding.layoutDisplayBoardTest.textDisplayBoard.setTextColor(ContextCompat.getColor(context, R.color.display_board_4)) }
        binding.buttonColor5.setOnClickListener { binding.layoutDisplayBoardTest.textDisplayBoard.setTextColor(ContextCompat.getColor(context, R.color.display_board_5)) }
        binding.buttonColor6.setOnClickListener { binding.layoutDisplayBoardTest.textDisplayBoard.setTextColor(ContextCompat.getColor(context, R.color.display_board_6)) }
        binding.buttonColor7.setOnClickListener { binding.layoutDisplayBoardTest.textDisplayBoard.setTextColor(ContextCompat.getColor(context, R.color.display_board_7)) }
        binding.buttonColor8.setOnClickListener { binding.layoutDisplayBoardTest.textDisplayBoard.setTextColor(ContextCompat.getColor(context, R.color.display_board_8)) }
        binding.buttonColor9.setOnClickListener { binding.layoutDisplayBoardTest.textDisplayBoard.setTextColor(ContextCompat.getColor(context, R.color.display_board_9)) }
        binding.buttonColor10.setOnClickListener { binding.layoutDisplayBoardTest.textDisplayBoard.setTextColor(ContextCompat.getColor(context, R.color.display_board_10)) }
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