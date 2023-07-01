package com.example.whisper.domain.contact

import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MuteContactUseCase(
    val contactsRepository: ContactsRepository,
    val recentChatsRepository: RecentChatsRepository
) {

    suspend operator fun invoke(
        contactUrl: String,
        onMute: suspend (MuteContactState) -> Unit
    ) {
        if (contactUrl.isEmpty()){
            onMute.invoke(MuteContactState.ErrorState)
            return
        }

        withContext(Dispatchers.IO) {
            contactsRepository.muteContactLocalDbCache(contactUrl)
            recentChatsRepository.muteRecentChatDbCache(contactUrl)
            onMute.invoke(MuteContactState.SuccessState)
        }
    }
}

sealed class MuteContactState {
    object ErrorState : MuteContactState()
    object SuccessState : MuteContactState()
}

