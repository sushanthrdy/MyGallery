package com.sushanth.mygallery.utils

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("imageUri")
fun loadImage(view: ImageView, uri: Uri?) {
    Glide.with(view.context)
        .load(uri)
        .into(view)
}