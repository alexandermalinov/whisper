package com.example.whisper.data.repository.contacts

import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import com.example.whisper.utils.responsehandler.ResponseResultOk
import com.sendbird.android.*


class ContactsRemoteSource : ContactsRepository.RemoteSource {
    private fun getConnectionStatus(filter: ConnectionStatus) = when (filter) {
        ConnectionStatus.CONNECTED -> GroupChannelListQuery.MemberStateFilter.ALL
        ConnectionStatus.INVITE_RECEIVED -> GroupChannelListQuery.MemberStateFilter.INVITED
        else -> GroupChannelListQuery.MemberStateFilter.ALL
    }

    /* --------------------------------------------------------------------------------------------
     * Override
     ---------------------------------------------------------------------------------------------*/
    override suspend fun getContacts(
        filter: ConnectionStatus,
        block: (Either<HttpError, List<GroupChannel>>) -> Unit
    ) {
        listQuery(filter).next { channels, error ->
            if (channels != null) {
                block.invoke(Either.right(channels))
            } else {
                block.invoke(Either.left(HttpError(serverMessage = error.message)))
            }
        }
    }

    override suspend fun searchUsers(
        username: String,
        block: (Either<HttpError, List<User>>) -> Unit
    ) {
        listQuery(ConnectionStatus.ALL).next { channels, sendBirdException ->
            if (sendBirdException != null) {
                block.invoke(Either.left(HttpError(serverMessage = sendBirdException.message)))
                return@next
            }
            val connectedUsersIds = channels.map { channel ->
                channel.members
                    .firstOrNull { member -> member.userId != SendBird.getCurrentUser().userId }
                    ?.userId
            }

            searchQuery(username).next { allUsers, exception ->
                if (exception == null) {
                    allUsers
                        .filter { user -> connectedUsersIds.none { it == user.userId } }
                        .let {
                            block.invoke(Either.right(it))
                        }
                } else {
                    block.invoke(Either.left(HttpError(serverMessage = exception.message)))
                }
            }
        }
    }

    override suspend fun addContact(
        id: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        GroupChannel.createChannel(createChannelParams(listOf(id))) { channel, exception ->
            if (exception != null) {
                block.invoke(Either.left(HttpError(serverMessage = exception.message)))
                return@createChannel
            }
            SendBird.setChannelInvitationPreference(false) {
                channel.inviteWithUserIds(listOf(id)) { sendBirdException ->
                    if (sendBirdException != null) {
                        block.invoke(Either.left(HttpError(serverMessage = sendBirdException.message)))
                    } else {
                        block.invoke(Either.right(ResponseResultOk))
                    }
                }
            }
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Private
    ----------------------------------------------------------------------------------------------*/
    private fun createMessagesQuery(unreadMessageCount: Int) = MessageListParams().apply {
        previousResultSize = 100
        nextResultSize = unreadMessageCount
        isInclusive = true
        setIncludeReactions(true)
        setReverse(true)
    }

    private fun createChannelParams(users: List<String>) = GroupChannelParams().apply {
        setPublic(false)
        setEphemeral(false)
        setDistinct(true)
        setSuper(false)
        addUserIds(users)
    }

    private fun listQuery(filter: ConnectionStatus) = GroupChannel.createMyGroupChannelListQuery()
        .apply {
            isIncludeEmpty = true
            isIncludeMetadata = true
            memberStateFilter = getConnectionStatus(filter)
            order = GroupChannelListQuery.Order.LATEST_LAST_MESSAGE
            limit = PAGING_LIMIT
        }

    private fun searchQuery(username: String) = SendBird.createApplicationUserListQuery().apply {
        setNicknameStartsWithFilter(username)
    }

    companion object {
        const val PAGING_LIMIT = 15
    }
}