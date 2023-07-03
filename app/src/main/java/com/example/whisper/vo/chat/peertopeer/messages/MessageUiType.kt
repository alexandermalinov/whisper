package com.example.whisper.vo.chat.peertopeer.messages

import android.net.Uri
import kotlin.math.log10
import kotlin.math.pow

sealed class MessageUiType(
    val type: String,
    val fileUri: Uri? = null,
    val name: String? = null,
    val size: String? = null
) {
    object Text : MessageUiType("Text")
    class Photo(fileUri: Uri?, name: String?, size: String?) :
        MessageUiType("Photo", fileUri, name, size)

    class Video(fileUri: Uri?, name: String?, size: String?) :
        MessageUiType("Video", fileUri, name, size)

    class Audio(fileUri: Uri?, name: String?, size: String?) :
        MessageUiType("Audio", fileUri, name, size)

    class Pdf(fileUri: Uri?, name: String?, size: String?) :
        MessageUiType("Pdf", fileUri, name, size)

    class Doc(fileUri: Uri?, name: String?, size: String?) :
        MessageUiType("Doc", fileUri, name, size)

    class Xls(fileUri: Uri?, name: String?, size: String?) :
        MessageUiType("Xls", fileUri, name, size) // Excel

    class Pptx(fileUri: Uri?, name: String?, size: String?) :
        MessageUiType("Pptx", fileUri, name, size) // Power Point

    class File(fileUri: Uri?, name: String?, size: String?) :
        MessageUiType("File", fileUri, name, size)

    fun formatFileSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(sizeInBytes.toDouble()) / log10(1024.0)).toInt()
        return String.format(
            "%.2f %s",
            sizeInBytes / 1024.0.pow(digitGroups.toDouble()),
            units[digitGroups]
        )
    }

    fun MessageUiType.isFileType() = this is Pdf ||
            this is Doc ||
            this is Xls ||
            this is Pptx ||
            this is File
}