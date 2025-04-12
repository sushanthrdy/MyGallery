package com.sushanth.mygallery.ui.media_view

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MediaViewViewModel @Inject constructor() : ViewModel() {

    private val _player = MutableLiveData<ExoPlayer?>()
    val player: LiveData<ExoPlayer?> get() = _player

    private var playbackPosition: Long = 0
    private var playWhenReady: Boolean = true
    private var mediaItem: MediaItem? = null

    fun initializePlayer(context: Context, mediaUri: Uri) {
        if (_player.value != null) return

        val exoPlayer = ExoPlayer.Builder(context).build()
        mediaItem = MediaItem.fromUri(mediaUri)

        exoPlayer.setMediaItem(mediaItem!!)
        exoPlayer.prepare()
        exoPlayer.seekTo(playbackPosition)
        exoPlayer.playWhenReady = playWhenReady

        _player.value = exoPlayer
    }

    fun play() {
        _player.value?.play()
    }

    fun pause() {
        _player.value?.pause()
    }

    fun savePlayerState() {
        _player.value?.let { player ->
            playbackPosition = player.currentPosition
            playWhenReady = player.playWhenReady
        }
    }

    fun releasePlayer() {
        savePlayerState()
        _player.value?.release()
        _player.value = null
    }

    override fun onCleared() {
        releasePlayer()
        super.onCleared()
    }
}
