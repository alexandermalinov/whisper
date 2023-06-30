package com.example.whisper.domain.contact

import com.example.whisper.data.local.model.ContactModel
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PinContactUseCase(
    val contactsRepository: ContactsRepository,
    val recentChatsRepository: RecentChatsRepository
) {

    suspend operator fun invoke(
        contact: ContactModel,
        onPin: suspend (PinContactsState) -> Unit
    ) {
        if (contact.contactUrl.isEmpty()) {
            onPin.invoke(PinContactsState.ErrorState)
            return
        }

        withContext(Dispatchers.IO) {
            contactsRepository.pinContactDbCache(contact)
            recentChatsRepository.pinRecentChatDbCache(contact)
            onPin.invoke(PinContactsState.SuccessState)
        }
    }
}

sealed class PinContactsState {
    object ErrorState : PinContactsState()
    object SuccessState : PinContactsState()
}

