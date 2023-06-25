package com.example.whisper.domain.contact

import com.example.whisper.data.repository.contacts.ContactsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AcceptContactInviteUseCase(val contactsRepository: ContactsRepository) {

    suspend operator fun invoke(
        contactUrl: String,
        coroutineScope: CoroutineScope,
        onContactInviteAccepted: suspend (AcceptContactInviteState) -> Unit
    ) {
        contactsRepository.acceptContactRequest(contactUrl) { either ->
            coroutineScope.launch(Dispatchers.IO) {
                either.foldSuspend({ error ->
                    onContactInviteAccepted.invoke(AcceptContactInviteState.ErrorState)
                }, { success ->
                    contactsRepository.acceptContactRequestDbCache(contactUrl)
                    onContactInviteAccepted.invoke(AcceptContactInviteState.SuccessState)
                })
            }
        }
    }
}

sealed class AcceptContactInviteState {
    object ErrorState : AcceptContactInviteState()
    object SuccessState : AcceptContactInviteState()
}

