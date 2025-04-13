package com.sushanth.mygallery.utils

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper

class MediaContentObserver(
    private val onChange: () -> Unit
) : ContentObserver(Handler(Looper.getMainLooper())) {

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        onChange()
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        onChange()
    }
}