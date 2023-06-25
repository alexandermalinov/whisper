package com.example.whisper.domain.contact

import com.example.whisper.data.local.model.toContactModels
import com.example.whisper.data.repository.contacts.ContactConnectionStatus
import com.example.whisper.data.repository.contacts.ContactsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PopulateContactsUseCase(
    val contactsRepository: ContactsRepository
) {

    suspend operator fun invoke(
        currentUserId: String,
        coroutineScope: CoroutineScope,
        callBack: suspend (PopulateContactsState) -> Unit
    ) {
        contactsRepository.getContacts(ContactConnectionStatus.CONNECTED) { either ->
            coroutineScope.launch(Dispatchers.IO) {
                either.foldSuspend({
                    callBack.invoke(PopulateContactsState.ErrorState)
                }, {
                    if (currentUserId.isEmpty()) {
                        callBack.invoke(PopulateContactsState.ErrorState)
                        return@foldSuspend
                    }
                    contactsRepository.addAllContactsDbCache(it.toContactModels(currentUserId))
                    callBack.invoke(PopulateContactsState.SuccessState)
                })
            }
        }
    }
}

sealed class PopulateContactsState {
    object ErrorState : PopulateContactsState()
    object SuccessState : PopulateContactsState()
}