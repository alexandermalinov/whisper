package com.example.whisper.vo.basecontacts

import com.example.whisper.R
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.RECENT_CHATS_PAGER_POSITION

data class BaseContactsUiModel(
    val pageTitle: Int = R.string.chats,
    val profilePictureUrl: String = EMPTY,
    val searchVisible: Boolean = true,
    val addContactOrCreateGroupButtonIconId: Int = R.drawable.ic_feather,
    val viewPagerPosition: Int = RECENT_CHATS_PAGER_POSITION,
    val bottomNavigationItemId: Int = R.id.recentChatsFragment
)
