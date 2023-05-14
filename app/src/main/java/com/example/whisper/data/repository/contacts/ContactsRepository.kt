package com.example.whisper.data.repository.contacts

import com.example.whisper.data.local.model.ContactModel
import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import com.example.whisper.utils.responsehandler.ResponseResultOk
import com.sendbird.android.GroupChannel
import com.sendbird.android.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ContactsRepository @Inject constructor(
    private val remote: RemoteSource,
    private val local: LocalSource
) {

    /* --------------------------------------------------------------------------------------------
     * Sources
     ---------------------------------------------------------------------------------------------*/
    interface RemoteSource {

        suspend fun getContacts(
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

        suspend fun getContacts(): Flow<List<ContactModel>>

        suspend fun getContactsInvited(): Flow<List<ContactModel>>

        suspend fun getContactsPending(): Flow<List<ContactModel>>

        suspend fun addContact(contact: ContactModel)

        suspend fun getContact(id: String): ContactModel

        suspend fun deleteContact(contactModel: ContactModel)

        suspend fun acceptContactRequest(contactModel: ContactModel)

        suspend fun declineContactRequest(contactModel: ContactModel)

        suspend fun blockContact(contactModel: ContactModel)

        suspend fun unBlockContact(contactModel: ContactModel)

        suspend fun muteContact(contactModel: ContactModel)

        suspend fun unmuteContact(contactModel: ContactModel)

        suspend fun pinContact(contactUrl: String)

        suspend fun unpinContact(contactUrl: String)
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
        remote.getContacts(filter, block)
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

    suspend fun addContactLocal(contactModel: ContactModel) {
        local.addContact(contactModel)
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

    suspend fun pinContactLocal(contactUrl: String) {
        local.pinContact(contactUrl)
    }

    suspend fun unpinContactLocal(contactId: String) {
        local.unpinContact(contactId)
    }
}