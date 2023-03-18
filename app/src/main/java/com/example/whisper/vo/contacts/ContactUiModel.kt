package com.example.whisper.vo.contacts

import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.USER_EMAIL
import com.example.whisper.utils.common.ZERO
import com.sendbird.android.GroupChannel
import com.sendbird.android.User

data class ContactUiModel(
    val contactId: String = EMPTY,
    val pictureUrl: String = EMPTY,
    val username: String = EMPTY,
    val email: String = EMPTY,
    val channelUrl: String = EMPTY,
    val createdAt: Long = ZERO.toLong(),
    var isInvited: Boolean = false,
    var isLoading: Boolean = false
)

/* -------------------------------------------------------------------------------------------------
 * Exposed
--------------------------------------------------------------------------------------------------*/
fun List<GroupChannel>.toContactsUiModel(loggedUserId: String) =
    map { it.toContactUiModel(loggedUserId) }

fun List<User>.toContactsUiModel() = map { user ->
    ContactUiModel(
        contactId = user.userId,
        pictureUrl = user.profileUrl,
        username = user.nickname,
        email = user.metaData[USER_EMAIL] ?: EMPTY
    )
}

fun GroupChannel.toContactUiModel(loggedUserId: String): ContactUiModel {
    val contact = getContact(loggedUserId)
    return ContactUiModel(
        contactId = contact?.userId ?: EMPTY,
        pictureUrl = contact?.profileUrl ?: EMPTY,
        username = contact?.nickname ?: EMPTY,
        email = contact?.metaData?.get(USER_EMAIL) ?: EMPTY,
        channelUrl = url,
        createdAt = createdAt
    )
}

/* -------------------------------------------------------------------------------------------------
 * Private
--------------------------------------------------------------------------------------------------*/
private fun GroupChannel.getContact(currentUserId: String) =
    members.find { it.userId != currentUserId }
