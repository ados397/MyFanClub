package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.DocumentDialogBinding


class DocumentDialog(context: Context, var content: String) : Dialog(context) {

    lateinit var binding: DocumentDialogBinding
    private val layout = R.layout.document_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DocumentDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        //window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setInfo()
    }

    fun setInfo() {
        binding.textContent.text = content
    }

    fun setButtonOk(name: String) {
        binding.buttonDocumentOk.text = name
    }

    fun setButtonCancel(name: String) {
        binding.buttonDocumentCancel.text = name
    }

    fun showButtonOk(visible: Boolean) {
        if (visible) {
            binding.buttonDocumentOk.visibility = View.VISIBLE
        } else {
            binding.buttonDocumentOk.visibility = View.GONE
        }
    }

    fun showButtonCancel(visible: Boolean) {
        if (visible) {
            binding.buttonDocumentCancel.visibility = View.VISIBLE
        } else {
            binding.buttonDocumentCancel.visibility = View.GONE
        }
    }

    private fun init() {
        //button_ok.setOnClickListener(this)
    }
}