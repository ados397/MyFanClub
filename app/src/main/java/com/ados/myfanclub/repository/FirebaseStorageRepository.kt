package com.ados.myfanclub.repository

import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream


class FirebaseStorageRepository() {
    private val TAG = "FirebaseStorageRepository"

    enum class ScheduleType {
        PERSONAL, FAN_CLUB
    }

    //<editor-fold desc="@ 변수 선언">

    // FirestoreStorage 초기화
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    //private val userProfilePath = "images/user_profile/"
    //private val fanClubSymbolPath = "images/fan_club_symbol/"
    private val userImagePath = "images/user/"
    private val fanClubImagePath = "images/fan_club/"
    private val userScheduleImagePath = "${userImagePath}/schedule/"
    private val fanClubScheduleImagePath = "${fanClubImagePath}/schedule/"
    private val userProfileImageName = "user_profile.jpg"
    private val fanClubSymbolImageName = "fan_club_symbol.jpg"


    //</editor-fold>


    //<editor-fold desc="@ 데이터 획득 함수">

    private fun compressBitmap(bitmap: Bitmap) : ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)

        return byteArrayOutputStream.toByteArray()
    }

    // 사용자 프로필 이미지 불러오기
    fun getUserProfileImage(uid: String, myCallback: (Uri?) -> Unit) {
        //val fileName = "${userProfilePath}user_profile_${uid}.jpg"
        val fileName = "${userImagePath}${uid}/${userProfileImageName}"

        storageRef.child(fileName).downloadUrl.addOnSuccessListener { uri -> //이미지 로드 성공시
            myCallback(uri)
        }.addOnFailureListener { //이미지 로드 실패시
            myCallback(null)
        }
    }

    // 사용자 프로필 이미지 저장
    fun setUserProfileImage(uid: String, bitmap: Bitmap, myCallback: (Boolean) -> Unit) {
        //val fileName = "user_profile_${uid}.jpg"
        val filePath = "${userImagePath}/${uid}/"

        //var imagesRef = storageRef.child(userProfilePath).child(fileName)    //기본 참조 위치/images/${fileName}
        var imagesRef = storageRef.child(filePath).child(userProfileImageName)
        //이미지 파일 업로드
        imagesRef.putBytes(compressBitmap(bitmap)).addOnSuccessListener {
            myCallback(true)
        }.addOnFailureListener {
            myCallback(false)
        }
    }

    // 사용자 프로필 이미지 삭제
    fun deleteUserProfileImage(uid: String, myCallback: (Boolean) -> Unit) {
        //val fileName = "user_profile_${uid}.jpg"
        val filePath = "${userImagePath}/${uid}/"

        var imagesRef = storageRef.child(filePath).child(userProfileImageName)    //기본 참조 위치/images/${fileName}
        //이미지 파일 삭제
        imagesRef.delete().addOnSuccessListener {
            myCallback(true)
        }.addOnFailureListener {
            myCallback(false)
        }
    }

    // 팬클럽 심볼 이미지 불러오기
    fun getFanClubSymbolImage(fanClubId: String, myCallback: (Uri?) -> Unit) {
        //val fileName = "${fanClubSymbolPath}fan_club_symbol_${fanClubId}.jpg"
        val fileName = "${fanClubImagePath}${fanClubId}/${fanClubSymbolImageName}"

        storageRef.child(fileName).downloadUrl.addOnSuccessListener { uri -> //이미지 로드 성공시
            myCallback(uri)
        }.addOnFailureListener { //이미지 로드 실패시
            myCallback(null)
        }
    }

    // 팬클럽 심볼 이미지 저장
    fun setFanClubSymbolImage(fanClubId: String, bitmap: Bitmap, myCallback: (Boolean) -> Unit) {
        //val fileName = "fan_club_symbol_${fanClubId}.jpg"
        val filePath = "${fanClubImagePath}/${fanClubId}/"

        //var imagesRef = storageRef.child(fanClubSymbolPath).child(fileName)    //기본 참조 위치/images/${fileName}
        var imagesRef = storageRef.child(filePath).child(fanClubSymbolImageName)
        //이미지 파일 업로드
        imagesRef.putBytes(compressBitmap(bitmap)).addOnSuccessListener {
            myCallback(true)
        }.addOnFailureListener {
            myCallback(false)
        }
    }

    // 팬클럽 심볼 이미지 삭제
    fun deleteFanClubSymbolImage(fanClubId: String, myCallback: (Boolean) -> Unit) {
        //val fileName = "fan_club_symbol_${fanClubId}.jpg"
        val filePath = "${fanClubImagePath}/${fanClubId}/"

        //var imagesRef = storageRef.child(fanClubSymbolPath).child(fileName)    //기본 참조 위치/images/${fileName}
        var imagesRef = storageRef.child(filePath).child(fanClubSymbolImageName)
        //이미지 파일 업로드
        imagesRef.delete().addOnSuccessListener {
            myCallback(true)
        }.addOnFailureListener {
            myCallback(false)
        }
    }

    // 스케줄 이미지 불러오기
    fun getScheduleImage(uid: String, scheduleId: String, type: ScheduleType, myCallback: (Uri?) -> Unit) {
        val fileName = if (type == ScheduleType.PERSONAL)
            "${userImagePath}${uid}/schedule/${scheduleId}.jpg"
        else
            "${fanClubImagePath}${uid}/schedule/${scheduleId}.jpg"

        storageRef.child(fileName).downloadUrl.addOnSuccessListener { uri -> //이미지 로드 성공시
            myCallback(uri)
        }.addOnFailureListener { //이미지 로드 실패시
            myCallback(null)
        }
    }

    // 스케줄 이미지 저장
    fun setScheduleImage(uid: String, scheduleId: String, type: ScheduleType, bitmap: Bitmap, myCallback: (Boolean) -> Unit) {
        val filePath = if (type == ScheduleType.PERSONAL)
            "${userImagePath}${uid}/schedule/"
        else
            "${fanClubImagePath}${uid}/schedule/"
        val fileName = "${scheduleId}.jpg"

        var imagesRef = storageRef.child(filePath).child(fileName)
        //이미지 파일 업로드
        imagesRef.putBytes(compressBitmap(bitmap)).addOnSuccessListener {
            myCallback(true)
        }.addOnFailureListener {
            myCallback(false)
        }
    }

    // 스케줄 이미지 삭제
    fun deleteScheduleImage(uid: String, scheduleId: String, type: ScheduleType, myCallback: (Boolean) -> Unit) {
        val filePath = if (type == ScheduleType.PERSONAL)
            "${userImagePath}${uid}/schedule/"
        else
            "${fanClubImagePath}${uid}/schedule/"
        val fileName = "${scheduleId}.jpg"

        var imagesRef = storageRef.child(filePath).child(fileName)
        //이미지 파일 업로드
        imagesRef.delete().addOnSuccessListener {
            myCallback(true)
        }.addOnFailureListener {
            myCallback(false)
        }
    }

    //</editor-fold>
}