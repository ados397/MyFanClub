package com.ados.myfanclub.dialog


import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.ImageViewDialogBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL


class ImageViewDialog(context: Context) : Dialog(context) {

    lateinit var binding: ImageViewDialogBinding
    private val layout = R.layout.image_view_dialog

    var imageUri: Uri? = null
    var imageID: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ImageViewDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setInfo()
    }

    fun setInfo() {
        if (imageUri != null) {
            Glide.with(context).load(imageUri).fitCenter().into(binding.imageView)
        } else if(imageID != null) {
            binding.imageView.setImageResource(imageID!!)
        }
        //binding.imageView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        //binding.imageView.requestLayout()
    }

    private fun init() {
        //button_ok.setOnClickListener(this)
    }
}