package com.example.whisper.domain.contact

import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeleteContactUseCase(
    val contactsRepository: ContactsRepository,
    val recentChatsRepository: RecentChatsRepository
) {

    suspend operator fun invoke(
        contactUrl: String,
        coroutineScope: CoroutineScope,
        isRecentChat: Boolean,
        onContactedDeleted: (DeleteContactState) -> Unit
    ) {
        if (isRecentChat) {
            recentChatsRepository.deleteRecentChatDbCache(contactUrl)
            onContactedDeleted.invoke(DeleteContactState.SuccessState)
            return
        }

        contactsRepository.deleteContact(contactUrl) { either ->
            coroutineScope.launch(Dispatchers.IO) {
                either.foldSuspend({ error ->
                    onContactedDeleted.invoke(DeleteContactState.ErrorState)
                }, { success ->
                    contactsRepository.deleteContactDbCache(contactUrl)
                    recentChatsRepository.deleteRecentChatDbCache(contactUrl)
                    onContactedDeleted.invoke(DeleteContactState.SuccessState)
                })
            }
        }
    }
}

sealed class DeleteContactState {
    object ErrorState : DeleteContactState()
    object SuccessState : DeleteContactState()
}

