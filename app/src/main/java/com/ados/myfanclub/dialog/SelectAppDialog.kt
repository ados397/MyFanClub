package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.SelectAppDialogBinding
import com.ados.myfanclub.model.AppDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SelectAppDialog(context: Context) : Dialog(context), OnAppListClickListener {

    lateinit var binding: SelectAppDialogBinding

    private var firestore : FirebaseFirestore? = null

    private val layout = R.layout.select_app_dialog
    var recyclerViewAdapter: RecyclerViewAdapterAppList? = null
    var selectedApp: AppDTO? = null

    private var apps : ArrayList<AppDTO> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SelectAppDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)

        firestore = FirebaseFirestore.getInstance()

        //var rootView = binding.root.rootView
        //recyclerView = rootView.findViewById(R.id.rv_app_list!!)as RecyclerView
        //recyclerView.layoutManager = GridLayoutManager(context, 3)
        //val spaceDecoration = VerticalSpaceItemDecoration(10)
        //recyclerView.addItemDecoration(spaceDecoration)

        binding.rvAppList.layoutManager = GridLayoutManager(context, 3)

        /*var order = 1
        apps.add(AppDTO(false, "com.ados.mrtrotrematch", "트롯 투표 - 리매치", "https://play-lh.googleusercontent.com/QGMXoQNqcqWhSSMmPSCaff6wSY4lNeqh4L6EGgRUuw7DqZJwtc2q9N0GxzMfWJmV7s8=s180-rw", order++))
        apps.add(AppDTO(false, "com.ados.mstrotrematch2", "트롯 투표 - 리매치W", "https://play-lh.googleusercontent.com/I1HVx52EJ4l17_9PTU0U69aBemkCizIfs0I3bL9rnSeSYmpn-Edvt-YY71y4innx3w=s180-rw", order++))
        apps.add(AppDTO(false, "com.ados.everybodysingerrematch", "국민투표 - 리매치", "https://play-lh.googleusercontent.com/82-co5fvKyduzNIH3qUaQFFVynzLfvNwxQTN5NwN8kxrcCmrq4CJWsgpkGS7Mw7IWw=s180-rw", order++))
        apps.add(AppDTO(false, "com.ados.trotfestivalrematch", "트롯 체전 - 리매치", "https://play-lh.googleusercontent.com/d9aFZ1CmiN8MhgEdEVPUbpNyLXGUd00CeCufRtUokxQfDEaKTEJVzSGNdW5oH6wykutz=s180-rw", order++))
        apps.add(AppDTO(false, "com.iloen.melon", "멜론", "https://play-lh.googleusercontent.com/GweSpOJ7p8RZ0lzMDr7sU0x5EtvbsAubkVjLY-chdyV6exnSUfl99Am0g8X0w_a2Qo4=s180-rw", order++))
        apps.add(AppDTO(false, "com.iloen.aztalk", "멜론 아지톡", "https://play-lh.googleusercontent.com/7nq_SFnbdah2Wyc-VsIbvWkHLoy1DGnwYPGs7DElwPz7amTUZU3zo-4SezywBs0vAg=s180-rw", order++))
        apps.add(AppDTO(false, "com.ktmusic.geniemusic", "지니뮤직", "https://play-lh.googleusercontent.com/gSjYDowrYi_BIdXKIsxhc4Y3Zj5zGA3os_SCm8cqWWCrXQYejcmser-UOEM-PnCGRgk=s180-rw", order++))
        apps.add(AppDTO(false, "com.neowiz.android.bugs", "벅스", "https://play-lh.googleusercontent.com/1EYBuARUQsNbhEQ1Ax4q9G-E_KuMuzxklmBdMXlIksKtYlebieUxvVLrrtQv6oBWR4k=s180-rw", order++))
        apps.add(AppDTO(false, "com.naver.vibe", "바이브", "https://play-lh.googleusercontent.com/yHyThRQ7idHmfkEaz0abkkCYZAMbNulSU-hZL0TP-KgXTP9Y1ph1w8-n-l0kaBnrfI8=s180-rw", order++))
        apps.add(AppDTO(false, "skplanet.musicmate", "플로", "https://play-lh.googleusercontent.com/31RGCuepZn9kCR-ASp6aM-fWNm34YvHX2EkkSsypIUHZ_nbDkKI_1Z8fsuSnfpvBEHk=s180-rw", order++))
        apps.add(AppDTO(false, "com.exodus.myloveactor", "최애돌 셀럽", "https://play-lh.googleusercontent.com/FQ7ui11q8GOfTXxqi0rySCnMvfCJvy1QlX11oRI2eoA6ZfimAv02b9vT4fjVi3ELMRQ=s180-rw", order++))
        apps.add(AppDTO(false, "net.ib.mn", "최애돌♡", "https://play-lh.googleusercontent.com/QNIlJogo2sQdwRqfVEUVl7Dkxx4vtPBUcAPYLWH1EFjd31IgoZgEWb14_VSdMUz-fu91=s180-rw", order++))
        apps.add(AppDTO(false, "kr.co.tf.starwars", "팬앤스타 ", "https://play-lh.googleusercontent.com/3csyq-4SZXyLPKBXsGS5Rxo6W9X501nOHZxwQy-4O67yz9h-S8q5tEkzwHzdgRIS3y8=s180-rw", order++))
        apps.add(AppDTO(false, "com.photocard.allstar", "팬플러스", "https://play-lh.googleusercontent.com/lc0s5wkLdEZI9DUaRBs0CaT8PQN_M4a6-iGm2980abZwJP_UVah_NfTjlLtKJfUjefVp=s180-rw", order++))
        apps.add(AppDTO(false, "com.square.thekking", "덕킹", "https://play-lh.googleusercontent.com/QwQvGzUVomwe5QaLrD2umqjsUJwo1HctgIXy6RyeofXdAx9IGDUg6uaD1lA5iD-ffuk=s180-rw", order++))
        apps.add(AppDTO(false, "com.nwz.ichampclient", "아이돌챔프", "https://play-lh.googleusercontent.com/_w1hjZwXhvcXJPPVFweoGhR1kXO-21ucw6nWtyL-uOOoHX7UshE91UWsWonUpBQXHzQ=s180-rw", order++))
        for (app in apps) {
            firestore?.collection("app")?.document()?.set(app)
        }*/

        setInfo()
    }

    fun setInfo() {
        firestore?.collection("app")?.orderBy("order", Query.Direction.ASCENDING)?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                apps.clear()
                for (document in task.result) {
                    var app = document.toObject(AppDTO::class.java)
                    apps.add(app)
                }

                recyclerViewAdapter = RecyclerViewAdapterAppList(apps, this)
                binding.rvAppList.adapter = recyclerViewAdapter
            }
        }
    }

    override fun onItemClick(item: AppDTO, position: Int) {
        recyclerViewAdapter?.selectItem(position)
        selectedApp = item
    }

}