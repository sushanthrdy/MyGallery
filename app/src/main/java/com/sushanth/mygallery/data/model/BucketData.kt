package com.sushanth.mygallery.data.model

import android.net.Uri

data class BucketData(
    val name: String,
    var imageCount: Int,
    var videoCount: Int,
    var latestDate: Long,
    var thumbnailUri: Uri?
)
