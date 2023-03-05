package com.example.whisper.ui.addcontact

import kotlinx.coroutines.flow.Flow

interface AddContactPresenter {
    fun onUsernameTextChanged(textFlow: Flow<CharSequence>)
    fun onAddContactClicked(contactId: String)
    fun onBackClicked()
}