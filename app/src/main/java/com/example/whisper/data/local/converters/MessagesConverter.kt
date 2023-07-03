package com.example.whisper.data.local.converters

import androidx.room.TypeConverter
import com.example.whisper.data.local.model.MessageStatus
import com.example.whisper.data.local.model.MessageType

class MessagesConverter {

    @TypeConverter
    fun fromMessageStatus(status: MessageStatus): String = status.name

    @TypeConverter
    fun toMessageStatus(status: String): MessageStatus = MessageStatus.valueOf(status)

    @TypeConverter
    fun fromMessageType(type: MessageType): String = type.name

    @TypeConverter
    fun toMessageType(type: String): MessageType = MessageType.valueOf(type)
}