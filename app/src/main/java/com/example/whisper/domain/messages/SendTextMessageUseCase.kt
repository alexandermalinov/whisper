package com.example.whisper.domain.messages

import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.messages.MessagesRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.utils.NetworkHandler
import com.sendbird.android.GroupChannel

class SendTextMessageUseCase(
    private val messagesRepository: MessagesRepository,
    private val contactRepository: ContactsRepository,
    private val recentChatRepository: RecentChatsRepository,
    private val networkHandler: NetworkHandler
) {

    suspend operator fun invoke(
        channel: GroupChannel?,
        messageBody: String,
        contactId: String
    ) {

        if (channel == null || messageBody.isEmpty() || contactId.isEmpty()) return

        val createdAt = System.currentTimeMillis()
        val localMessage = messagesRepository.addOutgoingMessageDbCache(
            contactId,
            messageBody,
            createdAt
        )

        val result = messagesRepository.sendMessage(channel, messageBody)
        contactRepository.getContactFromCacheOrDb(channel.url)
            ?.apply {
                lastMessage = messageBody
            }?.let {
                contactRepository.updateContactDbCache(it)
            }

        recentChatRepository.getRecentChatFromCacheOrDb(channel.url)
            ?.apply {
                lastMessage = messageBody
                lastMessageTimestamp = createdAt
            }?.let {
                recentChatRepository.updateRecentChatDbCache(it)
            }

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
        })
    }
}