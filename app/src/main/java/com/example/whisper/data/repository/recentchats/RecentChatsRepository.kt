package com.example.whisper.data.repository.recentchats

import com.example.whisper.data.repository.contacts.ContactConnectionStatus
import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import com.example.whisper.utils.responsehandler.ResponseResultOk
import com.sendbird.android.GroupChannel
import com.sendbird.android.User
import javax.inject.Inject

class RecentChatsRepository @Inject constructor(
    private val remote: RemoteSource,
    private val local: LocalSource
) {

    /* --------------------------------------------------------------------------------------------
     * Sources
     ---------------------------------------------------------------------------------------------*/
    interface RemoteSource {

        suspend fun getRecentChats(
            filter: ContactConnectionStatus,
            block: (Either<HttpError, List<GroupChannel>>) -> Unit
        )

        suspend fun searchUsers(
            username: String,
            block: (Either<HttpError, List<User>>) -> Unit
        )

        suspend fun addContact(
            contactId: String,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun getContact(
            id: String,
            block: (Either<HttpError, GroupChannel>) -> Unit
        )

        suspend fun deleteContact(
            id: String,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun acceptContactRequest(
            id: String,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun declineContactRequest(
            id: String,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun blockContact(
            id: String,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun unBlockContact(
            id: String,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun muteContact(
            channelId: String,
            contactId: String,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun unmuteContact(
            channelId: String,
            contactId: String,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun pinContact(
            contactId: String,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )

        suspend fun unpinContact(
            contactId: String,
            block: (Either<HttpError, ResponseResultOk>) -> Unit
        )
    }

    interface LocalSource {


    }

    /* --------------------------------------------------------------------------------------------
     * Exposed
     ---------------------------------------------------------------------------------------------*/
    suspend fun getContact(
        id: String,
        block: (Either<HttpError, GroupChannel>) -> Unit
    ) {
        remote.getContact(id, block)
    }

    suspend fun getContacts(
        filter: ContactConnectionStatus,
        block: (Either<HttpError, List<GroupChannel>>) -> Unit
    ) {
        remote.getRecentChats(filter, block)
    }

    suspend fun searchUsers(
        username: String,
        block: (Either<HttpError, List<User>>) -> Unit
    ) {
        remote.searchUsers(username, block)
    }

    suspend fun addContact(
        contactId: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.addContact(contactId, block)
    }

    suspend fun deleteContact(
        id: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.deleteContact(id, block)
    }

    suspend fun acceptContactRequest(
        id: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.acceptContactRequest(id, block)
    }

    suspend fun declineContactRequest(
        id: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.declineContactRequest(id, block)
    }

    suspend fun blockContact(
        id: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.blockContact(id, block)
    }

    suspend fun unBlockContact(
        id: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.unBlockContact(id, block)
    }

    suspend fun muteContact(
        channelId: String,
        contactId: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.muteContact(channelId, contactId, block)
    }

    suspend fun unmuteContact(
        channelId: String,
        contactId: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.unmuteContact(channelId, contactId, block)
    }

    suspend fun pinContact(
        contactId: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.pinContact(contactId, block)
    }

    suspend fun unpinContact(
        contactId: String,
        block: (Either<HttpError, ResponseResultOk>) -> Unit
    ) {
        remote.unpinContact(contactId, block)
    }
}