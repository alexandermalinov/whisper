package com.example.whisper.vo.recentchats

import com.example.whisper.utils.DateTimeFormatter
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.PINNED_CONTACTS
import com.example.whisper.utils.common.USER_EMAIL
import com.example.whisper.utils.common.ZERO
import com.sendbird.android.GroupChannel
import com.sendbird.android.User
import com.sendbird.android.User.ConnectionStatus

data class RecentChatUiModel(
    val chatUrl: String = EMPTY,
    val contactId: String = EMPTY,
    val username: String = EMPTY,
    val email: String = EMPTY,
    val profilePicture: String = EMPTY,
    val lastMessageText: String = EMPTY,
    val lastMessageTimestamp: String = EMPTY,
    val onlineStatus: OnlineStatus = OnlineStatus.OFFLINE,
    val unreadMessagesCount: Int = ZERO,
    val createdAt: Long = ZERO.toLong(),
    val isMuted: Boolean = false,
    val isPinned: Boolean = false
)

enum class OnlineStatus {
    ONLINE,
    OFFLINE,
    BUSY
}

fun List<GroupChannel>.toListOfRecentChatsUiModel(currentUser: User) =
    map { it.toRecentChatUiModel(currentUser) }

private fun GroupChannel.toRecentChatUiModel(currentUser: User): RecentChatUiModel {
    val contact = getContact(currentUser.userId)
    return RecentChatUiModel(
        chatUrl = url,
        contactId = contact?.userId ?: EMPTY,
        username = contact?.nickname ?: EMPTY,
        email = contact?.metaData?.get(USER_EMAIL) ?: EMPTY,
        profilePicture = contact?.profileUrl ?: EMPTY,
        onlineStatus = getOnlineStatus(contact?.connectionStatus ?: ConnectionStatus.OFFLINE),
        unreadMessagesCount = unreadMessageCount,
        createdAt = lastMessage?.createdAt ?: ZERO.toLong(),
        lastMessageText = lastMessage?.message ?: EMPTY,
        lastMessageTimestamp = DateTimeFormatter.formatMessageDateTime(
            lastMessage?.createdAt ?: ZERO.toLong()
        ),
        isMuted = contact?.isMuted ?: false,
        isPinned = currentUser.isContactPinned(contact?.userId)
    )
}

private fun GroupChannel.getContact(currentUserId: String) =
    members.find { it.userId != currentUserId }

private fun getOnlineStatus(status: ConnectionStatus) = when (status) {
    ConnectionStatus.ONLINE -> OnlineStatus.ONLINE
    ConnectionStatus.OFFLINE -> OnlineStatus.OFFLINE
    else -> OnlineStatus.BUSY
}

private fun User.isContactPinned(contactId: String?) = metaData[PINNED_CONTACTS]
    ?.filterNot { it.isWhitespace() }
    ?.split(',')
    ?.contains(contactId)
    ?: false