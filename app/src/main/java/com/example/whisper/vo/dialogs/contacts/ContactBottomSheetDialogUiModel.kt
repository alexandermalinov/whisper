package com.example.whisper.vo.dialogs.contacts

import com.example.whisper.utils.common.EMPTY

data class ContactBottomSheetDialogUiModel(
    val profileImageUrl: String = EMPTY,
    val channelUrl: String = EMPTY,
    val username: String = EMPTY,
    val email: String = EMPTY,
    val id: String = EMPTY,
    val isLoading: Boolean = false
)
