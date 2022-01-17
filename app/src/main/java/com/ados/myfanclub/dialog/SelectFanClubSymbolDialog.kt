package com.ados.myfanclub.dialog


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.SelectFanClubSymbolDialogBinding
import com.google.android.gms.auth.api.Auth

class SelectFanClubSymbolDialog(context: Context) : Dialog(context), OnFanClubSymbolClickListener {

    lateinit var binding: SelectFanClubSymbolDialogBinding

    private val layout = R.layout.select_fan_club_symbol_dialog
    var recyclerViewAdapter: RecyclerViewAdapterFanClubSymbol? = null
    var selectedSymbol: String = "reward_icon_01"
    var isOK: Boolean = false
    var isAddImage = false
    var mainActivity: MainActivity? = null
    //private lateinit var resultLauncher: ActivityResultLauncher<String>

    //갤러리 앱으로 이동하는 launcher 등록
    //private var launcher = registerForActivityResult(ActivityResultContracts.GetContent()) {
      //      it-> changeFragment(GalleryFragment(it))
    //}

    /*private val resultLauncher = context.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        ...
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SelectFanClubSymbolDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)


        /*resultLauncher = mainActivity!!.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                // 사진을 정상적으로 가져온 경우;

                Toast.makeText(context, "사진 가져오기 성공 $uri", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }*/

        binding.rvAppList.layoutManager = GridLayoutManager(context, 3)

        //var imageID = itemView.context.resources.getIdentifier(item.image, "drawable", itemView.context.packageName)

        val symbols : ArrayList<Int> = arrayListOf(R.drawable.add_image
            , R.drawable.reward_icon_01, R.drawable.reward_icon_02, R.drawable.reward_icon_03, R.drawable.reward_icon_04, R.drawable.reward_icon_05
            , R.drawable.reward_icon_06, R.drawable.reward_icon_07, R.drawable.reward_icon_08, R.drawable.reward_icon_09, R.drawable.reward_icon_10
            , R.drawable.reward_icon_11, R.drawable.reward_icon_12, R.drawable.reward_icon_13, R.drawable.reward_icon_14, R.drawable.reward_icon_15
            , R.drawable.reward_icon_16, R.drawable.reward_icon_17, R.drawable.reward_icon_18, R.drawable.reward_icon_19, R.drawable.reward_icon_20
            , R.drawable.reward_icon_21, R.drawable.reward_icon_22, R.drawable.reward_icon_23, R.drawable.reward_icon_24, R.drawable.reward_icon_25
            , R.drawable.reward_icon_26, R.drawable.reward_icon_27, R.drawable.reward_icon_28, R.drawable.reward_icon_29, R.drawable.reward_icon_30)

        recyclerViewAdapter = RecyclerViewAdapterFanClubSymbol(symbols, this)
        binding.rvAppList.adapter = recyclerViewAdapter

        binding.buttonSymbolCancel.setOnClickListener {
            isOK = false
            dismiss()
        }
    }

    override fun onItemClick(item: Int, position: Int) {
        //recyclerViewAdapter?.selectItem(position)
        //selectedApp = item
        //selectedSymbol = item
        if (position == 0) {
            isAddImage = true
        } else {
            selectedSymbol = "reward_icon_${String.format("%02d",position)}"
        }
        isOK = true
        dismiss()
    }

    private fun addImage() {
        //앱에서 앨범에 접근을 허용할지 선택하는 메시지, 한 번 허용하면 앱이 설치돼 있는 동안 다시 뜨지 않음.
        ActivityCompat.requestPermissions(mainActivity!!,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        //앱이 갤러리에 접근햐는 것을 허용했을 경우
        if (ContextCompat.checkSelfPermission(context.applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
            var pickImageFromAlbum = 0

            var intent = Intent(Intent.ACTION_PICK)
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            intent.type = "image/*"


            //resultLauncher.launch("image/*")

            //mainActivity!!.startActivityForResult(intent, pickImageFromAlbum)

            //launcher.launch("image/*")  //갤러리로 이동하는 런처 실행.


            //Toast.makeText(context, "갤러리 접근 권한 있음", Toast.LENGTH_SHORT).show()
        } else {    //앱이 갤러리에 접근햐는 것을 허용하지 않았을 경우
            Toast.makeText(context, "갤러리 접근 권한이 거부돼 있습니다. 설정에서 접근을 허용해 주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}