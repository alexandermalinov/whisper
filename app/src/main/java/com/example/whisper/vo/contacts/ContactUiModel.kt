package com.example.whisper.vo.contacts

import com.example.whisper.data.local.model.ContactModel
import com.example.whisper.data.local.model.UserModel
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
    var isMuted: Boolean = false,
    var isPinned: Boolean = false,
    var isLoading: Boolean = false
)

/* -------------------------------------------------------------------------------------------------
 * Exposed
--------------------------------------------------------------------------------------------------*/
fun List<User>.toContactsUiModel(loggedUserId: String) =
    filter { user ->
        user.userId != loggedUserId
    }.map { user ->
        ContactUiModel(
            contactId = user.userId,
            pictureUrl = user.profileUrl,
            username = user.nickname,
            email = user.metaData[USER_EMAIL] ?: EMPTY
        )
    }

fun List<ContactModel>.toContactsUiModels(currentUser: UserModel) = map {
    it.toContactUiModel(currentUser)
}

fun ContactModel.toContactUiModel(currentUser: UserModel) = ContactUiModel(
    contactId = contactId,
    pictureUrl = picture,
    username = username,
    email = email,
    channelUrl = contactUrl,
    createdAt = createdAt,
    isMuted = isMuted,
    isPinned = currentUser.isContactPinned(contactId)
)

fun GroupChannel.toContactUiModel(currentUser: UserModel): ContactUiModel {
    val contact = getContact(currentUser.userId)
    return ContactUiModel(
        contactId = contact?.userId ?: EMPTY,
        pictureUrl = contact?.profileUrl ?: EMPTY,
        username = contact?.nickname ?: EMPTY,
        email = contact?.metaData?.get(USER_EMAIL) ?: EMPTY,
        channelUrl = url,
        createdAt = createdAt,
        isMuted = contact?.isMuted ?: false,
        isPinned = currentUser.isContactPinned(contact?.userId)
    )
}

/* -------------------------------------------------------------------------------------------------
 * Private
--------------------------------------------------------------------------------------------------*/
private fun GroupChannel.getContact(currentUserId: String) =
    members.find { it.userId != currentUserId }

private fun UserModel.isContactPinned(contactId: String?): Boolean = pinnedContacts
    .firstOrNull { it.contactId == contactId } != null
/*metaData[PINNED_CONTACTS]
    ?.filterNot { it.isWhitespace() }
    ?.split(',')
    ?.contains(contactId)
    ?: false*/