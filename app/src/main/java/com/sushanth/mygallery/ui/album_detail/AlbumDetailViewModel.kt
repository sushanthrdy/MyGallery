package com.sushanth.mygallery.ui.album_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sushanth.mygallery.data.model.Media
import com.sushanth.mygallery.data.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(private val repository: MediaRepository) : ViewModel() {
    fun getPagedMedia(bucketName: String): Flow<PagingData<Media>> {
        return repository.getPagedMedia(bucketName).cachedIn(viewModelScope)
    }
}