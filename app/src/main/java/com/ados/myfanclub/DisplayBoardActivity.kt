package com.ados.myfanclub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ados.myfanclub.database.DBHelperReport
import com.ados.myfanclub.databinding.ActivityDisplayBoardBinding
import com.ados.myfanclub.dialog.*
import com.ados.myfanclub.model.*
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import java.util.*

class DisplayBoardActivity : AppCompatActivity(), OnDisplayBoardItemClickListener {
    private lateinit var binding: ActivityDisplayBoardBinding

    private val firebaseViewModel : FirebaseViewModel by viewModels()

    lateinit var recyclerViewAdapter : RecyclerViewAdapterDisplayBoard

    var preferencesDTO : PreferencesDTO? = null
    var currentUser: UserDTO? = null
    private var reportDialog: ReportDialog? = null
    private var displayBoardAddDialog: DisplayBoardAddDialog? = null
    private var gemQuestionDialog: GemQuestionDialog? = null
    lateinit var dbHandler : DBHelperReport

    val anim = AlphaAnimation(0.1f, 1.0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisplayBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = intent.getParcelableExtra("user")
        preferencesDTO = intent.getParcelableExtra("preferences")

        dbHandler = DBHelperReport(this)

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
            val itemsEx: ArrayList<DisplayBoardExDTO> = arrayListOf()
            for (display in firebaseViewModel.displayBoardDTOs.value!!) {
                itemsEx.add(DisplayBoardExDTO(display, dbHandler.getBlock(display.docName.toString())))
            }
            recyclerViewAdapter = RecyclerViewAdapterDisplayBoard(itemsEx, this)
            binding.rvDisplayBoard.adapter = recyclerViewAdapter
            binding.rvDisplayBoard.scrollToPosition(0)
        }

        binding.buttonAddAds.setOnClickListener {
            if (currentUser?.level!! < 5) {
                Toast.makeText(this, "레벨 [ 5 ] 달성 시 등록 가능합니다.", Toast.LENGTH_SHORT).show()
            } else {
                if (displayBoardAddDialog == null) {
                    displayBoardAddDialog = DisplayBoardAddDialog(this)
                    displayBoardAddDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    displayBoardAddDialog?.setCanceledOnTouchOutside(false)
                }
                displayBoardAddDialog?.currentUser = currentUser
                displayBoardAddDialog?.show()
                displayBoardAddDialog?.setInfo()
                displayBoardAddDialog?.binding?.buttonDisplayBoardAddCancel?.setOnClickListener { // Ok
                    displayBoardAddDialog?.dismiss()
                    displayBoardAddDialog = null
                }
                displayBoardAddDialog?.binding?.buttonDisplayBoardAddOk?.setOnClickListener { // Ok
                    val displayText = displayBoardAddDialog?.binding?.editDisplayBoard?.text.toString().trim()
                    val color = displayBoardAddDialog?.binding?.layoutDisplayBoardTest?.textDisplayBoard?.currentTextColor!!

                    if (displayText.isNullOrEmpty()) {
                        Toast.makeText(this, "내용을 입력 하세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        val question = GemQuestionDTO("다이아를 사용해 전광판을 등록합니다.", preferencesDTO?.priceDisplayBoard)
                        if (gemQuestionDialog == null) {
                            gemQuestionDialog = GemQuestionDialog(this, question)
                            gemQuestionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                            gemQuestionDialog?.setCanceledOnTouchOutside(false)
                        } else {
                            gemQuestionDialog?.question = question
                        }
                        gemQuestionDialog?.show()
                        gemQuestionDialog?.setInfo()
                        gemQuestionDialog?.binding?.buttonGemQuestionCancel?.setOnClickListener { // No
                            gemQuestionDialog?.dismiss()
                        }
                        gemQuestionDialog?.binding?.buttonGemQuestionOk?.setOnClickListener { // Ok
                            displayBoardAddDialog?.dismiss()
                            displayBoardAddDialog = null
                            gemQuestionDialog?.dismiss()

                            if ((currentUser?.paidGem!! + currentUser?.freeGem!!) < preferencesDTO?.priceDisplayBoard!!) {
                                Toast.makeText(this, "다이아가 부족합니다.", Toast.LENGTH_SHORT).show()
                            } else {
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
        }

        binding.buttonOk.setOnClickListener {
            finish()
        }
    }

    override fun onItemClick(item: DisplayBoardExDTO, position: Int) {
        if (!dbHandler.getBlock(item.displayBoardDTO?.docName.toString())) {
            if (reportDialog == null) {
                reportDialog = ReportDialog(this)
                reportDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                reportDialog?.setCanceledOnTouchOutside(false)
            }
            reportDialog?.reportDTO = ReportDTO(currentUser?.uid, currentUser?.nickname, item.displayBoardDTO?.userUid, item.displayBoardDTO?.userNickname, item.displayBoardDTO?.displayText, item.displayBoardDTO?.docName, ReportDTO.Type.DisplayBoard)
            reportDialog?.show()
            reportDialog?.setInfo()

            reportDialog?.setOnDismissListener {
                if (!reportDialog?.reportDTO?.reason.isNullOrEmpty()) {
                    firebaseViewModel.sendReport(reportDialog?.reportDTO!!) {
                        if (!dbHandler.getBlock(reportDialog?.reportDTO?.contentDocName.toString())) {
                            dbHandler.updateBlock(reportDialog?.reportDTO?.contentDocName.toString(), 1)
                        } else {
                            dbHandler.updateBlock(reportDialog?.reportDTO?.contentDocName.toString(), 0)
                        }

                        item.isBlocked = true
                        recyclerViewAdapter.notifyItemChanged(position)
                        Toast.makeText(this, "신고 처리 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}