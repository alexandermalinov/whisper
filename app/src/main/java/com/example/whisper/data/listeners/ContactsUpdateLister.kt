package com.example.whisper.data.listeners

import com.example.whisper.data.local.model.MessageModel
import com.example.whisper.data.local.model.MessageStatus
import com.example.whisper.data.local.model.MessageType
import com.example.whisper.data.local.model.toContactModel
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.messages.MessagesRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.utils.FileUtils
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.MEMBER_STATE_CONNECTED
import com.example.whisper.utils.common.MEMBER_STATE_INVITE_RECEIVED
import com.example.whisper.utils.common.RECENT_CHAT_HANDLER_ID
import com.sendbird.android.BaseChannel
import com.sendbird.android.BaseMessage
import com.sendbird.android.FileMessage
import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBird
import com.sendbird.android.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ContactsUpdateLister @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val recentChatsRepository: RecentChatsRepository,
    private val messagesRepository: MessagesRepository,
    private val usersRepository: UserRepository
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun initContactUpdateListener() {
        SendBird.addChannelHandler(
            RECENT_CHAT_HANDLER_ID,
            object : SendBird.ChannelHandler() {

                override fun onMessageReceived(channel: BaseChannel?, message: BaseMessage?) {
                    coroutineScope.launch {
                        if (channel == null || message == null) return@launch

                        if (message is FileMessage) {
                            handleIncomingFileMessage(message)
                        } else {
                            handleIncomingTextMessage(message)
                        }

                        messagesRepository.sendMessageDeliveredReceipt(message.sender.userId)

                        val contact = contactsRepository.getContactFromCacheOrDb(channel.url)
                            ?.apply {
                                lastMessage = message.message
                                lastMessageTimestamp = message.createdAt
                                contactsRepository.updateContactDbCache(this)
                            }
                            ?: return@launch

                        recentChatsRepository.getRecentChatFromCacheOrDb(channel.url)
                            ?.apply {
                                lastMessage = message.message
                                lastMessageTimestamp = message.createdAt
                                unreadMessagesCount += 1
                                recentChatsRepository.updateRecentChatDbCache(this)
                            }
                            ?: recentChatsRepository.addRecentChatDbCache(contact)

                    }
                }

                override fun onDeliveryReceiptUpdated(channel: GroupChannel?) {
                    coroutineScope.launch {
                        channel?.let {
                            messagesRepository.updateMessageStatus(
                                messageStatus = MessageStatus.DELIVERED,
                                channel = it,
                                isMyMessage = true
                            )
                        }
                    }
                }

                override fun onReadReceiptUpdated(channel: GroupChannel?) {
                    coroutineScope.launch {
                        channel?.let {
                            messagesRepository.updateMessageStatus(
                                messageStatus = MessageStatus.READ,
                                channel = it,
                                isMyMessage = true
                            )
                        }
                    }
                }

                override fun onChannelDeleted(
                    channelUrl: String?,
                    channelType: BaseChannel.ChannelType?
                ) {
                    super.onChannelDeleted(channelUrl, channelType)

                    if (channelUrl.isNullOrEmpty() || channelType == null) return

                    coroutineScope.launch {
                        contactsRepository.deleteContactDbCache(channelUrl)

                        recentChatsRepository
                            .getRecentChatFromCacheOrDb(channelUrl)
                            ?: return@launch

                        recentChatsRepository.deleteRecentChatDbCache(channelUrl)
                    }
                }

                override fun onUserReceivedInvitation(
                    channel: GroupChannel?,
                    inviter: User?,
                    invitees: MutableList<User>?
                ) {
                    super.onUserReceivedInvitation(channel, inviter, invitees)

                    val isUser = inviter?.userId == usersRepository.cachedUser.userId

                    if (channel == null || inviter == null || isUser) return

                    coroutineScope.launch {
                        val contact = inviter.toContactModel(channel, MEMBER_STATE_INVITE_RECEIVED)
                        contactsRepository.addContactDbCache(contact)
                    }
                }

                override fun onUserJoined(channel: GroupChannel?, user: User?) {
                    super.onUserJoined(channel, user)

                    if (channel == null) return

                    coroutineScope.launch {
                        val contact = contactsRepository
                            .getContactFromCacheOrDb(channel.url)
                            ?: return@launch

                        contact.memberState = MEMBER_STATE_CONNECTED
                        contactsRepository.updateContactDbCache(contact)
                    }
                }

                override fun onUserDeclinedInvitation(
                    channel: GroupChannel?,
                    inviter: User?,
                    invitee: User?
                ) {
                    super.onUserDeclinedInvitation(channel, inviter, invitee)

                    if (channel == null) return

                    coroutineScope.launch {
                        contactsRepository.deleteContactDbCache(channel.url)
                    }
                }

                override fun onChannelFrozen(channel: BaseChannel?) {
                    super.onChannelFrozen(channel)

                    if (channel == null) return

                    coroutineScope.launch {
                        contactsRepository.blockContactDbCache(channel.url)

                        recentChatsRepository
                            .getRecentChatFromCacheOrDb(channel.url)
                            ?: return@launch

                        recentChatsRepository.deleteRecentChatDbCache(channel.url)
                    }
                }

                override fun onUserMuted(channel: BaseChannel?, user: User?) {
                    super.onUserMuted(channel, user)

                    if (channel == null) return

                    coroutineScope.launch {
                        contactsRepository.muteContactLocalDbCache(channel.url)

                        recentChatsRepository
                            .getRecentChatFromCacheOrDb(channel.url)
                            ?: return@launch

                        recentChatsRepository.muteRecentChatDbCache(channel.url)
                    }
                }

                override fun onUserUnmuted(channel: BaseChannel?, user: User?) {
                    super.onUserUnmuted(channel, user)
                    if (channel == null) return
                    coroutineScope.launch {
                        contactsRepository.unMuteContactDbCache(channel.url)

                        recentChatsRepository
                            .getRecentChatFromCacheOrDb(channel.url)
                            ?: return@launch

                        recentChatsRepository.unMuteRecentChatDbCache(channel.url)
                    }
                }
            }
        )
    }

    private suspend fun handleIncomingTextMessage(message: BaseMessage) {
        val messageModel = MessageModel(
            id = message.messageId.toString(),
            senderId = message.sender.userId,
            receiverId = usersRepository.cachedUser.userId,
            body = message.message,
            fileUrl = EMPTY,
            fileName = EMPTY,
            fileSize = EMPTY,
            status = MessageStatus.DELIVERED,
            createdAt = message.createdAt,
            type = MessageType.TEXT
        )

        messagesRepository.addIncomingMessageDbCache(messageModel)
    }

    private suspend fun handleIncomingFileMessage(message: FileMessage) {
        val messageModel = MessageModel(
            id = message.messageId.toString(),
            senderId = message.sender.userId,
            receiverId = usersRepository.cachedUser.userId,
            body = message.message,
            fileUrl = message.url,
            fileName = message.name,
            fileSize = message.size.toString(),
            status = MessageStatus.DELIVERED,
            createdAt = message.createdAt,
            type = messagesRepository.getExtension(message.name)
        )

        messagesRepository.addIncomingMessageDbCache(messageModel)
    }
}