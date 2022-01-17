package com.ados.myfanclub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ados.myfanclub.databinding.ActivityDisplayBoardBinding
import com.ados.myfanclub.dialog.DisplayBoardAddDialog
import com.ados.myfanclub.dialog.GemQuestionDialog
import com.ados.myfanclub.dialog.RecyclerViewAdapterDisplayBoard
import com.ados.myfanclub.model.*
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.display_board_add_dialog.*
import kotlinx.android.synthetic.main.gem_question_dialog.*
import java.util.*

class DisplayBoardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDisplayBoardBinding

    private val firebaseViewModel : FirebaseViewModel by viewModels()

    lateinit var recyclerViewAdapter : RecyclerViewAdapterDisplayBoard

    var preferencesDTO : PreferencesDTO? = null
    var currentUser: UserDTO? = null

    val anim = AlphaAnimation(0.1f, 1.0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisplayBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = intent.getParcelableExtra("user")
        preferencesDTO = intent.getParcelableExtra("preferences")

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        binding.rvDisplayBoard.layoutManager = layoutManager

        anim.duration = 800
        anim.startOffset = 20
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        binding.layoutTitle.textDisplayBoard.startAnimation(anim)
        binding.layoutTitle.textDisplayBoard.text = "🎆 전광판 🎉 광고 📣"

        firebaseViewModel.getDisplayBoardsListen()
        firebaseViewModel.displayBoardDTOs.observe(this) {
            recyclerViewAdapter = RecyclerViewAdapterDisplayBoard(firebaseViewModel.displayBoardDTOs.value!!)
            binding.rvDisplayBoard.adapter = recyclerViewAdapter
            binding.rvDisplayBoard.scrollToPosition(0)
        }

        binding.buttonAddAds.setOnClickListener {
            if (currentUser?.level!! < 5) {
                Toast.makeText(this, "레벨 [ 5 ] 달성 시 등록 가능합니다.", Toast.LENGTH_SHORT).show()
            } else {
                val dialog = DisplayBoardAddDialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCanceledOnTouchOutside(false)
                dialog.currentUser = currentUser
                dialog.show()

                dialog.button_display_board_add_cancel.setOnClickListener { // Ok
                    dialog.dismiss()
                }

                dialog.button_display_board_add_ok.setOnClickListener { // Ok
                    val displayText = dialog.binding.editDisplayBoard.text.toString().trim()
                    val color = dialog.binding.layoutDisplayBoardTest.textDisplayBoard.currentTextColor

                    if (displayText.isNullOrEmpty()) {
                        Toast.makeText(this, "내용을 입력 하세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        val question = GemQuestionDTO("다이아를 사용해 전광판을 등록합니다.", preferencesDTO?.priceDisplayBoard)
                        val questionDialog = GemQuestionDialog(this, question)
                        questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        questionDialog.setCanceledOnTouchOutside(false)
                        questionDialog.show()
                        questionDialog.button_gem_question_cancel.setOnClickListener { // No
                            questionDialog.dismiss()
                        }
                        questionDialog.button_gem_question_ok.setOnClickListener { // Ok
                            dialog.dismiss()
                            questionDialog.dismiss()

                            firebaseViewModel.sendDisplayBoard(displayText, color, currentUser!!) {
                                // 다이아 차감
                                val oldPaidGemCount = currentUser?.paidGem!!
                                val oldFreeGemCount = currentUser?.freeGem!!
                                firebaseViewModel.useUserGem(currentUser?.uid.toString(), preferencesDTO?.priceDisplayBoard!!) { userDTO ->
                                    if (userDTO != null) {
                                        currentUser = userDTO

                                        var log = LogDTO("[다이아 차감] 전광판 등록으로 ${preferencesDTO?.priceDisplayBoard} 다이아 사용 (전광판 내용 -> \"displayText\"), (paidGem : $oldPaidGemCount -> ${currentUser?.paidGem}, freeGem : $oldFreeGemCount -> ${currentUser?.freeGem})", Date())
                                        firebaseViewModel.writeUserLog(currentUser?.uid.toString(), log) { }

                                        Toast.makeText(this, "전광판 등록 완료!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.buttonOk.setOnClickListener {
            finish()
        }
    }
}