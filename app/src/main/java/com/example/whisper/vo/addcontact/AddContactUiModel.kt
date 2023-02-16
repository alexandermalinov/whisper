package com.example.whisper.vo.addcontact

import com.example.whisper.vo.contacts.ContactUiModel

data class AddContactUiModel(
    val state: AddContactUiState = AddContactUiState.SEARCH_FOUND
)

enum class AddContactUiState {
    LOADING,
    SEARCH_EMPTY,
    SEARCH_FOUND,
    ERROR
}
