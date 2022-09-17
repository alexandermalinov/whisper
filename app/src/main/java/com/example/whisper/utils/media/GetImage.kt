package com.example.whisper.utils.media

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
import androidx.activity.result.contract.ActivityResultContracts

class GetImage : ActivityResultContracts.OpenDocument() {

    override fun createIntent(context: Context, input: Array<String>): Intent =
        super.createIntent(context, input).apply {
            addFlags(FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
}