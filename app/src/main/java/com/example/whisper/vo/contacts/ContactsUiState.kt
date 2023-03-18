package com.example.whisper.vo.contacts

import com.example.whisper.utils.common.EMPTY

data class ContactsUiState(
    val uiState: ContactsState = ContactsState.IDLE,
    val invitationsCount: String = EMPTY,
    val pendingCount: String = EMPTY
)

enum class ContactsState {
    IDLE,
    LOADING,
    ERROR,
    EMPTY
}
