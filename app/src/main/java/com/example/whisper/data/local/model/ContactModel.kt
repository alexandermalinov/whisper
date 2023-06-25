package com.example.whisper.data.local.model

import com.example.whisper.data.local.entity.Contact
import com.example.whisper.utils.common.*
import com.sendbird.android.GroupChannel
import com.sendbird.android.Member
import com.sendbird.android.User

data class ContactModel(
    val contactUrl: String,
    val contactId: String,
    val email: String,
    val username: String,
    val picture: String,
    val createdAt: Long,
    var memberState: String,
    var onlineStatus: String,
    var unreadMessagesCount: Int,
    var lastMessage: String,
    var lastMessageTimestamp: Long,
    var isMuted: Boolean,
    var isBlocked: Boolean,
)

fun List<ContactModel>.toContacts() = map { it.toContact() }

fun ContactModel.toContact() = Contact(
    contactUrl = contactUrl,
    contactId = contactId,
    email = email,
    username = username,
    picture = picture,
    createdAt = createdAt,
    memberState = memberState,
    onlineStatus = onlineStatus,
    unreadMessagesCount = unreadMessagesCount,
    lastMessage = lastMessage,
    lastMessageTimestamp = lastMessageTimestamp,
    isMuted = isMuted,
    isBlocked = isBlocked
)

fun List<GroupChannel>.toContacts(currentUserId: String): List<Contact> =
    map { it.toContact(currentUserId) }

fun GroupChannel.toContact(currentUserId: String): Contact {
    val contact = getContact(currentUserId)
    return Contact(
        contactUrl = url,
        contactId = contact?.userId ?: EMPTY,
        email = contact?.metaData?.get(USER_EMAIL) ?: EMPTY,
        username = contact?.nickname ?: EMPTY,
        picture = contact?.profileUrl ?: EMPTY,
        createdAt = createdAt,
        memberState = EMPTY,
        onlineStatus = getOnlineStatus(
            contact?.connectionStatus ?: User.ConnectionStatus.OFFLINE
        ),
        unreadMessagesCount = unreadMessageCount,
        lastMessage = lastMessage?.message ?: EMPTY,
        lastMessageTimestamp = lastMessage?.createdAt ?: ZERO.toLong(),
        isMuted = contact?.isMuted ?: false,
        isBlocked = contact?.isBlockedByMe ?: false
    )
}

fun List<GroupChannel>.toContactModels(currentUserId: String): List<ContactModel> =
    map { it.toContactModel(currentUserId) }

fun GroupChannel.toContactModel(
    currentUserId: String
): ContactModel {
    val contact = getContact(currentUserId)
    return ContactModel(
        contactUrl = url,
        contactId = contact?.userId ?: EMPTY,
        email = contact?.metaData?.get(USER_EMAIL) ?: EMPTY,
        username = contact?.nickname ?: EMPTY,
        picture = contact?.profileUrl ?: EMPTY,
        createdAt = createdAt,
        memberState = getMemberState(this),
        onlineStatus = getOnlineStatus(
            contact?.connectionStatus ?: User.ConnectionStatus.OFFLINE
        ),
        unreadMessagesCount = unreadMessageCount,
        lastMessage = lastMessage?.message ?: EMPTY,
        lastMessageTimestamp = lastMessage?.createdAt ?: ZERO.toLong(),
        isMuted = contact?.isMuted ?: false,
        isBlocked = contact?.isBlockedByMe ?: false
    )
}

fun User.toContactModel(channel: GroupChannel, memberState: String) = ContactModel(
    contactUrl = channel.url ?: EMPTY,
    contactId = userId ?: EMPTY,
    email = metaData?.get(USER_EMAIL) ?: EMPTY,
    username = nickname ?: EMPTY,
    picture = profileUrl ?: EMPTY,
    createdAt = channel.createdAt,
    memberState = memberState,
    onlineStatus = getOnlineStatus(connectionStatus ?: User.ConnectionStatus.OFFLINE),
    unreadMessagesCount = channel.unreadMessageCount,
    lastMessage = channel.lastMessage?.message ?: EMPTY,
    lastMessageTimestamp = channel.lastMessage?.createdAt ?: ZERO.toLong(),
    isMuted = false,
    isBlocked = false
)

private fun GroupChannel.getContact(currentUserId: String) =
    members.find { it.userId != currentUserId }

private fun getMemberState(contact: GroupChannel) = when {
    contact.joinedMemberCount == 1 && contact.myMemberState == Member.MemberState.INVITED -> MEMBER_STATE_INVITE_RECEIVED
    contact.joinedMemberCount == 1 && contact.myMemberState == Member.MemberState.JOINED -> MEMBER_STATE_INVITE_SENT
    contact.joinedMemberCount == 2 -> MEMBER_STATE_CONNECTED
    else -> MEMBER_STATE_NOT_CONNECTED
}

private fun getOnlineStatus(status: User.ConnectionStatus) = when (status) {
    User.ConnectionStatus.ONLINE -> "ONLINE"
    User.ConnectionStatus.OFFLINE -> "OFFLINE"
    else -> "BUSY"
}