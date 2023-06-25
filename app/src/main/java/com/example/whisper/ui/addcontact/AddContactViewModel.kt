package com.example.whisper.ui.addcontact

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.domain.contact.AddContactState
import com.example.whisper.domain.contact.AddContactUseCase
import com.example.whisper.navigation.PopBackStack
import com.example.whisper.ui.basecontacts.BaseContactsViewModel
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.vo.addcontact.AddContactEvents
import com.example.whisper.vo.addcontact.AddContactUiModel
import com.example.whisper.vo.addcontact.AddContactUiState
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
    application: Application,
    private val userRepository: UserRepository,
    private val contactsRepository: ContactsRepository,
) : BaseContactsViewModel(application, userRepository), AddContactPresenter {

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
        loggedUserId?.let { id ->
            val filteredUsers = users.filter { it.userId != id }.toContactsUiModel(id)
            _users.emit(filteredUsers)
        }

        when {
            users.isEmpty() -> AddContactUiState.SEARCH_EMPTY
            else -> AddContactUiState.SEARCH_FOUND
        }.let { newUiState ->
            changeState(newUiState)
        }
    }

    private suspend fun changeState(uiState: AddContactUiState) {
        _uiState.emit(_uiState.value.copy(state = uiState))
    }

    private suspend fun updateInvitedUser(contactId: String) {
        val updatedContacts = _users.value.toMutableList()
        updatedContacts.mapIndexed { index, uiModel ->
            if (uiModel.contactId == contactId) {
                val contact = ContactUiModel(
                    contactId = uiModel.contactId,
                    pictureUrl = uiModel.pictureUrl,
                    username = uiModel.username,
                    email = uiModel.email,
                    channelUrl = uiModel.channelUrl,
                    isInvited = true,
                    isLoading = false
                )
                updatedContacts[index] = contact
            }
        }
        _users.emit(updatedContacts)
    }

    private suspend fun updateLoadingUser(contactId: String, isLoading: Boolean) {
        val updatedContacts = _users.value.toMutableList()
        updatedContacts.mapIndexed { index, uiModel ->
            if (uiModel.contactId == contactId) {
                val contact = ContactUiModel(
                    contactId = uiModel.contactId,
                    pictureUrl = uiModel.pictureUrl,
                    username = uiModel.username,
                    email = uiModel.email,
                    channelUrl = uiModel.channelUrl,
                    isLoading = isLoading
                )
                updatedContacts[index] = contact
            }
        }
        _users.emit(updatedContacts)
    }
}