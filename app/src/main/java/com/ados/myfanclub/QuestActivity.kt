package com.ados.myfanclub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.databinding.ActivityQuestBinding
import com.ados.myfanclub.dialog.GetItemDialog
import com.ados.myfanclub.dialog.LoadingDialog
import com.ados.myfanclub.model.LogDTO
import com.ados.myfanclub.model.MailDTO
import com.ados.myfanclub.model.QuestDTO
import com.ados.myfanclub.model.UserDTO
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import kotlinx.android.synthetic.main.get_item_dialog.*
import java.util.*
import kotlin.collections.ArrayList

class QuestActivity : AppCompatActivity(), OnQuestItemClickListener {
    private lateinit var binding: ActivityQuestBinding

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterQuest

    private var loadingDialog : LoadingDialog? = null
    private var getItemDialog : GetItemDialog? = null

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private var currentUser: UserDTO? = null

    private var quests : ArrayList<QuestDTO> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = intent.getParcelableExtra("user")

        recyclerView = binding.rvQuest
        recyclerView.layoutManager = LinearLayoutManager(this)

        quests.add(QuestDTO("일일 과제 모두 달성", "모든 일일 과제를 완료 하세요.", 3, null, currentUser?.questGemGetTimes?.get("0")))
        quests.add(QuestDTO("개인 일일 스케줄", "개인 일일 스케줄을 1회 이상 완료 하세요.", 1, currentUser?.questSuccessTimes?.get("1"), currentUser?.questGemGetTimes?.get("1")))
        quests.add(QuestDTO("팬클럽 일일 스케줄", "팬클럽 일일 스케줄을 1회 이상 완료 하세요.", 1, currentUser?.questSuccessTimes?.get("2"), currentUser?.questGemGetTimes?.get("2")))
        quests.add(QuestDTO("개인 출석체크", "개인 출석체크를 완료 하세요.", 1, currentUser?.questSuccessTimes?.get("3"), currentUser?.questGemGetTimes?.get("3")))
        quests.add(QuestDTO("팬클럽 출석체크", "팬클럽 출석체크를 완료 하세요.", 1, currentUser?.questSuccessTimes?.get("4"), currentUser?.questGemGetTimes?.get("4")))
        quests.add(QuestDTO("개인 무료 경험치 광고", "개인 무료 경험치 광고를 1회 이상 시청 하세요.", 1, currentUser?.questSuccessTimes?.get("5"), currentUser?.questGemGetTimes?.get("5")))
        quests.add(QuestDTO("개인 무료 다이아 광고", "개인 무료 다이아 광고를 1회 이상 시청 하세요.", 1, currentUser?.questSuccessTimes?.get("6"), currentUser?.questGemGetTimes?.get("6")))
        quests.add(QuestDTO("팬클럽 무료 경험치 광고", "팬클럽 무료 경험치 광고를 1회 이상 시청 하세요.", 1, currentUser?.questSuccessTimes?.get("7"), currentUser?.questGemGetTimes?.get("7")))
        quests.add(QuestDTO("팬클럽 무료 다이아 광고", "팬클럽 무료 다이아 광고를 1회 이상 시청 하세요.", 1, currentUser?.questSuccessTimes?.get("8"), currentUser?.questGemGetTimes?.get("8")))

        setAdapter()

        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonGetAll.setOnClickListener {
            // 받을 퀘스트수와 받을 다이아수 획득
            var successCount = 0
            var gemCount = 0
            var positions = arrayListOf<String>()
            for (i in 1 until quests.size) {
                if (quests[i].isQuestSuccess()) {
                    successCount++

                    if (!quests[i].isQuestGemGet()) {
                        gemCount = gemCount.plus(quests[i].gemCount!!)
                        positions.add("$i")
                        quests[i].questGemGetTime = Date()
                    }
                }
            }

            if (successCount == (quests.size-1) && !quests[0].isQuestGemGet()) { // 모든 일일 퀘스트 완료
                gemCount = gemCount.plus(quests[0].gemCount!!)
                positions.add("0")
                quests[0].questGemGetTime = Date()
            }

            if (gemCount > 0) { // 받을 아이템이 있다면 작업
                loading()
                addGem(gemCount, positions, true)
            } else {
                Toast.makeText(this, "받을 보상이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setAdapter() {
        recyclerViewAdapter = RecyclerViewAdapterQuest(quests, this)
        recyclerView.adapter = recyclerViewAdapter
    }

    override fun onItemClick(item: QuestDTO, position: Int) {
        loading()
        addGem(item?.gemCount!!, arrayListOf("$position"), false)
        item.questGemGetTime = Date()
        recyclerViewAdapter.notifyItemChanged(position)
    }

    private fun addGem(gemCount: Int, positions: ArrayList<String>, isRefresh: Boolean) {
        val oldFreeGemCount = currentUser?.freeGem
        firebaseViewModel.addUserGem(currentUser?.uid.toString(), 0, gemCount) { userDTO ->
            if (userDTO != null) {
                for (pos in positions) {
                    userDTO?.questGemGetTimes?.set("$pos", Date())
                }
                currentUser = userDTO
                firebaseViewModel.updateUserQuestGemGetTimes(userDTO) { // 일일 퀘스트 보상 획득 시간 기록
                    var log = LogDTO("[일일퀘스트 완료] 다이아 $gemCount 획득 (freeGem : $oldFreeGemCount -> ${currentUser?.freeGem})", Date())
                    firebaseViewModel.writeUserLog(currentUser?.uid.toString(), log) { }

                    if (isRefresh) {
                        setAdapter()
                    }
                    loadingEnd()

                    if (getItemDialog == null) {
                        getItemDialog = GetItemDialog(this)
                        getItemDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        getItemDialog?.setCanceledOnTouchOutside(false)
                    }
                    getItemDialog?.mailDTO = MailDTO("", "", "", "", MailDTO.Item.FREE_GEM, gemCount)
                    getItemDialog?.show()
                    getItemDialog?.setInfo()

                    getItemDialog?.button_get_item_ok?.setOnClickListener {
                        getItemDialog?.dismiss()
                    }
                }
            }


        }
    }

    private fun loading() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(this)
            loadingDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            loadingDialog?.setCanceledOnTouchOutside(false)
        }
        loadingDialog?.show()
    }

    private fun loadingEnd() {
        android.os.Handler().postDelayed({
            if (loadingDialog != null) {
                loadingDialog?.dismiss()
            }
        }, 400)
    }
}