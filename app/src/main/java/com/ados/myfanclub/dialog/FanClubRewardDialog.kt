package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FanClubRewardDialogBinding
import com.ados.myfanclub.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*
import kotlin.collections.ArrayList


class FanClubRewardDialog(context: Context) : Dialog(context), OnFanClubRewardItemClickListener {

    lateinit var binding: FanClubRewardDialogBinding
    private val layout = R.layout.fan_club_reward_dialog
    private var firestore : FirebaseFirestore? = null
    lateinit var recyclerViewAdapter : RecyclerViewAdapterFanClubReward

    private var getItemDialog : GetItemDialog? = null

    private var fanClubRewards : ArrayList<FanClubRewardDTO> = arrayListOf()
    var mainActivity: MainActivity? = null
    var fanClubDTO: FanClubDTO? = null
    var currentMember: MemberDTO? = null
    var fanClubCheckoutCount: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FanClubRewardDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        //window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        firestore = FirebaseFirestore.getInstance()

        val layoutManager = LinearLayoutManager(context)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        binding.rvFanClubReward.layoutManager = layoutManager

        setInfo()

        binding.buttonGetAll.setOnClickListener {
            // 받을 보상수와 받을 다이아수 획득
            var rewardCount = 0
            var gemCount = 0
            var rewards = arrayListOf<FanClubRewardDTO>()
            for (reward in fanClubRewards) {
                if (fanClubCheckoutCount!! >= reward.checkoutCount!!) {
                    rewardCount++
                    if (!reward.isRewardGemGet()) {
                        gemCount = gemCount.plus(reward.gemCount!!)
                        reward.rewardGemGetTime = Date()
                        rewards.add(reward)
                    }
                }
            }

            if (gemCount > 0) { // 받을 아이템이 있다면 작업
                mainActivity?.loading()
                addGem(gemCount, rewards, true)
            } else {
                Toast.makeText(context, "받을 보상이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setInfo() {
        binding.textGemCount.text = "${fanClubDTO?.getCheckoutGemCount()}개"

        firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.collection("member")?.document(currentMember?.userUid.toString())?.collection("reward")?.orderBy("checkoutCount", Query.Direction.DESCENDING)?.get()?.addOnCompleteListener { task ->
            fanClubRewards.clear()
            if(task.isSuccessful) {
                for (document in task.result) {
                    var reward = document.toObject(FanClubRewardDTO::class.java)
                    fanClubRewards.add(reward)
                }

                // 보상 리스트가 firestore에 없다면 수동으로 추가 후 정렬
                if (fanClubDTO?.getCheckoutRewardCount() != fanClubRewards.size) {
                    for (i in 0 until fanClubDTO?.getCheckoutRewardCount()!!) {
                        // 출석체크 최초 30명, 이후 150명 마다 다이아 보상 획득 가능
                        val rewardCount = if (i == 0) 30 else i.times(150)
                        val gemCount = if (rewardCount.rem(600) == 0) 2 else 1 // 600명 단위 출석체크는 다이아 보상 2개 (600명, 1200명, 1800명, 2400명... 15000명)

                        var isFind = false
                        for (reward in fanClubRewards) {
                            if (reward.checkoutCount == rewardCount) {
                                isFind = true
                                break
                            }
                        }
                        if (!isFind) {
                            fanClubRewards.add(FanClubRewardDTO("${rewardCount}reward", rewardCount, gemCount))
                        }
                    }
                    fanClubRewards.sortByDescending { it.checkoutCount }
                }
                setAdapter()
                //binding.rvFanClubReward.scrollToPosition(0)
            }
        }
    }

    private fun init() {
        //button_ok.setOnClickListener(this)
    }

    private fun setAdapter() {
        recyclerViewAdapter = RecyclerViewAdapterFanClubReward(fanClubRewards, fanClubCheckoutCount!!, this)
        //recyclerViewAdapter = RecyclerViewAdapterFanClubReward(fanClubRewards, 800, this)
        binding.rvFanClubReward.adapter = recyclerViewAdapter
    }

    override fun onItemClick(item: FanClubRewardDTO, position: Int) {
        mainActivity?.loading()
        addGem(item.gemCount!!, arrayListOf(item), false)
        item.rewardGemGetTime = Date()
        recyclerViewAdapter.notifyItemChanged(position)
    }

    private fun addGem(gemCount: Int, rewards: ArrayList<FanClubRewardDTO>, isRefresh: Boolean) {
        var oldFreeGemCount = 0
        var userDTO = mainActivity?.getUser()
        var tsDoc = firestore?.collection("user")?.document(userDTO?.uid.toString())
        firestore?.runTransaction { transaction ->
            val user = transaction.get(tsDoc!!).toObject(UserDTO::class.java)
            oldFreeGemCount = user?.freeGem!!

            user.freeGem = user.freeGem?.plus(gemCount)

            userDTO = user

            transaction.set(tsDoc, user)
        }?.addOnSuccessListener {
            var log = LogDTO("[팬클럽 출석체크 보상] 다이아 $gemCount 획득 (freeGem : $oldFreeGemCount -> ${userDTO?.freeGem})", Date())
            firestore?.collection("user")?.document(userDTO?.uid.toString())?.collection("log")?.document()?.set(log)

            for (reward in rewards) {
                firestore?.collection("fanClub")?.document(fanClubDTO?.docName.toString())?.collection("member")
                    ?.document(currentMember?.userUid.toString())?.collection("reward")?.document(reward.docName.toString())?.set(reward)
            }

            if (isRefresh) {
                setAdapter()
            }
            mainActivity?.loadingEnd()

            if (getItemDialog == null) {
                getItemDialog = GetItemDialog(context)
                getItemDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                getItemDialog?.setCanceledOnTouchOutside(false)
            }
            getItemDialog?.mailDTO = MailDTO("", "", "", "", MailDTO.Item.FREE_GEM, gemCount)
            getItemDialog?.show()
            getItemDialog?.setInfo()

            getItemDialog?.binding?.buttonGetItemOk?.setOnClickListener {
                getItemDialog?.dismiss()
            }
        }?.addOnFailureListener {

        }
    }
}