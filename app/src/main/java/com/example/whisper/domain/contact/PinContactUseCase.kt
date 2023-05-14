package com.example.whisper.domain.contact

import androidx.lifecycle.viewModelScope
import com.example.whisper.data.repository.contacts.ContactsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PinContactUseCase(val contactsRepository: ContactsRepository) {

    suspend operator fun invoke(
        contactId: String,
        contactUrl: String,
        coroutineScope: CoroutineScope,
        onPin: (PinContactsState) -> Unit
    ) {
        contactsRepository.pinContact(contactId) { either ->
            coroutineScope.launch(Dispatchers.IO) {
                either.foldSuspend({ error ->
                    onPin.invoke(PinContactsState.ErrorState)
                }, { success ->
                    contactsRepository.pinContactLocal(contactUrl)
                    onPin.invoke(PinContactsState.SuccessState)
                })
            }
        }
    }
}

sealed class PinContactsState {
    object ErrorState : PinContactsState()
    object SuccessState : PinContactsState()
}

