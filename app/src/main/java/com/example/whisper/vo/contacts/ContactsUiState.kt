package com.example.whisper.vo.contacts

import com.example.whisper.utils.common.ZERO

data class ContactsUiState(
    val uiState: ContactsState = ContactsState.IDLE,
    val contactsCount: Int = ZERO,
    val invitationsCount: Int = ZERO,
    val pendingCount: Int = ZERO
)

enum class ContactsState {
    IDLE,
    LOADING,
    ERROR,
    EMPTY
}
