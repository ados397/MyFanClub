package com.ados.myfanclub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.databinding.ActivityMailBinding
import com.ados.myfanclub.dialog.GetItemDialog
import com.ados.myfanclub.dialog.LoadingDialog
import com.ados.myfanclub.dialog.MailDialog
import com.ados.myfanclub.dialog.QuestionDialog
import com.ados.myfanclub.model.LogDTO
import com.ados.myfanclub.model.MailDTO
import com.ados.myfanclub.model.QuestionDTO
import com.ados.myfanclub.model.UserDTO
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import kotlinx.android.synthetic.main.get_item_dialog.*
import kotlinx.android.synthetic.main.mail_dialog.*
import kotlinx.android.synthetic.main.question_dialog.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

class MailActivity : AppCompatActivity(), OnMailItemClickListener {
    private lateinit var binding: ActivityMailBinding

    private val firebaseViewModel : FirebaseViewModel by viewModels()

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterMail

    private var loadingDialog : LoadingDialog? = null

    private var userDTO: UserDTO? = null
    private var mails : ArrayList<MailDTO> = arrayListOf()
    private var successCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDTO = intent.getParcelableExtra("user")
        mails = intent.getParcelableArrayListExtra("mails")!!

        recyclerView = binding.rvMail
        recyclerView.layoutManager = LinearLayoutManager(this)

        setAdapter()

        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonGetAll.setOnClickListener {
            if (mails.size <= 0) {
                Toast.makeText(this, "받을 우편이 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                loading()

                successCount = 0
                var jobCount = 0
                var documentList = ""
                var item = MailDTO()

                // 받을 우편수와 받을 다이아수 획득
                for (i in mails) {
                    if (i.item == MailDTO.Item.PAID_GEM || i.item == MailDTO.Item.FREE_GEM) {
                        item.item = i.item
                        item.itemCount = item.itemCount?.plus(i.itemCount!!)
                        documentList += " (${i.docName}) "

                        jobCount++
                    }
                }

                println("우편함에서 아이템 받기 jobCount = $jobCount, successCount = $successCount")

                if (item.itemCount!! > 0) { // 받을 아이템이 있다면 작업
                    var log = LogDTO("[우편함에서 모두 받기 실행] 다이아 총 (${item.itemCount}) 획득, 받은 우편 리스트 - $documentList", Date())
                    firebaseViewModel.writeUserLog(userDTO?.uid.toString(), log) { }

                    // 우편에서 받은 다이아 적용
                    var paidGemCount = 0
                    var freeGemCount = 0
                    for (i in mails) {
                        when (i.item) {
                            MailDTO.Item.PAID_GEM -> paidGemCount = paidGemCount.plus(i.itemCount!!)
                            MailDTO.Item.FREE_GEM -> freeGemCount = freeGemCount.plus(i.itemCount!!)
                        }
                    }
                    firebaseViewModel.addUserGem(userDTO?.uid.toString(), paidGemCount, freeGemCount) {
                        if (it != null) {
                            // 받은 우편 삭제
                            var iter = mails.iterator()
                            while (iter.hasNext()) {
                                var mail = iter.next()
                                if (mail.item == MailDTO.Item.PAID_GEM || mail.item == MailDTO.Item.FREE_GEM) {
                                    firebaseViewModel.updateUserMailDelete(userDTO?.uid.toString(), mail.docName.toString()) {
                                        var log = LogDTO("[우편함에서 아이템 받기] 아이템(${mail.item}, ${mail.itemCount}) 획득, mail document(${mail.docName})", Date())
                                        firebaseViewModel.writeUserLog(userDTO?.uid.toString(), log) { }
                                        successCount++
                                    }
                                    iter.remove()
                                }
                            }
                        } else {
                            println("우편함에서 아이템 받기 실패 successCount = $successCount")
                        }
                    }

                    timer(period = 100)
                    {
                        if (jobCount == successCount) {
                            println("우편함에서 아이템 받기 완료?! jobCount = $jobCount, successCount = $successCount")
                            cancel()
                            this@MailActivity.runOnUiThread {
                                setAdapter()
                                loadingEnd()

                                val getDialog = GetItemDialog(this@MailActivity)
                                getDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                                getDialog.setCanceledOnTouchOutside(false)
                                getDialog.mailDTO = item
                                getDialog.show()

                                getDialog.button_get_item_ok.setOnClickListener {
                                    getDialog.dismiss()
                                }
                            }
                        }
                    }
                } else {
                    loadingEnd()
                    Toast.makeText(this, "받을 우편이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonDeleteAll.setOnClickListener {
            if (mails.size <= 0) {
                Toast.makeText(this, "삭제할 우편이 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                val question = QuestionDTO(
                    QuestionDTO.Stat.WARNING,
                    "우편함 모두 삭제",
                    "첨부된 아이템이 없는 우편을 모두 삭제 합니다.\n정말 삭제 하시겠습니까?",
                )
                val questionDialog = QuestionDialog(this, question)
                questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                questionDialog.setCanceledOnTouchOutside(false)
                questionDialog.show()
                questionDialog.button_question_cancel.setOnClickListener { // No
                    questionDialog.dismiss()
                }
                questionDialog.button_question_ok.setOnClickListener { // Ok
                    questionDialog.dismiss()
                    loading()

                    successCount = 0
                    var jobCount = 0
                    var documentList = ""
                    var iter = mails.iterator()
                    while (iter.hasNext()) {
                        var mail = iter.next()
                        if (mail.item == MailDTO.Item.NONE) {
                            documentList += " (${mail.docName}) "

                            // 우편 삭제 firestore 적용
                            applyFirestoreDeleteMail(mail)
                            iter.remove()
                            jobCount++
                        }
                    }
                    setAdapter()

                    if (!documentList.isNullOrEmpty()) {
                        var log = LogDTO("[우편함에서 모두 삭제 실행] 삭제한 우편 리스트 - $documentList", Date())
                        firebaseViewModel.writeUserLog(userDTO?.uid.toString(), log) { }

                        timer(period = 100)
                        {
                            if (jobCount == successCount) {
                                cancel()
                                this@MailActivity.runOnUiThread {
                                    loadingEnd()
                                    Toast.makeText(this@MailActivity, "우편 삭제 완료.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        loadingEnd()
                        Toast.makeText(this, "삭제할 우편이 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setAdapter() {
        recyclerViewAdapter = RecyclerViewAdapterMail(mails, this)
        recyclerView.adapter = recyclerViewAdapter

        if (mails.size > 0) {
            binding.rvMail.visibility = View.VISIBLE
            binding.textEmpty.visibility = View.GONE
        } else {
            binding.rvMail.visibility = View.GONE
            binding.textEmpty.visibility = View.VISIBLE
        }
    }

    private fun applyFirestoreGetItem(item: MailDTO) {
        var paidGemCount = 0
        var freeGemCount = 0
        when (item.item) {
            MailDTO.Item.PAID_GEM -> paidGemCount = item.itemCount!!
            MailDTO.Item.FREE_GEM -> freeGemCount = item.itemCount!!
        }
        firebaseViewModel.addUserGem(userDTO?.uid.toString(), paidGemCount, freeGemCount) {
            if (it != null) {
                firebaseViewModel.updateUserMailDelete(userDTO?.uid.toString(), item.docName.toString()) {
                    successCount++
                    println("우편함에서 아이템 받기 successCount = $successCount")
                    var log = LogDTO("[우편함에서 아이템 받기] 아이템(${item.item}, ${item.itemCount}) 획득, mail document(${item.docName})", Date())
                    firebaseViewModel.writeUserLog(userDTO?.uid.toString(), log) { }
                }
            } else {
                println("우편함에서 아이템 받기 실패 successCount = $successCount")
            }
        }
    }

    private fun applyFirestoreDeleteMail(item: MailDTO) {
        firebaseViewModel.updateUserMailDelete(userDTO?.uid.toString(), item.docName.toString()) {
            successCount++
            var log = LogDTO("[우편함에서 우편 삭제] mail document(${item.docName})", Date())
            firebaseViewModel.writeUserLog(userDTO?.uid.toString(), log) { }
        }
    }

    override fun onItemClick(item: MailDTO, position: Int) {
        val dialog = MailDialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.mailDTO = item
        dialog.show()

        dialog.button_mail_cancel.setOnClickListener { // No
            dialog.dismiss()
        }

        dialog.button_get.setOnClickListener {
            dialog.dismiss()
            loading()
            successCount = 0
            var jobCount = 1

            when (item.item) {
                MailDTO.Item.NONE -> {
                    // 일반 우편 삭제
                    applyFirestoreDeleteMail(item)
                    mails.remove(item)
                    setAdapter()

                    timer(period = 100)
                    {
                        if (jobCount == successCount) {
                            cancel()
                            this@MailActivity.runOnUiThread {
                                loadingEnd()
                                Toast.makeText(this@MailActivity, "우편 삭제 완료.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                MailDTO.Item.PAID_GEM, MailDTO.Item.FREE_GEM -> {
                    // 첨부된 아이템 누적 및 firestore 적용
                    applyFirestoreGetItem(item)
                    mails.remove(item)
                    setAdapter()

                    timer(period = 100)
                    {
                        if (jobCount == successCount) {
                            cancel()
                            this@MailActivity.runOnUiThread {
                                loadingEnd()

                                val getDialog = GetItemDialog(this@MailActivity)
                                getDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                                getDialog.setCanceledOnTouchOutside(false)
                                getDialog.mailDTO = item
                                getDialog.show()

                                getDialog.button_get_item_ok.setOnClickListener {
                                    getDialog.dismiss()
                                }
                            }
                        }
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