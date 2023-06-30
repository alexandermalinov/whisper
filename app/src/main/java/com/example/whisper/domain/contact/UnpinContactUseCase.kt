package com.example.whisper.domain.contact

import com.example.whisper.data.local.model.ContactModel
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UnpinContactUseCase(
    val contactsRepository: ContactsRepository,
    val recentChatsRepository: RecentChatsRepository
) {

    suspend operator fun invoke(
        contact: ContactModel,
        onPin: suspend (UnpinContactsState) -> Unit
    ) {
        if (contact.contactUrl.isEmpty()) {
            onPin.invoke(UnpinContactsState.ErrorState)
            return
        }

        withContext(Dispatchers.IO) {
            contactsRepository.unpinContactDbCache(contact)
            recentChatsRepository.unpinRecentDbCache(contact)
            onPin.invoke(UnpinContactsState.SuccessState)
        }
    }
}

sealed class UnpinContactsState {
    object ErrorState : UnpinContactsState()
    object SuccessState : UnpinContactsState()
}

