package com.ados.myfanclub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ados.myfanclub.model.*
import com.ados.myfanclub.repository.FirebaseRepository
import kotlinx.coroutines.launch
import java.util.ArrayList

class FirebaseViewModel(application: Application) : AndroidViewModel(application) {

    //<editor-fold desc="@ 변수 선언">

    private val repository : FirebaseRepository = FirebaseRepository()
    val userDTO = repository.userDTO
    val fanClubDTO = repository.fanClubDTO
    val memberDTO = repository.memberDTO
    val displayBoardDTO = repository.displayBoardDTO
    val mailDTOs = repository.mailDTOs
    val displayBoardDTOs = repository.displayBoardDTOs
    val userDTOs = repository.userDTOs
    val fanClubDTOs = repository.fanClubDTOs
    val scheduleDTOs = repository.scheduleDTOs
    val personalDashboardMissionDTOs = repository.personalDashboardMissionDTOs
    val fanClubDashboardMissionDTOs = repository.fanClubDashboardMissionDTOs
    val scheduleStatistics = repository.scheduleStatistics
    //val currentQuiz = repository.currentQuiz
    val adPolicyDTO = repository.adPolicyDTO
    val preferencesDTO = repository.preferencesDTO
    val token = repository.token
    val myResponse = repository.myResponse

    //</editor-fold>


    //<editor-fold desc="@ 데이터 획득 함수">

    // 사용자 불러오기(실시간)
    fun getUserListen(uid: String) {
        repository.getUserListen(uid)
    }

    // 사용자 불러오기(실시간) 중지
    fun stopUserListen() {
        repository.stopUserListen()
    }

    // 팬클럽 정보 불러오기(실시간)
    fun getFanClubListen(fanClubId: String) {
        repository.getFanClubListen(fanClubId)
    }

    // 팬클럽 정보 불러오기(실시간) 중지
    fun stopFanClubListen() {
        repository.stopFanClubListen()
    }

    // 팬클럽 멤버 불러오기(실시간)
    fun getMemberListen(fanClubId: String, userUid: String) {
        repository.getMemberListen(fanClubId, userUid)
    }

    // 팬클럽 멤버 불러오기(실시간) 중지
    fun stopMemberListen() {
        repository.stopMemberListen()
    }

    // 전광판 불러오기(실시간)
    fun getDisplayBoardListen() {
        repository.getDisplayBoardListen()
    }

    // 전광판 불러오기(실시간) 중지
    fun stopDisplayBoardListen() {
        repository.stopDisplayBoardListen()
    }

    // 메일 리스트 불러오기(실시간)
    fun getMailsListen(uid: String) {
        repository.getMailsListen(uid)
    }

    // 메일 리스트 불러오기(실시간) 중지
    fun stopMailsListen() {
        repository.stopMailsListen()
    }

    // 전광판 리스트 불러오기(실시간)
    fun getDisplayBoardsListen() {
        repository.getDisplayBoardsListen()
    }

    // 전광판 리스트 불러오기(실시간) 중지
    fun stopDisplayBoardsListen() {
        repository.stopDisplayBoardsListen()
    }

    // 사용자 불러오기
    fun getUser(uid: String, myCallback: (UserDTO?) -> Unit) {
        repository.getUser(uid) {
            myCallback(it)
        }
    }

    // 팬클럽 불러오기
    fun getFanClub(fanClubId: String, myCallback: (FanClubDTO?) -> Unit) {
        repository.getFanClub(fanClubId) {
            myCallback(it)
        }
    }

    // 팬클럽 멤버 불러오기
    fun getMember(fanClubId: String, userUid: String, myCallback: (MemberDTO?) -> Unit) {
        repository.getMember(fanClubId, userUid) {
            myCallback(it)
        }
    }

    // 사용자 리스트 획득(레벨 순)
    fun getUsers() {
        repository.getUsers()
    }

    // 팬클럽 리스트 획득(누적 경험치 순)
    fun getFanClubs() {
        repository.getFanClubs()
    }

    // 팬클럽 리스트 검색
    fun getFanClubsSearch(query: String) {
        repository.getFanClubsSearch(query)
    }

    // 팬클럽 멤버 리스트 획득(기여도 순)
    fun getMembers(fanClubId: String, memberType: FirebaseRepository.MemberType, myCallback: (ArrayList<MemberDTO>) -> Unit) {
        repository.getMembers(fanClubId, memberType) {
            myCallback(it)
        }
    }

    // 개인 스케줄 리스트 획득
    fun getPersonalSchedules(uid: String) {
        repository.getPersonalSchedules(uid)
    }

    // 팬클럽 스케줄 리스트 획득(실시간), 팬클럽 스케줄은 여러 사람이 추가할 수 있기 때문에 Listener 사용
    fun getFanClubSchedulesListen(fanClubId: String) {
        repository.getFanClubSchedulesListen(fanClubId)
    }

    // 팬클럽 스케줄 리스트 획득(실시간) 중지
    fun stopFanClubSchedulesListen() {
        repository.stopFanClubSchedulesListen()
    }

    // 개인 스케줄 및 진행도 불러오기
    fun getPersonalDashboardMission(uid: String, selectedCycle: ScheduleDTO.Cycle) {
        repository.getPersonalDashboardMission(uid, selectedCycle)
    }

    // 팬클럽 스케줄 및 진행도 불러오기
    fun getFanClubDashboardMission(fanClubId: String, userUid: String, selectedCycle: ScheduleDTO.Cycle) {
        repository.getFanClubDashboardMission(fanClubId, userUid, selectedCycle)
    }

    // 스케줄 통계 정보
    fun getPersonalScheduleStatistics(uid: String, cycle: String, fieldName: String) {
        repository.getPersonalScheduleStatistics(uid, cycle, fieldName)
    }

    // 팬클럽 스케줄 통계 정보
    fun getFanClubScheduleStatistics(fanClubId: String, userUid: String, cycle: String, fieldName: String) {
        repository.getFanClubScheduleStatistics(fanClubId, userUid, cycle, fieldName)
    }

    // 출석체크 완료한 팬클럽 멤버 카운트 획득
    fun getMemberCheckoutCount(fanClubId: String, myCallback: (Int) -> Unit) {
        repository.getMemberCheckoutCount(fanClubId) {
            myCallback(it)
        }
    }

    // 광고 설정 불러오기
    fun getAdPolicy() {
        repository.getAdPolicy()
    }

    // 환결 설정 불러오기
    fun getPreferences() {
        repository.getPreferences()
    }

    // 토큰 정보 불러오기
    fun getToken() {
        repository.getToken()
    }

    // 가입된 팬클럽이 있는 사용자 인지 확인 (null: 가입된 팬클럽 있음, not null: 미가입(userDTO 데이터 반환))
    fun getHaveFanClub(uid: String, myCallback: (UserDTO?) -> Unit) {
        repository.getHaveFanClub(uid) {
            myCallback(it)
        }
    }

    //</editor-fold>


    //<editor-fold desc="@ 트랜잭션 함수">

    // 사용자 무료 다이아 추가 (gemType : PAID_GEM 유료 다이아 추가, FREE_GEM 무료 다이아 추가)
    fun addUserGem(uid: String, paidGemCount: Int, freeGemCount: Int, firstPack: String? = null, myCallback: (UserDTO?) -> Unit) {
        repository.addUserGem(uid, paidGemCount, freeGemCount, firstPack) {
            myCallback(it)
        }
    }

    // 사용자 다이아 소비
    fun useUserGem(uid: String, gemCount: Int, myCallback: (UserDTO?) -> Unit) {
        repository.useUserGem(uid, gemCount) {
            myCallback(it)
        }
    }

    // 프리미엄 패키지 구매
    fun applyPremiumPackage(uid: String, myCallback: (UserDTO?) -> Unit) {
        repository.applyPremiumPackage(uid) {
            myCallback(it)
        }
    }

    // 사용자 팬클럽 정보(fanClubId) 삭제
    fun deleteUserFanClubId(uid: String, deportation: Boolean = false, myCallback: (UserDTO?) -> Unit) {
        repository.deleteUserFanClubId(uid, deportation) {
            myCallback(it)
        }
    }

    // 개인 경험치 추가
    fun addUserExp(uid: String, exp: Long, gemCount: Int, myCallback: (UserDTO?) -> Unit) {
        repository.addUserExp(uid, exp, gemCount) {
            myCallback(it)
        }
    }

    // 팬클럽 경험치 추가
    fun addFanClubExp(fanClubId: String, exp: Long, myCallback: (FanClubDTO?) -> Unit) {
        repository.addFanClubExp(fanClubId, exp) {
            myCallback(it)
        }
    }

    // 팬클럽 멤버 기여도 추가
    fun addMemberContribution(fanClubId: String, userUid: String, contribution: Long, myCallback: (MemberDTO?) -> Unit) {
        repository.addMemberContribution(fanClubId, userUid, contribution) {
            myCallback(it)
        }
    }

    // 팬클럽 멤버 수 증가
    fun addFanClubMemberCount(fanClubId: String, count: Int, myCallback: (FanClubDTO?) -> Unit) {
        repository.addFanClubMemberCount(fanClubId, count) {
            myCallback(it)
        }
    }

    //</editor-fold>


    //<editor-fold desc="@ 업데이트 및 Set 함수">

    // 사용자 정보 전체 업데이트
    fun updateUser(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUser(user) {
            myCallback(it)
        }
    }

    // 로그인 시간 기록
    fun updateUserLoginTime(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserLoginTime(user) {
            myCallback(it)
        }
    }

    // 사용자 프리미엄 패키지 다이아 수령 시간 기록
    fun updateUserPremiumGemGetTime(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserPremiumGemGetTime(user) {
            myCallback(it)
        }
    }

    // 사용자 토큰 업데이트
    fun updateUserToken(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserToken(user) {
            myCallback(it)
        }
    }

    // 사용자 메인타이틀 업데이트
    fun updateUserMainTitle(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserMainTitle(user) {
            myCallback(it)
        }
    }

    // 사용자 내 소개 업데이트
    fun updateUserAboutMe(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserAboutMe(user) {
            myCallback(it)
        }
    }

    // 사용자 프로필 업데이트
    fun updateUserProfile(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserProfile(user) {
            myCallback(it)
        }
    }

    // 사용자 닉네임 업데이트
    fun updateUserNickname(user: UserDTO, gemCount: Int, myCallback: (UserDTO?) -> Unit) {
        repository.updateUserNickname(user, gemCount) {
            myCallback(it)
        }
    }

    // 사용자 일일 과제 달성 업데이트
    fun updateUserQuestSuccessTimes(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserQuestSuccessTimes(user) {
            myCallback(it)
        }
    }

    // 사용자 일일 과제 보상 업데이트
    fun updateUserQuestGemGetTimes(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserQuestGemGetTimes(user) {
            myCallback(it)
        }
    }

    // 사용자 팬클럽 가입 신청 리스트 업데이트
    fun updateUserFanClubRequestId(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserFanClubRequestId(user) {
            myCallback(it)
        }
    }

    // 사용자 팬클럽 가입 승인 (팬클럽 ID 업데이트 및 팬클럽 신청 이력 삭제)
    fun updateUserFanClubApproval(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateUserFanClubApproval(user) {
            myCallback(it)
        }
    }

    // 사용자 팬클럽 가입 거절 (팬클럽 신청 목록에서 제거 및 개인 신청 리스트 제거)
    fun updateUserFanClubReject(fanClubId: String, userUid: String, myCallback: (Boolean) -> Unit) {
        repository.updateUserFanClubReject(fanClubId, userUid) {
            myCallback(it)
        }
    }

    // 사용자 메일 삭제 상태로 업데이트
    fun updateUserMailDelete(uid: String, mailUid: String, myCallback: (Boolean) -> Unit) {
        repository.updateUserMailDelete(uid, mailUid) {
            myCallback(it)
        }
    }

    // 사용자 메일 발송
    fun sendUserMail(uid: String, mail: MailDTO, myCallback: (Boolean) -> Unit) {
        repository.sendUserMail(uid, mail) {
            myCallback(it)
        }
    }

    // 전광판 등록
    fun sendDisplayBoard(displayText: String, color: Int, user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.sendDisplayBoard(displayText, color, user) {
            myCallback(it)
        }
    }

    // 팬클럽 정보 전체 업데이트
    fun updateFanClub(fanClub: FanClubDTO, myCallback: (Boolean) -> Unit) {
        repository.updateFanClub(fanClub) {
            myCallback(it)
        }
    }

    // 팬클럽 소개 업데이트
    fun updateFanClubDescription(fanClub: FanClubDTO, myCallback: (Boolean) -> Unit) {
        repository.updateFanClubDescription(fanClub) {
            myCallback(it)
        }
    }

    // 팬클럽 클럽장 닉네임 업데이트
    fun updateFanClubMasterNickname(fanClub: FanClubDTO, myCallback: (Boolean) -> Unit) {
        repository.updateFanClubMasterNickname(fanClub) {
            myCallback(it)
        }
    }

    // 팬클럽 멤버 내 소개 업데이트
    fun updateMemberAboutMe(fanClubId: String, member: MemberDTO, myCallback: (Boolean) -> Unit) {
        repository.updateMemberAboutMe(fanClubId, member) {
            myCallback(it)
        }
    }

    // 팬클럽 멤버 닉네임 업데이트
    fun updateMemberNickname(fanClubId: String, member: MemberDTO, myCallback: (Boolean) -> Unit) {
        repository.updateMemberNickname(fanClubId, member) {
            myCallback(it)
        }
    }

    // 팬클럽 멤버 정보 전체 업데이트
    fun updateMember(fanClubId: String, member: MemberDTO, myCallback: (Boolean) -> Unit) {
        repository.updateMember(fanClubId, member) {
            myCallback(it)
        }
    }

    // 팬클럽 멤버 토큰 업데이트
    fun updateMemberToken(user: UserDTO, myCallback: (Boolean) -> Unit) {
        repository.updateMemberToken(user) {
            myCallback(it)
        }
    }

    // 개인 출석체크 업데이트
    fun updateUserCheckout(uid: String, myCallback: (Boolean) -> Unit) {
        repository.updateUserCheckout(uid) {
            myCallback(it)
        }
    }

    // 팬클럽 출석체크 업데이트
    fun updateFanClubCheckout(fanClubId: String, userUid: String, myCallback: (Boolean) -> Unit) {
        repository.updateFanClubCheckout(fanClubId, userUid) {
            myCallback(it)
        }
    }

    // 사용자 이메일(ID) 사용유무 확인 (true: 사용중, false: 미사용중)
    fun findUserFromEmail(email: String, myCallback: (UserDTO?) -> Unit) {
        repository.findUserFromEmail(email) {
            myCallback(it)
        }
    }

    // 사용자 닉네임 사용유무 확인 (true: 사용중, false: 미사용중)
    fun isUsedUserNickname(nickname: String, myCallback: (Boolean) -> Unit) {
        repository.isUsedUserNickname(nickname) {
            myCallback(it)
        }
    }

    // 팬클럽 이름 사용유무 확인 (true: 사용중, false: 미사용중)
    fun isUsedFanClubName(name: String, myCallback: (Boolean) -> Unit) {
        repository.isUsedFanClubName(name) {
            myCallback(it)
        }
    }

    // 개인 스케줄 전체 업데이트
    fun updatePersonalSchedule(uid: String, schedule: ScheduleDTO, myCallback: (Boolean) -> Unit) {
        repository.updatePersonalSchedule(uid, schedule) {
            myCallback(it)
        }
    }

    // 팬클럽 스케줄 전체 업데이트
    fun updateFanClubSchedule(fanClubId: String, schedule: ScheduleDTO, myCallback: (Boolean) -> Unit) {
        repository.updateFanClubSchedule(fanClubId, schedule) {
            myCallback(it)
        }
    }

    // 개인 스케줄 우선순위 업데이트
    fun updatePersonalScheduleOrder(uid: String, schedule: ScheduleDTO, myCallback: (Boolean) -> Unit) {
        repository.updatePersonalScheduleOrder(uid, schedule) {
            myCallback(it)
        }
    }
    // 팬클럽 스케줄 우선순위 업데이트
    fun updateFanClubScheduleOrder(fanClubId: String, schedule: ScheduleDTO, myCallback: (Boolean) -> Unit) {
        repository.updateFanClubScheduleOrder(fanClubId, schedule) {
            myCallback(it)
        }
    }

    // 개인 스케줄 진행도 업데이트
    fun updatePersonalMissionProgress(uid: String, item: DashboardMissionDTO, myCallback: (Boolean) -> Unit) {
        repository.updatePersonalMissionProgress(uid, item) {
            myCallback(it)
        }
    }

    // 개인 스케줄 통계 정보 기록
    fun updatePersonalScheduleStatistics(uid: String, docName: String, fieldValue: Pair<String, String>, averagePercent: Int, myCallback: (Boolean) -> Unit) {
        repository.updatePersonalScheduleStatistics(uid, docName, fieldValue, averagePercent) {
            myCallback(it)
        }
    }

    // 팬클럽 스케줄 진행도 업데이트
    fun updateFanClubMissionProgress(fanClubId: String, userUid: String, item: DashboardMissionDTO, myCallback: (Boolean) -> Unit) {
        repository.updateFanClubMissionProgress(fanClubId, userUid, item) {
            myCallback(it)
        }
    }

    // 팬클럽 스케줄 통계 정보 기록
    fun updateFanClubScheduleStatistics(fanClubId: String, userUid: String, docName: String, fieldValue: Pair<String, String>, averagePercent: Int, myCallback: (Boolean) -> Unit) {
        repository.updateFanClubScheduleStatistics(fanClubId, userUid, docName, fieldValue, averagePercent) {
            myCallback(it)
        }
    }

    // 팬클럽 멤버 직책 업데이트
    fun updateMemberPosition(fanClubId: String, member: MemberDTO, myCallback: (Boolean) -> Unit) {
        repository.updateMemberPosition(fanClubId, member) {
            myCallback(it)
        }
    }

    // 팬클럽 멤버 레벨 업데이트
    fun updateMemberLevel(fanClubId: String, member: MemberDTO, myCallback: (Boolean) -> Unit) {
        repository.updateMemberLevel(fanClubId, member) {
            myCallback(it)
        }
    }

    // 팬클럽 삭제
    fun deleteFanClub(fanClubId: String, myCallback: (Boolean) -> Unit) {
        repository.deleteFanClub(fanClubId) {
            myCallback(it)
        }
    }

    // 팬클럽 멤버 삭제(추방)
    fun deleteMember(fanClubId: String, member: MemberDTO, myCallback: (FanClubDTO?) -> Unit) {
        repository.deleteMember(fanClubId, member) {
            myCallback(it)
        }
    }

    // 부클럽장 임명/해임 (isDelete: ture 임명, false 해임)
    fun updateFanClubSubMaster(fanClubId: String, member: MemberDTO, isDelete: Boolean = false, myCallback: (FanClubDTO?) -> Unit) {
        repository.updateFanClubSubMaster(fanClubId, member, isDelete) {
            myCallback(it)
        }
    }

    // 개인 스케줄 삭제
    fun deletePersonalSchedule(uid: String, scheduleId: String, myCallback: (Boolean) -> Unit) {
        repository.deletePersonalSchedule(uid, scheduleId) {
            myCallback(it)
        }
    }

    // 팬클럽 스케줄 삭제
    fun deleteFanClubSchedule(fanClubId: String, scheduleId: String, myCallback: (Boolean) -> Unit) {
        repository.deleteFanClubSchedule(fanClubId, scheduleId) {
            myCallback(it)
        }
    }

    //</editor-fold>

    //<editor-fold desc="@ 로그 기록 함수">

    // 사용자 로그 작성
    fun writeUserLog(uid: String, log: LogDTO, myCallback: (Boolean) -> Unit) {
        repository.writeUserLog(uid, log) {
            myCallback(it)
        }
    }

    // 팬클럽 로그 작성
    fun writeFanClubLog(fanClubId: String, log: LogDTO, myCallback: (Boolean) -> Unit) {
        repository.writeFanClubLog(fanClubId, log) {
            myCallback(it)
        }
    }

    // 관리자 로그 작성
    fun writeAdminLog(log: LogDTO, myCallback: (Boolean) -> Unit) {
        repository.writeAdminLog(log) {
            myCallback(it)
        }
    }

    //</editor-fold>

    // 푸시 메세지 전송
    fun sendNotification(notification: NotificationBody) {
        viewModelScope.launch {
            repository.sendNotification(notification)
        }
    }
}