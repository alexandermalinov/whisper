package com.example.whisper.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.example.whisper.data.local.model.MessageType
import com.example.whisper.utils.common.EMPTY
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import javax.inject.Inject


class FileUtils @Inject constructor(private val context: Context) {

    fun createFile(uri: Uri): File =
        File(context.externalCacheDir, getFileName(uri).first).let { file ->
            FileOutputStream(file).use { outputSteam ->
                context.contentResolver.openInputStream(uri).use { inputStream ->
                    inputStream?.copyTo(outputSteam)
                    outputSteam.flush()
                }
            }
            file
        }

    @SuppressLint("Range")
    fun getFileName(uri: Uri): Pair<String, String> {
        var name = EMPTY
        var size = EMPTY

        if (uri.toString().startsWith("content://")) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                size = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE))
            }
            cursor?.close()
        } else if (uri.toString().startsWith("file://")) {
            val file = File(uri.toString())
            name = file.name
            size = file.length().toString()
        }
        return Pair(name, size)
    }

    // Get MIME type from URI
    fun getMimeType(uri: Uri): String? =
        if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            context.contentResolver.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(fileExtension.lowercase(Locale.getDefault()))
        }

    fun String.getExtension(): MessageType = when (substringAfterLast('.', "")) {
        "jpg" -> MessageType.Photo
        "mp4" -> MessageType.Video
        "mp3" -> MessageType.Audio
        "pdf" -> MessageType.Pdf
        "docx" -> MessageType.Doc
        "xls" -> MessageType.Xls
        "pptx" -> MessageType.Pptx
        else -> MessageType.Pptx
    }
}