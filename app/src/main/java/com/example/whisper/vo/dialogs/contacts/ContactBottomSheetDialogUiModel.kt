package com.example.whisper.vo.dialogs.contacts

import com.example.whisper.ui.contacts.ContactState
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.INVALID_RES

data class ContactBottomSheetDialogUiModel(
    val uiState: ContactBottomSheetState = ContactBottomSheetState.IDLE,
    val profileImageUrl: String = EMPTY,
    val channelUrl: String = EMPTY,
    val username: String = EMPTY,
    val email: String = EMPTY,
    val id: String = EMPTY,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val contactStatus: ContactState = ContactState.JOINED,
    val errorTitle: Int = INVALID_RES,
    val errorMessage: Int = INVALID_RES
)

enum class ContactBottomSheetState {
    IDLE,
    LOADING,
    ERROR
}