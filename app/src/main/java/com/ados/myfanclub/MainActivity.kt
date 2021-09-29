package com.ados.myfanclub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.fragment.app.FragmentActivity
import com.ados.myfanclub.databinding.ActivityMainBinding
import com.ados.myfanclub.dialog.ChargeGemDialog
import com.ados.myfanclub.dialog.DisplayBoardDialog
import com.ados.myfanclub.dialog.QuestionDialog
import com.ados.myfanclub.model.*
import com.ados.myfanclub.page.ZoomOutPageTransformer
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.charge_gem_dialog.*
import kotlinx.android.synthetic.main.display_board_dialog.*
import kotlinx.android.synthetic.main.mission_dialog.*
import kotlinx.android.synthetic.main.question_dialog.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val tabIcon = listOf(
        R.drawable.dashboard,
        R.drawable.fan_club,
        R.drawable.schedule,
        R.drawable.user,
    )

    private var firestore : FirebaseFirestore? = null

    private var currentUser: UserDTO? = null
    private var currentFanClub: FanClubDTO? = null
    private var currentMember: MemberDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = intent.getParcelableExtra("user")
        if (currentUser == null) {
            val question = QuestionDTO(
                QuestionDTO.STAT.ERROR,
                "사용자 확인 실패",
                "로그인 정보가 올바르지 않습니다.\n앱을 종료합니다."
            )
            val dialog = QuestionDialog(this, question)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            dialog.showButtonOk(false)
            dialog.setButtonCancel("확인")
            dialog.button_question_cancel.setOnClickListener { // No
                dialog.dismiss()
                appExit()
            }
        }

        val anim = AlphaAnimation(0.1f, 1.0f)
        anim.duration = 800
        anim.startOffset = 20
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        binding.displayBoard.textDisplayBoard.startAnimation(anim)
        binding.displayBoard.textDisplayBoard.requestFocus()
        binding.displayBoard.textDisplayBoard.setOnFocusChangeListener { view, b ->
            if (!b) {
                binding.displayBoard.textDisplayBoard.requestFocus()
            }
        }

        firestore = FirebaseFirestore.getInstance()

        if (currentUser?.fanClubId == null) {
            setViewPager()
        } else {
            firestore?.collection("fanClub")?.document(currentUser?.fanClubId.toString())?.get()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (task.result.exists()) { // document 있음
                            currentFanClub = task.result.toObject(FanClubDTO::class.java)!!
                            firestore?.collection("fanClub")
                                ?.document(currentUser?.fanClubId.toString())?.collection("member")
                                ?.document(currentUser?.uid.toString())?.get()
                                ?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        if (task.result.exists()) { // document 있음
                                            currentMember = task.result.toObject(MemberDTO::class.java)!!
                                        }
                                    }
                                    setViewPager()
                                }
                        } else {
                            // 팬클럽 정보 가져오기 실패
                        }


                    }
                }
        }

        firestore?.collection("displayBoard")?.orderBy("order", Query.Direction.DESCENDING)?.limit(1)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if(querySnapshot == null)return@addSnapshotListener
            for(snapshot in querySnapshot){
                var displayBoardDTO = snapshot.toObject(DisplayBoardDTO::class.java)!!
                binding.displayBoard.textDisplayBoard.text = displayBoardDTO.displayText
                binding.displayBoard.textDisplayBoard.setTextColor(displayBoardDTO.color!!)
            }
        }

        binding.displayBoard.layoutMain.setOnClickListener {
            val dialog = DisplayBoardDialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.currentUser = currentUser
            dialog.show()

            dialog.button_display_board_ok.setOnClickListener { // Ok
                dialog.dismiss()
            }
        }

        binding.layoutGem.setOnClickListener {
            /*val dialog = ChargeGemDialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

            dialog.button_charge_cancel.setOnClickListener { // No
                dialog.dismiss()
            }

            dialog.button_charge_ok.setOnClickListener { // Ok
                dialog.dismiss()
            }*/

            startActivity(Intent(this, BuyGemActivity::class.java))
        }
    }

    fun refreshFanClubDTO(myCallback: (FanClubDTO) -> Unit) {
        firestore?.collection("fanClub")?.document(currentUser?.fanClubId.toString())?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.exists()) {
                currentFanClub = task.result.toObject(FanClubDTO::class.java)!!
                myCallback(currentFanClub!!)
            }
        }
    }

    fun refreshMemberDTO(myCallback: (MemberDTO) -> Unit) {
        firestore?.collection("fanClub")?.document(currentUser?.fanClubId.toString())
            ?.collection("member")?.document(currentUser?.uid.toString())?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result.exists()) {
                    currentMember = task.result.toObject(MemberDTO::class.java)!!
                    myCallback(currentMember!!)
                }
            }
    }

    private fun setViewPager() {
        binding.viewpager.isUserInputEnabled = false // 좌우 터치 스와이프 금지
        binding.viewpager.apply {
            adapter = MyPagerAdapter(
                context as FragmentActivity,
                currentFanClub,
                currentMember
            )
            setPageTransformer(ZoomOutPageTransformer())
        }

        TabLayoutMediator(
            binding.tabs,
            binding.viewpager
        ) { tab, position ->
            //tab.text = "${tabLayoutText[position]}"
            tab.setIcon(tabIcon[position])
        }.attach()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        appExit()
    }

    fun getUser() : UserDTO {
        return currentUser!!
    }

    fun setUser(user: UserDTO) {
        currentUser = user
    }

    fun setFanClub(fanClub: FanClubDTO) {
        currentFanClub = fanClub
    }

    fun setMember(member: MemberDTO) {
        currentMember = member
    }

    private fun appExit() {
        finishAffinity() //해당 앱의 루트 액티비티를 종료시킨다. (API  16미만은 ActivityCompat.finishAffinity())
        System.runFinalization() //현재 작업중인 쓰레드가 다 종료되면, 종료 시키라는 명령어이다.
        exitProcess(0) // 현재 액티비티를 종료시킨다.
    }
}