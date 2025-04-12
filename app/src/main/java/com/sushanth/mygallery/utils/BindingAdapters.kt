package com.sushanth.mygallery.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

@BindingAdapter("imageUri")
fun loadImage(view: ImageView, uri: Uri?) {
    Glide.with(view.context)
        .load(uri)
        .sizeMultiplier(0.8f)
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .placeholder(ColorDrawable(Color.DKGRAY))
        .dontAnimate()
        .into(view)
}