package com.ados.myfanclub

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.ados.myfanclub.databinding.ActivityBuyGemBinding
import com.ados.myfanclub.model.LogDTO
import com.ados.myfanclub.model.UserDTO
import com.ados.myfanclub.repository.FirebaseRepository
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import java.util.*

class AppStorage(context: Context) {

    private var pref: SharedPreferences = context.getSharedPreferences("storage", Context.MODE_PRIVATE)

    fun put(key: String?, value: Int) {
        val editor = pref.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String?): Int {
        return pref.getInt(key, 0)
    }
}

class BuyGemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBuyGemBinding
    private lateinit var bm: BillingModule

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private var userDTO: UserDTO? = null

    private val storage: AppStorage by lazy {
        AppStorage(this)
    }

    private var mSkuDetails = listOf<SkuDetails>()
        set(value) {
            field = value
            setSkuDetailsView()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuyGemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDTO = intent.getParcelableExtra("user")

        initBilling()

        if (userDTO?.firstGemPackage?.get("1")!!) {
            binding.imgFirstBuyPack1.visibility = View.VISIBLE
            binding.textGemPack1.text = "40 + 40 다이아"
        } else {
            binding.imgFirstBuyPack1.visibility = View.GONE
            binding.textGemPack1.text = "40 + 10 다이아"
        }

        if (userDTO?.firstGemPackage?.get("2")!!) {
            binding.imgFirstBuyPack2.visibility = View.VISIBLE
            binding.textGemPack2.text = "200 + 200 다이아"
        } else {
            binding.imgFirstBuyPack2.visibility = View.GONE
            binding.textGemPack2.text = "200 + 50 다이아"
        }

        if (userDTO?.firstGemPackage?.get("3")!!) {
            binding.imgFirstBuyPack3.visibility = View.VISIBLE
            binding.textGemPack3.text = "750 + 750 다이아"
        } else {
            binding.imgFirstBuyPack3.visibility = View.GONE
            binding.textGemPack3.text = "750 + 250 다이아"
        }

        if (userDTO?.firstGemPackage?.get("4")!!) {
            binding.imgFirstBuyPack4.visibility = View.VISIBLE
            binding.textGemPack4.text = "2000 + 2000 다이아"
        } else {
            binding.imgFirstBuyPack4.visibility = View.GONE
            binding.textGemPack4.text = "2000 + 650 다이아"
        }

        if (userDTO?.firstGemPackage?.get("5")!!) {
            binding.imgFirstBuyPack5.visibility = View.VISIBLE
            binding.textGemPack5.text = "4300 + 4300 다이아"
        }
        else {
            binding.imgFirstBuyPack5.visibility = View.GONE
            binding.textGemPack5.text = "4300 + 1400 다이아"
        }


        // 이벤트 패키지 활성화 코드

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun initBilling() {
        bm = BillingModule(this, lifecycleScope, object: BillingModule.Callback {
            override fun onBillingModulesIsReady() {
                bm.querySkuDetail(BillingClient.SkuType.INAPP, Sku.GEM_PACK_1, Sku.GEM_PACK_2, Sku.GEM_PACK_3, Sku.GEM_PACK_4, Sku.GEM_PACK_5, Sku.GEM_PACK_FIRST_1, Sku.GEM_PACK_FIRST_2, Sku.GEM_PACK_FIRST_3, Sku.GEM_PACK_FIRST_4, Sku.GEM_PACK_FIRST_5) { skuDetails ->
                    mSkuDetails = skuDetails
                }
            }

            override fun onSuccess(purchase: Purchase) {
                when (purchase.sku) {
                    Sku.GEM_PACK_1 -> {
                        applyGemPack(SkuName.GEM_PACK_1, SkuCount.GEM_PACK_1)
                        //isPurchasedRemoveAds = true
                    }
                    Sku.GEM_PACK_2 -> {
                        applyGemPack(SkuName.GEM_PACK_2, SkuCount.GEM_PACK_2)
                        // 크리스탈 1000개를 충전합니다.
                        //val currentCrystal = storage.getInt(PREF_KEY_CRYSTAL)
                        //storage.put(PREF_KEY_CRYSTAL, currentCrystal + 1000)
                        //updateCrystalView()
                    }
                    Sku.GEM_PACK_3 -> {
                        applyGemPack(SkuName.GEM_PACK_3, SkuCount.GEM_PACK_3)
                    }
                    Sku.GEM_PACK_4 -> {
                        applyGemPack(SkuName.GEM_PACK_4, SkuCount.GEM_PACK_4)
                    }
                    Sku.GEM_PACK_5 -> {
                        applyGemPack(SkuName.GEM_PACK_5, SkuCount.GEM_PACK_5)
                    }
                    Sku.GEM_PACK_FIRST_1 -> {
                        applyGemPack(SkuName.GEM_PACK_FIRST_1, SkuCount.GEM_PACK_FIRST_1, "1")
                    }
                    Sku.GEM_PACK_FIRST_2 -> {
                        applyGemPack(SkuName.GEM_PACK_FIRST_2, SkuCount.GEM_PACK_FIRST_2, "2")
                    }
                    Sku.GEM_PACK_FIRST_3 -> {
                        applyGemPack(SkuName.GEM_PACK_FIRST_3, SkuCount.GEM_PACK_FIRST_3, "3")
                    }
                    Sku.GEM_PACK_FIRST_4 -> {
                        applyGemPack(SkuName.GEM_PACK_FIRST_4, SkuCount.GEM_PACK_FIRST_4, "4")
                    }
                    Sku.GEM_PACK_FIRST_5 -> {
                        applyGemPack(SkuName.GEM_PACK_FIRST_5, SkuCount.GEM_PACK_FIRST_5, "5")
                    }
                }
            }

            override fun onFailure(errorCode: Int) {
                when (errorCode) {
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                        Toast.makeText(this@BuyGemActivity, "이미 구입한 상품입니다.", Toast.LENGTH_LONG).show()
                    }
                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                        Toast.makeText(this@BuyGemActivity, "구매를 취소하셨습니다.", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(this@BuyGemActivity, "error: $errorCode", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })

        setClickListeners()
    }

    private fun setClickListeners() {
        with (binding) {
            layoutGemPack1.setOnClickListener {
                var sku = Sku.GEM_PACK_1
                if (userDTO?.firstGemPackage?.get("1")!!) { // 첫 구매 패키지
                    sku = Sku.GEM_PACK_FIRST_1
                }

                mSkuDetails.find { it.sku == sku }?.let { skuDetail ->
                    bm.purchase(skuDetail)
                } ?: also {
                    Toast.makeText(this@BuyGemActivity, "상품을 찾을 수 없습니다.", Toast.LENGTH_LONG).show()
                }
            }

            layoutGemPack2.setOnClickListener {
                var sku = Sku.GEM_PACK_2
                if (userDTO?.firstGemPackage?.get("2")!!) { // 첫 구매 패키지
                    sku = Sku.GEM_PACK_FIRST_2
                }

                mSkuDetails.find { it.sku == sku }?.let { skuDetail ->
                    bm.purchase(skuDetail)
                } ?: also {
                    Toast.makeText(this@BuyGemActivity, "상품을 찾을 수 없습니다.", Toast.LENGTH_LONG).show()
                }
            }

            layoutGemPack3.setOnClickListener {
                var sku = Sku.GEM_PACK_3
                if (userDTO?.firstGemPackage?.get("3")!!) { // 첫 구매 패키지
                    sku = Sku.GEM_PACK_FIRST_3
                }

                mSkuDetails.find { it.sku == sku }?.let { skuDetail ->
                    bm.purchase(skuDetail)
                } ?: also {
                    Toast.makeText(this@BuyGemActivity, "상품을 찾을 수 없습니다.", Toast.LENGTH_LONG).show()
                }
            }

            layoutGemPack4.setOnClickListener {
                var sku = Sku.GEM_PACK_4
                if (userDTO?.firstGemPackage?.get("4")!!) { // 첫 구매 패키지
                    sku = Sku.GEM_PACK_FIRST_4
                }

                mSkuDetails.find { it.sku == sku }?.let { skuDetail ->
                    bm.purchase(skuDetail)
                } ?: also {
                    Toast.makeText(this@BuyGemActivity, "상품을 찾을 수 없습니다.", Toast.LENGTH_LONG).show()
                }
            }

            layoutGemPack5.setOnClickListener {
                var sku = Sku.GEM_PACK_5
                if (userDTO?.firstGemPackage?.get("5")!!) { // 첫 구매 패키지
                    sku = Sku.GEM_PACK_FIRST_5
                }

                mSkuDetails.find { it.sku == sku }?.let { skuDetail ->
                    bm.purchase(skuDetail)
                } ?: also {
                    Toast.makeText(this@BuyGemActivity, "상품을 찾을 수 없습니다.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setSkuDetailsView() {
        val builder = StringBuilder()
        for (skuDetail in mSkuDetails) {
            builder.append("<${skuDetail.title}>\n")
            builder.append(skuDetail.price)
            builder.append("\n======================\n\n")
        }
        //binding.tvSku.text = builder
    }

    // firstPack : 첫 구매 패키지 일 경우 첫 구매 했으므로 비활성화 시키기 위한 인자
    private fun applyGemPack(packageName: String, gemCount: Int, firstPack: String? = null) {
        var log = LogDTO("$packageName 결제 완료", Date())
        firebaseViewModel.writeUserLog(userDTO?.uid.toString(), log) {
            val oldPaidGem = userDTO?.paidGem!!
            firebaseViewModel.addUserGem(userDTO?.uid.toString(), gemCount, 0, firstPack) {
                if (it != null) {
                    var log2 = LogDTO("[다이아 패키지 구매] 다이아 추가 (paidGem : $oldPaidGem -> ${it.paidGem}, freeGem : ${it.freeGem}, totalGem : ${it.paidGem!! + it.freeGem!!}))", Date())
                    firebaseViewModel.writeUserLog(userDTO?.uid.toString(), log2) {
                        Toast.makeText(this, "다이아 패키지 구매 완료!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }
}