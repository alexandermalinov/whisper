package com.example.whisper.data.repository.recentchats

import com.example.whisper.utils.common.PINNED_CONTACTS
import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import com.example.whisper.utils.responsehandler.ResponseResultOk
import com.sendbird.android.GroupChannel
import com.sendbird.android.Member
import com.sendbird.android.SendBird
import javax.inject.Inject


class RecentChatsRemoteSource @Inject constructor() : RecentChatsRepository.RemoteSource {

    /* --------------------------------------------------------------------------------------------
     * Override
     ---------------------------------------------------------------------------------------------*/

    override suspend fun deleteRecentChat(
        id: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        GroupChannel.getChannel(id) { channel, sendBirdException ->
            if (sendBirdException != null) return@getChannel

            channel.delete { e ->
                if (e != null) {
                    block.invoke(Either.left(HttpError(serverMessage = e.message)))
                } else {
                    block.invoke(Either.right(ResponseResultOk))
                }
            }
        }
    }

    override suspend fun muteRecentChat(
        channelId: String,
        contactId: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        GroupChannel.getChannel(channelId) { channel, e ->
            if (e != null) block.invoke(Either.left(HttpError(serverMessage = e.message)))

            if (channel.myRole == Member.Role.OPERATOR) {
                channel.muteUserWithUserId(contactId) { exception ->
                    if (exception != null) {
                        block.invoke(Either.left(HttpError(serverMessage = exception.message)))
                    } else {
                        block.invoke(Either.right(ResponseResultOk))
                    }
                }
            }
        }
    }

    override suspend fun unmuteRecentChat(
        channelId: String,
        contactId: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        GroupChannel.getChannel(channelId) { channel, e ->
            if (e != null) {
                block.invoke(Either.left(HttpError(serverMessage = e.message)))
            }

            if (channel.myRole == Member.Role.OPERATOR) {
                channel.unmuteUserWithUserId(contactId) { exception ->
                    if (exception != null) {
                        block.invoke(Either.left(HttpError(serverMessage = exception.message)))
                    } else {
                        block.invoke(Either.right(ResponseResultOk))
                    }
                }
            }
        }
    }

    override suspend fun pinRecentChat(
        contactId: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        val currentUser = SendBird.getCurrentUser()
        val pinnedContacts = currentUser.metaData[PINNED_CONTACTS]
            ?.split(',')
            ?.plus(contactId)
            ?.joinToString()
            ?: contactId
        currentUser.metaData[PINNED_CONTACTS] = pinnedContacts
        currentUser.updateMetaData(currentUser.metaData) { metaDataMap, e ->
            if (e != null) {
                block.invoke(Either.left(HttpError(serverMessage = e.message)))
            } else {
                block.invoke(Either.right(ResponseResultOk))
            }
        }
    }

    override suspend fun unpinRecentChat(
        contactId: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        val currentUser = SendBird.getCurrentUser()
        val pinnedContacts = currentUser.metaData[PINNED_CONTACTS]
            ?.filterNot { it.isWhitespace() }
            ?.split(',')
            ?.minus(contactId)
            ?.joinToString()
            ?: return
        currentUser.metaData[PINNED_CONTACTS] = pinnedContacts
        currentUser.updateMetaData(currentUser.metaData) { metaDataMap, e ->
            if (e != null) {
                block.invoke(Either.left(HttpError(serverMessage = e.message)))
            } else {
                block.invoke(Either.right(ResponseResultOk))
            }
        }
    }

    companion object {
        const val PAGING_LIMIT = 15
    }
}