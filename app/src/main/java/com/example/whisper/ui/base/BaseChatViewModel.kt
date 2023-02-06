package com.example.whisper.ui.base

import androidx.lifecycle.viewModelScope
import com.example.whisper.data.repository.contacts.ConnectionStatus
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.vo.contacts.ContactUiModel
import com.example.whisper.vo.contacts.toContactsUiModel
import com.sendbird.android.SendBird
import com.sendbird.android.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseChatViewModel(
    private val userRepository: UserRepository,
    private val contactsRepository: ContactsRepository
) : BaseViewModel() {

    val contacts
        get() = _contacts.asStateFlow()

    protected val _contacts = MutableStateFlow(emptyList<ContactUiModel>())

    protected val currentUser: User? = SendBird.getCurrentUser()

    init {
        viewModelScope.launch {
            val loggedUserId = userRepository.getLoggedUserId()
            connectUser(loggedUserId)

            contactsRepository.getContacts(ConnectionStatus.CONNECTED) { either ->
                either.fold({ error ->
                    // TODO - Show contacts from local DB. Create LoadContactsUseCase
                }, { contacts ->
                    viewModelScope.launch {
                        _contacts.emit(contacts.toContactsUiModel(loggedUserId))
                    }
                })
            }
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private suspend fun connectUser(userId: String) {
        userRepository.connectToSendbird(userId) { either ->
            viewModelScope.launch {
                either.foldSuspend({ onFailure ->
                    Timber.tag("Sendbird").d("Failed to establish connection with Sendbird")
                    connectUser(userId)
                }, { onSuccess ->
                    Timber.tag("Sendbird").d("Connection to Sendbird is established")
                })
            }
        }
    }
}