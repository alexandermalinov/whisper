package com.example.whisper.ui.addcontact

import com.example.whisper.vo.contacts.ContactUiModel
import kotlinx.coroutines.flow.Flow

interface AddContactPresenter {
    fun onUsernameTextChanged(textFlow: Flow<CharSequence>)
    fun onAddContactClicked(contactId: String)
    fun onBackClicked()
}