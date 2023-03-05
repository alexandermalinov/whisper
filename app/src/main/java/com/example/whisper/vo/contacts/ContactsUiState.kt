package com.example.whisper.vo.contacts

data class ContactsUiState(
    val uiState: ContactsState = ContactsState.IDLE
)

enum class ContactsState {
    IDLE,
    LOADING,
    ERROR,
    EMPTY
}
