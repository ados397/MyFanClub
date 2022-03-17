package com.ados.myfanclub

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ados.myfanclub.databinding.ActivityNoticeBinding
import com.ados.myfanclub.model.NoticeDTO
import com.ados.myfanclub.viewmodel.FirebaseViewModel

class NoticeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoticeBinding

    private val firebaseViewModel : FirebaseViewModel by viewModels()

    lateinit var recyclerView : RecyclerView
    lateinit var recyclerViewAdapter : RecyclerViewAdapterNotice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.rvNotice
        recyclerView.layoutManager = LinearLayoutManager(this)

        binding.imgBack.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)

        firebaseViewModel.getNotices(false)
        firebaseViewModel.noticeDTOs.observe(this) {
            if (firebaseViewModel.noticeDTOs.value != null) {
                println("[로딩] 공지사항 획득")
                setAdapter()
            }
        }

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun setAdapter() {
        recyclerViewAdapter = RecyclerViewAdapterNotice(firebaseViewModel.noticeDTOs.value!!)
        recyclerView.adapter = recyclerViewAdapter
    }

}