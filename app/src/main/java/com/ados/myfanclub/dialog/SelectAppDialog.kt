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

        firestore?.collection("app")?.orderBy("order", Query.Direction.ASCENDING)?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                apps.clear()
                for (document in task.result) {
                    var app = document.toObject(AppDTO::class.java)!!
                    apps.add(app)
                }

                recyclerViewAdapter = RecyclerViewAdapterAppList(apps, this)
                binding.rvAppList.adapter = recyclerViewAdapter
            }
        }



        /*var firestore = FirebaseFirestore.getInstance()
        firestore?.collection("people")?.get()?.addOnSuccessListener { result ->
            for (document in result) {
                var person = document.toObject(RankDTO::class.java)!!
                people.add(person)
            }
            recyclerview_img.adapter = RecyclerViewAdapterImageSelect(people, this)
        }
            ?.addOnFailureListener { exception ->

            }*/





    }

    override fun onItemClick(item: AppDTO, position: Int) {
        recyclerViewAdapter?.selectItem(position)
        selectedApp = item
    }

}