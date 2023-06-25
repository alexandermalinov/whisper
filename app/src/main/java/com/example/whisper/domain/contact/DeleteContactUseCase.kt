package com.example.whisper.domain.contact

import com.example.whisper.data.repository.contacts.ContactsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeleteContactUseCase(val contactsRepository: ContactsRepository) {

    suspend operator fun invoke(
        contactUrl: String,
        coroutineScope: CoroutineScope,
        onContactedDeleted: (DeleteContactState) -> Unit
    ) {
        contactsRepository.deleteContact(contactUrl) { either ->
            coroutineScope.launch(Dispatchers.IO) {
                either.foldSuspend({ error ->
                    onContactedDeleted.invoke(DeleteContactState.ErrorState)
                }, { success ->
                    contactsRepository.deleteContactDbCache(contactUrl)
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

