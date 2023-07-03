package com.example.whisper.data.local.model

import com.example.whisper.data.local.entity.Message
import kotlinx.serialization.Serializable

@Serializable
data class MessageModel(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val body: String,
    val fileUrl: String,
    val fileName: String,
    val fileSize: String,
    val status: MessageStatus,
    val createdAt: Long,
    val type: MessageType
) : java.io.Serializable

fun MessageModel.toMessageEntity() = Message(
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