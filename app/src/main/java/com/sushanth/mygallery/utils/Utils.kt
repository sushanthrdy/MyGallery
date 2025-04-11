package com.sushanth.mygallery.utils

import java.util.Locale

object Utils {
    fun formatDuration(durationMillis: Long?): String {
        val totalSeconds = durationMillis?.div(1000)
        val minutes = totalSeconds?.div(60)
        val seconds = totalSeconds?.rem(60)
        return String.format( Locale.getDefault(),"%02d:%02d", minutes, seconds)
    }
}