package com.ados.myfanclub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
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
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

class MailActivity : AppCompatActivity(), OnMailItemClickListener {
    private lateinit var binding: ActivityMailBinding

    private val firebaseViewModel : FirebaseViewModel by viewModels()

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterMail

    private var loadingDialog : LoadingDialog? = null
    private var questionDialog: QuestionDialog? = null
    private var getItemDialog : GetItemDialog? = null
    private var mailDialog : MailDialog? = null

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
                Toast.makeText(this, "?????? ????????? ????????????.", Toast.LENGTH_SHORT).show()
            } else {
                loading()

                successCount = 0
                var jobCount = 0
                var documentList = ""
                var item = MailDTO()

                // ?????? ???????????? ?????? ???????????? ??????
                for (i in mails) {
                    if (i.item == MailDTO.Item.PAID_GEM || i.item == MailDTO.Item.FREE_GEM) {
                        item.item = i.item
                        item.itemCount = item.itemCount?.plus(i.itemCount!!)
                        documentList += " (${i.docName}) "

                        jobCount++
                    }
                }

                println("??????????????? ????????? ?????? jobCount = $jobCount, successCount = $successCount")

                if (item.itemCount!! > 0) { // ?????? ???????????? ????????? ??????
                    var log = LogDTO("[??????????????? ?????? ?????? ??????] ????????? ??? (${item.itemCount}) ??????, ?????? ?????? ????????? - $documentList", Date())
                    firebaseViewModel.writeUserLog(userDTO?.uid.toString(), log) { }

                    // ???????????? ?????? ????????? ??????
                    var paidGemCount = 0
                    var freeGemCount = 0
                    for (i in mails) {
                        when (i.item) {
                            MailDTO.Item.PAID_GEM -> paidGemCount = paidGemCount.plus(i.itemCount!!)
                            MailDTO.Item.FREE_GEM -> freeGemCount = freeGemCount.plus(i.itemCount!!)
                            else -> continue
                        }
                    }
                    firebaseViewModel.addUserGem(userDTO?.uid.toString(), paidGemCount, freeGemCount) {
                        if (it != null) {
                            // ?????? ?????? ??????
                            var iter = mails.iterator()
                            while (iter.hasNext()) {
                                var mail = iter.next()
                                if (mail.item == MailDTO.Item.PAID_GEM || mail.item == MailDTO.Item.FREE_GEM) {
                                    firebaseViewModel.updateUserMailDelete(userDTO?.uid.toString(), mail.docName.toString()) {
                                        var log2 = LogDTO("[??????????????? ????????? ??????] ?????????(${mail.item}, ${mail.itemCount}) ??????, mail document(${mail.docName})", Date())
                                        firebaseViewModel.writeUserLog(userDTO?.uid.toString(), log2) { }
                                        successCount++
                                    }
                                    iter.remove()
                                }
                            }
                        } else {
                            println("??????????????? ????????? ?????? ?????? successCount = $successCount")
                        }
                    }

                    timer(period = 100)
                    {
                        if (jobCount == successCount) {
                            println("??????????????? ????????? ?????? ???????! jobCount = $jobCount, successCount = $successCount")
                            cancel()
                            this@MailActivity.runOnUiThread {
                                setAdapter()
                                loadingEnd()

                                showGetItemDialog(item)
                            }
                        }
                    }
                } else {
                    loadingEnd()
                    Toast.makeText(this, "?????? ????????? ????????????.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonDeleteAll.setOnClickListener {
            if (mails.size <= 0) {
                Toast.makeText(this, "????????? ????????? ????????????.", Toast.LENGTH_SHORT).show()
            } else {
                val question = QuestionDTO(
                    QuestionDTO.Stat.WARNING,
                    "????????? ?????? ??????",
                    //"????????? ???????????? ?????? ????????? ?????? ?????? ?????????.\n?????? ?????? ???????????????????",
                    "?????? ????????? ?????? ?????? ?????????.\n?????? ?????? ???????????????????\n\n(?????? ???????????? ?????? ????????? ???????????? ????????????)",
                )
                if (questionDialog == null) {
                    questionDialog = QuestionDialog(this, question)
                    questionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    questionDialog?.setCanceledOnTouchOutside(false)
                } else {
                    questionDialog?.question = question
                }
                questionDialog?.show()
                questionDialog?.setInfo()
                questionDialog?.binding?.buttonQuestionCancel?.setOnClickListener { // No
                    questionDialog?.dismiss()
                    questionDialog = null
                }
                questionDialog?.binding?.buttonQuestionOk?.setOnClickListener { // Ok
                    questionDialog?.dismiss()
                    questionDialog = null
                    loading()

                    successCount = 0
                    var jobCount = 0
                    var documentList = ""
                    var iter = mails.iterator()
                    while (iter.hasNext()) {
                        var mail = iter.next()
                        if (mail.item == MailDTO.Item.NONE && mail.read!!) {
                            documentList += " (${mail.docName}) "

                            // ?????? ?????? firestore ??????
                            applyFirestoreDeleteMail(mail)
                            iter.remove()
                            jobCount++
                        }
                    }
                    setAdapter()

                    if (!documentList.isNullOrEmpty()) {
                        var log = LogDTO("[??????????????? ?????? ?????? ??????] ????????? ?????? ????????? - $documentList", Date())
                        firebaseViewModel.writeUserLog(userDTO?.uid.toString(), log) { }

                        timer(period = 100)
                        {
                            if (jobCount == successCount) {
                                cancel()
                                this@MailActivity.runOnUiThread {
                                    loadingEnd()
                                    Toast.makeText(this@MailActivity, "?????? ?????? ??????.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        loadingEnd()
                        Toast.makeText(this, "????????? ????????? ????????????.", Toast.LENGTH_SHORT).show()
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
            else -> return
        }
        firebaseViewModel.addUserGem(userDTO?.uid.toString(), paidGemCount, freeGemCount) {
            if (it != null) {
                firebaseViewModel.updateUserMailDelete(userDTO?.uid.toString(), item.docName.toString()) {
                    successCount++
                    println("??????????????? ????????? ?????? successCount = $successCount")
                    var log = LogDTO("[??????????????? ????????? ??????] ?????????(${item.item}, ${item.itemCount}) ??????, mail document(${item.docName})", Date())
                    firebaseViewModel.writeUserLog(userDTO?.uid.toString(), log) { }
                }
            } else {
                println("??????????????? ????????? ?????? ?????? successCount = $successCount")
            }
        }
    }

    private fun applyFirestoreDeleteMail(item: MailDTO) {
        firebaseViewModel.updateUserMailDelete(userDTO?.uid.toString(), item.docName.toString()) {
            successCount++
            var log = LogDTO("[??????????????? ?????? ??????] mail document(${item.docName})", Date())
            firebaseViewModel.writeUserLog(userDTO?.uid.toString(), log) { }
        }
    }

    override fun onItemClick(item: MailDTO, position: Int) {
        if (mailDialog == null) {
            mailDialog = MailDialog(this)
            mailDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            mailDialog?.setCanceledOnTouchOutside(false)
        }
        mailDialog?.mailDTO = item
        mailDialog?.show()
        mailDialog?.setInfo()

        if (item.read == false) { // ?????? ?????? ??????????????? ?????? ??????
            item.read = true
            firebaseViewModel.updateUserMailRead(userDTO?.uid.toString(), item.docName.toString()) {
                recyclerViewAdapter.notifyItemChanged(position)
                var log = LogDTO("[??????????????? ?????? ??????] mail document(${item.docName})", Date())
                firebaseViewModel.writeUserLog(userDTO?.uid.toString(), log) { }
            }
        }

        mailDialog?.binding?.buttonMailCancel?.setOnClickListener { // No
            mailDialog?.dismiss()
            mailDialog = null
        }

        mailDialog?.binding?.buttonGet?.setOnClickListener {
            mailDialog?.dismiss()
            mailDialog = null
            loading()
            successCount = 0
            var jobCount = 1

            when (item.item) {
                MailDTO.Item.NONE -> {
                    // ?????? ?????? ??????
                    applyFirestoreDeleteMail(item)
                    mails.remove(item)
                    setAdapter()

                    timer(period = 100)
                    {
                        if (jobCount == successCount) {
                            cancel()
                            this@MailActivity.runOnUiThread {
                                loadingEnd()
                                Toast.makeText(this@MailActivity, "?????? ?????? ??????.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                MailDTO.Item.PAID_GEM, MailDTO.Item.FREE_GEM -> {
                    // ????????? ????????? ?????? ??? firestore ??????
                    applyFirestoreGetItem(item)
                    mails.remove(item)
                    setAdapter()

                    timer(period = 100)
                    {
                        if (jobCount == successCount) {
                            cancel()
                            this@MailActivity.runOnUiThread {
                                loadingEnd()

                                showGetItemDialog(item)
                            }
                        }
                    }
                }
                else -> return@setOnClickListener
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
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            if (loadingDialog != null) {
                loadingDialog?.dismiss()
            }
        }, 400)
    }

    private fun showGetItemDialog(item: MailDTO) {
        if (getItemDialog == null) {
            getItemDialog = GetItemDialog(this@MailActivity)
            getItemDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            getItemDialog?.setCanceledOnTouchOutside(false)
        }
        getItemDialog?.mailDTO = item
        getItemDialog?.show()
        getItemDialog?.setInfo()

        getItemDialog?.binding?.buttonGetItemOk?.setOnClickListener {
            getItemDialog?.dismiss()
        }
    }
}