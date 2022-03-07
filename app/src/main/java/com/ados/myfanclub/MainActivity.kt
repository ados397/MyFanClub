package com.ados.myfanclub

import android.content.Intent
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.ados.myfanclub.database.DBHelperReport
import com.ados.myfanclub.databinding.ActivityMainBinding
import com.ados.myfanclub.dialog.*
import com.ados.myfanclub.model.*
import com.ados.myfanclub.page.ZoomOutPageTransformer
import com.ados.myfanclub.viewmodel.FirebaseStorageViewModel
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fan_club_question_dialog.*
import kotlinx.android.synthetic.main.gem_question_dialog.*
import kotlinx.android.synthetic.main.notice_dialog.*
import kotlinx.android.synthetic.main.question_dialog.*
import kotlinx.android.synthetic.main.tutorial_dialog.*
import java.io.FileDescriptor
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    var decimalFormat: DecimalFormat = DecimalFormat("###,###")

    private lateinit var binding: ActivityMainBinding
    private val tabIcon = listOf(
        R.drawable.dashboard,
        R.drawable.fan_club,
        R.drawable.schedule,
        R.drawable.user,
    )

    // 뷰모델 연결
    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private val firebaseStorageViewModel : FirebaseStorageViewModel by viewModels()

    private var firebaseAuth : FirebaseAuth? = null
    private var googleSignInClient : GoogleSignInClient? = null
    private var loadingDialog : LoadingDialog? = null

    private var oldUserDTO: UserDTO? = null
    private var currentUserExDTO = UserExDTO()
    private var oldFanClubDTO: FanClubDTO? = null
    private var currentFanClubExDTO = FanClubExDTO()
    private var oldMemberDTO: MemberDTO? = null

    private var backWaitTime = 0L //뒤로가기 연속 클릭 대기 시간
    private var displayCount = 0 // 전광판 일정 시간 유지를 위한 변수
    private var displayList = arrayListOf<DisplayBoardDTO>() // 표시할 전광판을 Queue 형식으로 저장
    private lateinit var currentDate : String // 12시 지나서 날짜 변경을 체크하기 위한 변수

    // 최초에 모든 항목들이 로딩 완료 되었을 때 ViewPager 호출
    // currentLoadingCount 가 successLoadingCount 와 같아져야 로딩이 완료된 것
    private var checkLoadingCount = 0 // 일정 시간이 지나도 완료가 안되면 강제로 완료 처리
    private var currentLoadingCount = 0
    private var successLoadingCount = 9
    private var isFirstRun = false // 최초 실행을 체크하기 위한 변수

    private var tutorialStep : MutableLiveData<Int> = MutableLiveData() // 튜토리얼 진행상태
    lateinit var dbHandler : DBHelperReport

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tutorialStep.value = 0

        updateCheck()

        autoTimeCheck()

        loading()
        currentDate = SimpleDateFormat("yyyyMMdd").format(Date())
        dbHandler = DBHelperReport(this)

        firebaseAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //기본 로그인 방식 사용
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

        // 로그인 체크
        loginCheck()

        setInfo()

        firebaseViewModel.getToken() // 토큰 획득
        firebaseViewModel.token.observe(this) {
            // 토큰이 변경되었다면 데이터 반영
            if(it != oldUserDTO?.token) {
                Log.d(TAG, "profileLoad: 토큰 변경되었음.")

                oldUserDTO?.token = it
                firebaseViewModel.updateUserToken(oldUserDTO!!) {
                    Log.d(TAG, "사용자 토큰 정보 업데이트 완료.")
                }

                if (!oldUserDTO?.fanClubId.isNullOrEmpty()) {
                    firebaseViewModel.updateMemberToken(oldUserDTO!!) {
                        Log.d(TAG, "팬클럽 멤버 토큰 정보 업데이트 완료.")
                    }
                }
            }
        }

        // 로그인 시간 기록 (중복 로그인 방지)
        oldUserDTO?.loginTime = Date()
        firebaseViewModel.updateUserLoginTime(oldUserDTO!!) {
            firebaseViewModel.getUserListen(oldUserDTO?.uid.toString()) // 사용자 정보 획득
            println("[로딩] 로그인 시간 기록")
            addLoadingCount()
        }

        // 광고 설정 획득
        firebaseViewModel.getAdPolicy()
        firebaseViewModel.adPolicyDTO.observe(this) {
            println("[로딩] 광고 획득")
            addLoadingCount()
        }

        // 환경 설정 획득
        firebaseViewModel.getPreferences()
        firebaseViewModel.preferencesDTO.observe(this) {
            println("[로딩] 환경 설정 획득")
            addLoadingCount()
        }

        firebaseViewModel.getNotices(true) // 공지사항 리스트 획득
        observeNotice()

        firebaseViewModel.getDisplayBoardListen() // 전광판 리스트 획득
        observeDisplayBoard() // 실시간 전광판 모니터링

        firebaseViewModel.getMailsListen(oldUserDTO?.uid.toString()) // 메일 리스트 획득
        observeMail() // 실시간 메일 모니터링

        observeUser() // 실시간 사용자 정보 모니터링
        observeFanClub() // 실시간 팬클럽 정보 모니터링
        observeMember() // 실시간 팬클럽 멤버 정보 모니터링
        observeTutorial() // 튜토리얼

        // 데이터 획득이 끝나면 ViewPager 호출
        timer(period = 100)
        {
            if (checkLoadingCount > 100) {
                cancel()
                runOnUiThread {
                    failedLoading()
                }
            }

            if (currentLoadingCount >= successLoadingCount) {
                println("[로딩] 카운트 $checkLoadingCount")
                cancel()
                runOnUiThread {
                    isFirstRun = true
                    loadingEnd()
                    setViewPager()
                    tutorialStart()
                    onNoticeDialog()
                }
            }
            checkLoadingCount++
        }

        binding.displayBoard.layoutMain.setOnClickListener {
            var intent = Intent(this, DisplayBoardActivity::class.java)
            intent.putExtra("user", firebaseViewModel.userDTO.value)
            intent.putExtra("preferences", firebaseViewModel.preferencesDTO.value)
            startActivity(intent)
        }

        binding.layoutPremium.setOnClickListener {
            var intent = Intent(this, PremiumPackageActivity::class.java)
            intent.putExtra("user", firebaseViewModel.userDTO.value)
            intent.putExtra("preferences", firebaseViewModel.preferencesDTO.value)
            startActivity(intent)
        }

        binding.layoutGem.setOnClickListener {
            var intent = Intent(this, BuyGemActivity::class.java)
            intent.putExtra("user", firebaseViewModel.userDTO.value)
            startActivity(intent)
        }

        binding.layoutMail.setOnClickListener {
            var intent = Intent(this, MailActivity::class.java)
            intent.putExtra("user", firebaseViewModel.userDTO.value)
            //intent.putParcelableArrayListExtra("mails", mails)
            intent.putParcelableArrayListExtra("mails", firebaseViewModel.mailDTOs.value)
            startActivity(intent)
        }

        binding.layoutQuest.setOnClickListener {
            var intent = Intent(this, QuestActivity::class.java)
            intent.putExtra("user", firebaseViewModel.userDTO.value)
            startActivity(intent)
        }

        adminSendMail()
    }

    private fun tutorialStart() {
        if (firebaseViewModel.userDTO.value?.tutorialEndedTime == null) {
            var cancelCount = 0
            val tutorialDialog = TutorialDialog(this)
            tutorialDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            tutorialDialog.setCanceledOnTouchOutside(false)
            tutorialDialog.show()
            tutorialDialog.button_tutorial_cancel.setOnClickListener { // No
                if (cancelCount >= 1) {
                    tutorialDialog.dismiss()
                    finishTutorialStep(false) // 튜토리얼 취소
                    Toast.makeText(this, "튜토리얼이 취소되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "정말 취소하시려면 다시 한번 눌러주세요.", Toast.LENGTH_SHORT).show()
                    cancelCount++
                }
            }
            tutorialDialog.button_tutorial_ok.setOnClickListener { // Ok
                tutorialDialog.dismiss()
                binding.viewpager.currentItem = 0 // 메인 탭 이동
                tutorialStep.value = 1
            }
        }
    }

    private fun onNoticeDialog() {
        val noticeDialog = NoticeDialog(this)
        noticeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        noticeDialog.setCanceledOnTouchOutside(false)
        noticeDialog.noticeDTO = firebaseViewModel.noticeDTOs.value!![0]
        noticeDialog.show()
        noticeDialog.button_notice_ok.setOnClickListener {
            noticeDialog.dismiss()
        }
    }

    private fun autoTimeCheck() : Boolean {
        val result = android.provider.Settings.Global.getInt(contentResolver, android.provider.Settings.Global.AUTO_TIME, 0)
        if (result == 1) {
            println("DateTime Sync: On")
            return true
        } else {
            val question = QuestionDTO(
                QuestionDTO.Stat.ERROR,
                "[날짜 및 시간 자동설정]을 사용하지 않으면 마이팬클럽을 이용할 수 없습니다.",
                "설정에서 [날짜 및 시간 자동설정]을 사용함으로 변경 후 마이팬클럽을 이용해 주세요.\n\n마이팬클럽을 종료합니다."
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
            return false
        }
        return false
    }

    private fun addLoadingCount() {
        if (!isFirstRun) {
            currentLoadingCount++
        }
    }

    private fun failedLoading() {
        val question = QuestionDTO(
            QuestionDTO.Stat.ERROR,
            "데이터 로딩 실패",
            "데이터 로딩에 실패하였습니다.\n마이팬클럽 종료 후 다시 실행해 주세요."
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

    private fun loginCheck() {
        oldUserDTO = intent.getParcelableExtra("user")
        if (oldUserDTO == null) {
            val question = QuestionDTO(
                QuestionDTO.Stat.ERROR,
                "사용자 확인 실패",
                "로그인 정보가 올바르지 않습니다.\n마이팬클럽을 종료합니다."
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

        if (oldUserDTO?.fanClubId.isNullOrEmpty()) {
            successLoadingCount = successLoadingCount.minus(2)
        }
    }

    private fun setInfo() {
        binding.imgPremiumEnable.visibility = View.GONE
        binding.imgPremiumDisable.visibility = View.VISIBLE
        binding.imgPremiumNew.visibility = View.VISIBLE

        // 전광판 애니메이션 설정
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

        // 타이머 설정
        displayTimer()
        dateCheckTimer()
    }

    // 실시간 사용자 정보 모니터링
    private fun observeUser() {
        firebaseViewModel.userDTO.observe(this) {
            currentUserExDTO?.userDTO = it
            if (currentUserExDTO?.userDTO?.imgProfile == null) {
                currentUserExDTO?.imgProfileUri = null
            } else {
                firebaseStorageViewModel.getUserProfile(it?.uid.toString()) { uri ->
                    currentUserExDTO?.imgProfileUri = uri
                }
            }

            // 차단 여부 확인
            checkBlock()

            // 팬클럽 상태 확인
            checkFanClubState()

            // 로그인 시간이 변경되었으면 중복 로그인으로 판단하여 종료
            checkMultiLogin()

            // 다이아 실시간 반영
            binding.textGemCount.text = "${decimalFormat.format(firebaseViewModel.userDTO.value?.paidGem!! + firebaseViewModel.userDTO.value?.freeGem!!)}"

            // 프리미엄 패키지
            checkPremium()

            // 일일 퀘스트 수령할 보상이 있다면 UI 출력
            checkQuestNew()

            oldUserDTO = it?.copy() // 기존 정보와 변경사항 체크를 위해 복사
            println("[로딩] 사용자 획득")
            addLoadingCount()
        }
    }

    // 실시간 팬클럽 정보 모니터링
    private fun observeFanClub() {
        firebaseViewModel.fanClubDTO.observe(this) { fanClubDTO ->
            currentFanClubExDTO?.fanClubDTO = fanClubDTO
            if (currentFanClubExDTO?.fanClubDTO?.imgSymbolCustom != null) {
                firebaseStorageViewModel.getFanClubSymbol(fanClubDTO?.docName.toString()) { uri ->
                    currentFanClubExDTO?.imgSymbolCustomUri = uri
                }
            }

            oldFanClubDTO = fanClubDTO?.copy() // 기존 정보와 변경사항 체크를 위해 복사
            println("[로딩] 팬클럽 획득")
            addLoadingCount()
        }
    }

    // 실시간 팬클럽 멤버 정보 모니터링
    private fun observeMember() {
        firebaseViewModel.memberDTO.observe(this) {
            oldMemberDTO = it?.copy() // 기존 정보와 변경사항 체크를 위해 복사
            println("[로딩] 멤버 획득")
            addLoadingCount()
        }
    }

    // 공지사항 모니터링
    private fun observeNotice() {
        firebaseViewModel.noticeDTOs.observe(this) {
            if (firebaseViewModel.noticeDTOs.value != null) {
                println("[로딩] 공지사항 획득")
                addLoadingCount()
            }
        }
    }

    // 실시간 전광판 모니터링
    private fun observeDisplayBoard() {
        firebaseViewModel.displayBoardDTO.observe(this) {
            if (firebaseViewModel.displayBoardDTO.value != null) {
                if (!firebaseViewModel.displayBoardDTO.value?.displayText.isNullOrEmpty()) {
                    // 차단된 전광판
                    var displayBoard = firebaseViewModel.displayBoardDTO.value!!
                    if (dbHandler?.getBlock(firebaseViewModel.displayBoardDTO.value!!.docName.toString())) {
                        displayBoard.displayText = "내가 신고한 글입니다."
                        displayBoard.color = ContextCompat.getColor(this, R.color.text_disable)
                    }

                    // 대기열에 없다면 즉시 전광판 표시
                    if (displayList.size == 0) {
                        binding.displayBoard.textDisplayBoard.text = displayBoard.displayText
                        binding.displayBoard.textDisplayBoard.setTextColor(displayBoard.color!!)
                    }

                    displayList.add(displayBoard)
                    println("[로딩] 전광판 획득 ${firebaseViewModel.displayBoardDTO.value!!}")
                    addLoadingCount()
                }
            }
        }
    }

    // 실시간 메일 모니터링
    private fun observeMail() {
        firebaseViewModel.mailDTOs.observe(this) {
            println("메일 참조 오류 ${firebaseViewModel.mailDTOs?.value}, ${firebaseViewModel.mailDTOs}")
            if (firebaseViewModel.mailDTOs?.value != null) {
                var count = 0
                if (firebaseViewModel.mailDTOs?.value!!.size > 0) {
                    // 읽지 않은 메일이 있으면 새로운 메일 알림 표시
                    for (mail in firebaseViewModel.mailDTOs?.value!!) {
                        if (mail.read == false) {
                            count++
                            break
                        }
                    }
                }
                if (count > 0) {
                    binding.imgMailNew.visibility = View.VISIBLE
                } else {
                    binding.imgMailNew.visibility = View.GONE
                }

                /*if (firebaseViewModel.mailDTOs.value?.size!! > 0) {
                    binding.imgMailNew.visibility = View.VISIBLE
                } else {
                    binding.imgMailNew.visibility = View.GONE
                }*/
                println("[로딩] 메일 획득")
                addLoadingCount()
            }
        }
    }

    private fun setViewPager() {
        binding.viewpager.isUserInputEnabled = false // 좌우 터치 스와이프 금지
        binding.viewpager.apply {
            adapter = MyPagerAdapter(
                context as FragmentActivity
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
        //super.onBackPressed()
        //appExit()

        if (tutorialStep.value == 0) { // 튜토리얼이 진행중이 아닐때만 종료
            if(System.currentTimeMillis() - backWaitTime >=2000 ) {
                backWaitTime = System.currentTimeMillis()
                Snackbar.make(binding.layoutMain,"'뒤로' 버튼을 한번 더 누르면 종료됩니다.", Snackbar.LENGTH_LONG).show()
            } else {
                finish() //액티비티 종료
            }
        }
    }

    fun getAdPolicy() : AdPolicyDTO {
        return firebaseViewModel.adPolicyDTO.value!!
    }

    fun getPreferences() : PreferencesDTO {
        return firebaseViewModel.preferencesDTO.value!!
    }

    fun getUser() : UserDTO {
        return firebaseViewModel.userDTO.value!!
    }

    fun getUserEx() : UserExDTO? {
        return currentUserExDTO
    }

    fun getFanClub() : FanClubDTO? {
        return firebaseViewModel.fanClubDTO.value
    }

    fun getFanClubEx() : FanClubExDTO? {
        return currentFanClubExDTO
    }

    fun getMember() : MemberDTO? {
        return firebaseViewModel.memberDTO.value
    }

    private fun finishMainActivity() {
        finish()
    }

    fun loading() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(this)
            loadingDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            loadingDialog?.setCanceledOnTouchOutside(false)
        }
        loadingDialog?.show()
    }

    fun loadingEnd() {
        android.os.Handler().postDelayed({
            if (loadingDialog != null) {
                loadingDialog?.dismiss()
            }
        }, 400)
    }

    private fun appExit() {
        finishAffinity() //해당 앱의 루트 액티비티를 종료시킨다. (API  16미만은 ActivityCompat.finishAffinity())
        System.runFinalization() //현재 작업중인 쓰레드가 다 종료되면, 종료 시키라는 명령어이다.
        exitProcess(0) // 현재 액티비티를 종료시킨다.
    }

    private fun adminSendMail() {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm")
        dateFormat.parse("2022.1.31 23:59")

        for (i in 1..1) {
        val docName = "master${System.currentTimeMillis()}"

            var mail = MailDTO(docName,"마이팬클럽 오픈 기념", "감사한 마음으로 다이아 10개를 모든 유저에게 드립니다.", "운영자", MailDTO.Item.FREE_GEM, 10, Date(), dateFormat.parse("2022.1.31 23:59"))
            //var mail = MailDTO(docName,"안녕하세요.", "반갑습니다.", "운영자", MailDTO.ITEM.NONE, 0, Date(), dateFormat.parse("2022-1-31 23:59"))
            //firebaseViewModel.sendUserMail(firebaseViewModel.userDTO.value?.uid.toString(), mail) { }
        }
    }

    // 사용자 차단 확인
    private fun checkBlock() {
        if (firebaseViewModel.userDTO.value?.isBlock()!!) {
            val question = QuestionDTO(
                QuestionDTO.Stat.ERROR,
                "회원님의 계정은 이용이 제한되었습니다.",
                "제한 일시 : ${SimpleDateFormat("yyyy.MM.dd HH:mm").format(firebaseViewModel.userDTO.value?.blockStartTime)}\n" +
                        "해제 일시 : ${SimpleDateFormat("yyyy.MM.dd HH:mm").format(firebaseViewModel.userDTO.value?.blockEndTime)}\n" +
                        "제한 사유 : ${firebaseViewModel.userDTO.value?.blockReason}\n\n" +
                        "마이팬클럽을 종료합니다."
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
    }

    // 로그인 시간이 변경되었으면 중복 로그인으로 판단하여 종료
    private fun checkMultiLogin() {
        if (oldUserDTO?.loginTime != firebaseViewModel.userDTO.value?.loginTime) {
            // 로그아웃 처리됨
            firebaseAuth?.signOut()
            //Auth.GoogleSignInApi.signOut()
            googleSignInClient?.signOut()?.addOnCompleteListener {

            }

            val question = QuestionDTO(
                QuestionDTO.Stat.WARNING,
                "중복 로그인",
                "다른 기기에서 해당 계정으로 접속하여 마이팬클럽을 종료합니다."
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
    }

    // 프리미엄 패키지 확인 및 UI 출력
    private fun checkPremium() {
        if (firebaseViewModel.userDTO.value?.isPremium()!!) {
            binding.imgPremiumEnable.visibility = View.VISIBLE
            binding.imgPremiumDisable.visibility = View.GONE
            when {
                firebaseViewModel.userDTO.value?.isPremiumRenew()!! || !firebaseViewModel.userDTO.value?.isPremiumGemGet()!! -> { // 프리미엄 패키지 갱신 기간이거나 매일 다이아 수령을 안했다면 알림 표시
                    binding.imgPremiumNew.visibility = View.VISIBLE
                }
                else -> {
                    binding.imgPremiumNew.visibility = View.GONE
                }
            }
        } else {
            binding.imgPremiumEnable.visibility = View.GONE
            binding.imgPremiumDisable.visibility = View.VISIBLE
            binding.imgPremiumNew.visibility = View.VISIBLE
        }
    }

    // 팬클럽 가입과 탈퇴 시 UI 출력
    private fun checkFanClubState() {
        if (oldUserDTO?.fanClubId.isNullOrEmpty() && !firebaseViewModel.userDTO.value?.fanClubId.isNullOrEmpty()) { // 팬클럽 가입 승인됨 (팬클럽 ID가 없다가 생김)
            // 팬클럽 정보 Listen
            firebaseViewModel.stopFanClubListen()
            firebaseViewModel.stopMemberListen()
            firebaseViewModel.getFanClubListen(firebaseViewModel.userDTO.value?.fanClubId.toString()) // 팬클럽 정보 획득
            firebaseViewModel.getMemberListen(firebaseViewModel.userDTO.value?.fanClubId.toString(), firebaseViewModel.userDTO.value?.uid.toString()) // 팬클럽 멤버 정보 획득

            // 팬클럽 가입 대화상자 호출
            val dialog = FanClubQuestionDialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

            dialog.button_fan_club_question_ok.setOnClickListener { // Ok
                dialog.dismiss()

                // 팬클럽 메인 화면 이동
                setViewPager()
                binding.viewpager.currentItem = 1
            }
        } else if (!oldUserDTO?.fanClubId.isNullOrEmpty() && firebaseViewModel.userDTO.value?.fanClubId.isNullOrEmpty()) { // 팬클럽 탈퇴 (팬클럽 ID가 있다가 없어짐)
            firebaseViewModel.stopFanClubListen()
            firebaseViewModel.stopMemberListen()

            if (firebaseViewModel.userDTO.value?.fanClubQuitDate == firebaseViewModel.userDTO.value?.fanClubDeportationDate) { // 팬클럽 추방
                val question = QuestionDTO(
                    QuestionDTO.Stat.WARNING,
                    "팬클럽 추방",
                    "가입하신 팬클럽에서 추방 되었습니다."
                )
                val dialog = QuestionDialog(this, question)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
                dialog.showButtonOk(false)
                dialog.setButtonCancel("확인")
                dialog.button_question_cancel.setOnClickListener { // No
                    dialog.dismiss()

                    // 팬클럽 정보를 초기화 시키고 ViewPager 다시 호출
                    setViewPager()
                }
            } else {
                // 팬클럽 정보를 초기화 시키고 ViewPager 다시 호출
                setViewPager()
                binding.viewpager.currentItem = 1
            }
        } else if (firebaseViewModel.userDTO.value?.fanClubId != firebaseViewModel.userDTO.value?.fanClubId) { // 팬클럽이 변경 (이럴 경우는 없을테지만 새로운 팬클럽 정보를 Listen 하도록 처리)
            firebaseViewModel.stopFanClubListen()
            firebaseViewModel.stopMemberListen()
            firebaseViewModel.getFanClubListen(firebaseViewModel.userDTO.value?.fanClubId.toString()) // 팬클럽 정보 획득
            firebaseViewModel.getMemberListen(firebaseViewModel.userDTO.value?.fanClubId.toString(), firebaseViewModel.userDTO.value?.uid.toString()) // 팬클럽 멤버 정보 획득
        } else if (!firebaseViewModel.userDTO.value?.fanClubId.isNullOrEmpty()) { // 팬클럽 정보가 있으면 데이터 획득
            firebaseViewModel.getFanClubListen(firebaseViewModel.userDTO.value?.fanClubId.toString()) // 팬클럽 정보 획득
            firebaseViewModel.getMemberListen(firebaseViewModel.userDTO.value?.fanClubId.toString(), firebaseViewModel.userDTO.value?.uid.toString()) // 팬클럽 멤버 정보 획득
        } else { // 팬클럽 정보가 없으면 데이터 획득 중지
            firebaseViewModel.stopFanClubListen()
            firebaseViewModel.stopMemberListen()
        }
    }

    // 일일 퀘스트 수령할 보상이 있다면 UI 출력
    private fun checkQuestNew() {
        var successCount = 0
        var isQuestNew = false
        for (i in 1..firebaseViewModel.userDTO.value?.questSuccessTimes?.size!!) { // 1 ~ 8
            val quest = QuestDTO("", "", 1, firebaseViewModel.userDTO.value?.questSuccessTimes?.get("$i"), firebaseViewModel.userDTO.value?.questGemGetTimes?.get("$i"))
            if (quest.isQuestSuccess()) { // 퀘스트 완료 했는데 보상 수령안한 항목
                successCount++
                if (!quest.isQuestGemGet()) {
                    isQuestNew = true
                    break
                }
            }
        }

        if (!isQuestNew) {
            val quest = QuestDTO("", "", 1, null, firebaseViewModel.userDTO.value?.questGemGetTimes?.get("0"))
            if (successCount == firebaseViewModel.userDTO.value?.questSuccessTimes?.size && !quest.isQuestGemGet()) {
                isQuestNew = true
            }
        }

        if (isQuestNew) {
            binding.imgQuestNew.visibility = View.VISIBLE
        } else {
            binding.imgQuestNew.visibility = View.GONE
        }
    }

    // 전광판 타이머
    private fun displayTimer() {
        timer(period = 1000)
        {
            // 전광판은 5번 깜빡이고 다음 문자열 표시
            if (firebaseViewModel.preferencesDTO.value != null) {
                if (displayList.size > 0 && displayCount >= firebaseViewModel.preferencesDTO.value?.displayBoardPeriod!!) {
                    runOnUiThread {
                        displayList.removeAt(0)
                        displayCount = 0

                        // 대기열에 있다면 다음 문자열 표시
                        if (displayList.size > 0) {
                            binding.displayBoard.textDisplayBoard.text = displayList[0].displayText
                            binding.displayBoard.textDisplayBoard.setTextColor(displayList[0].color!!)
                        }
                    }
                }
                displayCount++
            }
        }
    }

    // 날짜 변경 체크 타이머
    private fun dateCheckTimer() {
        timer(period = 1000)
        {
            val checkDate = SimpleDateFormat("yyyyMMdd").format(Date())
            if (currentDate != checkDate) {
                runOnUiThread {
                    println("날짜가 변경되었습니다 $currentDate -> $checkDate")
                    currentDate = checkDate

                    checkPremium()
                    checkQuestNew()
                }
            }
        }
    }

    fun getBitmap(uri: Uri): Bitmap? {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor

        return decodeSampledBitmapFileDescriptor(fileDescriptor, 200, 200)
    }

    fun moveScheduleTab() {
        binding.viewpager.currentItem = 2
    }

    private fun rotatedBitmap(fd: FileDescriptor, bitmap: Bitmap?): Bitmap? {
        val matrix = Matrix()
        var resultBitmap : Bitmap? = null
        when(getOrientationOfImage(fd)){
            0 -> matrix.setRotate(0F)
            90 -> matrix.setRotate(90F)
            180 -> matrix.setRotate(180F)
            270 -> matrix.setRotate(270F)
        }
        resultBitmap = try{
            bitmap?.let { Bitmap.createBitmap(it, 0, 0, bitmap.width, bitmap.height, matrix, true) }
        }catch (e: Exception){
            e.printStackTrace()
            null
        }
        return resultBitmap
    }

    private fun getOrientationOfImage(fd: FileDescriptor): Int? {
        var exif: ExifInterface?
        var result: Int? = null
        
        try{
            exif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ExifInterface(fd)
            } else {
                return -1
            }
        }catch (e: Exception){
            e.printStackTrace()
            return -1
        }
        
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
        if(orientation != -1){
            result = when(orientation){
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        }
        return result
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun decodeSampledBitmapFileDescriptor(
        fd: FileDescriptor,
        reqWidth: Int,
        reqHeight: Int,
    ): Bitmap? {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFileDescriptor(fd, null, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            rotatedBitmap(fd, BitmapFactory.decodeFileDescriptor(fd, null, this))
        }
    }

    fun getTutorialStep() : MutableLiveData<Int> {
        return tutorialStep
    }

    fun addTutorialStep(addStep : Int = 1) {
        var timeCount = 0
        timer(period = 100)
        {
            if (timeCount > 3) {
                cancel()
                runOnUiThread {
                    tutorialStep.value = tutorialStep.value!!.plus(addStep)
                }
            }
            timeCount++
        }
        //tutorialStep.value = tutorialStep.value!!.plus(1)
    }

    fun finishTutorialStep(isCompleted: Boolean) {
        var timeCount = 0
        timer(period = 100)
        {
            if (timeCount > 3) {
                cancel()
                runOnUiThread {
                    tutorialStep.value = 0
                    firebaseViewModel.updateUserTutorialEndedTime(firebaseViewModel.userDTO.value?.uid.toString()) {
                        var log = if (isCompleted) {
                            LogDTO("[튜토리얼 종료] 튜토리얼 완료", Date())
                        } else {
                            LogDTO("[튜토리얼 종료] 튜토리얼 취소", Date())
                        }
                        firebaseViewModel.writeUserLog(firebaseViewModel.userDTO.value?.uid.toString(), log) { }
                    }
                }
            }
            timeCount++
        }
    }

    private fun observeTutorial() {
        getTutorialStep()?.observe(this) {
            onTutorial(tutorialStep.value!!)
        }
    }

    fun getMainLayoutRect() : Rect {
        val location = IntArray(2)
        binding.layoutMain.getLocationOnScreen(location)
        return Rect(location[0], location[1], location[0] + binding.layoutMain.width, location[1] + binding.layoutMain.height)
    }

    private fun getTabRect(item: Int) : Rect {
        val width = binding.tabs.width.div(4)
        val location = IntArray(2)
        binding.tabs.getLocationOnScreen(location)
        return when (item) {
            0 -> Rect(location[0], location[1], location[0] + width, location[1] + binding.tabs.height)
            1 -> Rect(location[0], location[1], location[0] + width.times(3), location[1] + binding.tabs.height)
            2 -> Rect(location[0], location[1], location[0] + width.times(5), location[1] + binding.tabs.height)
            else -> Rect(location[0], location[1], location[0] + width.times(7), location[1] + binding.tabs.height)
        }
    }

    private fun onTutorial(step: Int) {
        when (step) {
            1 -> {
                println("튜토리얼 Step - $step")
                TapTargetSequence(this)
                    .targets(
                        TapTarget.forView(binding.layoutMain,
                            "[ 마이팬클럽 ]에서는 '스트리밍 앱', '투표 앱', '유튜브 링크', '뉴스 기사', 'SNS' 등 덕질에 필요한 모든걸 클릭 한번만으로 실행 가능합니다.",
                            "- OK 버튼을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(this, R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.layoutMain,
                            "그동안 이름도 기억하기 힘들고 일일이 찾기도 힘들었던 수 많은 앱들과 유튜브 링크들을 한 번의 클릭으로 편하게 사용해 보세요!",
                            "- OK 버튼을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(this, R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.layoutMain,
                            "[ 마이팬클럽 ]으로 스마트한 덕질 라이프와 효율의 극대화를 즐겨보세요!",
                            "- OK 버튼을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(this, R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forBounds(getTabRect(2),
                            "우선 개인 스케줄 등록 방법에 대해 알아보도록 하겠습니다.",
                            "- 스케줄 탭을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .tintTarget(true)).listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            binding.viewpager.currentItem = 2 // 스케줄 탭 이동
                            addTutorialStep()
                        }
                        override fun onSequenceStep(tutorialStep: TapTarget, targetClicked: Boolean) {
                            //Toast.makeText(secondActivity.this,"GREAT!",Toast.LENGTH_SHORT).show();
                            //println("튜토리얼 ${tutorialStep}")
                        }
                        override fun onSequenceCanceled(lastTarget: TapTarget) {
                        }
                    }).start()
            }
            6 -> {
                println("튜토리얼 Step - $step")
                TapTargetView.showFor(this,
                    TapTarget.forBounds(getTabRect(0),
                        "이제 다시 메인화면으로 돌아가 보겠습니다.",
                        "- 메인 탭을 눌러주세요.")
                        .cancelable(false)
                        .dimColor(R.color.black)
                        .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                        .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                        .titleTextSize(18) // Specify the size (in sp) of the title text
                        .transparentTarget(true)
                        .tintTarget(true),object : TapTargetView.Listener() {
                        // The listener can listen for regular clicks, long clicks or cancels
                        override fun onTargetClick(view: TapTargetView) {
                            super.onTargetClick(view) // This call is optional

                            binding.viewpager.currentItem = 0 // 메인 탭 이동
                            addTutorialStep()
                        }
                    })
            }
            /*7 -> {
                println("튜토리얼 Step - $step")
                TapTargetView.showFor(this,
                    TapTarget.forView(binding.layoutMain,
                        "메인 화면에서 등록된 스케줄을 기준으로 매일매일 해야할 일을 확인할 수 있습니다.",
                        "- OK 버튼을 눌러주세요.") // All options below are optional
                        .cancelable(false)
                        .dimColor(R.color.black)
                        .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                        .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                        .titleTextSize(18) // Specify the size (in sp) of the title text
                        .icon(ContextCompat.getDrawable(this, R.drawable.ok))
                        .tintTarget(true),object : TapTargetView.Listener() {
                        // The listener can listen for regular clicks, long clicks or cancels
                        override fun onTargetClick(view: TapTargetView) {
                            super.onTargetClick(view) // This call is optional
                            addTutorialStep()
                        }
                    })
            }*/
            12 -> {
                println("튜토리얼 Step - $step")
                TapTargetSequence(this)
                    .targets(
                        TapTarget.forView(binding.layoutMain,
                            "[ 마이팬클럽 ]에서는 '팬클럽'을 통해 많은 팬들과 스케줄을 공유하며 공통된 목표를 달성할 수 있습니다.",
                            "- OK 버튼을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(this, R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.layoutMain,
                            "내가 추가한 스케줄을 다른 팬클럽원이 수행 가능하고, 다른 팬클럽원이 추가한 스케줄을 내가 수행 할 수 있습니다!",
                            "- OK 버튼을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(this, R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.layoutMain,
                            "버튼 한번으로 스케줄 수행이 가능해 팬카페를 통해 공지를 하는 것 보다 훨씬 빠르게 전달이 가능합니다!",
                            "- OK 버튼을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(this, R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forView(binding.layoutMain,
                            "팬클럽에 가입하기 위해서는 개인 레벨3 을 달성해야 하며 레벨7 달성 시 직접 팬클럽을 창설할 수도 있습니다.",
                            "- OK 버튼을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .icon(ContextCompat.getDrawable(this, R.drawable.ok))
                            .tintTarget(true),
                        TapTarget.forBounds(getTabRect(3),
                            "나의 레벨 확인과 레벨업을 하는 방법에 대해서 알아보겠습니다.",
                            "- 프로필 탭을 눌러주세요.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .tintTarget(true)).listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            binding.viewpager.currentItem = 3 // 프로필 탭 이동
                            addTutorialStep()
                        }
                        override fun onSequenceStep(tutorialStep: TapTarget, targetClicked: Boolean) {
                            //Toast.makeText(secondActivity.this,"GREAT!",Toast.LENGTH_SHORT).show();
                            //println("튜토리얼 ${tutorialStep}")
                        }
                        override fun onSequenceCanceled(lastTarget: TapTarget) {
                        }
                    }).start()
            }
            18 -> {
                println("튜토리얼 Step - $step")
                val rect = Rect(0, -130, binding.layoutMain.width, 100)
                TapTargetSequence(this)
                    .targets(
                        TapTarget.forView(binding.layoutMail,
                            "상단의 우편함에서 레벨업 보상으로 발송된 다이아를 획득 할 수 있습니다.",
                            "- 우편함에 빨간색 표시가 되면 새로운 우편이 도착했다는 알람 입니다.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .tintTarget(true),
                        TapTarget.forView(binding.layoutQuest,
                            "간단한 일일 과제를 수행하면 매일 무료 다이아를 획득할 수 있습니다.",
                            "- 일일 과제는 매일 초기화 됩니다.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .tintTarget(true),
                        TapTarget.forBounds(rect,
                            "레벨5 달성 시 전광판을 통해 나의 스타를 응원할 수도 있습니다!",
                            "- 레벨이 오를수록 특별한 색상의 전광판 홍보가 가능합니다.") // All options below are optional
                            .cancelable(false)
                            .dimColor(R.color.black)
                            .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                            .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                            .titleTextSize(18) // Specify the size (in sp) of the title text
                            .transparentTarget(true)
                            .targetRadius(100)
                            .tintTarget(true)).listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() {
                            addTutorialStep()
                        }
                        override fun onSequenceStep(tutorialStep: TapTarget, targetClicked: Boolean) {
                        }
                        override fun onSequenceCanceled(lastTarget: TapTarget) {
                        }
                    }).start()
            }
            20 -> {
                println("튜토리얼 Step - $step")
                TapTargetView.showFor(this,
                    TapTarget.forBounds(getTabRect(1),
                        "팬클럽 가입과 창설은 아래의 팬클럽 탭에서 가능합니다.",
                        "- 팬클럽 탭을 눌러주세요.") // All options below are optional
                        .cancelable(false)
                        .dimColor(R.color.black)
                        .outerCircleColor(R.color.charge_back) // Specify a color for the outer circle
                        .outerCircleAlpha(0.9f) // Specify the alpha amount for the outer circle
                        .titleTextSize(18) // Specify the size (in sp) of the title text
                        .transparentTarget(true)
                        .tintTarget(true),object : TapTargetView.Listener() {
                        // The listener can listen for regular clicks, long clicks or cancels
                        override fun onTargetClick(view: TapTargetView) {
                            super.onTargetClick(view) // This call is optional
                            binding.viewpager.currentItem = 1 // 팬클럽 탭 이동
                            addTutorialStep()
                        }
                    })
            }
        }


    }

    //lateinit var appUpdateManager : AppUpdateManager
    private fun updateCheck() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)

        println("업데이트 시작")
        appUpdateManager?.let {
            it.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                println("업데이트 ? ${appUpdateInfo.updateAvailability()}")
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                    // or AppUpdateType.FLEXIBLE
                    appUpdateManager?.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE, // or AppUpdateType.FLEXIBLE
                        this,
                        100
                    )
                }
            }
        }
    }

    fun getBlockDisplayBoard(docName: String) : Boolean {
        return dbHandler?.getBlock(docName)
    }

    fun updateBlockDisplayBoard(docName: String, isBlock: Int) {
        dbHandler?.updateBlock(docName, isBlock)
    }
}