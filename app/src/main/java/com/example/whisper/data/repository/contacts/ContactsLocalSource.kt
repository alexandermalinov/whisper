package com.example.whisper.data.repository.contacts

import com.example.whisper.data.local.dao.ContactDao
import com.example.whisper.data.local.dao.UserDao
import com.example.whisper.data.local.entity.toContact
import com.example.whisper.data.local.entity.toContactModels
import com.example.whisper.data.local.model.*
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.utils.common.MEMBER_STATE_CONNECTED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ContactsLocalSource @Inject constructor(
    private val contactDao: ContactDao,
    private val userDao: UserDao,
    private var userRepository: UserRepository
) : ContactsRepository.LocalSource {

    override suspend fun updateContact(contact: ContactModel) {
        contactDao.updateContact(contact.toContact())
    }

    override fun getContactsFlow(): Flow<List<ContactModel>> =
        contactDao.getContactsFlow().map { it.toContactModels() }

    override suspend fun getAllContactDb(): List<ContactModel> =
        contactDao.getContacts().toContactModels()

    override fun getContactsInvited(): Flow<List<ContactModel>> =
        contactDao.getInvitedContacts().map { it.toContactModels() }

    override fun getContactsPending(): Flow<List<ContactModel>> =
        contactDao.getPendingContacts().map { it.toContactModels() }

    override suspend fun addContact(contact: ContactModel) {
        contactDao.insertContact(contact.toContact())
    }

    override suspend fun addContacts(contacts: List<ContactModel>) {
        contactDao.insertContacts(contacts.toContacts())
    }

    override suspend fun getContactDb(contactUrl: String): ContactModel? =
        contactDao.getContact(contactUrl)?.toContact()

    override suspend fun deleteContact(contactModel: ContactModel) {
        contactDao.deleteContact(contactModel.toContact())
    }

    override suspend fun deleteAllContacts() {
        contactDao.deleteAllContacts()
    }

    override suspend fun acceptContactRequest(contactModel: ContactModel) {
        val contact = contactModel.copy(memberState = MEMBER_STATE_CONNECTED).toContact()
        contactDao.updateContact(contact)
    }

    override suspend fun declineContactRequest(contactModel: ContactModel) {
        contactDao.deleteContact(contactModel.toContact())
    }

    override suspend fun blockContact(contactModel: ContactModel) {
        val contact = contactModel.copy(isBlocked = true).toContact()
        contactDao.updateContact(contact)
    }

    override suspend fun unBlockContact(contactModel: ContactModel) {
        val contact = contactModel.copy(isBlocked = false).toContact()
        contactDao.updateContact(contact)

    }

    override suspend fun muteContact(contactModel: ContactModel) {
        val contact = contactModel.copy(isMuted = true).toContact()
        contactDao.updateContact(contact)
    }

    override suspend fun unmuteContact(contactModel: ContactModel) {
        val contact = contactModel.copy(isMuted = false).toContact()
        contactDao.updateContact(contact)
    }

    override suspend fun pinContact(contactUrl: String) {
        val contact = contactDao.getContact(contactUrl)?.toContact() ?: return
        userRepository.cachedUser = userRepository.cachedUser.copy(
            pinnedContacts = userRepository.cachedUser
                .pinnedContacts
                .plus(contact)
        )
        userDao.updateUser(userRepository.cachedUser.toUser())
    }

    override suspend fun unpinContact(contactUrl: String) {
        userRepository.cachedUser = userRepository.cachedUser.copy(
            pinnedContacts = userRepository.cachedUser
                .pinnedContacts
                .filter { it.contactUrl != contactUrl }
        )
        userDao.updateUser(userRepository.cachedUser.toUser())
    }
}