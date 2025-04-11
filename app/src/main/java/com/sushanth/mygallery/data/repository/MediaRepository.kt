package com.sushanth.mygallery.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.sushanth.mygallery.data.model.Album
import com.sushanth.mygallery.data.model.Media
import com.sushanth.mygallery.data.model.MediaBucketType
import com.sushanth.mygallery.data.paging.MediaStorePagingSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediaRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val contentResolver: ContentResolver = context.contentResolver

    suspend fun fetchAllAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val albumMap = mutableMapOf<String, Album>()

        val imageAlbums = fetchAlbumsFromMediaStore(
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            isVideo = false
        )
        val videoAlbums = fetchAlbumsFromMediaStore(
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            isVideo = true
        )

        // Extract All Images and All Videos separately
        val allImagesAlbum = imageAlbums.firstOrNull { it.name == MediaBucketType.ALL_IMAGES.label && it.itemCount > 0 }
        val allVideosAlbum = videoAlbums.firstOrNull { it.name == MediaBucketType.ALL_VIDEOS.label && it.itemCount > 0 }

        // Exclude All Images and All Videos from the regular albums list
        val otherAlbums = (imageAlbums + videoAlbums)
            .filterNot { it.name == MediaBucketType.ALL_IMAGES.label || it.name == MediaBucketType.ALL_VIDEOS.label }

        otherAlbums.forEach { album ->
            val existing = albumMap[album.name]
            if (existing == null) {
                albumMap[album.name] = album
            } else {
                albumMap[album.name] = existing.copy(
                    itemCount = existing.itemCount + album.itemCount,
                    thumbnailUri = album.thumbnailUri ?: existing.thumbnailUri,
                    imageCount = existing.imageCount + album.imageCount,
                    videoCount = existing.videoCount + album.videoCount
                )
            }
        }

        // Final list: All Images -> All Videos -> rest
        val finalList = listOfNotNull(allImagesAlbum, allVideosAlbum) + albumMap.values

        return@withContext finalList
    }

    private fun fetchAlbumsFromMediaStore(uri: Uri, isVideo: Boolean): List<Album> {
        var mediaCount = 0
        var thumbnailUri: Uri? = null
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DATA
        )

        val albumMap = mutableMapOf<String, Pair<Uri, Int>>() // bucketKey -> (thumbUri, count)

        contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val bucketNameIndex =
                cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
            val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val bucketName = cursor.getString(bucketNameIndex) ?: MediaBucketType.INTERNAL_STORAGE.label
                val path = cursor.getString(dataIndex)?.lowercase() ?: continue

                if (path.contains("/cache") || path.contains(".nomedia") || path.contains("thumbnails")) {
                    continue
                }

                val contentUri = Uri.withAppendedPath(uri, id.toString())

                mediaCount++
                if (thumbnailUri == null) thumbnailUri = contentUri

                if (!albumMap.containsKey(bucketName)) {
                    albumMap[bucketName] = Pair(contentUri, 1)
                } else {
                    val (existingUri, count) = albumMap[bucketName]!!
                    albumMap[bucketName] = Pair(existingUri, count + 1)
                }
            }
        }

        return listOf(
            Album(
                name = if (isVideo) MediaBucketType.ALL_VIDEOS.label else MediaBucketType.ALL_IMAGES.label,
                itemCount = mediaCount,
                thumbnailUri = thumbnailUri,
                mediaItems = listOf(),
                imageCount = if (!isVideo) mediaCount else 0,
                videoCount = if (isVideo) mediaCount else 0
            )
        ) + albumMap.map { (folderName, value) ->
            Album(
                name = folderName,
                itemCount = value.second,
                thumbnailUri = value.first,
                mediaItems = emptyList(),
                imageCount = if (!isVideo) value.second else 0,
                videoCount = if (isVideo) value.second else 0
            )
        }
    }

    fun getPagedMedia(bucketName: String): Flow<PagingData<Media>> {
        return Pager(
            config = PagingConfig(pageSize = 100, prefetchDistance = 80, enablePlaceholders = false),
            pagingSourceFactory = {
                MediaStorePagingSource(context, bucketName)
            }
        ).flow
    }
}