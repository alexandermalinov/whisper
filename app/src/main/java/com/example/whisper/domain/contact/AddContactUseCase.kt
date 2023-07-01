package com.example.whisper.domain.contact

import com.example.whisper.data.local.model.toContactModel
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.user.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddContactUseCase(
    private val userRepository: UserRepository,
    private val contactsRepository: ContactsRepository
) {

    suspend operator fun invoke(
        contactId: String,
        coroutineScope: CoroutineScope,
        callback: suspend (AddContactState) -> Unit
    ) {
        if (contactId.isEmpty()) {
            callback.invoke(AddContactState.ErrorState)
            return
        }

        contactsRepository.addContact(contactId) { either ->
            coroutineScope.launch(Dispatchers.IO) {
                either.foldSuspend({ failure ->
                    callback.invoke(AddContactState.NetworkErrorState)
                }, { channel ->
                    val contact = channel.toContactModel(
                        currentUserId = userRepository.cachedUser.userId,
                        dbContact = null
                    )

                    contactsRepository.addContactDbCache(contact)
                    callback.invoke(AddContactState.SuccessState)
                })
            }
        }
    }
}

sealed class AddContactState {
    object NetworkErrorState : AddContactState()
    object ErrorState : AddContactState()
    object SuccessState : AddContactState()
}