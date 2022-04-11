package com.ados.myfanclub

import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.ados.myfanclub.databinding.ActivityPremiumPackageBinding
import com.ados.myfanclub.dialog.GetItemDialog
import com.ados.myfanclub.dialog.LoadingDialog
import com.ados.myfanclub.model.LogDTO
import com.ados.myfanclub.model.MailDTO
import com.ados.myfanclub.model.PreferencesDTO
import com.ados.myfanclub.model.UserDTO
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class PremiumPackageActivity : AppCompatActivity() {
    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    private lateinit var binding: ActivityPremiumPackageBinding
    private lateinit var bm: BillingModule

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private var currentUser: UserDTO? = null
    private var preferencesDTO: PreferencesDTO? = null
    private var loadingDialog : LoadingDialog? = null
    private var getItemDialog : GetItemDialog? = null

    private var mSkuDetails = listOf<SkuDetails>()
        set(value) {
            field = value
            setSkuDetailsView()
        }

    private var toast : Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPremiumPackageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = intent.getParcelableExtra("user")
        preferencesDTO = intent.getParcelableExtra("preferences")

        setInfo()

        initBilling()

        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonBuyPremiumPackage.setOnClickListener {
            var sku = if (currentUser?.isPremiumRenew()!!) { // 프리미엄 패키지 갱신 상품 구매
                Sku.PREMIUM_PACK_RENEW
            } else { // 프리미엄 패키지 구매
                Sku.PREMIUM_PACK
            }

            mSkuDetails.find { it.sku == sku }?.let { skuDetail ->
                bm.purchase(skuDetail)
            } ?: also {
                Toast.makeText(this@PremiumPackageActivity, "상품을 찾을 수 없습니다.", Toast.LENGTH_LONG).show()
            }
        }

        binding.buttonPremiumGem.setOnClickListener {
            if (currentUser?.isPremiumGemGet()!!) {
                if (toast == null) {
                    toast = Toast.makeText(this, "오늘은 이미 보상을 수령하였습니다.", Toast.LENGTH_SHORT)
                }
                toast?.show()
            } else {
                addPremiumGem(preferencesDTO?.rewardPremiumPackCheckoutGem!!, true)
            }
        }
    }

    private fun setInfo() {
        if (currentUser?.isPremium()!!) {
            if (currentUser?.isPremiumRenew()!!) { // 프리미엄 패키지 기간이 7일 이하로 남았을 때 갱신 가능
                binding.buttonBuyPremiumPackage.visibility = View.VISIBLE
                binding.textDiscount.visibility = View.VISIBLE
                binding.textDiscount.paintFlags = binding.textDiscount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.textPrice.text = "\\ 8,900 갱신"
            } else {
                binding.buttonBuyPremiumPackage.visibility = View.GONE
            }

            binding.layoutGetPremiumGem.visibility = View.VISIBLE
            binding.textExpireDate.visibility = View.VISIBLE

            binding.textExpireDate.text = "남은 보상 일수 : ${currentUser?.getPremiumDay()}"

            // 오늘 프리미엄 패키지 다이아 수령 여부
            if (currentUser?.isPremiumGemGet()!!) {
                binding.layoutPremiumGem.background  = AppCompatResources.getDrawable(this, R.drawable.btn_round_disable)
                binding.textPremiumGem.text = "오늘 수령 완료"
            } else {
                binding.layoutPremiumGem.background  = AppCompatResources.getDrawable(this, R.drawable.btn_round_pay)
                binding.textPremiumGem.text = "프리미엄 다이아 수령"
            }

        } else {
            binding.buttonBuyPremiumPackage.visibility = View.VISIBLE
            binding.textDiscount.visibility = View.GONE
            binding.textPrice.text = "\\ 9,900 구매"

            binding.layoutGetPremiumGem.visibility = View.GONE
            binding.textExpireDate.visibility = View.GONE
        }

        binding.textPremiumBuyGem.text = " 즉시 다이아 ${preferencesDTO?.rewardPremiumPackBuyGem!!}개 지급!"
        binding.textPremiumEverydayGem.text = " 매일 다이아 ${preferencesDTO?.rewardPremiumPackCheckoutGem!!}개 지급!"

        val totalGem = preferencesDTO?.rewardPremiumPackCheckoutGem!!.times(30).plus(preferencesDTO?.rewardPremiumPackBuyGem!!)
        binding.textPremiumTotalGem.text = "${decimalFormat.format(totalGem)}개의 혜택!!"
    }

    private fun initBilling() {
        bm = BillingModule(this, lifecycleScope, object: BillingModule.Callback {
            override fun onBillingModulesIsReady() {
                bm.querySkuDetail(BillingClient.SkuType.INAPP, Sku.PREMIUM_PACK, Sku.PREMIUM_PACK_RENEW, Sku.PREMIUM_PACK_RENEW) { skuDetails ->
                    mSkuDetails = skuDetails
                }
            }

            override fun onSuccess(purchase: Purchase) {
                when (purchase.sku) {
                    Sku.PREMIUM_PACK -> { // 패키지 구매
                        applyPremiumPack(SkuName.PREMIUM_PACK)
                    }
                    Sku.PREMIUM_PACK_RENEW -> { // 패키지 갱신
                        applyPremiumPack(SkuName.PREMIUM_PACK_RENEW)
                    }
                }
            }

            override fun onFailure(errorCode: Int) {
                when (errorCode) {
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                        Toast.makeText(this@PremiumPackageActivity, "이미 구입한 상품입니다.", Toast.LENGTH_LONG).show()
                    }
                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                        Toast.makeText(this@PremiumPackageActivity, "구매를 취소하셨습니다.", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(this@PremiumPackageActivity, "error: $errorCode", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })

    }

    private fun setSkuDetailsView() {
        val builder = StringBuilder()
        for (skuDetail in mSkuDetails) {
            builder.append("<${skuDetail.title}>\n")
            builder.append(skuDetail.price)
            builder.append("\n======================\n\n")
        }
        //Toast.makeText(this@PremiumPackageActivity, "상품 $builder", Toast.LENGTH_LONG).show()
    }

    private fun applyPremiumPack(packageType: String) {
        var log = LogDTO("$packageType 결제 완료", Date())
        firebaseViewModel.writeUserLog(currentUser?.uid.toString(), log) {
            val oldExpireTime = SimpleDateFormat("yyyy.MM.dd HH:mm").format(currentUser?.premiumExpireTime!!)
            firebaseViewModel.applyPremiumPackage(currentUser?.uid.toString()) { userDTO ->
                if (userDTO != null) {
                    var log2 = LogDTO("[프리미엄 패키지 구매] 프리미엄 패키지 구매 완료. 만료일 : ($oldExpireTime -> ${SimpleDateFormat("yyyy.MM.dd HH:mm").format(userDTO.premiumExpireTime!!)})", Date())
                    firebaseViewModel.writeUserLog(currentUser?.uid.toString(), log2) { }

                    Toast.makeText(this, "프리미엄 패키지 구매 완료!", Toast.LENGTH_SHORT).show()

                    addPremiumGem(preferencesDTO?.rewardPremiumPackBuyGem!!, false)
                }
            }
        }

        /*var oldExpireTime = ""
        var tsDoc = firestore?.collection("user")?.document(currentUser?.uid.toString())
        firestore?.runTransaction { transaction ->
            val user = transaction.get(tsDoc!!).toObject(UserDTO::class.java)
            oldExpireTime = SimpleDateFormat("yyyy.MM.dd HH:mm").format(user?.premiumExpireTime)

            val calendar= Calendar.getInstance()
            // 프리미엄 패키지 만료전에 갱신 시 남은 날짜 + 30일
            if (user?.premiumExpireTime!! > calendar.time) {
                calendar.time = user?.premiumExpireTime
                calendar.add(Calendar.DATE, 30)
            } else { // 새로 구입 시 오늘 날짜 + 29일 (총 30일)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.add(Calendar.DATE, 29)
            }

            user?.premiumExpireTime = calendar.time
            currentUser = user

            transaction.set(tsDoc, user!!)
        }?.addOnSuccessListener { result ->
            var log = LogDTO("[프리미엄 패키지 구매] 프리미엄 패키지 구매 완료. 만료일 : ($oldExpireTime -> ${SimpleDateFormat("yyyy.MM.dd HH:mm").format(currentUser?.premiumExpireTime)})", Date())
            firestore?.collection("user")?.document(currentUser?.uid.toString())?.collection("log")?.document()?.set(log)

            Toast.makeText(this, "프리미엄 패키지 구매 완료!", Toast.LENGTH_SHORT).show()

            addPremiumGem(200, false)
        }?.addOnFailureListener { e ->

        }*/
    }

    private fun addPremiumGem(gemCount: Int, isDayGem: Boolean) {
        loading()

        val oldPaidGem = currentUser?.paidGem!!
        firebaseViewModel.addUserGem(currentUser?.uid.toString(), gemCount, 0) { userDTO ->
            if (userDTO != null) {
                if (isDayGem) { // 매일 받는 다이아 일 때 수령 받은 날짜 기록
                    userDTO.premiumGemGetTime = Date()
                    firebaseViewModel.updateUserPremiumGemGetTime(userDTO) { }
                }
                currentUser = userDTO

                var log = LogDTO("[프리미엄 패키지 다이아] 다이아 $gemCount 추가 (paidGem : $oldPaidGem -> ${userDTO.paidGem}, freeGem : ${userDTO.freeGem}, totalGem : ${userDTO.paidGem!! + userDTO.freeGem!!}))", Date())
                firebaseViewModel.writeUserLog(currentUser?.uid.toString(), log) { }

                setInfo()

                loadingEnd()

                if (getItemDialog == null) {
                    getItemDialog = GetItemDialog(this)
                    getItemDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    getItemDialog?.setCanceledOnTouchOutside(false)
                }
                getItemDialog?.mailDTO = MailDTO("", "", "", "", MailDTO.Item.PAID_GEM, gemCount)
                getItemDialog?.show()
                getItemDialog?.setInfo()

                getItemDialog?.binding?.buttonGetItemOk?.setOnClickListener {
                    getItemDialog?.dismiss()
                }
            }
        }

        /*var oldCount = 0
        var newCount = 0
        var freeGemCount = 0
        var tsDoc = firestore?.collection("user")?.document(currentUser?.uid.toString())
        firestore?.runTransaction { transaction ->
            val user = transaction.get(tsDoc!!).toObject(UserDTO::class.java)
            if (isDayGem) { // 매일 받는 다이아 일때만 날짜 기록
                user?.premiumGemGetTime = Date()
            }

            oldCount = user?.paidGem!!
            user?.paidGem = user?.paidGem?.plus(gemCount)

            newCount = user?.paidGem!!
            freeGemCount = user?.freeGem!!
            currentUser = user

            transaction.set(tsDoc, user!!)
        }?.addOnSuccessListener { result ->
            var log = LogDTO("[프리미엄 패키지 다이아] 다이아 $gemCount 추가 (paidGem : $oldCount -> $newCount, freeGem : $freeGemCount, totalGem : ${newCount+freeGemCount}))", Date())
            firestore?.collection("user")?.document(currentUser?.uid.toString())?.collection("log")?.document()?.set(log)

            setInfo()

            loadingEnd()

            val getDialog = GetItemDialog(this)
            getDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            getDialog.setCanceledOnTouchOutside(false)
            getDialog.mailDTO = MailDTO("", "", "", "", MailDTO.Item.PAID_GEM, gemCount)
            getDialog.show()

            getDialog.binding.buttonGetItemOk.setOnClickListener {
                getDialog.dismiss()
            }
        }?.addOnFailureListener { e ->

        }*/
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
}