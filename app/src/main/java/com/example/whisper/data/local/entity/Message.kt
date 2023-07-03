package com.example.whisper.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.whisper.data.local.model.MessageModel
import com.example.whisper.data.local.model.MessageStatus
import com.example.whisper.data.local.model.MessageType

@kotlinx.serialization.Serializable
@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "sender_id") val senderId: String,
    @ColumnInfo(name = "receiver_id") val receiverId: String,
    @ColumnInfo(name = "body") val body: String,
    @ColumnInfo(name = "file_url") val fileUrl: String,
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "file_size") val fileSize: String,
    @ColumnInfo(name = "status") val status: MessageStatus,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "type") val type: MessageType,
)

fun List<Message>.toMessageModels() = map { it.toMessageModel() }

fun Message.toMessageModel() = MessageModel(
    id = id,
    senderId = senderId,
    receiverId = receiverId,
    body = body,
    fileUrl = fileUrl,
    fileName = fileName,
    fileSize = fileSize,
    status = status,
    createdAt = createdAt,
    type = type
)