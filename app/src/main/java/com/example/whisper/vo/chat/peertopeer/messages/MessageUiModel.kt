package com.example.whisper.vo.chat.peertopeer.messages

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.core.net.toUri
import com.example.whisper.data.local.model.MessageModel
import com.example.whisper.data.local.model.MessageStatus
import com.example.whisper.data.local.model.MessageType
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.ZERO

@Immutable
sealed class MessageUiModel(
    open val id: String = EMPTY,
    open val text: String = EMPTY,
    open val fileUrl: String = EMPTY,
    open val type: MessageUiType = MessageUiType.Text,
    open val status: MessageStatus = MessageStatus.DELIVERED,
    open val createdAt: Long = ZERO.toLong(),
) {
    @Immutable
    data class OutgoingMessageUiModel(
        override val id: String,
        override val text: String,
        override val status: MessageStatus,
        override val createdAt: Long
    ) : MessageUiModel(
        id = id,
        text = text,
        fileUrl = EMPTY,
        status = status,
        createdAt = createdAt
    )

    @Immutable
    data class IncomingMessageUiModel(
        override val id: String,
        override val text: String,
        override val status: MessageStatus,
        override val createdAt: Long
    ) : MessageUiModel(
        id = id,
        text = text,
        fileUrl = EMPTY,
        status = status,
        createdAt = createdAt
    )

    @Immutable
    data class OutgoingFileMessageUiModel(
        override val id: String,
        override val fileUrl: String,
        override val type: MessageUiType,
        override val status: MessageStatus,
        override val createdAt: Long
    ) : MessageUiModel(
        id = id,
        text = EMPTY,
        fileUrl = fileUrl,
        type = type,
        status = status,
        createdAt = createdAt
    )

    @Immutable
    data class IncomingFileMessageUiModel(
        override val id: String,
        override val fileUrl: String,
        override val type: MessageUiType,
        override val status: MessageStatus,
        override val createdAt: Long
    ) : MessageUiModel(
        id = id,
        text = EMPTY,
        fileUrl = fileUrl,
        type = type,
        status = status,
        createdAt = createdAt
    )

    @Immutable
    data class OutgoingVideoMessageUiModel(
        override val id: String,
        override val fileUrl: String,
        override val type: MessageUiType,
        override val status: MessageStatus,
        override val createdAt: Long,
        var videoState: VideoMessageStates = VideoMessageStates.INITIAL,
        var progress: Int = 0,
    ) : MessageUiModel(
        id = id,
        text = EMPTY,
        fileUrl = fileUrl,
        type = type,
        status = status,
        createdAt = createdAt
    ) {
        companion object {
            fun OutgoingVideoMessageUiModel.isVideoPlaying() =
                videoState == VideoMessageStates.INITIAL || videoState == VideoMessageStates.PAUSED
        }
    }

    @Immutable
    data class IncomingVideoMessageUiModel(
        override val id: String,
        override val fileUrl: String,
        override val type: MessageUiType,
        override val status: MessageStatus,
        override val createdAt: Long,
        var videoState: VideoMessageStates = VideoMessageStates.INITIAL,
        val progress: Int = 0,
    ) : MessageUiModel(
        id = id,
        text = EMPTY,
        fileUrl = fileUrl,
        type = type,
        status = status,
        createdAt = createdAt
    )
}

fun MessageModel.toOutgoingMessageUiModels() = MessageUiModel.OutgoingMessageUiModel(
    id = id,
    text = body,
    status = status,
    createdAt = createdAt
)

fun MessageModel.toIncomingMessageUiModels() = MessageUiModel.IncomingMessageUiModel(
    id = id,
    text = body,
    status = status,
    createdAt = createdAt
)

fun MessageModel.toOutgoingFileMessageUiModels() =
    MessageUiModel.OutgoingFileMessageUiModel(
        id = id,
        fileUrl = fileUrl,
        type = type.messageTypeToMessageUiType(fileUrl.toUri(), fileName, fileSize),
        status = status,
        createdAt = createdAt
    )

fun MessageModel.toIncomingFileMessageUiModels() =
    MessageUiModel.IncomingFileMessageUiModel(
        id = id,
        fileUrl = fileUrl,
        type = type.messageTypeToMessageUiType(fileUrl.toUri(), fileName, fileSize),
        status = status,
        createdAt = createdAt
    )

fun MessageModel.toOutgoingVideoMessageUiModels() =
    MessageUiModel.OutgoingVideoMessageUiModel(
        id = id,
        fileUrl = fileUrl,
        type = type.messageTypeToMessageUiType(fileUrl.toUri(), fileName, fileSize),
        status = status,
        createdAt = createdAt
    )

fun MessageModel.toIncomingVideoMessageUiModels() =
    MessageUiModel.IncomingVideoMessageUiModel(
        id = id,
        fileUrl = fileUrl,
        type = type.messageTypeToMessageUiType(fileUrl.toUri(), fileName, fileSize),
        status = status,
        createdAt = createdAt
    )

private fun MessageType.messageTypeToMessageUiType(
    fileUri: Uri,
    fileName: String,
    fileSize: String
): MessageUiType = when (this) {
    MessageType.Photo -> MessageUiType.Photo(fileUri, fileName, fileSize)
    MessageType.Video -> MessageUiType.Video(fileUri, fileName, fileSize)
    MessageType.Audio -> MessageUiType.Audio(fileUri, fileName, fileSize)
    MessageType.Pdf -> MessageUiType.Pdf(fileUri, fileName, fileSize)
    MessageType.Doc -> MessageUiType.Doc(fileUri, fileName, fileSize)
    MessageType.Xls -> MessageUiType.Xls(fileUri, fileName, fileSize)
    MessageType.Pptx -> MessageUiType.Pptx(fileUri, fileName, fileSize)
    else -> MessageUiType.File(fileUri, fileName, fileSize)
}