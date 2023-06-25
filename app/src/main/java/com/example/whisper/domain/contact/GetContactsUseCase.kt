package com.example.whisper.domain.contact

import com.example.whisper.data.local.model.ContactModel
import com.example.whisper.data.local.model.UserModel
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.utils.common.MEMBER_STATE_CONNECTED
import com.example.whisper.utils.common.MEMBER_STATE_INVITE_RECEIVED
import com.example.whisper.utils.common.MEMBER_STATE_INVITE_SENT

class GetContactsUseCase(val contactsRepository: ContactsRepository, val currentUser: UserModel?) {

    suspend operator fun invoke(allContacts: List<ContactModel>): GetContactsState {
        if (currentUser == null) return GetContactsState.ErrorState

        val contactsSentInvite = mutableListOf<ContactModel>()
        val contactsReceivedInvite = mutableListOf<ContactModel>()
        val addedContacts = mutableListOf<ContactModel>()

        allContacts.forEach { contact ->
            when {
                contact.isBlocked -> return@forEach // contact is blocked
                contact.memberState == MEMBER_STATE_INVITE_SENT -> {
                    contactsSentInvite.add(contact)
                }
                contact.memberState == MEMBER_STATE_INVITE_RECEIVED -> {
                    contactsReceivedInvite.add(contact)
                }
                contact.memberState == MEMBER_STATE_CONNECTED -> {
                    addedContacts.add(contact)
                }
            }
        }
        return GetContactsState.SuccessState(
            contactsSentInvite = contactsSentInvite.sortedBy { it.createdAt },
            contactsReceivedInvite = contactsReceivedInvite.sortedBy { it.createdAt },
            addedContacts = addedContacts.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.username })
        )
    }
}

sealed class GetContactsState {
    object ErrorState : GetContactsState()
    data class SuccessState(
        val contactsSentInvite: List<ContactModel>,
        val contactsReceivedInvite: List<ContactModel>,
        val addedContacts: List<ContactModel>
    ) : GetContactsState()
}