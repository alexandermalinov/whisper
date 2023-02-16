package com.example.whisper.ui.addcontact

import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.navigation.NavGraph
import com.example.whisper.navigation.PopBackStack
import com.example.whisper.ui.base.BaseChatViewModel
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.vo.addcontact.AddContactEvents
import com.example.whisper.vo.addcontact.AddContactUiModel
import com.example.whisper.vo.addcontact.AddContactUiState
import com.example.whisper.vo.contacts.ContactUiModel
import com.example.whisper.vo.contacts.toContactsUiModel
import com.sendbird.android.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddContactViewModel @Inject constructor(
    userRepository: UserRepository,
    private val contactsRepository: ContactsRepository
) : BaseChatViewModel(userRepository, contactsRepository), AddContactPresenter {

    val uiState
        get() = _uiState.asStateFlow()

    val users
        get() = _users.asStateFlow()

    val addContactEvents
        get() = _addContactEvents.asSharedFlow()

    private val _uiState = MutableStateFlow(AddContactUiModel())
    private val _users = MutableStateFlow(emptyList<ContactUiModel>())
    private val _addContactEvents = MutableSharedFlow<AddContactEvents>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            changeState(AddContactUiState.LOADING)
            searchUser(searchedUsername = EMPTY)
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onUsernameTextChanged(textFlow: Flow<CharSequence>) {
        textFlow
            .debounce(1000L)
            .onEach { username ->
                withContext(Dispatchers.IO) {
                    searchUser(searchedUsername = username.toString())
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onAddContactClicked(contactId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val contact = _users.value.first { it.contactId == contactId }
            val contactUiModel = ContactUiModel(
                contact.contactId,
                contact.pictureUrl,
                contact.username,
                contact.email,
                contact.channelUrl,
                true
            )
            val contacts = _users.value.filter { it.contactId != contactId }.plus(contactUiModel)

            contactsRepository.addContact(contactId) { either ->
                viewModelScope.launch {
                    either.foldSuspend(
                        { failure -> },
                        { success -> _users.emit(contacts) }
                    )
                }
            }
        }
    }

    override fun onBackClicked() {
        _navigationLiveData.value = PopBackStack
    }

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private suspend fun searchUser(searchedUsername: String) {
        contactsRepository.searchUsers(searchedUsername) { either ->
            viewModelScope.launch {
                either.foldSuspend({ error ->
                    _users.emit(emptyList())
                    _addContactEvents.emit(AddContactEvents.HIDE_KEYBOARD)
                    changeState(AddContactUiState.ERROR)
                }, { users ->
                    onSearchSuccessful(users)
                })
            }
        }
    }

    private suspend fun onSearchSuccessful(users: List<User>) {
        _users.emit(users.toContactsUiModel())

        val newUiState = when {
            users.isEmpty() -> AddContactUiState.SEARCH_EMPTY
            else -> AddContactUiState.SEARCH_FOUND
        }
        changeState(newUiState)
    }

    private suspend fun changeState(uiState: AddContactUiState) {
        _uiState.emit(_uiState.value.copy(state = uiState))
    }
}