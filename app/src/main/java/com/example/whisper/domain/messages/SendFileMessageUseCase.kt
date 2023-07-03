package com.example.whisper.domain.messages

import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.messages.MessagesRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.utils.NetworkHandler
import com.sendbird.android.GroupChannel

class SendFileMessageUseCase(
    private val messagesRepository: MessagesRepository,
    private val contactRepository: ContactsRepository,
    private val recentChatRepository: RecentChatsRepository,
    private val networkHandler: NetworkHandler
) {

    suspend operator fun invoke(
        channel: GroupChannel?,
        fileUrl: String,
        fileName: String,
        fileSize: String,
        mimeType: String,
        contactId: String
    ) {

        if (channel == null || fileUrl.isEmpty() || contactId.isEmpty()) return

        val createdAt = System.currentTimeMillis()
        val localMessage = messagesRepository.addOutgoingFileMessageDbCache(
            receiverId = contactId,
            fileUrl = fileUrl,
            fileName = fileName,
            fileSize = fileSize,
            createdAt = createdAt,
            mimeType = mimeType
        )

        val result = messagesRepository.sendFileMessage(
            channel = channel,
            fileUrl = fileUrl,
            fileName = fileName,
            fileSize = fileSize,
            mimeType = mimeType
        )

        return result.foldSuspend({ httpError ->
            if (networkHandler.isNetworkAvailable()) {

            } else {

            }
        }, { message ->
            val updatedMessage = localMessage.copy(
                id = message.messageId.toString()
            )
            messagesRepository.updateMessageId(
                updatedMessage = updatedMessage,
                oldMessage = localMessage
            )

            contactRepository.getContactFromCacheOrDb(channel.url)
                ?.apply {
                    lastMessage = message.message
                }?.let {
                    contactRepository.updateContactDbCache(it)
                }

            recentChatRepository.getRecentChatFromCacheOrDb(channel.url)
                ?.apply {
                    lastMessage = message.message
                    lastMessageTimestamp = createdAt
                }?.let {
                    recentChatRepository.updateRecentChatDbCache(it)
                }
        })
    }
}