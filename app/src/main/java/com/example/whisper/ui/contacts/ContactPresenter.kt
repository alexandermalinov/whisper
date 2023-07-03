package com.example.whisper.ui.contacts

import com.example.whisper.vo.contacts.ContactUiModel

interface ContactPresenter {

    fun navigateToAddContact()

    fun expandInvitations()

    fun expandPending()

    fun acceptInvite(contactId: String)

    fun declineInvite(contactId: String)

    fun showBottomDialog(contact: ContactUiModel): Boolean

    fun onClick()
}