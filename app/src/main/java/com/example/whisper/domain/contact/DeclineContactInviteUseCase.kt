package com.example.whisper.domain.contact

import com.example.whisper.data.repository.contacts.ContactsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeclineContactInviteUseCase(val contactsRepository: ContactsRepository) {

    suspend operator fun invoke(
        contactUrl: String,
        coroutineScope: CoroutineScope,
        onContactInviteDeclined: suspend (DeclineContactInviteState) -> Unit
    ) {
        contactsRepository.declineContactRequest(contactUrl) { either ->
            coroutineScope.launch(Dispatchers.IO) {
                either.foldSuspend({ error ->
                    onContactInviteDeclined.invoke(DeclineContactInviteState.ErrorState)
                }, { success ->
                    contactsRepository.declineContactRequestDbCache(contactUrl)
                    onContactInviteDeclined.invoke(DeclineContactInviteState.SuccessState)
                })
            }
        }
    }
}

sealed class DeclineContactInviteState {
    object ErrorState : DeclineContactInviteState()
    object SuccessState : DeclineContactInviteState()
}

