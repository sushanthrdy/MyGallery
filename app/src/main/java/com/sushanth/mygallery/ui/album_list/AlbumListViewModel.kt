package com.sushanth.mygallery.ui.album_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushanth.mygallery.data.model.Album
import com.sushanth.mygallery.data.repository.MediaRepository
import com.sushanth.mygallery.utils.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumListViewModel @Inject constructor(private val mediaRepository: MediaRepository) :
    ViewModel() {

    private val _albumUIState = MutableLiveData<UIState<List<Album>>>()
    val albumUIState: LiveData<UIState<List<Album>>> = _albumUIState

    var isGrid = true

    init {
        fetchAlbums()
    }

    fun fetchAlbums() {
        viewModelScope.launch {
            _albumUIState.value = UIState.Loading
            val albums = mediaRepository.fetchAllAlbums()
            _albumUIState.value = UIState.Success(albums)
        }
    }
}