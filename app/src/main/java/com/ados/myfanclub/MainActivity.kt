package com.ados.myfanclub

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.ados.myfanclub.databinding.ActivityMainBinding
import com.ados.myfanclub.dialog.FanClubQuestionDialog
import com.ados.myfanclub.dialog.LoadingDialog
import com.ados.myfanclub.dialog.QuestionDialog
import com.ados.myfanclub.model.*
import com.ados.myfanclub.page.ZoomOutPageTransformer
import com.ados.myfanclub.viewmodel.FirebaseStorageViewModel
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fan_club_question_dialog.*
import kotlinx.android.synthetic.main.question_dialog.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer
import kotlin.system.exitProcess
import java.io.FileDescriptor


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
    private var oldFanClubDTO: FanClubDTO? = null
    private var oldMemberDTO: MemberDTO? = null

    private var displayCount = 0 // 전광판 일정 시간 유지를 위한 변수
    private var displayList = arrayListOf<DisplayBoardDTO>() // 표시할 전광판을 Queue 형식으로 저장
    private lateinit var currentDate : String // 12시 지나서 날짜 변경을 체크하기 위한 변수

    // 최초에 모든 항목들이 로딩 완료 되었을 때 ViewPager 호출
    // currentLoadingCount 가 successLoadingCount 와 같아져야 로딩이 완료된 것
    private var checkLoadingCount = 0 // 일정 시간이 지나도 완료가 안되면 강제로 완료 처리
    private var currentLoadingCount = 0
    private var successLoadingCount = 8
    private var isFirstRun = false // 최초 실행을 체크하기 위한 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loading()
        currentDate = SimpleDateFormat("yyyyMMdd").format(Date())

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

        firebaseViewModel.getDisplayBoardListen() // 전광판 리스트 획득
        observeDisplayBoard() // 실시간 전광판 모니터링

        firebaseViewModel.getMailsListen(oldUserDTO?.uid.toString()) // 메일 리스트 획득
        observeMail() // 실시간 메일 모니터링

        observeUser() // 실시간 사용자 정보 모니터링
        observeFanClub() // 실시간 팬클럽 정보 모니터링
        observeMember() // 실시간 팬클럽 멤버 정보 모니터링

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
                }
            }
            checkLoadingCount++
        }

        binding.displayBoard.layoutMain.setOnClickListener {
            var intent = Intent(this, DisplayBoardActivity::class.java)
            intent.putExtra("user", firebaseViewModel.userDTO.value)
            intent.putExtra("preferences", firebaseViewModel.preferencesDTO.value)
            startActivity(intent)

            /*val dialog = DisplayBoardDialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCanceledOnTouchOutside(false)
            dialog.preferencesDTO = firebaseViewModel.preferencesDTO.value
            dialog.currentUser = firebaseViewModel.userDTO.value
            dialog.show()

            dialog.button_display_board_ok.setOnClickListener { // Ok
                dialog.dismiss()
            }*/
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

    private fun addLoadingCount() {
        if (!isFirstRun) {
            currentLoadingCount++
        }
    }

    private fun failedLoading() {
        val question = QuestionDTO(
            QuestionDTO.Stat.ERROR,
            "데이터 로딩 실패",
            "데이터 로딩에 실패하였습니다.\n앱 종료 후 다시 실행해 주세요."
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

    // 실시간 전광판 모니터링
    private fun observeDisplayBoard() {
        firebaseViewModel.displayBoardDTO.observe(this) {
            // 대기열에 없다면 즉시 전광판 표시
            if (displayList.size == 0) {
                binding.displayBoard.textDisplayBoard.text = firebaseViewModel.displayBoardDTO.value?.displayText
                binding.displayBoard.textDisplayBoard.setTextColor(firebaseViewModel.displayBoardDTO.value?.color!!)
            }

            displayList.add(firebaseViewModel.displayBoardDTO.value!!)
            println("[로딩] 전광판 획득")
            addLoadingCount()
        }
    }

    // 실시간 메일 모니터링
    private fun observeMail() {
        firebaseViewModel.mailDTOs.observe(this) {
            if (firebaseViewModel.mailDTOs.value?.size!! > 0) { // 메일이 있으면 새로운 메일 알림 표시
                binding.imgMailNew.visibility = View.VISIBLE
            } else {
                binding.imgMailNew.visibility = View.GONE
            }
            println("[로딩] 메일 획득")
            addLoadingCount()
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
        super.onBackPressed()
        appExit()
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

    fun getFanClub() : FanClubDTO? {
        return firebaseViewModel.fanClubDTO.value
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

    fun getBitmap(uri: Uri): Bitmap {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor

        return decodeSampledBitmapFileDescriptor(fileDescriptor, 200, 200)
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
    ): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFileDescriptor(fd, null, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeFileDescriptor(fd, null, this)
        }
    }
}