package com.ados.myfanclub.util

import android.app.Activity
import android.widget.Toast
import com.ados.myfanclub.model.AdPolicyDTO
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

open class AdsManager() {
    companion object {
        // 광고 종류
        const val AD_TYPE_ADMOB = "admob"
        const val AD_TYPE_CAULY = "cauly"
        const val AD_TYPE_ADFIT = "adfit"
        const val AD_TYPE_FACEBOOK = "facebook"
        const val AD_TYPE_UNITY = "unity"

        // 광고 ID
        const val ADMOB_ID_BANNER = "ca-app-pub-1859147676618347/4227002193"
        const val ADMOB_ID_INTERSTITIAL = "ca-app-pub-1859147676618347/3815938659"
        const val ADMOB_ID_NATIVE = "ca-app-pub-1859147676618347/2613348252"
        const val ADMOB_ID_REWARD_USER_GEM = "ca-app-pub-1859147676618347/1390738250"
        const val ADMOB_ID_REWARD_USER_EXP = "ca-app-pub-1859147676618347/7026208312"
        const val ADMOB_ID_REWARD_FAN_CLUB_GEM = "ca-app-pub-1859147676618347/6963656802"
        const val ADMOB_ID_REWARD_FAN_CLUB_EXP = "ca-app-pub-1859147676618347/9398248450"

        // 광고 테스트 ID
        const val ADMOB_TEST_ID_BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val ADMOB_TEST_ID_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
        const val ADMOB_TEST_ID_NATIVE = "ca-app-pub-3940256099942544/2247696110"
        const val ADMOB_TEST_ID_REWARD = "ca-app-pub-3940256099942544/5224354917"
    }

    var admobID = ""
}

class AdsInterstitialManager(val activity: Activity, private val adPolicyDTO: AdPolicyDTO) : AdsManager() {
    private var mInterstitialAd: InterstitialAd? = null

    init {
        //admobID = ADMOB_ID_INTERSTITIAL
        admobID = ADMOB_TEST_ID_INTERSTITIAL // @테스트
    }

    fun callInterstitial(myCallback: (Boolean) -> Unit) {
        when (adPolicyDTO.ad_interstitial) {
            AD_TYPE_ADMOB -> {
                interstitialAdmob {
                    myCallback(it)
                }
            }
            AD_TYPE_CAULY -> {
                interstitialAdmob {
                    myCallback(it)
                }
            }
            else -> {

            }
        }
    }

    private fun interstitialAdmob(myCallback: (Boolean) -> Unit) {
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(activity, admobID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                //Log.d(TAG, adError?.message)
                println("광고 로드 실패 ${adError.message}")
                mInterstitialAd = null
                myCallback(false)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                //Log.d(TAG, 'Ad was loaded.')
                println("광고 로드 성공")
                mInterstitialAd = interstitialAd

                mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        //Log.d(TAG, 'Ad was dismissed.')
                        println("광고 Ad was dismissed.")
                        myCallback(true)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                        //Log.d(TAG, 'Ad failed to show.')
                        println("광고 Ad failed to show.")
                        myCallback(false)
                    }

                    override fun onAdShowedFullScreenContent() {
                        //Log.d(TAG, 'Ad showed fullscreen content.')
                        println("광고 Ad showed fullscreen content.")
                        mInterstitialAd = null
                    }
                }

                if (mInterstitialAd != null) {
                    mInterstitialAd?.show(activity)
                    println("광고 Show")
                } else {
                    //Log.d("TAG", "The interstitial ad wasn't ready yet.")
                    println("광고 null")
                    myCallback(false)
                }
            }
        })
    }

    /*fun interstitialCauly(isFirst : Boolean) {
        var adInfo: CaulyAdInfo
        adInfo = CaulyAdInfoBuilder("5ja4etfN").build()
        var interstial = CaulyInterstitialAd()
        interstial.setAdInfo(adInfo)

        val adCallback = object : CaulyInterstitialAdListener {
            override fun onReceiveInterstitialAd(p0: CaulyInterstitialAd?, p1: Boolean) {
                p0?.show()
            }

            override fun onFailedToReceiveInterstitialAd(p0: CaulyInterstitialAd?, p1: Int, p2: String?) {
                if (isFirst) {
                    interstitialAdmob(false)
                } else {
                    startActivity(mainIntent)
                    finish()
                }
            }

            override fun onClosedInterstitialAd(p0: CaulyInterstitialAd?) {
                startActivity(mainIntent)
                finish()
            }

            override fun onLeaveInterstitialAd(p0: CaulyInterstitialAd?) {

            }

        }

        interstial.setInterstialAdListener(adCallback)
        interstial.requestInterstitialAd(this)
    }*/
}

class AdsRewardManager(val activity: Activity, private val adPolicyDTO: AdPolicyDTO, rewardType: RewardType) : AdsManager() {
    enum class RewardType {
        REWARD_USER_GEM, REWARD_USER_EXP, REWARD_FAN_CLUB_GEM, REWARD_FAN_CLUB_EXP
    }

    private var adsInterstitialManager = AdsInterstitialManager(activity, adPolicyDTO)
    private var mRewardedAdmob: RewardedAd? = null
    private var isRunReward = false

    init {
        admobID = when (rewardType) {
            RewardType.REWARD_USER_GEM -> ADMOB_ID_REWARD_USER_GEM
            RewardType.REWARD_USER_EXP -> ADMOB_ID_REWARD_USER_EXP
            RewardType.REWARD_FAN_CLUB_GEM -> ADMOB_ID_REWARD_FAN_CLUB_GEM
            RewardType.REWARD_FAN_CLUB_EXP -> ADMOB_ID_REWARD_FAN_CLUB_EXP
        }
        admobID = ADMOB_TEST_ID_REWARD // @테스트

        loadRewardedAdmob()
    }

    fun callReward(myCallback: (Boolean) -> Unit) {
        if (!isRunReward) {
            isRunReward = true
            showReward(adPolicyDTO.ad_reward1.toString()) { reward1 ->
                if (!reward1) {
                    showReward(adPolicyDTO.ad_reward1.toString()) { reward2 ->
                        if (!reward2) {
                            showReward(adPolicyDTO.ad_reward1.toString()) { reward3 ->
                                if (!reward3) {
                                    adsInterstitialManager.callInterstitial {
                                        isRunReward = false
                                        myCallback(it)
                                    }
                                } else {
                                    isRunReward = false
                                    myCallback(true)
                                }
                            }
                        } else {
                            isRunReward = false
                            myCallback(true)
                        }
                    }
                } else {
                    isRunReward = false
                    myCallback(true)
                }
            }
        } else {
            //Toast.makeText(activity, "아직 광고를 시청할 수 없습니다.", Toast.LENGTH_SHORT).show()
            myCallback(false)
        }
    }

    private fun showReward(adType: String, myCallback: (Boolean) -> Unit) {
        when (adType) {
            AD_TYPE_ADMOB -> {
                showRewardAdmob {
                    myCallback(it)
                }
            }
            AD_TYPE_FACEBOOK -> {
                showRewardAdmob {
                    myCallback(it)
                }
            }
            else -> {

            }
        }
    }

    private fun loadRewardedAdmob() {
        var adRequest = AdRequest.Builder().build()

        RewardedAd.load(activity, admobID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                //Log.d(TAG, adError?.message)
                mRewardedAdmob = null
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                //Log.d(TAG, "Ad was loaded.")
                mRewardedAdmob = rewardedAd
            }
        })
    }

    private fun showRewardAdmob(myCallback: (Boolean) -> Unit) {
        if (mRewardedAdmob != null) {
            mRewardedAdmob?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    //Log.d(TAG, "Ad was shown.")
                    loadRewardedAdmob()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    // Called when ad fails to show.
                    //Log.d(TAG, "Ad failed to show.")
                }

                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    // Set the ad reference to null so you don't show the ad a second time.
                    //Log.d(TAG, "Ad was dismissed.")
                    mRewardedAdmob = null
                    loadRewardedAdmob()
                }
            }
            mRewardedAdmob?.show(activity) { _ ->
                //var rewardAmount = rewardItem.amount
                //var rewardType = rewardItem.type
                myCallback(true)
            }
        } else {
            //Toast.makeText(activity, "아직 광고를 시청할 수 없습니다.", Toast.LENGTH_SHORT).show()
            //Log.d(TAG, "The rewarded ad wasn't ready yet.")
            //Toast.makeText(activity, "The rewarded ad wasn't ready yet.", Toast.LENGTH_SHORT).show()
            myCallback(false)
        }
    }
}

