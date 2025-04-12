package com.sushanth.mygallery.utils

import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.TypedValue
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

@BindingAdapter("imageUri")
fun loadImage(view: ImageView, uri: Uri?) {
    val typedValue = TypedValue()
    view.context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainer, typedValue, true)
    val placeholderColor = ContextCompat.getColor(view.context, typedValue.resourceId)

    Glide.with(view.context)
        .load(uri)
        .sizeMultiplier(0.8f)
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .placeholder(ColorDrawable(placeholderColor))
        .dontAnimate()
        .into(view)
}