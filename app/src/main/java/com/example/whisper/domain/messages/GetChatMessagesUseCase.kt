package com.example.whisper.domain.messages

import com.example.whisper.data.local.model.MessageType
import com.example.whisper.data.repository.messages.MessagesRepository
import com.example.whisper.vo.chat.peertopeer.messages.MessageUiModel
import com.example.whisper.vo.chat.peertopeer.messages.toIncomingFileMessageUiModels
import com.example.whisper.vo.chat.peertopeer.messages.toIncomingMessageUiModels
import com.example.whisper.vo.chat.peertopeer.messages.toIncomingVideoMessageUiModels
import com.example.whisper.vo.chat.peertopeer.messages.toOutgoingFileMessageUiModels
import com.example.whisper.vo.chat.peertopeer.messages.toOutgoingMessageUiModels
import com.example.whisper.vo.chat.peertopeer.messages.toOutgoingVideoMessageUiModels

class GetChatMessagesUseCase(private val messageRepository: MessagesRepository) {

    suspend operator fun invoke(
        loggedUserId: String?,
        contactId: String?,
        getMessages: suspend (List<MessageUiModel>) -> Unit
    ) {
        loggedUserId ?: return
        contactId ?: return

        messageRepository.getMessagesFlow(loggedUserId, contactId)
            .collect { messageModels ->
                messageModels.map { message ->
                    when {
                        message.senderId == loggedUserId && message.type == MessageType.TEXT -> {
                            message.toOutgoingMessageUiModels()
                        }

                        message.senderId != loggedUserId && message.type == MessageType.TEXT -> {
                            message.toIncomingMessageUiModels()
                        }

                        message.senderId == loggedUserId && message.type == MessageType.Video -> {
                            message.toOutgoingVideoMessageUiModels()
                        }

                        message.senderId != loggedUserId && message.type == MessageType.Video -> {
                            message.toIncomingVideoMessageUiModels()
                        }

                        message.senderId == loggedUserId && (message.type == MessageType.FILE ||
                                message.type == MessageType.Photo ||
                                message.type == MessageType.Pdf ||
                                message.type == MessageType.Doc ||
                                message.type == MessageType.Xls ||
                                message.type == MessageType.Pptx) -> {
                            message.toOutgoingFileMessageUiModels()
                        }

                        else -> {
                            message.toIncomingFileMessageUiModels()
                        }
                    }
                }.sortedByDescending { message -> message.createdAt }.let {
                    getMessages(it)
                }
            }
    }
}