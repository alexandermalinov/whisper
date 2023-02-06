package com.example.whisper.ui.addcontact

import kotlinx.coroutines.flow.Flow

interface AddContactPresenter {

    fun onUsernameTextChanged(textFlow: Flow<CharSequence>)

    fun addContact(contactId: String)
}