package com.example.whisper.data.repository.contacts

import com.example.whisper.data.local.dao.ContactDao
import com.example.whisper.data.local.dao.UserDao
import com.example.whisper.data.local.entity.User
import com.example.whisper.data.local.entity.toContact
import com.example.whisper.data.local.entity.toContactModels
import com.example.whisper.data.local.model.ContactModel
import com.example.whisper.data.local.model.UserModel
import com.example.whisper.data.local.model.toContact
import com.example.whisper.data.local.model.toUser
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.utils.common.EMPTY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ContactsLocalSource @Inject constructor(
    private val contactDao: ContactDao,
    private val userDao: UserDao,
    private var userRepository: UserRepository
) : ContactsRepository.LocalSource {

    override suspend fun getContacts(): Flow<List<ContactModel>> =
        contactDao.getContacts().map { it.toContactModels() }

    override suspend fun getContactsInvited(): Flow<List<ContactModel>> =
        contactDao.getInvitedContacts().map { it.toContactModels() }

    override suspend fun getContactsPending(): Flow<List<ContactModel>> =
        contactDao.getPendingContacts().map { it.toContactModels() }

    override suspend fun addContact(contact: ContactModel) {
        contactDao.insertContact(contact.toContact())
    }

    override suspend fun getContact(url: String): ContactModel =
        contactDao.getContact(url).toContact()

    override suspend fun deleteContact(contactModel: ContactModel) {
        contactDao.deleteContact(contactModel.toContact())
    }

    override suspend fun acceptContactRequest(contactModel: ContactModel) {
        val contact = contactModel.copy(memberState = "JOINED").toContact()
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
        if (userRepository.cachedUser == null) return

        val contact = contactDao.getContact(contactUrl).toContact()
        userRepository.cachedUser = userRepository.cachedUser?.copy(
            pinnedContacts = userRepository.cachedUser
                ?.pinnedContacts
                ?.plus(contact)
                ?: emptyList()
        )
        userDao.updateUser(userRepository.cachedUser?.toUser() ?: User())
    }

    override suspend fun unpinContact(contactUrl: String) {
        if (userRepository.cachedUser == null) return

        userRepository.cachedUser = userRepository.cachedUser?.copy(
            pinnedContacts = userRepository.cachedUser
                ?.pinnedContacts
                ?.filter { it.contactUrl != contactUrl }
                ?: emptyList()
        )
        userDao.updateUser(userRepository.cachedUser?.toUser() ?: User())
    }
}