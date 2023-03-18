package com.example.whisper.ui.contacts

interface ContactPresenter {

    fun navigateToAddContact()

    fun expandInvitations()

    fun expandPending()

    fun acceptInvite(contactId: String)

    fun declineInvite(contactId: String)
}