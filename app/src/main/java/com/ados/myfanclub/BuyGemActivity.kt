package com.ados.myfanclub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ados.myfanclub.databinding.ActivityBuyGemBinding

class BuyGemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBuyGemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuyGemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}