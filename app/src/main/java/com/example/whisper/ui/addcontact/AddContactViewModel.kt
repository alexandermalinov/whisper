package com.example.whisper.ui.addcontact

import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.domain.contact.AddContactState
import com.example.whisper.domain.contact.AddContactUseCase
import com.example.whisper.navigation.PopBackStack
import com.example.whisper.ui.basecontacts.BaseContactsViewModel
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.vo.addcontact.AddContactEvents
import com.example.whisper.vo.addcontact.AddContactUiModel
import com.example.whisper.vo.addcontact.AddContactUiState
import com.example.whisper.vo.addcontact.AddContactUiState.SEARCH_EMPTY
import com.example.whisper.vo.addcontact.AddContactUiState.SEARCH_FOUND
import com.example.whisper.vo.contacts.ContactUiModel
import com.example.whisper.vo.contacts.toContactsUiModel
import com.example.whisper.vo.dialogs.TitleMessageDialog
import com.sendbird.android.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddContactViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val contactsRepository: ContactsRepository,
) : BaseContactsViewModel(userRepository),
    AddContactPresenter {

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
            updateLoadingUser(contactId, true)

            AddContactUseCase(userRepository, contactsRepository).invoke(
                contactId = contactId,
                coroutineScope = viewModelScope
            ) { state ->
                when (state) {
                    is AddContactState.ErrorState -> {
                        updateLoadingUser(contactId, false)
                        _dialogFlow.emit(
                            TitleMessageDialog(
                                R.string.error_dialog_title,
                                R.string.error_something_went_wrong_try_again
                            )
                        )
                    }
                    is AddContactState.NetworkErrorState -> {
                        updateLoadingUser(contactId, false)
                        _dialogFlow.emit(
                            TitleMessageDialog(
                                R.string.error_dialog_title_network,
                                R.string.error_dialog_message_body_no_network
                            )
                        )
                    }
                    is AddContactState.SuccessState -> {
                        updateInvitedUser(contactId)
                    }
                }
            }
        }
    }

    override fun onBackClicked() {
        viewModelScope.launch {
            _navigationFlow.emit(PopBackStack)
        }
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
        val currentUserId = userRepository.cachedUser.userId
        users.filter { it.userId != currentUserId }
            .toContactsUiModel(currentUserId)
            .let { filteredUsers -> _users.emit(filteredUsers) }

        val newUiState = if (users.isEmpty()) SEARCH_EMPTY else SEARCH_FOUND
        changeState(newUiState)
    }

    private suspend fun changeState(uiState: AddContactUiState) {
        _uiState.emit(_uiState.value.copy(state = uiState))
    }

    private suspend fun updateInvitedUser(contactId: String) {
        _users.value
            .map { uiModel ->
                if (uiModel.contactId == contactId) {
                    uiModel.copy(isInvited = true, isLoading = false)
                } else {
                    uiModel
                }
            }
            .toMutableList()
            .let { updatedContacts -> _users.emit(updatedContacts) }
    }

    private suspend fun updateLoadingUser(contactId: String, isLoading: Boolean) {
        _users.value
            .map { uiModel ->
                if (uiModel.contactId == contactId) {
                    uiModel.copy(isLoading = isLoading)
                } else {
                    uiModel
                }
            }
            .toMutableList()
            .let { updatedContacts -> _users.emit(updatedContacts) }
    }
}