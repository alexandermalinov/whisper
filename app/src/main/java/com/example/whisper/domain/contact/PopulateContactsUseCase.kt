package com.example.whisper.domain.contact

import com.example.whisper.data.local.model.toContactModels
import com.example.whisper.data.repository.contacts.ContactConnectionStatus
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.utils.common.EMPTY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PopulateContactsUseCase(
    val contactsRepository: ContactsRepository,
    private val recentChatsRepository: RecentChatsRepository
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
                }, { contacts ->
                    if (currentUserId.isEmpty()) {
                        callBack.invoke(PopulateContactsState.ErrorState)
                        return@foldSuspend
                    }

                    val dbContacts = contactsRepository.getContactsDb()

                    val contactModels = contacts.toContactModels(currentUserId, dbContacts)
                    contactsRepository.addAllContactsDbCache(contactModels)

                    val recentChats = contactModels.filter { contact ->
                        contact.lastMessage != EMPTY
                    }

                    recentChatsRepository.addAllRecentChatDbCache(recentChats)
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