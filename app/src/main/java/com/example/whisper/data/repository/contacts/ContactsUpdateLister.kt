package com.example.whisper.data.repository.contacts

import com.example.whisper.data.local.model.toContactModel
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.MEMBER_STATE_CONNECTED
import com.example.whisper.utils.common.MEMBER_STATE_INVITE_RECEIVED
import com.example.whisper.utils.common.RECENT_CHAT_HANDLER_ID
import com.sendbird.android.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ContactsUpdateLister(
    private val contactsRepository: ContactsRepository,
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

                        val contact = contactsRepository.cachedContacts
                            .find { it.contactUrl == channel.url }
                            ?: contactsRepository.getContactDb(channel.url ?: EMPTY)
                            ?: return@launch

                        contact.lastMessage = message.message
                        contact.lastMessageTimestamp = message.createdAt
                        contactsRepository.updateContactDbCache(contact)
                    }
                }

                override fun onChannelDeleted(
                    channelUrl: String?,
                    channelType: BaseChannel.ChannelType?
                ) {
                    super.onChannelDeleted(channelUrl, channelType)

                    if (channelUrl == null || channelUrl.isEmpty() || channelType == null) return

                    coroutineScope.launch {
                        val contact = contactsRepository.cachedContacts
                            .find { it.contactUrl == channelUrl }
                            ?: contactsRepository.getContactDb(channelUrl)
                            ?: return@launch

                        contactsRepository.deleteContactDbCache(contact.contactUrl)
                    }
                }

                override fun onUserReceivedInvitation(
                    channel: GroupChannel?,
                    inviter: User?,
                    invitees: MutableList<User>?
                ) {
                    super.onUserReceivedInvitation(channel, inviter, invitees)

                    coroutineScope.launch {
                        if (channel == null ||
                            inviter == null ||
                            inviter.userId == usersRepository.cachedUser.userId
                        ) return@launch

                        val contact = inviter.toContactModel(channel, MEMBER_STATE_INVITE_RECEIVED)
                        contactsRepository.addContactDbCache(contact)
                    }
                }

                override fun onUserJoined(channel: GroupChannel?, user: User?) {
                    super.onUserJoined(channel, user)

                    coroutineScope.launch {
                        val contact = contactsRepository.cachedContacts
                            .find { it.contactUrl == channel?.url }
                            ?: contactsRepository.getContactDb(channel?.url ?: EMPTY)
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

                    coroutineScope.launch {
                        val contact = contactsRepository.cachedContacts
                            .find { it.contactUrl == channel?.url }
                            ?: contactsRepository.getContactDb(channel?.url ?: EMPTY)
                            ?: return@launch

                        contactsRepository.deleteContactDbCache(contact.contactUrl)
                    }
                }

                override fun onChannelFrozen(channel: BaseChannel?) {
                    super.onChannelFrozen(channel)

                    coroutineScope.launch {
                        val contact = contactsRepository.cachedContacts
                            .find { it.contactUrl == channel?.url }
                            ?: contactsRepository.getContactDb(channel?.url ?: EMPTY)
                            ?: return@launch

                        contactsRepository.blockContactDbCache(contact.contactUrl)
                    }
                }

                override fun onUserMuted(channel: BaseChannel?, user: User?) {
                    super.onUserMuted(channel, user)

                    coroutineScope.launch {
                        val contact = contactsRepository.cachedContacts
                            .find { it.contactId == user?.userId }
                            ?: contactsRepository.getContactDb(channel?.url ?: EMPTY)
                            ?: return@launch

                        contactsRepository.muteContactLocalDbCache(contact.contactUrl)
                    }
                }

                override fun onUserUnmuted(channel: BaseChannel?, user: User?) {
                    super.onUserUnmuted(channel, user)
                    coroutineScope.launch {
                        val contact = contactsRepository.cachedContacts
                            .find { it.contactId == user?.userId }
                            ?: contactsRepository.getContactDb(channel?.url ?: EMPTY)
                            ?: return@launch

                        contactsRepository.unMuteContactDbCache(contact.contactUrl)
                    }
                }
            })
    }
}