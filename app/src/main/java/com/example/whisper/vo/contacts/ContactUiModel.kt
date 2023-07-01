package com.example.whisper.vo.contacts

import com.example.whisper.data.local.model.ContactModel
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.USER_EMAIL
import com.example.whisper.utils.common.ZERO
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

fun List<ContactModel>.toContactsUiModels() = map { it.toContactUiModel() }

fun ContactModel.toContactUiModel() = ContactUiModel(
    contactId = contactId,
    pictureUrl = picture,
    username = username,
    email = email,
    channelUrl = contactUrl,
    createdAt = createdAt,
    isMuted = isMuted,
    isPinned = isPinned
)