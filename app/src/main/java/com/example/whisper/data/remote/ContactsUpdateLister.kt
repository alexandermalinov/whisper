package com.example.whisper.data.remote

import com.example.whisper.data.local.model.toContactModel
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.utils.common.MEMBER_STATE_CONNECTED
import com.example.whisper.utils.common.MEMBER_STATE_INVITE_RECEIVED
import com.example.whisper.utils.common.RECENT_CHAT_HANDLER_ID
import com.sendbird.android.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

class ContactsUpdateLister @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val recentChatsRepository: RecentChatsRepository,
    private val usersRepository: UserRepository
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun initContactUpdateListener() {
        SendBird.addChannelHandler(
            RECENT_CHAT_HANDLER_ID,
            object : SendBird.ChannelHandler() {

                override fun onMessageReceived(channel: BaseChannel?, message: BaseMessage?) {
                    coroutineScope.launch {
                        if (channel == null || message == null) return@launch

                        val contact = contactsRepository.getContactFromCacheOrDb(channel.url)
                            ?: return@launch

                        val recentChat =
                            recentChatsRepository.getRecentChatFromCacheOrDb(channel.url)

                        contact.lastMessage = message.message
                        contact.lastMessageTimestamp = message.createdAt
                        contactsRepository.updateContactDbCache(contact)

                        recentChat?.lastMessage = message.message
                        recentChat?.lastMessageTimestamp = message.createdAt

                        if (recentChat != null) {
                            recentChatsRepository.updateRecentChatDbCache(recentChat)
                        } else {
                            recentChatsRepository.addRecentChatDbCache(contact)
                        }
                    }
                }

                override fun onChannelDeleted(
                    channelUrl: String?,
                    channelType: BaseChannel.ChannelType?
                ) {
                    super.onChannelDeleted(channelUrl, channelType)

                    if (channelUrl == null || channelUrl.isEmpty() || channelType == null) return

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
            })
    }
}