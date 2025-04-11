package com.sushanth.mygallery.data.model

import android.net.Uri

data class Album(
    val name: String,
    val itemCount: Int,
    val thumbnailUri: Uri?,
    val mediaItems: List<Media>
)
