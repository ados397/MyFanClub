package com.ados.myfanclub.repository

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.ados.myfanclub.api.RetrofitInstance
import com.ados.myfanclub.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.exp_up_fan_club_dialog.*
import kotlinx.android.synthetic.main.level_up_fan_club_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import okhttp3.ResponseBody
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random
import com.google.android.gms.tasks.OnFailureListener

import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target

import com.google.android.gms.tasks.OnSuccessListener
import java.io.ByteArrayOutputStream


class FirebaseStorageRepository() {
    private val TAG = "FirebaseStorageRepository"

    //<editor-fold desc="@ 변수 선언">

    // FirestoreStorage 초기화
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    private val userProfilePath = "images/user_profile/"
    private val fanClubSymbolPath = "images/fan_club_symbol/"

    //</editor-fold>


    //<editor-fold desc="@ 데이터 획득 함수">

    private fun compressBitmap(bitmap: Bitmap) : ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)

        return byteArrayOutputStream.toByteArray()
    }

    // 사용자 프로필 이미지 불러오기
    fun getUserProfile(uid: String, myCallback: (Uri?) -> Unit) {
        val fileName = "${userProfilePath}user_profile_${uid}.jpg"

        storageRef.child(fileName).downloadUrl.addOnSuccessListener { uri -> //이미지 로드 성공시
            myCallback(uri)
        }.addOnFailureListener { //이미지 로드 실패시
            myCallback(null)
        }
    }

    // 사용자 프로필 이미지 저장
    fun setUserProfile(uid: String, bitmap: Bitmap, myCallback: (Boolean) -> Unit) {
        val fileName = "user_profile_${uid}.jpg"

        var imagesRef = storageRef.child(userProfilePath).child(fileName)    //기본 참조 위치/images/${fileName}
        //이미지 파일 업로드
        imagesRef.putBytes(compressBitmap(bitmap)).addOnSuccessListener {
            myCallback(true)
        }.addOnFailureListener {
            myCallback(false)
        }
    }

    // 팬클럽 심볼 이미지 불러오기
    fun getFanClubSymbol(fanClubId: String, myCallback: (Uri?) -> Unit) {
        val fileName = "${fanClubSymbolPath}fan_club_symbol_${fanClubId}.jpg"

        storageRef.child(fileName).downloadUrl.addOnSuccessListener { uri -> //이미지 로드 성공시
            myCallback(uri)
        }.addOnFailureListener { //이미지 로드 실패시
            myCallback(null)
        }
    }

    // 팬클럽 심볼 이미지 저장
    fun setFanClubSymbol(fanClubId: String, bitmap: Bitmap, myCallback: (Boolean) -> Unit) {
        val fileName = "fan_club_symbol_${fanClubId}.jpg"

        var imagesRef = storageRef.child(fanClubSymbolPath).child(fileName)    //기본 참조 위치/images/${fileName}
        //이미지 파일 업로드
        imagesRef.putBytes(compressBitmap(bitmap)).addOnSuccessListener {
            myCallback(true)
        }.addOnFailureListener {
            myCallback(false)
        }
    }

    //</editor-fold>
}