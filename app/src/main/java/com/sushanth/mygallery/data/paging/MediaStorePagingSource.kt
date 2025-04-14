package com.sushanth.mygallery.data.paging

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.sushanth.mygallery.data.model.Media
import com.sushanth.mygallery.data.model.MediaBucketType

class MediaStorePagingSource(
    private val context: Context,
    private val bucketName: String
) : PagingSource<Long, Media>() {

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Media> {
        try {
            val pageSize = params.loadSize
            val lastDateAdded = params.key ?: Long.MAX_VALUE

            val mediaList = mutableListOf<Media>()

            val collection = MediaStore.Files.getContentUri("external")

            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DURATION,
            )

            val (selection, selectionArgs) = when (bucketName) {
                MediaBucketType.ALL_IMAGES.label -> {
                    Pair(
                        """
                ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?
                AND ${MediaStore.Files.FileColumns.DATE_ADDED} < ?
                """.trimIndent(),
                        arrayOf(
                            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                            lastDateAdded.toString()
                        )
                    )
                }
                MediaBucketType.ALL_VIDEOS.label -> {
                    Pair(
                        """
                ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?
                AND ${MediaStore.Files.FileColumns.DATE_ADDED} < ?
                """.trimIndent(),
                        arrayOf(
                            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                            lastDateAdded.toString()
                        )
                    )
                }
                MediaBucketType.INTERNAL_STORAGE.label -> {
                    Pair(
                        """
                (${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?)
                AND ${MediaStore.Files.FileColumns.DATA} LIKE ? 
                AND ${MediaStore.Files.FileColumns.DATA} NOT LIKE ?
                AND ${MediaStore.Files.FileColumns.DATE_ADDED} < ?
                """.trimIndent(),
                        arrayOf(
                            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                            "/storage/emulated/0/%",
                            "/storage/emulated/0/%/%",
                            lastDateAdded.toString()
                        )
                    )
                }
                else -> {
                    Pair(
                        """
                (${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?)
                AND ${MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME}=?
                AND ${MediaStore.Files.FileColumns.DATE_ADDED} < ?
                """.trimIndent(),
                        arrayOf(
                            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                            bucketName,
                            lastDateAdded.toString()
                        )
                    )
                }
            }

            val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val typeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val bucketColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
                val dateAddedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                val videoDurationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DURATION)

                while (cursor.moveToNext() && mediaList.size < pageSize) {
                    val id = cursor.getLong(idColumn)
                    val mediaType = cursor.getInt(typeColumn)
                    val filePath = cursor.getString(dataColumn)
                    if (filePath.contains("/cache", ignoreCase = true) ||
                        filePath.contains(".nomedia", ignoreCase = true) ||
                        filePath.contains("thumbnails", ignoreCase = true)
                    ) {
                        continue
                    }
                    val folder = cursor.getString(bucketColumn)?:MediaBucketType.INTERNAL_STORAGE.label
                    val dateAdded = cursor.getLong(dateAddedColumn)

                    val videoDuration = if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                        cursor.getLong(videoDurationColumn)
                    } else {
                        null
                    }

                    val contentUri = when (mediaType) {
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE ->
                            ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO ->
                            ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

                        else -> continue
                    }

                    mediaList.add(
                        Media(
                            id = id,
                            contentUri = contentUri,
                            filePath = filePath,
                            folderName = folder,
                            dateAdded = dateAdded,
                            isVideo = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                            videoDuration = videoDuration
                        )
                    )
                }
            }

            val nextKey = mediaList.lastOrNull()?.dateAdded

            return LoadResult.Page(
                data = mediaList,
                prevKey = null,
                nextKey = nextKey
            )
        }catch (e:Exception){
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Long, Media>): Long? = state.anchorPosition?.let { state.closestItemToPosition(it)?.dateAdded }
}