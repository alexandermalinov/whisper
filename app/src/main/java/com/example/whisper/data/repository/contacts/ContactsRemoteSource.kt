package com.example.whisper.data.repository.contacts

import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import com.example.whisper.utils.responsehandler.ResponseResultOk
import com.sendbird.android.*


class ContactsRemoteSource : ContactsRepository.RemoteSource {
    private fun getConnectionStatus(filter: ContactConnectionStatus) = when (filter) {
        ContactConnectionStatus.CONNECTED -> GroupChannelListQuery.MemberStateFilter.ALL
        ContactConnectionStatus.INVITE_RECEIVED -> GroupChannelListQuery.MemberStateFilter.INVITED
        else -> GroupChannelListQuery.MemberStateFilter.ALL
    }

    /* --------------------------------------------------------------------------------------------
     * Override
     ---------------------------------------------------------------------------------------------*/
    override suspend fun getContact(id: String, block: (Either<HttpError, GroupChannel>) -> Unit) {
        GroupChannel.getChannel(id) { channel, sendBirdException ->
            if (sendBirdException == null)
                block.invoke(Either.right(channel))
            else
                block.invoke(Either.left(HttpError(serverMessage = sendBirdException.message)))
        }
    }

    override suspend fun deleteContact(
        id: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        getContact(id) { either ->
            either.fold({ httpError ->
                block.invoke(Either.left(httpError))
            }, { contact ->
                contact.delete { e ->
                    if (e != null) {
                        block.invoke(Either.left(HttpError(serverMessage = e.message)))
                    } else {
                        block.invoke(Either.right(ResponseResultOk))
                    }
                }
            })
        }
    }

    override suspend fun getContacts(
        filter: ContactConnectionStatus,
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
        listQuery(ContactConnectionStatus.ALL).next { channels, sendBirdException ->
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
        contactId: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        val usersIds = listOf(SendBird.getCurrentUser().userId, contactId)

        GroupChannel.createChannel(createChannelParams(usersIds)) { channel, exception ->
            if (exception != null) {
                block.invoke(Either.left(HttpError(serverMessage = exception.message)))
                return@createChannel
            }
            SendBird.setChannelInvitationPreference(false) {
                channel.inviteWithUserIds(listOf(contactId)) { sendBirdException ->
                    if (sendBirdException != null) {
                        block.invoke(Either.left(HttpError(serverMessage = sendBirdException.message)))
                    } else {
                        block.invoke(Either.right(ResponseResultOk))
                    }
                }
            }
        }
    }

    override suspend fun acceptContactRequest(
        id: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        getContact(id) { either ->
            either.fold({ httpError ->
                block.invoke(Either.left(httpError))
            }, { contact ->
                contact.acceptInvitation { e ->
                    if (e != null) {
                        block.invoke(Either.left(HttpError(serverMessage = e.message)))
                    } else {
                        block.invoke(Either.right(ResponseResultOk))
                    }
                }
            })
        }
    }

    override suspend fun declineContactRequest(
        id: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        getContact(id) { either ->
            either.fold({ httpError ->
                block.invoke(Either.left(httpError))
            }, { contact ->
                contact.declineInvitation { e ->
                    if (e != null) {
                        block.invoke(Either.left(HttpError(serverMessage = e.message)))
                    } else {
                        block.invoke(Either.right(ResponseResultOk))
                    }
                }
            })
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
        setOperatorUserIds(users)
    }

    private fun listQuery(filter: ContactConnectionStatus) =
        GroupChannel.createMyGroupChannelListQuery()
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