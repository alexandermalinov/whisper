package com.example.whisper.data.repository.messages

import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import com.sendbird.android.BaseMessage
import com.sendbird.android.FileMessage
import com.sendbird.android.FileMessageParams
import com.sendbird.android.GroupChannel
import com.sendbird.android.MessageListParams
import com.sendbird.android.SendBird
import com.sendbird.android.UserMessage
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MessagesRemoteSource @Inject constructor() : MessagesRepository.RemoteSource {

    /* --------------------------------------------------------------------------------------------
     * Private
     ---------------------------------------------------------------------------------------------*/
    private fun createMessagesQuery(unreadMessageCount: Int) = MessageListParams().apply {
        previousResultSize = 100
        nextResultSize = unreadMessageCount
        isInclusive = true
        //setIncludeReactions(true)
        setReverse(true)
    }

    private fun createFileMessageParams(
        fileUrl: String,
        fileName: String,
        fileSize: String,
        mimeType: String
    ) =
        FileMessageParams().apply {
            this.file = File(fileUrl)
            //this.fileUrl = fileUrl
            if (fileName.isNotEmpty()) this.fileName = fileName
            if (fileSize.isNotEmpty()) this.setFileSize(fileSize.toInt())
            this.mimeType = mimeType
        }

    /* --------------------------------------------------------------------------------------------
     * Override
     ---------------------------------------------------------------------------------------------*/

    override suspend fun getChannelMessages(
        channel: GroupChannel
    ): Either<HttpError, List<BaseMessage>> =
        suspendCoroutine { continuation ->
            channel.getMessagesByTimestamp(
                channel.lastMessage?.createdAt ?: 0L,
                createMessagesQuery(channel.unreadMessageCount)
            ) { messages, error ->
                if (error == null) {
                    continuation.resume(Either.right(messages))
                } else {
                    continuation.resume(Either.left(HttpError(serverMessage = error.message)))
                }
            }
        }

    override suspend fun sendMessage(
        channel: GroupChannel,
        message: String
    ): Either<HttpError, UserMessage> = suspendCoroutine { continuation ->
        channel.sendUserMessage(message) { userMessage, error ->
            if (error != null) {
                continuation.resume(Either.left(HttpError(serverMessage = error.message)))
            } else {
                continuation.resume(Either.right(userMessage))
            }
        }
    }

    override suspend fun sendFileMessage(
        channel: GroupChannel,
        fileUrl: String,
        fileName: String,
        fileSize: String,
        mimeType: String
    ): Either<HttpError, FileMessage> = suspendCoroutine { continuation ->
        channel.sendFileMessage(
            createFileMessageParams(
                fileUrl,
                fileName,
                fileSize,
                mimeType
            )
        ) { message, error ->
            if (error != null) {
                continuation.resume(Either.left(HttpError(serverMessage = error.message)))
            } else {
                continuation.resume(Either.right(message))
            }
        }
    }

    override suspend fun markAsDelivered(contactId: String) {
        SendBird.markAsDelivered(contactId)
    }

    override suspend fun marksAsRead(groupChannel: GroupChannel) {
        groupChannel.markAsRead(null)
    }
}