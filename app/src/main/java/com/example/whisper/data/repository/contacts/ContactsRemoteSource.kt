package com.example.whisper.data.repository.contacts

import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import com.sendbird.android.GroupChannel
import com.sendbird.android.GroupChannelListQuery
import com.sendbird.android.SendBird
import com.sendbird.android.User


class ContactsRemoteSource : ContactsRepository.RemoteSource {

    private fun listQuery(filter: ConnectionStatus) = GroupChannel.createMyGroupChannelListQuery()
        .apply {
            isIncludeEmpty = true
            isIncludeMetadata = true
            memberStateFilter = getConnectionStatus(filter)
            order = GroupChannelListQuery.Order.LATEST_LAST_MESSAGE
            limit = PAGING_LIMIT
        }

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

    override suspend fun getNotConnectedUsers(block: (Either<HttpError, List<User>>) -> Unit) {
        // SendBird.createFriendListQuery().next()
        // SendBird.addFriends()

        listQuery(ConnectionStatus.ALL).next { channels, sendBirdException ->
            if (sendBirdException != null) {
                block.invoke(Either.left(HttpError(serverMessage = sendBirdException.message)))
                return@next
            }
            val connectedUsersIds = channels.map { channel ->
                channel.members
                    .first { member -> member.userId != SendBird.getCurrentUser().userId }
                    .userId
            }

            SendBird.createApplicationUserListQuery().next { allUsers, sendBirdException ->
                if (sendBirdException == null) {
                    allUsers.filter { user -> connectedUsersIds.none { it == user.userId } }.let {
                        block.invoke(Either.right(it))
                    }
                } else {
                    block.invoke(Either.left(HttpError(serverMessage = sendBirdException.message)))
                }
            }
        }
    }

    companion object {
        const val PAGING_LIMIT = 15
    }
}