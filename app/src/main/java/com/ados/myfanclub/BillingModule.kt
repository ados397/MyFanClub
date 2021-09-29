package com.ados.myfanclub

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import com.android.billingclient.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillingModule(
    private val activity: Activity,
    private val lifeCycleScope: LifecycleCoroutineScope,
    private val callback: Callback
) {
    interface Callback {
        fun onBillingModulesIsReady()
        fun onSuccess(purchase: Purchase)
        fun onFailure(errorCode: Int)
    }

    // 구매관련 업데이트 수신
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when {
            billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null -> {
                // 제대로 구매 완료, 구매 확인 처리를 해야합니다. 3일 이내 구매확인하지 않으면 자동으로 환불됩니다.
                for (purchase in purchases) {
                    confirmPurchase(purchase)
                }
            }
            else -> {
                // 구매 실패
                callback.onFailure(billingResult.responseCode)
            }
        }
    }

    private var billingClient: BillingClient = BillingClient.newBuilder(activity)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()


    private fun confirmPurchase(purchase: Purchase) {

    }

    init {
        billingClient.startConnection(object: BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // 여기서부터 billingClient 활성화 됨
                    callback.onBillingModulesIsReady()
                } else {
                    callback.onFailure(billingResult.responseCode)
                }
            }

            override fun onBillingServiceDisconnected() {
                // GooglePlay와 연결이 끊어졌을때 재시도하는 로직이 들어갈 수 있음.
                Log.e("BillingModule", "Disconnected.")
            }
        })
    }

    /**
     * 원하는 sku id를 가지고있는 상품 정보를 가져옵니다.
     * @param sku sku 목록
     * @param resultBlock sku 상품정보 콜백
     */
    fun querySkuDetail(
        type: String = BillingClient.SkuType.INAPP,
        vararg sku: String,
        resultBlock: (List<SkuDetails>) -> Unit = {}
    ) {
        SkuDetailsParams.newBuilder().apply {
            // 인앱, 정기결제 유형중에서 고름. (SkuType.INAPP, SkuType.SUBS)
            setSkusList(sku.asList()).setType(type)
            // 비동기적으로 상품정보를 가져옵니다.
            lifeCycleScope.launch(Dispatchers.IO) {
                val skuDetailResult = billingClient.querySkuDetails(build())
                withContext(Dispatchers.Main) {
                    resultBlock(skuDetailResult.skuDetailsList ?: emptyList())
                }
            }
        }
    }

    /**
     * 구매 시작하기
     * @param skuDetail 구매하고자하는 항목. querySkuDetail()을 통해 획득한 SkuDetail
     */
    fun purchase(
        skuDetail: SkuDetails
    ) {
        val flowParams = BillingFlowParams.newBuilder().apply {
            setSkuDetails(skuDetail)
        }.build()

        // 구매 절차를 시작, OK라면 제대로 된것입니다.
        val responseCode = billingClient.launchBillingFlow(activity, flowParams).responseCode
        if (responseCode != BillingClient.BillingResponseCode.OK) {
            callback.onFailure(responseCode)
        }
        // 이후 부터는 purchasesUpdatedListener를 거치게 됩니다.
    }
}