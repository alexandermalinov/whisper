package com.example.whisper.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@kotlinx.serialization.Serializable
@Entity(tableName = "recent_chats")
data class RecentChat(
    @PrimaryKey
    @ColumnInfo(name = "contact_url") val contactUrl: String,
    @ColumnInfo(name = "contact_id") val contactId: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "picture") val picture: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "member_state") val memberState: String,
    @ColumnInfo(name = "online_status") val onlineStatus: String,
    @ColumnInfo(name = "unread_messages_count") val unreadMessagesCount: Int,
    @ColumnInfo(name = "last_message") val lastMessage: String,
    @ColumnInfo(name = "last_message_timestamp") val lastMessageTimestamp: Long,
    @ColumnInfo(name = "is_muted") val isMuted: Boolean,
    @ColumnInfo(name = "is_blocked") val isBlocked: Boolean,
)