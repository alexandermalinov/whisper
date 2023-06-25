package com.example.whisper.domain.contact

import com.example.whisper.data.repository.contacts.ContactsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MuteContactUseCase(val contactsRepository: ContactsRepository) {

    suspend operator fun invoke(
        contactId: String,
        contactUrl: String,
        coroutineScope: CoroutineScope,
        onMute: (MuteContactState) -> Unit
    ) {
        contactsRepository.muteContact(contactUrl, contactId) { either ->
            coroutineScope.launch(Dispatchers.IO) {
                either.foldSuspend({ error ->
                    onMute.invoke(MuteContactState.ErrorState)
                }, { success ->
                    contactsRepository.muteContactLocalDbCache(contactUrl)
                    onMute.invoke(MuteContactState.SuccessState)
                })
            }
        }
    }
}

sealed class MuteContactState {
    object ErrorState : MuteContactState()
    object SuccessState : MuteContactState()
}

