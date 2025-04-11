package com.sushanth.mygallery.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.sushanth.mygallery.data.model.Album
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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
        val allImagesAlbum = imageAlbums.firstOrNull { it.name == "All Images" && it.itemCount > 0 }
        val allVideosAlbum = videoAlbums.firstOrNull { it.name == "All Videos" && it.itemCount > 0 }

        // Exclude All Images and All Videos from the regular albums list
        val otherAlbums = (imageAlbums + videoAlbums)
            .filterNot { it.name == "All Images" || it.name == "All Videos" }

        otherAlbums.forEach { album ->
            val existing = albumMap[album.name]
            if (existing == null) {
                albumMap[album.name] = album
            } else {
                albumMap[album.name] = existing.copy(
                    itemCount = existing.itemCount + album.itemCount,
                    thumbnailUri = album.thumbnailUri ?: existing.thumbnailUri
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
                val bucketName = cursor.getString(bucketNameIndex) ?: "Internal Storage"
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
                if (isVideo) "All Videos" else "All Images",
                mediaCount,
                thumbnailUri,
                listOf()
            )
        ) + albumMap.map { (folderName, value) ->
            Album(
                name = folderName,
                itemCount = value.second,
                thumbnailUri = value.first,
                mediaItems = emptyList()
            )
        }
    }
}