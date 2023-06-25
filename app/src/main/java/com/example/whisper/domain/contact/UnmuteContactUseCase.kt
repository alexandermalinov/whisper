package com.example.whisper.domain.contact

import com.example.whisper.data.repository.contacts.ContactsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UnmuteContactUseCase(val contactsRepository: ContactsRepository) {

    suspend operator fun invoke(
        contactId: String,
        contactUrl: String,
        coroutineScope: CoroutineScope,
        onUnmute: (UnmuteContactState) -> Unit
    ) {
        contactsRepository.unmuteContact(contactUrl, contactId) { either ->
            coroutineScope.launch(Dispatchers.IO) {
                either.foldSuspend({ error ->
                    onUnmute.invoke(UnmuteContactState.ErrorState)
                }, { success ->
                    contactsRepository.unMuteContactDbCache(contactUrl)
                    onUnmute.invoke(UnmuteContactState.SuccessState)
                })
            }
        }
    }
}

sealed class UnmuteContactState {
    object ErrorState : UnmuteContactState()
    object SuccessState : UnmuteContactState()
}

