package com.example.whisper.vo.contacts

import com.example.whisper.utils.common.EMPTY
import com.sendbird.android.GroupChannel
import com.sendbird.android.User

data class ContactUiModel(
    val contactId: String = EMPTY,
    val pictureUrl: String = EMPTY,
    val username: String = EMPTY,
    val channelUrl: String = EMPTY
)

/* -------------------------------------------------------------------------------------------------
 * Exposed
--------------------------------------------------------------------------------------------------*/
fun GroupChannel.toContactUiModel(loggedUserId: String): ContactUiModel {
    val contact = getContact(loggedUserId)
    return ContactUiModel(
        contactId = contact?.userId ?: EMPTY,
        pictureUrl = contact?.profileUrl ?: EMPTY,
        username = contact?.nickname ?: EMPTY,
        channelUrl = url
    )
}

fun List<GroupChannel>.toContactsUiModel(loggedUserId: String) =
    map { it.toContactUiModel(loggedUserId) }

fun List<User>.toContactsUiModel() = map { user ->
    ContactUiModel(
        contactId = user.userId,
        pictureUrl = user.profileUrl,
        username = user.nickname
    )
}

/* -------------------------------------------------------------------------------------------------
 * Private
--------------------------------------------------------------------------------------------------*/
private fun GroupChannel.getContact(currentUserId: String) =
    members.find { it.userId != currentUserId }
