package com.example.whisper.domain.contact

import com.example.whisper.data.repository.contacts.ContactsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UnpinContactUseCase(val contactsRepository: ContactsRepository) {

    suspend operator fun invoke(
        contactId: String,
        contactUrl: String,
        coroutineScope: CoroutineScope,
        onPin: (UnpinContactsState) -> Unit
    ) {
        contactsRepository.unpinContact(contactId) { either ->
            coroutineScope.launch(Dispatchers.IO) {
                either.foldSuspend({ error ->
                    onPin.invoke(UnpinContactsState.ErrorState)
                }, { success ->
                    contactsRepository.unpinContactDbCache(contactUrl)
                    onPin.invoke(UnpinContactsState.SuccessState)
                })
            }
        }
    }
}

sealed class UnpinContactsState {
    object ErrorState : UnpinContactsState()
    object SuccessState : UnpinContactsState()
}

