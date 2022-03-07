package com.ados.myfanclub.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.ados.myfanclub.api.RetrofitInstance
import com.ados.myfanclub.model.*
import com.ados.myfanclub.util.Utility
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import okhttp3.ResponseBody
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class FirebaseRepository() {
    private val TAG = "FirebaseRepository"

    //<editor-fold desc="@ 변수 선언">

    enum class GemType {
        PAID_GEM, FREE_GEM
    }

    enum class MemberType {
        ALL, MEMBER_ONLY, GUEST_ONLY
    }

    /*val userDTO = MutableLiveData<UserDTO>() // 유저 정보
    val currentQuiz = MutableLiveData<QuizDTO>() // 퀴즈 정보*/
    val userDTO = MutableLiveData<UserDTO?>() // 사용자 정보
    var userDTOListener : ListenerRegistration? = null
    val fanClubDTO = MutableLiveData<FanClubDTO?>() // 팬클럽 정보
    var fanClubDTOListener : ListenerRegistration? = null
    val memberDTO = MutableLiveData<MemberDTO?>() // 팬클럽 멤버 정보
    var memberDTOListener : ListenerRegistration? = null
    val displayBoardDTO = MutableLiveData<DisplayBoardDTO>() // 전광판 정보
    var displayBoardDTOListener : ListenerRegistration? = null
    val fanClubChatDTO = MutableLiveData<DisplayBoardDTO>() // 팬클럽 채팅 정보
    var fanClubChatDTOListener : ListenerRegistration? = null
    val mailDTOs = MutableLiveData<ArrayList<MailDTO>>() // 메일 리스트
    var mailDTOsListener : ListenerRegistration? = null
    val displayBoardDTOs = MutableLiveData<ArrayList<DisplayBoardDTO>>() // 전광판 리스트
    var displayBoardDTOsListener : ListenerRegistration? = null
    val fanClubChatDTOs = MutableLiveData<ArrayList<DisplayBoardDTO>>() // 팬클럽 채팅 리스트
    var fanClubChatDTOsListener : ListenerRegistration? = null
    val noticeDTOs = MutableLiveData<ArrayList<NoticeDTO>>() // 공지사항 리스트 정보
    val userDTOs = MutableLiveData<ArrayList<UserDTO>>() // 사용자 리스트 정보
    val fanClubDTOs = MutableLiveData<ArrayList<FanClubDTO>>() // 팬클럽 리스트 정보
    val scheduleDTOs = MutableLiveData<ArrayList<ScheduleDTO>>() // 스케줄 리스트
    var scheduleDTOsListener : ListenerRegistration? = null
    val personalDashboardMissionDTOs = MutableLiveData<ArrayList<DashboardMissionDTO>>() // 개인 스케줄 진행도 리스트
    val fanClubDashboardMissionDTOs = MutableLiveData<ArrayList<DashboardMissionDTO>>() // 팬클럽 스케줄 진행도 리스트
    val scheduleStatistics = MutableLiveData<MutableMap<String, Int>>() // 스케줄 통계 정보
    val adPolicyDTO = MutableLiveData<AdPolicyDTO>() // 광고 설정
    val preferencesDTO = MutableLiveData<PreferencesDTO>() // 환경 설정
    val token = MutableLiveData<String>() // 토큰 정보
    val myResponse : MutableLiveData<Response<ResponseBody>> = MutableLiveData() // 메세지 수신 정보

    // Firestore 초기화
    private val firestore = FirebaseFirestore.getInstance()

    // FireMessaging 초기화
    private val fireMessaging = FirebaseMessaging.getInstance()

    //</editor-fold>


    //<editor-fold desc="@ 데이터 획득 함수">

    // 사용자 불러오기(실시간)
    fun getUserListen(uid: String) {
        if (userDTOListener == null) {
            userDTOListener = firestore.collection("user")?.document(uid)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (documentSnapshot == null || !documentSnapshot?.exists()!!) return@addSnapshotListener
                val user = documentSnapshot?.toObject(UserDTO::class.java)!!
                userDTO.value = user
            }
        }
    }

    // 사용자 불러오기(실시간) 중지
    fun stopUserListen() {
        if (userDTOListener != null) {
            userDTOListener?.remove()
            userDTOListener = null
            userDTO.value = null
        }
    }

    // 팬클럽 정보 불러오기(실시간)
    fun getFanClubListen(fanClubId: String) {
        if (fanClubDTOListener == null) {
            fanClubDTOListener = firestore.collection("fanClub")?.document(fanClubId)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (documentSnapshot == null || !documentSnapshot?.exists()!!) return@addSnapshotListener
                val fanClub = documentSnapshot?.toObject(FanClubDTO::class.java)!!
                fanClubDTO.value = fanClub
            }
        }
    }

    // 팬클럽 정보 불러오기(실시간) 중지
    fun stopFanClubListen() {
        if (fanClubDTOListener != null) {
            fanClubDTOListener?.remove()
            fanClubDTOListener = null
            fanClubDTO.value = null
        }
    }

    // 팬클럽 멤버 불러오기(실시간)
    fun getMemberListen(fanClubId: String, userUid: String) {
        if (memberDTOListener == null) {
            memberDTOListener = firestore.collection("fanClub")?.document(fanClubId)
                ?.collection("member")?.document(userUid)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if (documentSnapshot == null || !documentSnapshot?.exists()!!) return@addSnapshotListener
                    val member = documentSnapshot?.toObject(MemberDTO::class.java)!!
                    memberDTO.value = member
            }
        }
    }

    // 팬클럽 멤버 불러오기(실시간) 중지
    fun stopMemberListen() {
        if (memberDTOListener != null) {
            memberDTOListener?.remove()
            memberDTOListener = null
            memberDTO.value = null
        }
    }

    // 전광판 불러오기(실시간)
    fun getDisplayBoardListen() {
        if (displayBoardDTOListener == null) {
            displayBoardDTOListener = firestore.collection("displayBoard")?.orderBy("order", Query.Direction.DESCENDING)?.limit(1)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot == null) return@addSnapshotListener
                for(snapshot in querySnapshot){
                    var displayBoard = snapshot.toObject(DisplayBoardDTO::class.java)!!
                    displayBoardDTO.value = displayBoard
                }
            }
        }
    }

    // 전광판 불러오기(실시간) 중지
    fun stopDisplayBoardListen() {
        if (displayBoardDTOListener != null) {
            displayBoardDTOListener?.remove()
            displayBoardDTOListener = null
            displayBoardDTO.value = null
        }
    }

    // 팬클럽 채팅 불러오기(실시간)
    fun getFanClubChatListen(fanClubId: String) {
        if (fanClubChatDTOListener == null) {
            fanClubChatDTOListener = firestore.collection("fanClub")
                ?.document(fanClubId)?.collection("chat")?.orderBy("createTime", Query.Direction.DESCENDING)
                ?.limit(1)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot == null) return@addSnapshotListener
                    for(snapshot in querySnapshot){
                        var displayBoard = snapshot.toObject(DisplayBoardDTO::class.java)!!
                        fanClubChatDTO.value = displayBoard
                    }
            }
        }
    }

    // 팬클럽 채팅 불러오기(실시간) 중지
    fun stopFanClubChatListen() {
        if (fanClubChatDTOListener != null) {
            fanClubChatDTOListener?.remove()
            fanClubChatDTOListener = null
            fanClubChatDTO.value = null
        }
    }

    // 메일 리스트 불러오기(실시간)
    fun getMailsListen(uid: String) {
        if (mailDTOsListener == null) {
            mailDTOsListener = firestore.collection("user")?.document(uid)?.collection("mail")?.whereEqualTo("deleted", false)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot == null) return@addSnapshotListener

                var mails : ArrayList<MailDTO> = arrayListOf()
                val date = Date()
                for(snapshot in querySnapshot){
                    var mail = snapshot.toObject(MailDTO::class.java)!!

                    // 유효한 우편만 획득
                    if (date < mail.expireTime && !mail.deleted) { (mail)
                        mails.add(mail)
                    }
                }
                mails.sortByDescending { it.expireTime }
                mailDTOs.value = mails
            }
        }
    }

    // 메일 리스트 불러오기(실시간) 중지
    fun stopMailsListen() {
        if (mailDTOsListener != null) {
            mailDTOsListener?.remove()
            mailDTOsListener = null
            mailDTOs.value = arrayListOf()
        }
    }

    // 전광판 리스트 불러오기(실시간)
    fun getDisplayBoardsListen() {
        if (displayBoardDTOsListener == null) {
            val limit = 30L
            displayBoardDTOsListener = firestore.collection("displayBoard")?.orderBy("order", Query.Direction.DESCENDING)?.limit(limit)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot == null) return@addSnapshotListener

                var displayBoards : ArrayList<DisplayBoardDTO> = arrayListOf()
                for(snapshot in querySnapshot){
                    var displayBoard = snapshot.toObject(DisplayBoardDTO::class.java)!!
                    displayBoards.add(displayBoard)
                }
                if (displayBoards.size < limit) {
                    for (i in displayBoards.size..limit) {
                        displayBoards.add(DisplayBoardDTO("", ""))
                    }
                }
                displayBoardDTOs.value = displayBoards
            }
        }
    }

    // 전광판 리스트 불러오기(실시간) 중지
    fun stopDisplayBoardsListen() {
        if (displayBoardDTOsListener != null) {
            displayBoardDTOsListener?.remove()
            displayBoardDTOsListener = null
            displayBoardDTOs.value = arrayListOf()
        }
    }

    // 팬클럽 채팅 리스트 불러오기(실시간)
    fun getFanClubChatsListen(fanClubId: String) {
        if (fanClubChatDTOsListener == null) {
            val limit = 30L
            fanClubChatDTOsListener = firestore.collection("fanClub")
                ?.document(fanClubId)?.collection("chat")?.orderBy("createTime", Query.Direction.DESCENDING)
                ?.limit(limit)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot == null) return@addSnapshotListener

                var displayBoards : ArrayList<DisplayBoardDTO> = arrayListOf()
                for(snapshot in querySnapshot){
                    var displayBoard = snapshot.toObject(DisplayBoardDTO::class.java)!!
                    displayBoards.add(displayBoard)
                }
                if (displayBoards.size < limit) {
                    for (i in displayBoards.size..limit) {
                        displayBoards.add(DisplayBoardDTO("", ""))
                    }
                }
                fanClubChatDTOs.value = displayBoards
            }
        }
    }

    // 팬클럽 채팅 리스트 불러오기(실시간) 중지
    fun stopFanClubChatsListen() {
        if (fanClubChatDTOsListener != null) {
            fanClubChatDTOsListener?.remove()
            fanClubChatDTOsListener = null
            fanClubChatDTOs.value = arrayListOf()
        }
    }

    // 사용자 불러오기
    fun getUser(uid: String, myCallback: (UserDTO?) -> Unit) {
        var user: UserDTO? = null
        firestore.collection("user")?.document(uid)?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.exists()) {
                user = task.result.toObject(UserDTO::class.java)!!
            }
            myCallback(user)
        }
    }

    // 팬클럽 불러오기
    fun getFanClub(fanClubId: String, myCallback: (FanClubDTO?) -> Unit) {
        var fanClub: FanClubDTO? = null
        firestore.collection("fanClub")?.document(fanClubId)?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.exists()) {
                fanClub = task.result.toObject(FanClubDTO::class.java)!!
            }
            myCallback(fanClub)
        }
    }

    // 팬클럽 멤버 불러오기
    fun getMember(fanClubId: String, userUid: String, myCallback: (MemberDTO?) -> Unit) {
        var member: MemberDTO? = null
        firestore.collection("fanClub")?.document(fanClubId)
            ?.collection("member")?.document(userUid)?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result.exists()) {
                    member = task.result.toObject(MemberDTO::class.java)!!
                }
                myCallback(member)
            }
    }

    // 공지사항 리스트 획득(최신순)
    fun getNotices(isMain: Boolean) {
        firestore.collection("notice")?.orderBy("insertTime", Query.Direction.DESCENDING)?.limit(30)?.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var notices : ArrayList<NoticeDTO> = arrayListOf()
                for (document in task.result) {
                    var notice = document.toObject(NoticeDTO::class.java)!!
                    if (isMain) { // 메인에 표시할 공지는 하나만 획득
                        if (notice.displayMain!!) {
                            notices.add(notice)
                            break
                        }
                    } else {
                        notices.add(notice)
                    }
                }
                noticeDTOs.value = notices
            }
        }
    }

    // 사용자 리스트 획득(레벨 순)
    fun getUsers() {
        firestore.collection("user")?.orderBy("level", Query.Direction.DESCENDING)?.limit(100)?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var users : ArrayList<UserDTO> = arrayListOf()
                for (document in task.result) {
                    var user = document.toObject(UserDTO::class.java)!!
                    users.add(user)
                }
                userDTOs.value = users
            }
        }
    }

    // 팬클럽 리스트 획득(누적 경험치 순)
    fun getFanClubs() {
        firestore.collection("fanClub")?.orderBy("expTotal", Query.Direction.DESCENDING)?.limit(100)?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var fanClubs : ArrayList<FanClubDTO> = arrayListOf()
                for (document in task.result) {
                    var fanClub = document.toObject(FanClubDTO::class.java)!!
                    fanClubs.add(fanClub)
                }
                fanClubDTOs.value = fanClubs
            }
        }
    }

    // 팬클럽 리스트 검색
    fun getFanClubsSearch(query: String) {
        if (query.isNullOrEmpty()) { // 검색어가 없을 경우 랜덤으로 팬클럽 표시
            // 팬클럽 생성 시간이 특정날짜 기준 이하인 팬클럽을 랜덤으로 획득
            // 오늘날짜 - 팬클럽 15개 이상 생성된 날짜 중 랜덤 값
            val startDate = SimpleDateFormat("yyyyMMdd").parse("20210905").time
            val calendar= Calendar.getInstance()
            val range = ((calendar.time.time - startDate) / (24 * 60 * 60 * 1000)).toInt()
            val random = Random.nextInt(0, range)
            calendar.add(Calendar.DATE, -random)

            println("랜덤 : $random, 레인지 : $range, ${calendar.time}, $calendar")

            firestore.collection("fanClub")?.whereLessThan("createTime", calendar.time)?.limit(15).get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var fanClubs : ArrayList<FanClubDTO> = arrayListOf()
                    for (document in task.result) {
                        var fanClub = document.toObject(FanClubDTO::class.java)!!
                        fanClubs.add(fanClub)
                    }
                    fanClubDTOs.value = fanClubs
                }
            }
        } else { // 파이어베이스가 like 검색이 안되기 때문에 전체 팬클럽 리스트에서 일일이 비교해야 함
            firestore.collection("fanClub")?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var fanClubs : ArrayList<FanClubDTO> = arrayListOf()
                    for (document in task.result) {
                        var fanClub = document.toObject(FanClubDTO::class.java)!!
                        if (fanClub.name!!.contains(query)) { // 팬클럽 명 검색
                            fanClubs.add(fanClub)
                        } else if (fanClub.description!!.contains(query)) { // 팬클럽 소개 검색
                            fanClubs.add(fanClub)
                        }
                    }
                    fanClubDTOs.value = fanClubs
                }
            }
        }
    }

    // 팬클럽 멤버 리스트 획득(기여도 순)
    fun getMembers(fanClubId: String, memberType: MemberType, myCallback: (ArrayList<MemberDTO>) -> Unit) {
        firestore.collection("fanClub")?.document(fanClubId)
            ?.collection("member")?.orderBy("contribution", Query.Direction.DESCENDING)?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var members : ArrayList<MemberDTO> = arrayListOf()
                    for (document in task.result) {
                        var member = document.toObject(MemberDTO::class.java)!!

                        when (memberType) {
                            MemberType.ALL -> members.add(member) // 멤버 + 가입신청자 모두 획득

                            MemberType.MEMBER_ONLY -> { // 멤버만 획득
                                if (member.position != MemberDTO.Position.GUEST) {
                                    members.add(member)
                                }
                            }

                            MemberType.GUEST_ONLY -> { // 가입신청자만 획득
                                if (member.position == MemberDTO.Position.GUEST) {
                                    members.add(member)
                                }
                            }
                        }

                    }
                    myCallback(members)
                }
            }
    }

    // 개인 스케줄 리스트 획득
    fun getPersonalSchedules(uid: String) {
        firestore.collection("user")?.document(uid)?.collection("schedule")?.orderBy("order", Query.Direction.ASCENDING)?.get()?.addOnSuccessListener { result ->
            var schedules : ArrayList<ScheduleDTO> = arrayListOf()
            for (document in result) {
                var schedule = document.toObject(ScheduleDTO::class.java)!!
                schedules.add(schedule)
            }
            scheduleDTOs.value = schedules
        }?.addOnFailureListener { exception ->

        }
    }

    // 팬클럽 스케줄 리스트 획득(실시간), 팬클럽 스케줄은 여러 사람이 추가할 수 있기 때문에 Listener 사용
    fun getFanClubSchedulesListen(fanClubId: String) {
        if (scheduleDTOsListener == null) {
            scheduleDTOsListener = firestore.collection("fanClub")?.document(fanClubId)?.collection("schedule")?.orderBy("order", Query.Direction.ASCENDING)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                println("언제 들어오나?? ㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁㅁ")
                if(querySnapshot == null)return@addSnapshotListener

                var schedules : ArrayList<ScheduleDTO> = arrayListOf()
                for(snapshot in querySnapshot){
                    var schedule = snapshot.toObject(ScheduleDTO::class.java)!!
                    schedules.add(schedule)
                }
                scheduleDTOs.value = schedules
            }
        }
    }

    // 팬클럽 스케줄 리스트 획득(실시간) 중지
    fun stopFanClubSchedulesListen() {
        if (scheduleDTOsListener != null) {
            scheduleDTOsListener?.remove()
            scheduleDTOsListener = null
            scheduleDTOs.value = arrayListOf()
        }
    }

    // 업데이트 및 서버점검 체크 (실시간)
    fun getServerUpdate() {
        if (displayBoardDTOListener == null) {
            displayBoardDTOListener = firestore.collection("displayBoard")?.orderBy("order", Query.Direction.DESCENDING)?.limit(1)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot == null) return@addSnapshotListener
                for(snapshot in querySnapshot){
                    var displayBoard = snapshot.toObject(DisplayBoardDTO::class.java)!!
                    displayBoardDTO.value = displayBoard
                }
            }
        }
    }

    // 전광판 불러오기(실시간) 중지
    fun stopServerUpdate() {
        if (displayBoardDTOListener != null) {
            displayBoardDTOListener?.remove()
            displayBoardDTOListener = null
            displayBoardDTO.value = null
        }
    }

    // 개인 스케줄 및 진행도 불러오기
    fun getPersonalDashboardMission(uid: String, selectedCycle: ScheduleDTO.Cycle) {
        firestore.collection("user")?.document(uid)?.collection("schedule")?.orderBy("order", Query.Direction.ASCENDING)?.get()?.addOnCompleteListener { task ->
            if(!task.isSuccessful)return@addOnCompleteListener

            var personalSchedules : ArrayList<ScheduleDTO> = arrayListOf()
            var personalMissions : ArrayList<DashboardMissionDTO> = arrayListOf()

            for (document in task.result) {
                var schedule = document.toObject(ScheduleDTO::class.java)!!
                // 현재 시간이 기간내에 속한 스케줄만 표시
                if (schedule.isScheduleVisible(selectedCycle)) {
                    personalSchedules.add(schedule)

                    var mission = DashboardMissionDTO()
                    mission.type = DashboardMissionDTO.Type.PERSONAL
                    mission.scheduleDTO = schedule

                    var docName = schedule.getProgressDocName()

                    firestore.collection("user")?.document(uid)
                        ?.collection("schedule")?.document(schedule.docName.toString())
                        ?.collection("progress")?.document(docName)?.get()?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                var scheduleProgressDTO = ScheduleProgressDTO(docName, 0, schedule.count)
                                if (task.result.exists()) { // document 있음
                                    scheduleProgressDTO = task.result.toObject(ScheduleProgressDTO::class.java)!!
                                }

                                mission.scheduleProgressDTO = scheduleProgressDTO

                                personalMissions.add(mission)
                            }
                        }
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                while(true) {
                    println("personalSchedules.size = ${personalSchedules.size}, personalMissions.size = ${personalMissions.size}")
                    if (personalSchedules.size == personalMissions.size) {
                        personalMissions.sortBy { it.scheduleDTO?.order }
                        personalDashboardMissionDTOs.value = personalMissions
                        break
                    }
                    delay(100)
                }
            }
        }
    }

    // 팬클럽 스케줄 및 진행도 불러오기
    fun getFanClubDashboardMission(fanClubId: String, userUid: String, selectedCycle: ScheduleDTO.Cycle) {
        firestore.collection("fanClub")?.document(fanClubId)?.collection("schedule")?.orderBy("order", Query.Direction.ASCENDING)?.get()?.addOnCompleteListener { task ->
            if(!task.isSuccessful)return@addOnCompleteListener

            var fanClubSchedules : ArrayList<ScheduleDTO> = arrayListOf()
            var fanClubMissions : ArrayList<DashboardMissionDTO> = arrayListOf()

            for (document in task.result) {
                var schedule = document.toObject(ScheduleDTO::class.java)!!
                // 현재 시간이 기간내에 속한 스케줄만 표시
                if (schedule.isScheduleVisible(selectedCycle)) {
                    fanClubSchedules.add(schedule)

                    var mission = DashboardMissionDTO()
                    mission.type = DashboardMissionDTO.Type.FAN_CLUB
                    mission.scheduleDTO = schedule

                    var docName = schedule.getProgressDocName()
                    firestore.collection("fanClub")?.document(fanClubId)
                        ?.collection("member")?.document(userUid)
                        ?.collection("schedule")?.document(schedule.docName.toString())
                        ?.collection("progress")?.document(docName)?.get()
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                var scheduleProgressDTO = ScheduleProgressDTO(docName, 0, schedule.count)
                                if (task.result.exists()) { // document 있음
                                    scheduleProgressDTO = task.result.toObject(ScheduleProgressDTO::class.java)!!
                                }

                                mission.scheduleProgressDTO = scheduleProgressDTO

                                fanClubMissions.add(mission)
                            }
                        }
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                while(true) {
                    println("fanClubMissions.size = ${fanClubMissions.size}, fanClubMissions.size = ${fanClubMissions.size}")
                    if (fanClubSchedules.size == fanClubMissions.size) {
                        fanClubMissions.sortBy { it.scheduleDTO?.order }
                        fanClubDashboardMissionDTOs.value = fanClubMissions
                        break
                    }
                    delay(100)
                }
            }
        }
    }

    // 개인 스케줄 통계 정보
    fun getPersonalScheduleStatistics(uid: String, cycle: String, fieldName: String) {
        firestore.collection("user")?.document(uid)
            ?.collection("scheduleStatistics")?.document(cycle).get()?.addOnCompleteListener { task ->
                var percents: MutableMap<String, Int> = mutableMapOf()
                if (task.isSuccessful && task.result.exists()) {
                    if (task.result.contains(fieldName)) {
                        percents = task.result!![fieldName] as MutableMap<String, Int>
                    }
                }
                scheduleStatistics.value = percents
            }
    }

    // 팬클럽 스케줄 통계 정보
    fun getFanClubScheduleStatistics(fanClubId: String, userUid: String, cycle: String, fieldName: String) {
        firestore.collection("fanClub")?.document(fanClubId)
            ?.collection("member")?.document(userUid)
            ?.collection("scheduleStatistics")?.document(cycle).get()?.addOnCompleteListener { task ->
                var percents: MutableMap<String, Int> = mutableMapOf()
                if (task.isSuccessful && task.result.exists()) {
                    if (task.result.contains(fieldName)) {
                        percents = task.result!![fieldName] as MutableMap<String, Int>
                    }
                }
                scheduleStatistics.value = percents
            }
    }

    // 오늘 다이아뽑기 완료 횟수
    fun getTodayCompleteGambleCount(uid: String, myCallback: (Long) -> Unit) {
        var completeGambleCount = 0L
        val currentDate = SimpleDateFormat("yyyyMMdd").format(Date())
        firestore.collection("user")?.document(uid)
            ?.collection("otherOption")?.document("completeGambleCount").get()?.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result.exists()) {
                    if (task.result.contains(currentDate)) {
                        completeGambleCount = task.result.getLong(currentDate)!!
                    }
                }
                myCallback(completeGambleCount)
            }
    }

    // 출석체크 완료한 팬클럽 멤버 카운트 획득
    fun getMemberCheckoutCount(fanClubId: String, myCallback: (Int) -> Unit) {
        val startCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val endCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }

        firestore.collection("fanClub")?.document(fanClubId)?.collection("member")
            ?.whereLessThanOrEqualTo("checkoutTime", endCal.time)
            ?.whereGreaterThanOrEqualTo("checkoutTime", startCal.time)?.get()?.addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    myCallback(task.result.size())
                } else {
                    myCallback(0)
                }
            }
    }

    // 광고 설정 불러오기
    fun getAdPolicy() {
        firestore.collection("preferences").document("ad_policy")?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.exists()) {
                val adPolicy = task.result.toObject(AdPolicyDTO::class.java)!!
                adPolicyDTO.value = adPolicy
            }
        }
    }

    // 환결 설정 불러오기
    fun getPreferences() {
        firestore.collection("preferences").document("preferences")?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (documentSnapshot == null) return@addSnapshotListener
            val preferences = documentSnapshot?.toObject(PreferencesDTO::class.java)!!
            preferencesDTO.value = preferences
        }
    }

    // 토큰 정보 불러오기
    fun getToken() {
        fireMessaging.token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            token.value = task.result
        })
    }

    // 사용자 이메일(ID) 사용유무 확인 (true: 사용중, false: 미사용중)
    fun findUserFromEmail(email: String, myCallback: (UserDTO?) -> Unit) {
        var user: UserDTO? = null
        firestore.collection("user")?.whereEqualTo("userId", email)?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.size() > 0) {
                for (document in task.result) { // 사용자 찾음
                    user = document.toObject(UserDTO::class.java)!!
                    myCallback(user)
                }
            } else { // 사용자 못 찾음
                myCallback(user)
            }
        }
    }

    // 사용자 닉네임 사용유무 확인 (true: 사용중, false: 미사용중)
    fun isUsedUserNickname(nickname: String, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.whereEqualTo("nickname", nickname)?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.size() > 0) { // 이름 사용중
                myCallback(true)
            } else { // 사용 가능한 이름
                myCallback(false)
            }
        }
    }

    // 팬클럽 이름 사용유무 확인 (true: 사용중, false: 미사용중)
    fun isUsedFanClubName(name: String, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")?.whereEqualTo("name", name)?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.size() > 0) { // 이름 사용중
                myCallback(true)
            } else { // 사용 가능한 이름
                myCallback(false)
            }
        }
    }

    // 가입된 팬클럽이 있는 사용자 인지 확인 (null: 가입된 팬클럽 있음, not null: 미가입(userDTO 데이터 반환))
    fun getHaveFanClub(uid: String, myCallback: (UserDTO?) -> Unit) {
        var user: UserDTO? = null
        firestore.collection("user")?.document(uid)?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.exists()) {
                user = task.result.toObject(UserDTO::class.java)!!
                if (!user?.fanClubId.isNullOrEmpty()) { // 팬클럽에 이미 가입된 사용자
                    myCallback(null)
                } else {
                    myCallback(user)
                }
            } else {
                myCallback(user)
            }
        }
    }

    //</editor-fold>


    //<editor-fold desc="@ 트랜잭션 함수">

    // 사용자 다이아 추가 (gemType : PAID_GEM 유료 다이아 추가, FREE_GEM 무료 다이아 추가)
    fun addUserGem(uid: String, paidGemCount: Int, freeGemCount: Int, firstPack: String?, myCallback: (UserDTO?) -> Unit) {
        var user: UserDTO? = null
        var tsDoc = firestore.collection("user")?.document(uid)
        firestore.runTransaction { transaction ->
            user = transaction.get(tsDoc!!).toObject(UserDTO::class.java)!!

            if (!firstPack.isNullOrEmpty()) { // 첫 구매 패키지 적용
                user!!.firstGemPackage[firstPack] = false
            }

            if (paidGemCount > 0) { // 유료 다이아 추가
                user?.paidGem = user?.paidGem?.plus(paidGemCount)
            }

            if (freeGemCount > 0) { // 무료 다이아 추가
                user?.freeGem = user?.freeGem?.plus(freeGemCount)
            }

            transaction.set(tsDoc, user!!)
        }?.addOnSuccessListener { result ->
            myCallback(user)
        }?.addOnFailureListener { e ->
            myCallback(user)
        }
    }

    // 사용자 다이아 소비
    fun useUserGem(uid: String, gemCount: Int, myCallback: (UserDTO?) -> Unit) {
        var user: UserDTO? = null
        var tsDoc = firestore.collection("user")?.document(uid)
        firestore.runTransaction { transaction ->
            user = transaction.get(tsDoc!!).toObject(UserDTO::class.java)!!

            user?.useGem(gemCount) // 다이아 차감

            transaction.set(tsDoc, user!!)
        }?.addOnSuccessListener { result ->
            myCallback(user)
        }?.addOnFailureListener { e ->
            myCallback(user)
        }
    }

    // 프리미엄 패키지 구매
    fun applyPremiumPackage(uid: String, myCallback: (UserDTO?) -> Unit) {
        var user: UserDTO? = null
        var tsDoc = firestore.collection("user")?.document(uid)
        firestore.runTransaction { transaction ->
            user = transaction.get(tsDoc!!).toObject(UserDTO::class.java)

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
            user?.premiumExpireTime = calendar.time // 프리미엄 패키지 유효기간 적용

            transaction.set(tsDoc, user!!)
        }?.addOnSuccessListener { result ->
            myCallback(user)
        }?.addOnFailureListener { e ->
            myCallback(user)
        }
    }

    // 사용자 팬클럽 정보(fanClubId) 삭제 (deportation : true 강제추방)
    fun deleteUserFanClubId(uid: String, deportation: Boolean, myCallback: (UserDTO?) -> Unit) {
        var user: UserDTO? = null
        var tsUserDoc = firestore.collection("user")?.document(uid)
        firestore.runTransaction { transaction ->
            user = transaction.get(tsUserDoc!!).toObject(UserDTO::class.java)

            val date = Date()
            user?.fanClubId = null
            user?.fanClubQuitDate = date
            if (deportation) { // 강제추방 날짜 기록
                user?.fanClubDeportationDate = date
            }

            transaction.set(tsUserDoc, user!!)
        }?.addOnSuccessListener { result ->
            myCallback(user)
        }?.addOnFailureListener { e ->
            myCallback(user)
        }
    }

    // 개인 경험치 추가
    fun addUserExp(uid: String, exp: Long, gemCount: Int, myCallback: (UserDTO?) -> Unit) {
        var user: UserDTO? = null
        var tsDoc = firestore.collection("user")?.document(uid)
        firestore.runTransaction { transaction ->
            user = transaction.get(tsDoc!!).toObject(UserDTO::class.java)

            // 다이아 사용일 경우 다이아 소모 적용
            if (gemCount > 0) {
                user?.useGem(gemCount)
            }

            user?.addExp(exp) // 경험치 적용

            transaction.set(tsDoc, user!!)
        }?.addOnSuccessListener { result ->
            myCallback(user)
        }?.addOnFailureListener { e ->
            myCallback(user)
        }
    }

    // 팬클럽 경험치 추가
    fun addFanClubExp(fanClubId: String, exp: Long, myCallback: (FanClubDTO?) -> Unit) {
        var fanClub: FanClubDTO? = null
        var tsDoc = firestore.collection("fanClub")?.document(fanClubId)
        firestore.runTransaction { transaction ->
            fanClub = transaction.get(tsDoc!!).toObject(FanClubDTO::class.java)

            fanClub?.addExp(exp) // 경험치 적용

            transaction.set(tsDoc, fanClub!!)
        }?.addOnSuccessListener { result ->
            myCallback(fanClub)
        }?.addOnFailureListener { e ->
            myCallback(fanClub)
        }
    }

    // 팬클럽 멤버 기여도 추가
    fun addMemberContribution(fanClubId: String, userUid: String, contribution: Long, myCallback: (MemberDTO?) -> Unit) {
        var member: MemberDTO? = null
        var tsDoc = firestore.collection("fanClub")?.document(fanClubId)?.collection("member")?.document(userUid)
        firestore.runTransaction { transaction ->
            member = transaction.get(tsDoc!!).toObject(MemberDTO::class.java)

            member?.contribution = member?.contribution?.plus(contribution)!!

            transaction.set(tsDoc, member!!)
        }?.addOnSuccessListener { result ->
            myCallback(member)
        }?.addOnFailureListener { e ->
            myCallback(member)
        }
    }

    // 팬클럽 멤버 수 증가
    fun addFanClubMemberCount(fanClubId: String, count: Int, myCallback: (FanClubDTO?) -> Unit) {
        var fanClub: FanClubDTO? = null
        var tsDoc = firestore.collection("fanClub")?.document(fanClubId)
        firestore.runTransaction { transaction ->
            fanClub = transaction.get(tsDoc!!).toObject(FanClubDTO::class.java)

            fanClub?.memberCount = fanClub?.memberCount?.plus(count) // 가입된 멤버 수 증가

            transaction.set(tsDoc, fanClub!!)
        }?.addOnSuccessListener { result ->
            myCallback(fanClub)
        }?.addOnFailureListener { e ->
            myCallback(fanClub)
        }
    }

    //</editor-fold>


    //<editor-fold desc="@ 업데이트 및 Set 함수">

    // 사용자 정보 전체 업데이트
    fun updateUser(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(user.uid.toString())?.set(user)?.addOnCompleteListener {
            myCallback(true)
        }
    }

    // 로그인 시간 기록
    fun updateUserLoginTime(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(user.uid.toString())?.update("loginTime", user.loginTime).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 사용자 프리미엄 패키지 다이아 수령 시간 기록
    fun updateUserPremiumGemGetTime(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(user.uid.toString())?.update("premiumGemGetTime", user.premiumGemGetTime).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 사용자 토큰 업데이트
    fun updateUserToken(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(user.uid.toString())?.update("token", user.token).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 사용자 메인타이틀 업데이트
    fun updateUserMainTitle(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(user.uid.toString())?.update("mainTitle", user.mainTitle).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 사용자 내 소개 업데이트
    fun updateUserAboutMe(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(user.uid.toString())?.update("aboutMe", user.aboutMe).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 사용자 프로필 업데이트
    fun updateUserProfile(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(user.uid.toString())?.update("imgProfile", user.imgProfile).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 사용자 닉네임 업데이트
    fun updateUserNickname(user: UserDTO, gemCount: Int, myCallback: (UserDTO?) -> Unit) {
        var resultUser: UserDTO? = null
        var tsDoc = firestore.collection("user")?.document(user.uid.toString())
        firestore.runTransaction { transaction ->
            resultUser = transaction.get(tsDoc!!).toObject(UserDTO::class.java)!!

            resultUser?.nickname = user.nickname
            resultUser?.nicknameChangeDate = Date()

            if (gemCount > 0) {
                resultUser?.useGem(gemCount) // 다이아 차감
            }

            transaction.set(tsDoc, resultUser!!)
        }?.addOnSuccessListener { result ->
            myCallback(resultUser)
        }?.addOnFailureListener { e ->
            myCallback(resultUser)
        }
    }

    // 사용자 일일 과제 달성 업데이트
    fun updateUserQuestSuccessTimes(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(user.uid.toString())?.update("questSuccessTimes", user.questSuccessTimes)?.addOnCompleteListener { task ->
            myCallback(true)
        }
    }

    // 사용자 일일 과제 보상 업데이트
    fun updateUserQuestGemGetTimes(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(user.uid.toString())?.update("questGemGetTimes", user.questGemGetTimes)?.addOnCompleteListener { task ->
            myCallback(true)
        }
    }

    // 사용자 팬클럽 가입 신청 리스트 업데이트
    fun updateUserFanClubRequestId(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(user.uid.toString())?.update("fanClubRequestId", user.fanClubRequestId)?.addOnCompleteListener { task ->
            myCallback(true)
        }
    }

    // 사용자 팬클럽 가입 승인 (팬클럽 ID 업데이트 및 팬클럽 신청 이력 삭제)
    fun updateUserFanClubApproval(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(user.uid.toString())?.update("fanClubId", user.fanClubId)?.addOnCompleteListener {
            // 다른 팬 클럽에 신청한 이력이 있으면 찾아서 삭제
            for (id in user.fanClubRequestId) {
                if (user.fanClubId != id) { // 현재 팬클럽이 아닌 나머지 팬클럽만 삭제
                    firestore.collection("fanClub")?.document(id)?.collection("member")?.document(user.uid.toString())?.delete()?.addOnCompleteListener {
                        // 삭제 성공
                    }
                }
            }

            // 팬클럽 가입 리스트 제거
            user.fanClubRequestId.clear()
            updateUserFanClubRequestId(user) {

            }

            myCallback(true)
        }
    }

    // 사용자 팬클럽 가입 거절 (팬클럽 신청 목록에서 제거 및 개인 신청 리스트 제거)
    fun updateUserFanClubReject(fanClubId: String, userUid: String, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")?.document(fanClubId)?.collection("member")?.document(userUid)?.delete()?.addOnCompleteListener {
            // 사용자 팬클럽 신청 리스트에서 해당 팬클럽 제거
            getUser(userUid) { user ->
                if (user != null) {
                    user?.fanClubRequestId?.remove(fanClubId)
                    updateUserFanClubRequestId(user!!) {

                    }
                }
            }
            myCallback(true)
        }
    }

    // 사용자 메일 읽음 상태로 업데이트
    fun updateUserMailRead(uid: String, mailUid: String, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(uid)
            ?.collection("mail")?.document(mailUid)?.update("read", true)?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 사용자 메일 삭제 상태로 업데이트
    fun updateUserMailDelete(uid: String, mailUid: String, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(uid)
            ?.collection("mail")?.document(mailUid)?.update("deleted", true)?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 사용자 메일 발송
    fun sendUserMail(uid: String, mail: MailDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(uid)
            ?.collection("mail")?.document(mail.docName.toString())?.set(mail)?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 전광판 등록
    fun sendDisplayBoard(displayText: String, color: Int, user: UserDTO, myCallback: (Boolean) -> Unit) {
        // 마지막 전광판 항목을 찾아서 order 증가
        firestore.collection("displayBoard")?.orderBy("order", Query.Direction.DESCENDING)?.limit(1)?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    var displayBoardDTO = document.toObject(DisplayBoardDTO::class.java)!!

                    var newDisplayBoard = DisplayBoardDTO()
                    newDisplayBoard.docName = Utility.randomDocumentName()
                    newDisplayBoard.displayText = displayText
                    newDisplayBoard.userUid = user.uid
                    newDisplayBoard.userNickname = user.nickname
                    newDisplayBoard.color = color
                    newDisplayBoard.order = displayBoardDTO.order?.plus(1)
                    newDisplayBoard.createTime = Date()

                    firestore.collection("displayBoard")?.document().set(newDisplayBoard)?.addOnCompleteListener {
                        myCallback(true)
                    }
                }
            }
        }
    }

    // 팬클럽 채팅 전송
    fun sendFanClubChat(fanClubId: String, chat: DisplayBoardDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")
            ?.document(fanClubId)?.collection("chat")?.document().set(chat)?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 팬클럽 정보 전체 업데이트
    fun updateFanClub(fanClub: FanClubDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")?.document(fanClub.docName.toString())?.set(fanClub)?.addOnCompleteListener {
            myCallback(true)
        }
    }

    // 팬클럽 소개 업데이트
    fun updateFanClubDescription(fanClub: FanClubDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")?.document(fanClub.docName.toString())?.update("description", fanClub.description)?.addOnCompleteListener {
            myCallback(true)
        }
    }

    // 팬클럽 클럽장 닉네임 업데이트
    fun updateFanClubMasterNickname(fanClub: FanClubDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")?.document(fanClub.docName.toString())
            ?.update("masterNickname", fanClub.masterNickname)?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 팬클럽 멤버 내 소개 업데이트
    fun updateMemberAboutMe(fanClubId: String, member: MemberDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")?.document(fanClubId)
            ?.collection("member")?.document(member?.userUid.toString())
            ?.update("userAboutMe", member.userAboutMe)?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 팬클럽 멤버 닉네임 업데이트
    fun updateMemberNickname(fanClubId: String, member: MemberDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")?.document(fanClubId)
            ?.collection("member")?.document(member?.userUid.toString())
            ?.update("userNickname", member.userNickname)?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 팬클럽 멤버 정보 전체 업데이트
    fun updateMember(fanClubId: String, member: MemberDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")?.document(fanClubId)
            ?.collection("member")?.document(member.userUid.toString())?.set(member)?.addOnCompleteListener {
            myCallback(true)
        }
    }

    // 팬클럽 멤버 토큰 업데이트
    fun updateMemberToken(user: UserDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")?.document(user?.fanClubId.toString())
            ?.collection("member")?.document(user?.uid.toString())?.update("token", user?.token)?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 개인 튜토리얼 종료 시간 업데이트
    fun updateUserTutorialEndedTime(uid: String, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(uid)?.update("tutorialEndedTime", Date())?.addOnCompleteListener {
            myCallback(true)
        }
    }

    // 개인 출석체크 업데이트
    fun updateUserCheckout(uid: String, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(uid)?.update("checkoutTime", Date())?.addOnCompleteListener {
            myCallback(true)
        }
    }

    // 팬클럽 출석체크 업데이트
    fun updateFanClubCheckout(fanClubId: String, userUid: String, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")?.document(fanClubId)
            ?.collection("member")?.document(userUid)?.update("checkoutTime", Date())?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 개인 스케줄 전체 업데이트
    fun updatePersonalSchedule(uid: String, schedule: ScheduleDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(uid)
            ?.collection("schedule")?.document(schedule.docName.toString())
            ?.set(schedule)?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 팬클럽 스케줄 전체 업데이트
    fun updateFanClubSchedule(fanClubId: String, schedule: ScheduleDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")?.document(fanClubId)
            ?.collection("schedule")?.document(schedule.docName.toString())
            ?.set(schedule)?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 개인 스케줄 우선순위 업데이트
    fun updatePersonalScheduleOrder(uid: String, schedule: ScheduleDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(uid)
            ?.collection("schedule")?.document(schedule.docName.toString())
            ?.update("order", schedule.order)?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 팬클럽 스케줄 우선순위 업데이트
    fun updateFanClubScheduleOrder(fanClubId: String, schedule: ScheduleDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")?.document(fanClubId)
            ?.collection("schedule")?.document(schedule.docName.toString())
            ?.update("order", schedule.order)?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 개인 스케줄 진행도 업데이트
    fun updatePersonalMissionProgress(uid: String, item: DashboardMissionDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(uid)
            ?.collection("schedule")?.document(item.scheduleDTO?.docName.toString())
            ?.collection("progress")?.document(item.scheduleProgressDTO?.docName.toString())
            ?.set(item.scheduleProgressDTO!!)?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 개인 스케줄 통계 정보 기록
    fun updatePersonalScheduleStatistics(uid: String, docName: String, fieldValue: Pair<String, String>, averagePercent: Int, myCallback: (Boolean) -> Unit) {
        var values: MutableMap<String, Int> = mutableMapOf()
        firestore.collection("user")?.document(uid)
            ?.collection("scheduleStatistics")?.document(docName)?.get()?.addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    if (task.result.contains(fieldValue.first)) {
                        values = task.result!![fieldValue.first] as MutableMap<String, Int>
                    }
                }
                values[fieldValue.second] = averagePercent
                if (!task.result.exists()) { // document 없으면 생성
                    val docData = hashMapOf(fieldValue.first to values)
                    firestore.collection("user")?.document(uid)
                        ?.collection("scheduleStatistics")?.document(docName)?.set(docData)?.addOnCompleteListener {
                            myCallback(true)
                    }
                } else { // document 있으면 기존 데이터에 update
                    firestore.collection("user")?.document(uid)
                        ?.collection("scheduleStatistics")?.document(docName)?.update(fieldValue.first, values)?.addOnCompleteListener {
                            myCallback(true)
                    }
                }
            }
    }

    // 팬클럽 스케줄 진행도 업데이트
    fun updateFanClubMissionProgress(fanClubId: String, userUid: String, item: DashboardMissionDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")?.document(fanClubId)
            ?.collection("member")?.document(userUid)
            ?.collection("schedule")?.document(item.scheduleDTO?.docName.toString())
            ?.collection("progress")?.document(item.scheduleProgressDTO?.docName.toString())
            ?.set(item.scheduleProgressDTO!!)?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 팬클럽 스케줄 통계 정보 기록
    fun updateFanClubScheduleStatistics(fanClubId: String, userUid: String, docName: String, fieldValue: Pair<String, String>, averagePercent: Int, myCallback: (Boolean) -> Unit) {
        var values: MutableMap<String, Int> = mutableMapOf()
        firestore.collection("fanClub")?.document(fanClubId)
            ?.collection("member")?.document(userUid)
            ?.collection("scheduleStatistics")?.document(docName)?.get()?.addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    if (task.result.contains(fieldValue.first)) {
                        values = task.result!![fieldValue.first] as MutableMap<String, Int>
                    }
                }
                values[fieldValue.second] = averagePercent
                if (!task.result.exists()) { // document 없으면 생성
                    val docData = hashMapOf(fieldValue.first to values)
                    firestore.collection("fanClub")?.document(fanClubId)
                        ?.collection("member")?.document(userUid)
                        ?.collection("scheduleStatistics")?.document(docName)?.set(docData)
                        ?.addOnCompleteListener {
                            myCallback(true)
                        }
                } else { // document 있으면 기존 데이터에 update
                    firestore.collection("fanClub")?.document(fanClubId)
                        ?.collection("member")?.document(userUid)
                        ?.collection("scheduleStatistics")?.document(docName)?.update(fieldValue.first, values)
                        ?.addOnCompleteListener {
                            myCallback(true)
                        }
                }
            }
    }

    // 오늘 다이아뽑기 완료 횟수 기록
    fun updateTodayCompleteGambleCount(uid: String, myCallback: (Long?) -> Unit) {
        var completeGambleCount : Long? = null
        val currentDate = SimpleDateFormat("yyyyMMdd").format(Date())
        var tsDoc = firestore.collection("user")?.document(uid)?.collection("otherOption")?.document("completeGambleCount")
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(tsDoc!!)
            completeGambleCount = snapshot.getLong(currentDate)
            if (completeGambleCount != null) {
                completeGambleCount = completeGambleCount?.plus(1)
                transaction.update(tsDoc, currentDate, completeGambleCount)
            } else {
                completeGambleCount = 1
                val docData = hashMapOf(currentDate to completeGambleCount)
                transaction.set(tsDoc, docData)
            }
        }?.addOnSuccessListener { result ->
            myCallback(completeGambleCount)
        }?.addOnFailureListener { e ->
            myCallback(completeGambleCount)
        }
    }

    // 팬클럽 멤버 직책 업데이트
    fun updateMemberPosition(fanClubId: String, member: MemberDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")?.document(fanClubId)
            ?.collection("member")?.document(member.userUid.toString())
            ?.update("position", member.position)?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 팬클럽 멤버 레벨 업데이트
    fun updateMemberLevel(fanClubId: String, member: MemberDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")?.document(fanClubId)
            ?.collection("member")?.document(member.userUid.toString())
            ?.update("userLevel", member.userLevel)?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 신고
    fun sendReport(report: ReportDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("admin").document("report")
            .collection(report.getCollectionName()).document().set(report).addOnCompleteListener {
            myCallback(true)
        }
    }

    // 팬클럽 삭제
    fun deleteFanClub(fanClubId: String, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")?.document(fanClubId)?.delete()?.addOnCompleteListener {
            myCallback(true)
        }
    }

    // 팬클럽 멤버 삭제(추방)
    fun deleteMember(fanClubId: String, member: MemberDTO, myCallback: (FanClubDTO?) -> Unit) {
        var fanClub: FanClubDTO? = null
        var isSubMaster = (member.position == MemberDTO.Position.SUB_MASTER) // 삭제되는 멤버가 부클럽장인지 확인
        firestore.collection("fanClub")?.document(fanClubId)
            ?.collection("member")?.document(member.userUid.toString())?.delete()?.addOnCompleteListener {
                var tsDoc = firestore.collection("fanClub")?.document(fanClubId)
                firestore.runTransaction { transaction ->
                    fanClub = transaction.get(tsDoc!!).toObject(FanClubDTO::class.java)

                    fanClub?.memberCount = fanClub?.memberCount?.minus(1) // 팬클럽 멤버 1 감소

                    // 부클럽장이 삭제될 경우 부클럽장 카운트 감소
                    if (isSubMaster) {
                        fanClub?.subMasterCount = fanClub?.subMasterCount?.minus(1)
                    }

                    transaction.set(tsDoc, fanClub!!)
                }?.addOnSuccessListener { result ->
                    myCallback(fanClub)
                }?.addOnFailureListener { e ->
                    myCallback(fanClub)
                }
            }
    }

    // 부클럽장 임명/해임 (isDelete: ture 임명, false 해임)
    fun updateFanClubSubMaster(fanClubId: String, member: MemberDTO, isDelete: Boolean, myCallback: (FanClubDTO?) -> Unit) {
        var fanClub: FanClubDTO? = null
        updateMemberPosition(fanClubId, member) {
            var tsDoc = firestore.collection("fanClub")?.document(fanClubId)
            firestore.runTransaction { transaction ->
                fanClub = transaction.get(tsDoc!!).toObject(FanClubDTO::class.java)

                if (isDelete) {
                    fanClub?.subMasterCount = fanClub?.subMasterCount?.minus(1) // 부클럽장 수 1 감소
                } else {
                    fanClub?.subMasterCount = fanClub?.subMasterCount?.plus(1) // 부클럽장 수 1 증가
                }

                transaction.set(tsDoc, fanClub!!)
            }?.addOnSuccessListener { result ->
                myCallback(fanClub)
            }?.addOnFailureListener { e ->
                myCallback(fanClub)
            }
        }
    }

    // 개인 스케줄 삭제
    fun deletePersonalSchedule(uid: String, scheduleId: String, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(uid)
            ?.collection("schedule")?.document(scheduleId)?.delete()?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 팬클럽 스케줄 삭제
    fun deleteFanClubSchedule(fanClubId: String, scheduleId: String, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")?.document(fanClubId)
            ?.collection("schedule")?.document(scheduleId)?.delete()?.addOnCompleteListener {
                myCallback(true)
            }
    }

    //</editor-fold>


    //<editor-fold desc="@ 로그 기록 함수">

    // 사용자 로그 작성
    fun writeUserLog(uid: String, log: LogDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("user")?.document(uid)
            ?.collection("log")?.document().set(log)?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 팬클럽 로그 작성
    fun writeFanClubLog(fanClubId: String, log: LogDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("fanClub")?.document(fanClubId)
            ?.collection("log")?.document().set(log)?.addOnCompleteListener {
                myCallback(true)
            }
    }

    // 관리자 로그 작성
    fun writeAdminLog(log: LogDTO, myCallback: (Boolean) -> Unit) {
        firestore.collection("adminLog")?.document().set(log)?.addOnCompleteListener {
            myCallback(true)
        }
    }

    //</editor-fold>



    // 푸시 메세지 전송
    suspend fun sendNotification(notification: NotificationBody) {
        myResponse.value = RetrofitInstance.api.sendNotification(notification)
    }
}