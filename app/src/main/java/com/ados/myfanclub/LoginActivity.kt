package com.ados.myfanclub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.ados.myfanclub.databinding.ActivityLoginBinding
import com.ados.myfanclub.dialog.DocumentDialog
import com.ados.myfanclub.dialog.MaintenanceDialog
import com.ados.myfanclub.model.UserDTO
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.*


class LoginActivity : AppCompatActivity() {
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    private val firebaseViewModel : FirebaseViewModel by viewModels()
    private var firebaseAuth : FirebaseAuth? = null
    private var googleSignInClient : GoogleSignInClient? = null

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var callbackManager : CallbackManager? = null
    private var documentDialog : DocumentDialog? = null

    private var backWaitTime = 0L //뒤로가기 연속 클릭 대기 시간

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // 업데이트 설정 획득
        firebaseViewModel.getServerUpdateListen()
        observeUpdate()

        // 구글 로그인 처리
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            println("구글 로그인 $result")
            if (result.resultCode == RESULT_OK) {
                var signResult = Auth.GoogleSignInApi.getSignInResultFromIntent(result.data!!)!!
                if (signResult.isSuccess) {
                    var account = signResult.signInAccount
                    firebaseAuthWithGoogle(account)
                }
            }
        }

        // 구글 로그인 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //기본 로그인 방식 사용
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

        callbackManager = CallbackManager.Factory.create()

        binding.buttonJoin.setOnClickListener {
            firebaseViewModel.getTermsOfUse { document ->
                if (documentDialog == null) {
                    documentDialog = DocumentDialog(this, document)
                    documentDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    documentDialog?.setCanceledOnTouchOutside(false)
                } else {
                    documentDialog?.content = document
                }
                documentDialog?.show()
                documentDialog?.setInfo()
                documentDialog?.setButtonOk("동의")
                documentDialog?.setButtonCancel("동의안함")
                documentDialog?.binding?.buttonDocumentCancel?.setOnClickListener { // No
                    documentDialog?.dismiss()
                    Toast.makeText(this, "약관에 동의해야 회원가입이 가능합니다.", Toast.LENGTH_SHORT).show()
                }
                documentDialog?.binding?.buttonDocumentOk?.setOnClickListener { // Ok
                    documentDialog?.dismiss()
                    Toast.makeText(this, "약관에 동의 하셨습니다.", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, JoinActivity::class.java))
                }
            }
        }

        binding.buttonFindPassword.setOnClickListener {
            startActivity(Intent(this, FindPasswordActivity::class.java))
        }

        binding.editPassword.setOnKeyListener { _, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KEYCODE_ENTER) {
                login()
            }
            false
        }

        binding.buttonLogin.setOnClickListener {
            login()
        }

        binding.buttonLoginGoogle.setOnClickListener {
            val signInIntent = googleSignInClient?.signInIntent
            //startActivityForResult(signInIntent,GOOGLE_LOGIN_CODE)
            resultLauncher.launch(signInIntent)
        }

        binding.buttonLoginFacebook.setOnClickListener {
            facebookLogin()
        }
    }

    // 업데이트 모니터링
    private fun observeUpdate() {
        firebaseViewModel.updateDTO.observe(this) {
            if (firebaseViewModel.updateDTO.value != null) {
                if (firebaseViewModel.updateDTO.value!!.maintenance!!) { // 서버 점검 대화상자 출력
                    onMaintenanceDialog()
                }
            }
        }
    }

    private fun onMaintenanceDialog() {
        val maintenanceDialog = MaintenanceDialog(this, MaintenanceDialog.JobType.MAINTENANCE)
        maintenanceDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        maintenanceDialog.setCanceledOnTouchOutside(false)
        maintenanceDialog.updateDTO = firebaseViewModel.updateDTO.value
        maintenanceDialog.show()
        maintenanceDialog.binding.buttonMaintenanceOk.setOnClickListener {
            maintenanceDialog.dismiss()
            finish() //액티비티 종료
        }
    }

    private fun facebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))

        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                println("페이스북 onSuccess")
                handleFacebookAccessToken(result?.accessToken)

            }

            override fun onCancel() {
                println("페이스북 onCancel")
            }

            override fun onError(error: FacebookException?) {
                println("페이스북 onError")
            }

        })
    }

    private fun login() {
        val email = binding.editEmail.text.toString().trim()
        when {
            email.isNullOrEmpty() -> {
                Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            binding.editPassword.text.isNullOrEmpty() -> {
                Toast.makeText(this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                firebaseAuth?.signInWithEmailAndPassword(email, binding.editPassword.text.toString())?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (firebaseAuth?.currentUser?.isEmailVerified!!) { // 인증 받은 사용자인지 확인
                            firebaseViewModel.getUser(firebaseAuth?.uid!!) { userDTO ->
                                if (userDTO != null) {
                                    if (userDTO.deleteTime != null) { // 탈퇴한 사용자
                                        firebaseAuth?.signOut()
                                        googleSignInClient?.signOut()?.addOnCompleteListener { }
                                        Toast.makeText(this, "탈퇴처리된 사용자입니다.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        callMainActivity(userDTO)
                                        Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(this, "로그인에 실패하였습니다. 관리자에게 문의 하세요.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            /*firestore?.collection("user")?.document(firebaseAuth?.uid!!)?.get()?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    if (task.result.exists()) { // document 있음
                                        var user = task.result.toObject(UserDTO::class.java)!!
                                        callMainActivity(user)
                                        Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                                    } else { // document 없으면 회원 가입 페이지로 이동
                                        Toast.makeText(this, "로그인에 실패하였습니다. 관리자에게 문의 하세요.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }*/
                        } else {
                            firebaseAuth?.signOut()
                            callLoginEmailVerifyActivity(email, binding.editPassword.text.toString())
                            //Toast.makeText(this, "이메일 인증이 완료되지 않았습니다. 이메일 인증 완료 후 로그인 가능합니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else if (!task.exception?.message.isNullOrEmpty()) {
                        //Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, "로그인에 실패하였습니다. 이메일과 비밀번호를 확인해보세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(account : GoogleSignInAccount?) {
        // 구글 로그인은 동일한 이메일로 로그인한 계정(페이스북, 이메일)이 있어도 로그인이 되어버리기 때문에 이미 가입된 정보가 있는지 확인해서 없을때만 처리
        firebaseViewModel.findUserFromEmail(account?.email.toString()) { userDTO ->
            if (userDTO != null) { // 로그인한 구글 계정과 동일한 이메일의 사용자 존재
                if (userDTO.deleteTime != null) { // 탈퇴한 사용자
                    firebaseAuth?.signOut()
                    googleSignInClient?.signOut()?.addOnCompleteListener { }
                    Toast.makeText(this, "탈퇴처리된 사용자입니다.", Toast.LENGTH_SHORT).show()
                    return@findUserFromEmail
                } else if (userDTO.loginType != UserDTO.LoginType.GOOGLE) { // 구글 로그인이 아니라면 다른 방법으로 이미 가입한 사용자
                    Toast.makeText(this, "이미 가입된 이메일 입니다.", Toast.LENGTH_SHORT).show()
                    googleSignInClient?.signOut()?.addOnCompleteListener { }
                    return@findUserFromEmail
                }
            }

            var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
            firebaseAuth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var addUser = UserDTO()
                    addUser.uid = firebaseAuth?.currentUser?.uid
                    addUser.userId = firebaseAuth?.currentUser?.email
                    addUser.loginType = UserDTO.LoginType.GOOGLE
                    addUser.level = 1
                    addUser.exp = 0L
                    addUser.paidGem = 0
                    addUser.freeGem = 0
                    addUser.aboutMe = ""
                    addUser.mainTitle = ""
                    addUser.premiumExpireTime = Date()
                    addUser.createTime = Date()

                    Toast.makeText(this, "구글 로그인 성공", Toast.LENGTH_SHORT).show()
                    loginOrJoin(addUser)
                } else if (!task.exception?.message.isNullOrEmpty()) {
                    //Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    //Toast.makeText(this, "구글 로그인에 실패하였습니다. 이메일과 비밀번호를 확인해보세요.", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "이미 가입된 이메일 입니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        /*firestore?.collection("user")?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    var user = document.toObject(UserDTO::class.java)!!
                    if (user.loginType != UserDTO.LoginType.GOOGLE) {
                        when {
                            user.userId!! == account?.email.toString() -> {
                                Toast.makeText(this, "이미 가입된 이메일 입니다.", Toast.LENGTH_SHORT).show()
                                googleSignInClient?.signOut()?.addOnCompleteListener {

                                }
                                return@addOnCompleteListener
                            }
                        }
                    }
                }

                var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
                firebaseAuth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var addUser = UserDTO()
                        addUser.uid = firebaseAuth?.currentUser?.uid
                        addUser.userId = firebaseAuth?.currentUser?.email
                        addUser.loginType = UserDTO.LoginType.GOOGLE
                        addUser.level = 1
                        addUser.exp = 0L
                        addUser.paidGem = 0
                        addUser.freeGem = 0
                        addUser.aboutMe = ""
                        addUser.mainTitle = ""
                        addUser.premiumExpireTime = Date()
                        addUser.createTime = Date()

                        Toast.makeText(this, "구글 로그인 성공", Toast.LENGTH_SHORT).show()
                        loginOrJoin(addUser)
                    } else if (!task.exception?.message.isNullOrEmpty()) {
                        //Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                        //Toast.makeText(this, "구글 로그인에 실패하였습니다. 이메일과 비밀번호를 확인해보세요.", Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, "이미 가입된 이메일 입니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }*/
    }

    private fun handleFacebookAccessToken(token : AccessToken?){
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        firebaseAuth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var addUser = UserDTO()
                addUser.uid = firebaseAuth?.currentUser?.uid
                addUser.userId = firebaseAuth?.currentUser?.email
                addUser.loginType = UserDTO.LoginType.FACEBOOK
                addUser.level = 1
                addUser.exp = 0L
                addUser.paidGem = 0
                addUser.freeGem = 0
                addUser.aboutMe = ""
                addUser.mainTitle = ""
                addUser.premiumExpireTime = Date()
                addUser.createTime = Date()

                Toast.makeText(this, "페이스북 로그인 성공", Toast.LENGTH_SHORT).show()
                loginOrJoin(addUser)
            } else if (!task.exception?.message.isNullOrEmpty()) {
                //Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "페이스북 로그인에 실패하였습니다. 동일한 이메일을 사용하는 사용자가 이미 존재할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginOrJoin(user: UserDTO) {
        firebaseViewModel.getUser(user.uid!!) { userDTO ->
            if (userDTO != null) {
                if (userDTO.nickname.isNullOrEmpty()) { // 닉네임이 없으면 회원 가입 페이지로 이동
                    callJoinActivity(userDTO)
                } else {
                    callMainActivity(userDTO)
                }
            } else {
                callJoinActivity(user)
            }
        }
        /*firestore?.collection("user")?.document(userDTO.uid!!)?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.result.exists()) { // document 있음
                    var user = task.result.toObject(UserDTO::class.java)!!
                    if (user.nickname.isNullOrEmpty()) { // 닉네임이 없으면 회원 가입 페이지로 이동
                        callJoinActivity(user)
                    } else {
                        callMainActivity(user)
                    }
                } else { // document 없으면 회원 가입 페이지로 이동
                    callJoinActivity(userDTO)
                }
            }
        }*/
    }

    private fun callJoinActivity(user: UserDTO) {
        var intent = Intent(this, JoinActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
    }

    private fun callMainActivity(user: UserDTO) {
        var intent = Intent(this, MainActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
        finish()
    }

    private fun callLoginEmailVerifyActivity(email: String, password: String) {
        var intent = Intent(this, LoginEmailVerifyActivity::class.java)
        intent.putExtra("email", email)
        intent.putExtra("password", password)
        startActivity(intent)
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        //appExit()
        if(System.currentTimeMillis() - backWaitTime >=2000 ) {
            backWaitTime = System.currentTimeMillis()
            Snackbar.make(binding.layoutMain,"'뒤로' 버튼을 한번 더 누르면 앱이 종료됩니다.", Snackbar.LENGTH_LONG).show()
        } else {
            finish() //액티비티 종료
        }
    }
}