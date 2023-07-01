package com.example.whisper.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.whisper.utils.common.EMPTY
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class FileUtils @Inject constructor(private val context: Context) {

    fun createFile(uri: Uri): File = File(context.externalCacheDir, getFileName(uri)).let { file ->
        FileOutputStream(file).use { outputSteam ->
            context.contentResolver.openInputStream(uri).use { inputStream ->
                inputStream?.copyTo(outputSteam)
                outputSteam.flush()
            }
        }
        file
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String {
        var name = EMPTY

        if (uri.toString().startsWith("content://")) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
            cursor?.close()
        } else if (uri.toString().startsWith("file://")) {
            name = File(uri.toString()).name
        }
        return name
    }
}