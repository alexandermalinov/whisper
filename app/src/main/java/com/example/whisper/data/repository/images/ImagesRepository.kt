package com.example.whisper.data.repository.images

import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.ReadingFromStorageError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ImagesRepository @Inject constructor(private val local: LocalSource) {

    var cachedImages: List<String> = emptyList()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        coroutineScope.launch {
            val imagesResult = local.getInternalStorageImages()
            imagesResult.foldSuspend(
                { readingFromStorageError ->
                    Timber.e("${readingFromStorageError.errorMessage}")
                }, { images ->
                    cachedImages = images
                }
            )
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Sources
     ---------------------------------------------------------------------------------------------*/

    interface LocalSource {

        suspend fun getInternalStorageImages(): Either<ReadingFromStorageError, List<String>>
    }

    /* --------------------------------------------------------------------------------------------
     * Exposed
     ---------------------------------------------------------------------------------------------*/

    suspend fun getInternalStorageImages(): Either<ReadingFromStorageError, List<String>> =
        local.getInternalStorageImages()
}