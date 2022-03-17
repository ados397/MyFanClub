package com.ados.myfanclub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.ados.myfanclub.databinding.ActivitySplashBinding
import com.ados.myfanclub.model.UserDTO
import com.ados.myfanclub.util.AdsInterstitialManager
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    private var _binding: ActivitySplashBinding? = null
    private val binding get() = _binding!!

    private var firebaseAuth : FirebaseAuth? = null
    private val firebaseViewModel : FirebaseViewModel by viewModels()

    private var googleSignInClient : GoogleSignInClient? = null

    // AD
    private var adsInterstitialManager : AdsInterstitialManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //기본 로그인 방식 사용
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

        firebaseViewModel.getAdPolicy()
        firebaseViewModel.adPolicyDTO.observe(this) {
            adsInterstitialManager = AdsInterstitialManager(this, firebaseViewModel.adPolicyDTO.value!!)
            adsInterstitialManager?.callInterstitial {
                startActivity()
            }
        }
    }

    private fun callLoginActivity() {
        firebaseAuth?.signOut()
        //Auth.GoogleSignInApi.signOut()
        googleSignInClient?.signOut()?.addOnCompleteListener { }
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun isLogin() : Boolean {
        return firebaseAuth?.currentUser != null
    }

    private fun startActivity() {
        if (isLogin()) { // 로그인 정보가 있으면 메인 페이지 이동
            firebaseViewModel.getUser(firebaseAuth?.currentUser?.uid!!) { userDTO ->
                if (userDTO != null) {
                    if (userDTO.nickname.isNullOrEmpty()) { // 닉네임이 없으면 로그아웃 후 로그인 페이지로 이동
                        callLoginActivity()
                    } else { // 데이터가 모두 있을때만 자동 로그인
                        var intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("user", userDTO)
                        startActivity(intent)
                        finish()
                    }
                } else { // document 없으면 로그아웃 후 로그인 페이지로 이동
                    callLoginActivity()
                }
            }
        } else { // 로그인 정보가 없으면 로그인 페이지 이동
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}