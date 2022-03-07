package com.ados.myfanclub.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ados.myfanclub.model.*
import com.ados.myfanclub.repository.FirebaseRepository
import com.ados.myfanclub.repository.FirebaseStorageRepository
import kotlinx.coroutines.launch
import java.util.ArrayList

class FirebaseStorageViewModel(application: Application) : AndroidViewModel(application) {

    //<editor-fold desc="@ 변수 선언">

    private val repository : FirebaseStorageRepository = FirebaseStorageRepository()

    //</editor-fold>


    //<editor-fold desc="@ 데이터 획득 함수">

    // 사용자 프로필 이미지 불러오기
    fun getUserProfile(uid: String, myCallback: (Uri?) -> Unit) {
        repository.getUserProfile(uid) {
            myCallback(it)
        }
    }

    // 사용자 프로필 이미지 저장
    fun setUserProfile(uid: String, bitmap: Bitmap, myCallback: (Boolean) -> Unit) {
        repository.setUserProfile(uid, bitmap) {
            myCallback(it)
        }
    }

    // 사용자 프로필 이미지 삭제
    fun deleteUserProfile(uid: String, myCallback: (Boolean) -> Unit) {
        repository.deleteUserProfile(uid) {
            myCallback(it)
        }
    }

    // 팬클럽 심볼 이미지 불러오기
    fun getFanClubSymbol(fanClubId: String, myCallback: (Uri?) -> Unit) {
        repository.getFanClubSymbol(fanClubId) {
            myCallback(it)
        }
    }

    // 팬클럽 심볼 이미지 저장
    fun setFanClubSymbol(fanClubId: String, bitmap: Bitmap, myCallback: (Boolean) -> Unit) {
        repository.setFanClubSymbol(fanClubId, bitmap) {
            myCallback(it)
        }
    }

    // 팬클럽 심볼 이미지 삭제
    fun deleteFanClubSymbol(fanClubId: String, myCallback: (Boolean) -> Unit) {
        repository.deleteFanClubSymbol(fanClubId) {
            myCallback(it)
        }
    }

    //</editor-fold>
}