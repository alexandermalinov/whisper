package com.example.whisper.data.repository.messages

import com.example.whisper.data.local.model.MessageModel
import com.example.whisper.data.local.model.MessageStatus
import com.example.whisper.data.local.model.MessageType
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.MIME_TYPE_AUDIO
import com.example.whisper.utils.common.MIME_TYPE_DOC
import com.example.whisper.utils.common.MIME_TYPE_PDF
import com.example.whisper.utils.common.MIME_TYPE_PHOTO
import com.example.whisper.utils.common.MIME_TYPE_PPTX
import com.example.whisper.utils.common.MIME_TYPE_VIDEO
import com.example.whisper.utils.common.MIME_TYPE_XLS
import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import com.example.whisper.vo.chat.peertopeer.messages.MessageUiType
import com.sendbird.android.BaseMessage
import com.sendbird.android.FileMessage
import com.sendbird.android.GroupChannel
import com.sendbird.android.UserMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject

class MessagesRepository @Inject constructor(
    private val remote: RemoteSource,
    private val local: LocalSource,
    private val userRepository: UserRepository,
    private val contactsRepository: ContactsRepository,
    private val recentChatsRepository: RecentChatsRepository,
) {

    var cachedMessages: HashMap<String, MessageModel> = hashMapOf()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        coroutineScope.launch {
            cachedMessages = local.getMessages().associateBy { it.id } as HashMap
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Sources
     ---------------------------------------------------------------------------------------------*/
    interface RemoteSource {

        suspend fun getChannelMessages(channel: GroupChannel): Either<HttpError, List<BaseMessage>>

        suspend fun sendMessage(
            channel: GroupChannel,
            message: String
        ): Either<HttpError, UserMessage>

        suspend fun sendFileMessage(
            channel: GroupChannel,
            fileUrl: String,
            fileName: String,
            fileSize: String,
            mimeType: String
        ): Either<HttpError, FileMessage>

        suspend fun markAsDelivered(contactId: String)

        suspend fun marksAsRead(groupChannel: GroupChannel)
    }

    interface LocalSource {

        suspend fun updateMessage(message: MessageModel)

        fun getMessagesFlow(currentUserId: String, contactId: String): Flow<List<MessageModel>>

        fun getContactMessages(currentUserId: String, contactId: String): List<MessageModel>

        suspend fun getMessages(): List<MessageModel>

        suspend fun getMessage(id: String): MessageModel?

        suspend fun addMessage(message: MessageModel)

        suspend fun deleteMessage(message: MessageModel)
    }

    /* --------------------------------------------------------------------------------------------
     * Exposed
     ---------------------------------------------------------------------------------------------*/

    suspend fun syncRemoteAndLocalMessages(
        channel: GroupChannel,
        currentUserId: String,
        contactId: String
    ) {
        val contactLocalMessages = local.getContactMessages(currentUserId, contactId)
        remote.getChannelMessages(channel).foldSuspend({ error ->
            Timber.e("${error.errorMessage}")
        }, { messages ->
            messages.mapNotNull { remoteMessage ->
                if (contactLocalMessages.none { it.id == remoteMessage.messageId.toString() }) {
                    remoteMessage
                } else {
                    null
                }
            }.let { remoteMessages ->
                remoteMessages.forEach { message ->
                    message.sender?.userId ?: return@forEach

                    val messageModel = MessageModel(
                        id = message.messageId.toString(),
                        senderId = message.sender.userId,
                        receiverId = if (message.sender.userId == currentUserId) contactId else currentUserId,
                        body = message.message,
                        fileUrl = if (message is FileMessage) message.url else EMPTY,
                        fileName = if (message is FileMessage) message.name else EMPTY,
                        fileSize = if (message is FileMessage) message.size.toString() else EMPTY,
                        status = MessageStatus.DELIVERED,
                        createdAt = message.createdAt,
                        type = if (message is FileMessage) getExtension(message.name) else MessageType.TEXT
                    )

                    local.addMessage(messageModel)
                }
            }
        })
    }

    fun getMessagesFlow(currentUserId: String, contactId: String) = local.getMessagesFlow(
        currentUserId,
        contactId
    )

    suspend fun sendMessage(
        channel: GroupChannel,
        message: String
    ): Either<HttpError, UserMessage> = remote.sendMessage(channel, message)

    suspend fun sendFileMessage(
        channel: GroupChannel,
        fileUrl: String,
        fileName: String,
        fileSize: String,
        mimeType: String
    ): Either<HttpError, FileMessage> =
        remote.sendFileMessage(channel, fileUrl, fileName, fileSize, mimeType)

    suspend fun addOutgoingMessageDbCache(
        receiverId: String,
        message: String,
        createdAt: Long
    ): MessageModel {
        val messageModel = MessageModel(
            id = UUID.randomUUID().toString(),
            senderId = userRepository.cachedUser.userId,
            receiverId = receiverId,
            body = message,
            fileUrl = EMPTY,
            fileName = EMPTY,
            fileSize = EMPTY,
            status = MessageStatus.SENDING,
            createdAt = createdAt,
            type = MessageType.TEXT
        )
        local.addMessage(messageModel)
        cachedMessages[messageModel.id] = messageModel

        return messageModel
    }

    suspend fun addIncomingMessageDbCache(message: MessageModel) {
        local.addMessage(message)
        cachedMessages[message.id] = message
    }

    suspend fun addOutgoingFileMessageDbCache(
        receiverId: String,
        fileUrl: String,
        createdAt: Long,
        mimeType: String,
        fileName: String,
        fileSize: String
    ): MessageModel {
        val messageModel = MessageModel(
            id = UUID.randomUUID().toString(),
            senderId = userRepository.cachedUser.userId,
            receiverId = receiverId,
            body = EMPTY,
            fileUrl = fileUrl,
            fileName = fileName,
            fileSize = fileSize,
            status = MessageStatus.SENDING,
            createdAt = createdAt,
            type = getFileType(mimeType)
        )

        local.addMessage(messageModel)
        cachedMessages[messageModel.id] = messageModel

        return messageModel
    }

    suspend fun updateMessageId(
        updatedMessage: MessageModel,
        oldMessage: MessageModel
    ) {
        local.addMessage(updatedMessage)
        local.deleteMessage(oldMessage)
        cachedMessages.remove(oldMessage.id)
        cachedMessages[updatedMessage.id] = updatedMessage
    }

    suspend fun updateMessageStatus(
        messageStatus: MessageStatus,
        channel: GroupChannel,
        isMyMessage: Boolean
    ) {
        if (isMyMessage.not() && messageStatus == MessageStatus.SENDING) return

        val oldMessage: MessageModel =
            cachedMessages.values
                .filter { message ->
                    val contactId = channel.members
                        .find { userRepository.cachedUser.userId != it.userId }
                        ?.userId
                        ?: EMPTY

                    isMessageFromChannel(message, contactId)
                }
                .maxByOrNull { it.createdAt }
                ?: return

        val updatedMessage: MessageModel = oldMessage.copy(status = messageStatus)
        cachedMessages.remove(oldMessage.id)
        cachedMessages[oldMessage.id] = updatedMessage
        local.updateMessage(message = updatedMessage)

        if (messageStatus == MessageStatus.READ && channel.myLastRead.toString() == oldMessage.id) {
            updateRecentChatUnreadMessages(channel)
        }
    }

    suspend fun sendMessageDeliveredReceipt(contactId: String) {
        remote.markAsDelivered(contactId)
    }

    suspend fun sendMessageReadReceipt(groupChannel: GroupChannel) {
        remote.marksAsRead(groupChannel)
        updateRecentChatUnreadMessages(groupChannel)
    }

    private suspend fun updateRecentChatUnreadMessages(channel: GroupChannel) {
        contactsRepository.getContactFromCacheOrDb(channel.url)
            ?.let { contact ->
                contact.unreadMessagesCount = 0
                contactsRepository.updateContactDbCache(contact)
            } ?: return

        recentChatsRepository.getRecentChatFromCacheOrDb(channel.url)
            ?.let { recentChat ->
                recentChat.unreadMessagesCount = 0
                recentChatsRepository.updateRecentChatDbCache(recentChat)
            }
    }

    fun getExtension(filePath: String): MessageType = when (filePath.substringAfterLast('.', "")) {
        "jpg" -> MessageType.Photo
        "png" -> MessageType.Photo
        "mp4" -> MessageType.Video
        "mp3" -> MessageType.Audio
        "pdf" -> MessageType.Pdf
        "docx" -> MessageType.Doc
        "txt" -> MessageType.Doc
        "xls" -> MessageType.Xls
        "pptx" -> MessageType.Pptx
        else -> MessageType.Pptx
    }

    private fun isMessageFromChannel(message: MessageModel, contactId: String) =
        (message.senderId == userRepository.cachedUser.userId && message.receiverId == contactId) ||
                (message.senderId == contactId && message.receiverId == userRepository.cachedUser.userId)

    private fun getFileType(mimeType: String) = when (mimeType) {
        MIME_TYPE_PHOTO -> {
            MessageType.Photo
        }

        MIME_TYPE_VIDEO -> {
            MessageType.Video
        }

        MIME_TYPE_AUDIO -> {
            MessageType.Audio
        }

        MIME_TYPE_PDF -> {
            MessageType.Pdf
        }

        MIME_TYPE_DOC -> {
            MessageType.Doc
        }

        MIME_TYPE_XLS -> {
            MessageType.Xls
        }

        MIME_TYPE_PPTX -> {
            MessageType.Pptx
        }

        else -> {
            MessageType.FILE
        }
    }
}