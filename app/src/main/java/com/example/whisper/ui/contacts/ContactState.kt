package com.example.whisper.ui.contacts

import com.example.whisper.utils.common.MEMBER_STATE_CONNECTED
import com.example.whisper.utils.common.MEMBER_STATE_INVITE_SENT

enum class ContactState {
    JOINED,
    PENDING,
    INVITED
}

fun getContactState(contactState: String) = when (contactState) {
    MEMBER_STATE_CONNECTED -> ContactState.JOINED
    MEMBER_STATE_INVITE_SENT -> ContactState.PENDING
    else -> ContactState.INVITED
}