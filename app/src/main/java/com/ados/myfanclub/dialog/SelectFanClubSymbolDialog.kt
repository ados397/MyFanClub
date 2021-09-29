package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.SelectFanClubSymbolDialogBinding
import com.google.firebase.firestore.FirebaseFirestore

class SelectFanClubSymbolDialog(context: Context) : Dialog(context), OnFanClubSymbolClickListener {

    lateinit var binding: SelectFanClubSymbolDialogBinding

    private var firestore : FirebaseFirestore? = null

    private val layout = R.layout.select_fan_club_symbol_dialog
    var recyclerViewAdapter: RecyclerViewAdapterFanClubSymbol? = null
    var selectedSymbol: String = "reward_icon_01"
    var isOK: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SelectFanClubSymbolDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)

        firestore = FirebaseFirestore.getInstance()

        binding.rvAppList.layoutManager = GridLayoutManager(context, 3)

        //var imageID = itemView.context.resources.getIdentifier(item.image, "drawable", itemView.context.packageName)

        val symbols : ArrayList<Int> = arrayListOf(R.drawable.reward_icon_01, R.drawable.reward_icon_02, R.drawable.reward_icon_03, R.drawable.reward_icon_04, R.drawable.reward_icon_05
            , R.drawable.reward_icon_06, R.drawable.reward_icon_07, R.drawable.reward_icon_08, R.drawable.reward_icon_09, R.drawable.reward_icon_10, R.drawable.reward_icon_11, R.drawable.reward_icon_12
            , R.drawable.reward_icon_13, R.drawable.reward_icon_14, R.drawable.reward_icon_15, R.drawable.reward_icon_16, R.drawable.reward_icon_17, R.drawable.reward_icon_18, R.drawable.reward_icon_19
            , R.drawable.reward_icon_20, R.drawable.reward_icon_21, R.drawable.reward_icon_22, R.drawable.reward_icon_23, R.drawable.reward_icon_24, R.drawable.reward_icon_25, R.drawable.reward_icon_26
            , R.drawable.reward_icon_27, R.drawable.reward_icon_28, R.drawable.reward_icon_29, R.drawable.reward_icon_30)

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
        selectedSymbol = "reward_icon_${String.format("%02d",position+1)}"
        isOK = true
        dismiss()
    }
}