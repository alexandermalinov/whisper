package com.example.whisper.data.repository.contacts

import com.example.whisper.data.local.model.ContactModel
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.utils.common.MEMBER_STATE_CONNECTED
import com.example.whisper.utils.responsehandler.Either
import com.example.whisper.utils.responsehandler.HttpError
import com.example.whisper.utils.responsehandler.ResponseResultOk
import com.sendbird.android.GroupChannel
import com.sendbird.android.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ContactsRepository @Inject constructor(
    private val remote: RemoteSource,
    private val local: LocalSource,
    private val usersRepository: UserRepository,
    var cachedContacts: List<ContactModel>
) {

    lateinit var contactsUpdateLister: ContactsUpdateLister
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        coroutineScope.launch {
            cachedContacts = local.getAllContactDb()
            contactsUpdateLister = ContactsUpdateLister(this@ContactsRepository, usersRepository)
            contactsUpdateLister.initContactUpdateListener()
        }
    }

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
            block: (Either<HttpError, GroupChannel>) -> Unit
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

        suspend fun updateContact(contact: ContactModel)

        fun getContactsFlow(): Flow<List<ContactModel>>

        suspend fun getAllContactDb(): List<ContactModel>

        fun getContactsInvited(): Flow<List<ContactModel>>

        fun getContactsPending(): Flow<List<ContactModel>>

        suspend fun addContact(contact: ContactModel)

        suspend fun addContacts(contacts: List<ContactModel>)

        suspend fun getContactDb(contactUrl: String): ContactModel?

        suspend fun deleteContact(contactModel: ContactModel)

        suspend fun deleteAllContacts()

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

    fun getContactsDbFlow() = local.getContactsFlow()

    suspend fun searchUsers(
        username: String,
        block: (Either<HttpError, List<User>>) -> Unit
    ) {
        remote.searchUsers(username, block)
    }

    suspend fun addContact(
        contactId: String,
        block: (Either<HttpError, GroupChannel>) -> Unit
    ) {
        remote.addContact(contactId, block)
    }

    suspend fun addContactDbCache(contactModel: ContactModel) {
        local.addContact(contactModel)
        cachedContacts.plus(contactModel)
    }

    suspend fun addAllContactsDbCache(contactsModels: List<ContactModel>) {
        local.addContacts(contactsModels)
        cachedContacts = contactsModels
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

    suspend fun updateContactDbCache(contactModel: ContactModel) {
        local.updateContact(contactModel)
        cachedContacts = cachedContacts.map { contact ->
            if (contact.contactUrl == contactModel.contactUrl) {
                contactModel
            } else {
                contact
            }
        }
    }

    suspend fun deleteContactDbCache(contactUrl: String) {
        val contact = getContactFromCacheOrDb(contactUrl)
        if (contact != null) local.deleteContact(contact)
        cachedContacts.minus(contact)
    }

    suspend fun acceptContactRequestDbCache(contactUrl: String) {
        val contact = getContactFromCacheOrDb(contactUrl)
        contact?.memberState = MEMBER_STATE_CONNECTED
        if (contact != null) local.acceptContactRequest(contact)
        cachedContacts.find { it.contactId == contactUrl }?.memberState = MEMBER_STATE_CONNECTED
    }

    suspend fun declineContactRequestDbCache(contactUrl: String) {
        val contact = getContactFromCacheOrDb(contactUrl)
        if (contact != null) local.declineContactRequest(contact)
        cachedContacts.minus(contact)
    }

    suspend fun blockContactDbCache(contactUrl: String) {
        val contact = getContactFromCacheOrDb(contactUrl)
        if (contact != null) local.blockContact(contact)
        cachedContacts.find { it.contactId == contactUrl }?.isBlocked = true
    }

    suspend fun unblockContactDbCache(contactUrl: String) {
        val contact = getContactFromCacheOrDb(contactUrl)
        if (contact != null) local.unBlockContact(contact)
        cachedContacts.find { it.contactUrl == contactUrl }?.isBlocked = false
    }

    suspend fun muteContactLocalDbCache(contactUrl: String) {
        val contact = getContactFromCacheOrDb(contactUrl)
        if (contact != null) local.muteContact(contact)
        cachedContacts.find { it.contactId == contactUrl }?.isMuted = true
    }

    suspend fun unMuteContactDbCache(contactUrl: String) {
        val contact = getContactFromCacheOrDb(contactUrl)
        if (contact != null) local.muteContact(contact)
        cachedContacts.find { it.contactId == contactUrl }?.isMuted = false
    }

    suspend fun pinContactDbCache(contactUrl: String) {
        local.pinContact(contactUrl)
    }

    suspend fun unpinContactDbCache(contactId: String) {
        local.unpinContact(contactId)
    }

    suspend fun deleteAllContactDbCache() {
        local.deleteAllContacts()
    }

    suspend fun getContactDb(contactUrl: String) = local.getContactDb(contactUrl)

    private suspend fun getContactFromCacheOrDb(contactUrl: String) = cachedContacts
        .find { it.contactUrl == contactUrl }
        ?: local.getContactDb(contactUrl)
}