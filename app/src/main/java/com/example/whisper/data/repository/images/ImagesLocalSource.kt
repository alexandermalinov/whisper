package com.example.whisper.data.repository.images

import android.content.ContentResolver
import android.provider.MediaStore
import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.ReadingFromStorageError
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ImagesLocalSource @Inject constructor(
    private val contentResolver: ContentResolver
) : ImagesRepository.LocalSource {

    override suspend fun getInternalStorageImages(): Either<ReadingFromStorageError, List<String>> =
        suspendCoroutine { continuation ->
            val imagePaths = mutableListOf<String>()
            val sortOrderByNewest = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            try {
                contentResolver.query(
                    getMediaStoreUri(),
                    createMediaStoreProjection(),
                    null,
                    null,
                    sortOrderByNewest
                )?.use { cursor ->
                    val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    while (cursor.moveToNext()) {
                        val data = cursor.getString(dataColumn)
                        imagePaths.add(data)
                    }
                }
                continuation.resume(Either.right(imagePaths))
            } catch (e: Exception) {
                continuation.resume(Either.left(ReadingFromStorageError()))
            }
        }

    private fun getMediaStoreUri() =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

    private fun createMediaStoreProjection() = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATA
    )
}