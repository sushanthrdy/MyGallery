package com.sushanth.mygallery.data.model

import android.net.Uri

data class Media(
    val id: Long,
    val contentUri: Uri,
    val filePath: String,
    val folderName: String,
    val dateAdded: Long,
    val isVideo: Boolean,
    val videoDuration: Long? = null
)
