package com.example.whisper.utils.media

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
import androidx.activity.result.contract.ActivityResultContracts

class GetDocument : ActivityResultContracts.OpenDocument() {

    override fun createIntent(context: Context, input: Array<String>): Intent =
        super.createIntent(context, input).apply {
            val mimetypes = arrayOf(
                "application/*",
                "font/*",
                "message/*",
                "model/*",
                "multipart/*",
                "text/*",
                "video/*"
            )
            putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            type = "*/*"
            addFlags(FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
}