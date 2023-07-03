package com.example.whisper.data.repository.messages

import com.example.whisper.data.local.dao.MessageDao
import com.example.whisper.data.local.entity.toMessageModel
import com.example.whisper.data.local.entity.toMessageModels
import com.example.whisper.data.local.model.MessageModel
import com.example.whisper.data.local.model.toMessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MessagesLocalSource @Inject constructor(
    private val messageDao: MessageDao
) : MessagesRepository.LocalSource {

    override suspend fun updateMessage(message: MessageModel) {
        messageDao.updateMessage(message.toMessageEntity())
    }

    override fun getMessagesFlow(
        currentUserId: String,
        contactId: String
    ): Flow<List<MessageModel>> = messageDao
        .getMessagesWithContactFlow(currentUserId, contactId)
        .map { it.toMessageModels() }

    override fun getContactMessages(currentUserId: String, contactId: String): List<MessageModel> =
        messageDao
            .getContactMessages(currentUserId, contactId)
            .map { it.toMessageModel() }

    override suspend fun getMessages(): List<MessageModel> = messageDao
        .getMessages()
        .toMessageModels()

    override suspend fun addMessage(message: MessageModel) {
        messageDao.insertMessage(message.toMessageEntity())
    }

    override suspend fun getMessage(id: String): MessageModel? = messageDao
        .getMessage(id)
        ?.toMessageModel()

    override suspend fun deleteMessage(message: MessageModel) {
        messageDao.deleteMessage(message = message.toMessageEntity())
    }
}