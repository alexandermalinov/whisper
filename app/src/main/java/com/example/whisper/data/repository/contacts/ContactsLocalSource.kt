package com.example.whisper.data.repository.contacts

import com.example.whisper.data.local.dao.ContactDao
import com.example.whisper.data.local.entity.toContact
import com.example.whisper.data.local.entity.toContactModels
import com.example.whisper.data.local.model.ContactModel
import com.example.whisper.data.local.model.toContact
import com.example.whisper.data.local.model.toContacts
import com.example.whisper.utils.common.MEMBER_STATE_CONNECTED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ContactsLocalSource @Inject constructor(
    private val contactDao: ContactDao
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

    override suspend fun addAllContacts(contacts: List<ContactModel>) {
        // TODO WHISPER-6 Add check if the contact should be updated or added, so the isPinned value
        // is not lost + more optimised code
        contactDao.deleteAllContacts()
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

    override suspend fun pinContact(contactModel: ContactModel) {
        val contact = contactModel.copy(isPinned = true).toContact()
        contactDao.updateContact(contact)
    }

    override suspend fun unpinContact(contactModel: ContactModel) {
        val contact = contactModel.copy(isPinned = false).toContact()
        contactDao.updateContact(contact)
    }
}