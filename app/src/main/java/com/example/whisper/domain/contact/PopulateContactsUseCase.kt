package com.example.whisper.domain.contact

import com.example.whisper.data.local.model.toContactModels
import com.example.whisper.data.repository.contacts.ContactConnectionStatus
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.utils.common.EMPTY

class PopulateContactsUseCase(
    val contactsRepository: ContactsRepository,
    private val recentChatsRepository: RecentChatsRepository
) {

    suspend operator fun invoke(currentUserId: String): PopulateContactsState {
        val contactsResult = contactsRepository.getContacts(ContactConnectionStatus.CONNECTED)

        return contactsResult.foldSuspend({
            PopulateContactsState.ErrorState
        }, { contacts ->
            if (currentUserId.isEmpty()) {
                PopulateContactsState.ErrorState
            } else {
                val dbContacts = contactsRepository.getContactsDb()
                val contactModels = contacts.toContactModels(currentUserId, dbContacts)
                contactsRepository.addAllContactsDbCache(contactModels)

                val recentChats = contactModels.filter { contact -> contact.lastMessage != EMPTY }
                recentChatsRepository.addAllRecentChatDbCache(recentChats)
                PopulateContactsState.SuccessState
            }
        })
    }
}

sealed class PopulateContactsState {
    object ErrorState : PopulateContactsState()
    object SuccessState : PopulateContactsState()
}