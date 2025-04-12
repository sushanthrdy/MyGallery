package com.sushanth.mygallery.utils

sealed class UIState<out T> {
    object Loading : UIState<Nothing>()
    data class Success<T>(val data: T) : UIState<T>()
}