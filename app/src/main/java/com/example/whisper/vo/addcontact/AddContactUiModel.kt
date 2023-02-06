package com.example.whisper.vo.addcontact

import com.example.whisper.vo.contacts.ContactUiModel

data class AddContactUiModel(
    val contacts: List<ContactUiModel> = emptyList(),
    val isLoading: Boolean = false,
)
