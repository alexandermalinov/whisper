package com.example.whisper.domain.contact

import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.messages.MessagesRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class SyncMessagesUseCase(
    val coroutineScope: CoroutineScope,
    val contactsRepository: ContactsRepository,
    private val messagesRepository: MessagesRepository,
    private val recentChatsRepository: RecentChatsRepository
) {

    suspend operator fun invoke(currentUserId: String) {
        recentChatsRepository.getRecentChatsDb().forEach { recentChat ->
            contactsRepository.getContact(recentChat.contactUrl) { either ->
                coroutineScope.launch {
                    either.foldSuspend({ error ->
                        Timber.e("${error.errorMessage}")
                    }, { channel ->
                        messagesRepository.syncRemoteAndLocalMessages(
                            channel,
                            currentUserId,
                            recentChat.contactId
                        )
                    })
                }
            }
        }
    }
}