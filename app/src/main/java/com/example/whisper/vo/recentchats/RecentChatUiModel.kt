package com.example.whisper.vo.recentchats

import com.example.whisper.utils.DateTimeFormatter
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.ZERO
import com.sendbird.android.GroupChannel
import com.sendbird.android.User.ConnectionStatus

data class RecentChatUiModel(
    val id: String = EMPTY,
    val username: String = EMPTY,
    val profilePicture: String = EMPTY,
    val lastMessageText: String = EMPTY,
    val lastMessageTimestamp: String = EMPTY,
    val onlineStatus: OnlineStatus = OnlineStatus.OFFLINE,
    val unreadMessagesCount: Int = ZERO,
    val createdAt: Long = ZERO.toLong()
)

enum class OnlineStatus {
    ONLINE,
    OFFLINE,
    BUSY
}

fun List<GroupChannel>.toListOfRecentChatsUiModel(loggedUserId: String) =
    map { it.toRecentChatUiModel(loggedUserId) }

private fun GroupChannel.toRecentChatUiModel(loggedUserId: String): RecentChatUiModel {
    val contact = getContact(loggedUserId)
    return RecentChatUiModel(
        id = url,
        username = contact?.nickname ?: EMPTY,
        profilePicture = contact?.profileUrl ?: EMPTY,
        onlineStatus = getOnlineStatus(contact?.connectionStatus ?: ConnectionStatus.OFFLINE),
        unreadMessagesCount = unreadMessageCount,
        createdAt = lastMessage?.createdAt ?: ZERO.toLong(),
        lastMessageText = lastMessage?.message ?: EMPTY,
        lastMessageTimestamp = DateTimeFormatter.formatMessageDateTime(
            lastMessage?.createdAt ?: ZERO.toLong()
        ),
    )
}

private fun GroupChannel.getContact(currentUserId: String) =
    members.find { it.userId != currentUserId }

private fun getOnlineStatus(status: ConnectionStatus) = when (status) {
    ConnectionStatus.ONLINE -> OnlineStatus.ONLINE
    ConnectionStatus.OFFLINE -> OnlineStatus.OFFLINE
    else -> OnlineStatus.BUSY
}