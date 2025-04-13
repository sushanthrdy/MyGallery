package com.sushanth.mygallery.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.sushanth.mygallery.data.model.Album
import com.sushanth.mygallery.data.model.BucketData
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
        val imageAlbums = fetchAlbumsFromStore(
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            mediaType = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
        )
        val videoAlbums = fetchAlbumsFromStore(
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            mediaType = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
        )

        val albumMap = mutableMapOf<String, BucketData>()

        var allImageCount = 0
        var allVideoCount = 0
        var allImageThumbnail: Uri? = null
        var allVideoThumbnail: Uri? = null

        imageAlbums.forEach {
            val existing = albumMap[it.name]
            allImageCount += it.count
            if (allImageThumbnail == null) allImageThumbnail = it.thumbnailUri
            if (existing == null) {
                albumMap[it.name] = BucketData(it.name, it.count, 0, it.dateAdded, it.thumbnailUri)
            } else {
                existing.imageCount += it.count
                if (it.dateAdded > existing.latestDate) {
                    existing.thumbnailUri = it.thumbnailUri
                    existing.latestDate = it.dateAdded
                }
            }
        }

        videoAlbums.forEach {
            val existing = albumMap[it.name]
            allVideoCount += it.count
            if (allVideoThumbnail == null) allVideoThumbnail = it.thumbnailUri
            if (existing == null) {
                albumMap[it.name] = BucketData(it.name, 0, it.count, it.dateAdded, it.thumbnailUri)
            } else {
                existing.videoCount += it.count
                if (it.dateAdded > existing.latestDate) {
                    existing.thumbnailUri = it.thumbnailUri
                    existing.latestDate = it.dateAdded
                }
            }
        }

        val allAlbums = mutableListOf<Album>()
        if (allImageCount > 0) {
            allAlbums += Album(
                name = MediaBucketType.ALL_IMAGES.label,
                itemCount = allImageCount,
                thumbnailUri = allImageThumbnail,
                imageCount = allImageCount,
                videoCount = 0
            )
        }
        if (allVideoCount > 0) {
            allAlbums += Album(
                name = MediaBucketType.ALL_VIDEOS.label,
                itemCount = allVideoCount,
                thumbnailUri = allVideoThumbnail,
                imageCount = 0,
                videoCount = allVideoCount
            )
        }

        val folderAlbums = albumMap.values.map {
            Album(
                name = it.name,
                itemCount = it.imageCount + it.videoCount,
                thumbnailUri = it.thumbnailUri,
                imageCount = it.imageCount,
                videoCount = it.videoCount
            )
        }

        return@withContext allAlbums + folderAlbums
    }

    private fun fetchAlbumsFromStore(uri: Uri, mediaType: Int): List<TempAlbumData> {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.DATE_ADDED
        )

        val albumList = mutableListOf<TempAlbumData>()

        contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val bucketNameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
            val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)

            val bucketCountMap = mutableMapOf<String, TempAlbumData>()

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val bucketName = cursor.getString(bucketNameIndex) ?: MediaBucketType.INTERNAL_STORAGE.label
                val dateAdded = cursor.getLong(dateAddedIndex)

                val contentUri = when (mediaType) {
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE ->
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO ->
                        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    else -> continue
                }

                val entry = bucketCountMap[bucketName]
                if (entry == null) {
                    bucketCountMap[bucketName] = TempAlbumData(
                        name = bucketName,
                        count = 1,
                        thumbnailUri = contentUri,
                        dateAdded = dateAdded
                    )
                } else {
                    entry.count++
                    if (dateAdded > entry.dateAdded) {
                        entry.thumbnailUri = contentUri
                        entry.dateAdded = dateAdded
                    }
                }
            }

            albumList.addAll(bucketCountMap.values)
        }

        return albumList
    }

    fun getPagedMedia(bucketName: String): Flow<PagingData<Media>> {
        return Pager(
            config = PagingConfig(pageSize = 100, prefetchDistance = 80, enablePlaceholders = false),
            pagingSourceFactory = {
                MediaStorePagingSource(context, bucketName)
            }
        ).flow
    }

    // Temporary class for internal grouping
    private data class TempAlbumData(
        val name: String,
        var count: Int,
        var thumbnailUri: Uri?,
        var dateAdded: Long
    )
}
