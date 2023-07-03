package com.example.whisper.utils.media

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.whisper.navigation.External
import com.example.whisper.utils.common.IMAGE_TYPE
import com.example.whisper.utils.common.SELECT_DOCUMENT_KEY
import com.example.whisper.utils.common.SELECT_IMAGE_KEY

interface ActivityResultHandler {

    fun provideObserver(destination: External): List<ActivityResultObserver>
}

interface ActivityResultObserver {

    fun launch()

    fun getKey(): String
}

class SelectImageObserver(
    private val registry: ActivityResultRegistry,
    private val onSelect: (Uri?) -> Unit
) : DefaultLifecycleObserver, ActivityResultObserver {

    private lateinit var selectImageFromGalleryResult: ActivityResultLauncher<Array<String>>

    override fun onCreate(owner: LifecycleOwner) {
        selectImageFromGalleryResult = registry.register(
            SELECT_IMAGE_KEY,
            owner,
            GetImage()
        ) { uri ->
            onSelect.invoke(uri)
        }
    }

    override fun launch() {
        selectImageFromGalleryResult.launch(arrayOf(IMAGE_TYPE))
    }

    override fun getKey(): String = SELECT_IMAGE_KEY
}

class SelectDocumentObserver(
    private val registry: ActivityResultRegistry,
    private val onSelect: (Uri?) -> Unit
) : DefaultLifecycleObserver, ActivityResultObserver {

    private lateinit var selectDocumentResult: ActivityResultLauncher<Array<String>>

    override fun onCreate(owner: LifecycleOwner) {
        selectDocumentResult = registry.register(
            SELECT_DOCUMENT_KEY,
            owner,
            GetDocument()
        ) { uri ->
            onSelect.invoke(uri)
        }
    }

    override fun launch() {
        selectDocumentResult.launch(arrayOf(IMAGE_TYPE))
    }

    override fun getKey(): String = SELECT_DOCUMENT_KEY
}