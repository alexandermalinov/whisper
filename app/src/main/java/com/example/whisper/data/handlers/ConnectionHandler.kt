package com.example.whisper.data.handlers

import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.messages.MessagesRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.domain.contact.PopulateContactsUseCase
import com.example.whisper.domain.contact.SyncMessagesUseCase
import com.example.whisper.utils.common.EMPTY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConnectionHandler @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val recentChatsRepository: RecentChatsRepository,
    private val userRepository: UserRepository,
    private val messagesRepository: MessagesRepository
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun initConnectionHandler() {
        coroutineScope.launch {
            val currentUser = userRepository.getLoggedUser()

            if (currentUser == null) initConnectionHandler()

            userRepository.connectUserSendbird(currentUser?.userId ?: EMPTY).foldSuspend({
                //initConnectionHandler()
            }, { user ->
                val userModel = userRepository.cachedUser.copy(
                    username = user.nickname,
                    profilePicture = user.profileUrl
                )

                userRepository.updateUserLocalDB(userModel)

                PopulateContactsUseCase(
                    contactsRepository = contactsRepository,
                    recentChatsRepository = recentChatsRepository
                ).invoke(userRepository.cachedUser.userId)

                SyncMessagesUseCase(
                    coroutineScope = coroutineScope,
                    contactsRepository = contactsRepository,
                    messagesRepository = messagesRepository,
                    recentChatsRepository = recentChatsRepository
                ).invoke(userModel.userId)
            })
        }
    }
}