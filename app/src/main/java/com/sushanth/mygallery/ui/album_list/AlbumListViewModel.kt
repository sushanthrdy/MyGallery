package com.sushanth.mygallery.ui.album_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushanth.mygallery.data.model.Album
import com.sushanth.mygallery.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumListViewModel @Inject constructor(private val mediaRepository: MediaRepository) : ViewModel() {

    private val _albums = MutableLiveData<List<Album>>()
    val albums: LiveData<List<Album>> = _albums

    init {
        viewModelScope.launch {
            val allAlbums = mediaRepository.fetchAllAlbums()
            _albums.value = allAlbums
        }
    }
}