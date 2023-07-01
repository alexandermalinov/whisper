package com.example.whisper.domain.contact

import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UnmuteContactUseCase(
    val contactsRepository: ContactsRepository,
    val recentChatsRepository: RecentChatsRepository
) {

    suspend operator fun invoke(
        contactUrl: String,
        onUnmute: suspend (UnmuteContactState) -> Unit
    ) {
        if (contactUrl.isEmpty()) {
            onUnmute.invoke(UnmuteContactState.ErrorState)
            return
        }

        withContext(Dispatchers.IO) {
            contactsRepository.unMuteContactDbCache(contactUrl)
            recentChatsRepository.unMuteRecentChatDbCache(contactUrl)
            onUnmute.invoke(UnmuteContactState.SuccessState)
        }
    }
}

sealed class UnmuteContactState {
    object ErrorState : UnmuteContactState()
    object SuccessState : UnmuteContactState()
}

