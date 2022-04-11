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
        var addApps : ArrayList<AppDTO> = arrayListOf()
        addApps.add(AppDTO(false, "com.iloen.melon", "멜론"
            , "https://play-lh.googleusercontent.com/GweSpOJ7p8RZ0lzMDr7sU0x5EtvbsAubkVjLY-chdyV6exnSUfl99Am0g8X0w_a2Qo4=s180-rw", order++))
        addApps.add(AppDTO(false, "com.iloen.aztalk", "멜론 아지톡"
            , "https://play-lh.googleusercontent.com/7nq_SFnbdah2Wyc-VsIbvWkHLoy1DGnwYPGs7DElwPz7amTUZU3zo-4SezywBs0vAg=s180-rw", order++))
        addApps.add(AppDTO(false, "com.ktmusic.geniemusic", "지니뮤직"
            , "https://play-lh.googleusercontent.com/gSjYDowrYi_BIdXKIsxhc4Y3Zj5zGA3os_SCm8cqWWCrXQYejcmser-UOEM-PnCGRgk=s180-rw", order++))
        addApps.add(AppDTO(false, "com.neowiz.android.bugs", "벅스"
            , "https://play-lh.googleusercontent.com/1EYBuARUQsNbhEQ1Ax4q9G-E_KuMuzxklmBdMXlIksKtYlebieUxvVLrrtQv6oBWR4k=s180-rw", order++))
        addApps.add(AppDTO(false, "com.naver.vibe", "바이브"
            , "https://play-lh.googleusercontent.com/yHyThRQ7idHmfkEaz0abkkCYZAMbNulSU-hZL0TP-KgXTP9Y1ph1w8-n-l0kaBnrfI8=s180-rw", order++))
        addApps.add(AppDTO(false, "skplanet.musicmate", "플로"
            , "https://play-lh.googleusercontent.com/31RGCuepZn9kCR-ASp6aM-fWNm34YvHX2EkkSsypIUHZ_nbDkKI_1Z8fsuSnfpvBEHk=s180-rw", order++))
        addApps.add(AppDTO(false, "com.spotify.music", "스포티파이"
            , "https://play-lh.googleusercontent.com/UrY7BAZ-XfXGpfkeWg0zCCeo-7ras4DCoRalC_WXXWTK9q5b0Iw7B0YQMsVxZaNB7DM=s180-rw", order++))
        addApps.add(AppDTO(false, "com.kakao.music", "카카오뮤직"
            , "https://play-lh.googleusercontent.com/waHxALPR_cDykucu_QZ8YI7zEsuzzI-4_76bmD19WBx2JwBvNokISMcT5H6K8qxXvQ=s180-rw", order++))
        addApps.add(AppDTO(false, "com.sec.android.app.music", "삼성뮤직"
            , "https://play-lh.googleusercontent.com/olj6n0kCUo_x3lNfgvzdGR5k_NEsz2D9PuC8evI0hYCHLSQHBhKY-cQwZ4EtWnac28o=s180-rw", order++))
        addApps.add(AppDTO(false, "co.benx.weverse", "위버스"
            , "https://play-lh.googleusercontent.com/Qgv-Ui1OXodpjWjgxbC85QPM6jkLnuEBjC0qK4SjtunnNw_m09V_kOXvToaJhsHLwGaT=s180-rw", order++))
        addApps.add(AppDTO(false, "com.vlending.apps.mubeat", "뮤빗"
            , "https://play-lh.googleusercontent.com/YHVs88UIQzMbRbRFABiDq3hQgPY6Br2GuXDj09AqCjoxx4mJxiyqwO2RF2prYoLLPAIF=s180-rw", order++))
        addApps.add(AppDTO(false, "com.dearu.bubble.stars", "bubble"
            , "https://play-lh.googleusercontent.com/XUauDvbhjtsfYr7wHTY1Hx6n0zZI7huwOmkK3GmYtq79IKldsEg8zgkMAAN8hW1ys0Jq=s180-rw", order++))
        addApps.add(AppDTO(false, "com.hanteo.whosfanglobal", "후즈팬"
            , "https://play-lh.googleusercontent.com/abHT7gUM-YVrvD-LjcqJ0PydJRMpmLrXWVxywKs7jiHr5d8A-h7oNDP7GXJCy2yb0A=s180-rw", order++))
        addApps.add(AppDTO(false, "com.nwz.ichampclient", "아이돌챔프"
            , "https://play-lh.googleusercontent.com/_w1hjZwXhvcXJPPVFweoGhR1kXO-21ucw6nWtyL-uOOoHX7UshE91UWsWonUpBQXHzQ=s180-rw", order++))
        addApps.add(AppDTO(false, "inc.rowem.passicon", "스타플래닛"
            , "https://play-lh.googleusercontent.com/KONrrtvpHNVDK_9On6zLWTD6GSrGVx82lQzGDx576Rt8gqR1xrjVog_k8inFWPfZDw=s180-rw", order++))
        addApps.add(AppDTO(false, "com.ados.mrtrotrematch", "트롯 투표 - 리매치"
            , "https://play-lh.googleusercontent.com/QGMXoQNqcqWhSSMmPSCaff6wSY4lNeqh4L6EGgRUuw7DqZJwtc2q9N0GxzMfWJmV7s8=s180-rw", order++))
        addApps.add(AppDTO(false, "com.ados.mstrotrematch2", "트롯 투표 - 리매치W"
            , "https://play-lh.googleusercontent.com/I1HVx52EJ4l17_9PTU0U69aBemkCizIfs0I3bL9rnSeSYmpn-Edvt-YY71y4innx3w=s180-rw", order++))
        addApps.add(AppDTO(false, "com.ados.everybodysingerrematch", "국민투표 - 리매치"
            , "https://play-lh.googleusercontent.com/82-co5fvKyduzNIH3qUaQFFVynzLfvNwxQTN5NwN8kxrcCmrq4CJWsgpkGS7Mw7IWw=s180-rw", order++))
        addApps.add(AppDTO(false, "com.ados.trotfestivalrematch", "트롯 체전 - 리매치"
            , "https://play-lh.googleusercontent.com/d9aFZ1CmiN8MhgEdEVPUbpNyLXGUd00CeCufRtUokxQfDEaKTEJVzSGNdW5oH6wykutz=s180-rw", order++))
        addApps.add(AppDTO(false, "com.exodus.myloveactor", "최애돌 셀럽"
            , "https://play-lh.googleusercontent.com/FQ7ui11q8GOfTXxqi0rySCnMvfCJvy1QlX11oRI2eoA6ZfimAv02b9vT4fjVi3ELMRQ=s180-rw", order++))
        addApps.add(AppDTO(false, "net.ib.mn", "최애돌♡"
            , "https://play-lh.googleusercontent.com/QNIlJogo2sQdwRqfVEUVl7Dkxx4vtPBUcAPYLWH1EFjd31IgoZgEWb14_VSdMUz-fu91=s180-rw", order++))
        addApps.add(AppDTO(false, "kr.co.tf.starwars", "팬앤스타"
            , "https://play-lh.googleusercontent.com/3csyq-4SZXyLPKBXsGS5Rxo6W9X501nOHZxwQy-4O67yz9h-S8q5tEkzwHzdgRIS3y8=s180-rw", order++))
        addApps.add(AppDTO(false, "com.photocard.allstar", "팬플러스"
            , "https://play-lh.googleusercontent.com/lc0s5wkLdEZI9DUaRBs0CaT8PQN_M4a6-iGm2980abZwJP_UVah_NfTjlLtKJfUjefVp=s180-rw", order++))
        addApps.add(AppDTO(false, "com.square.thekking", "덕킹"
            , "https://play-lh.googleusercontent.com/QwQvGzUVomwe5QaLrD2umqjsUJwo1HctgIXy6RyeofXdAx9IGDUg6uaD1lA5iD-ffuk=s180-rw", order++))
        addApps.add(AppDTO(false, "com.google.android.youtube", "유튜브"
            , "https://play-lh.googleusercontent.com/lMoItBgdPPVDJsNOVtP26EKHePkwBg-PkuY9NOrc-fumRtTFP4XhpUNk_22syN4Datc=s180-rw", order++))
        addApps.add(AppDTO(false, "com.twitter.android", "트위터"
            , "https://play-lh.googleusercontent.com/8sc6LSo3dRf54GaLdQR8UZfzd_fgHgWMJlNxGLP1HWPEU7YY4UxkyHc8-qCNwtyiqO55=s180-rw", order++))
        addApps.add(AppDTO(false, "com.instagram.android", "인스타그램"
            , "https://play-lh.googleusercontent.com/2sREY-8UpjmaLDCTztldQf6u2RGUtuyf6VT5iyX3z53JS4TdvfQlX-rNChXKgpBYMw=s180-rw", order++))
        addApps.add(AppDTO(false, "com.ss.android.ugc.trill", "틱톡"
            , "https://play-lh.googleusercontent.com/2kdv4gGWKchMkThhxMYlWlkSouhx6BP50X1b7O7_Yl78fFCitAe3t4hLACuCyC9tsJA=s180-rw", order++))
        addApps.add(AppDTO(false, "com.naver.vapp", "V LIVE"
            , "https://play-lh.googleusercontent.com/P1Z8zqhW7VrOztUHOL1mE3igdw7Z5Nuns9yJjeL2lfbqpAT2B8SBPx9F9mJPjwIx5XM=s180-rw", order++))
        for (app in addApps) {
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