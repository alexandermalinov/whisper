package com.example.whisper.ui.contacts

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.contacts.ContactConnectionStatus
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.navigation.NavGraph
import com.example.whisper.ui.base.ConnectionStatus
import com.example.whisper.ui.basecontacts.BaseContactsViewModel
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.vo.contacts.ContactUiModel
import com.example.whisper.vo.contacts.ContactsState
import com.example.whisper.vo.contacts.ContactsUiState
import com.example.whisper.vo.contacts.toContactsUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    application: Application,
    userRepository: UserRepository,
    private val contactsRepository: ContactsRepository
) : BaseContactsViewModel(application, userRepository), ContactPresenter {

    val uiState
        get() = _uiState.asStateFlow()

    val contacts
        get() = _contacts.asStateFlow()

    val invitationsExpandEvent
        get() = _invitationsExpandEvent.asStateFlow()

    val pendingExpandEvent
        get() = _pendingExpandEvent.asStateFlow()

    private val _uiState = MutableStateFlow(ContactsUiState())
    private val _contacts = MutableStateFlow(emptyList<ContactUiModel>())
    private val _invitationsExpandEvent = MutableStateFlow<Boolean>(false)
    private val _pendingExpandEvent = MutableStateFlow<Boolean>(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(_uiState.value.copy(uiState = ContactsState.LOADING))
            connectionStatus.collect { connectionStatus ->
                when (connectionStatus) {
                    ConnectionStatus.CONNECTED -> {
                        fetchContacts()
                    }
                    ConnectionStatus.NOT_CONNECTED -> {
                        _uiState.emit(_uiState.value.copy(uiState = ContactsState.ERROR))
                    }
                    ConnectionStatus.CONNECTING -> {
                        _uiState.emit(_uiState.value.copy(uiState = ContactsState.LOADING))
                    }
                }
            }
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun navigateToAddContact() {
        _navigationLiveData.value = NavGraph(R.id.action_baseContactsFragment_to_addContactFragment)
    }

    override fun expandInvitations() {
        viewModelScope.launch {
            _invitationsExpandEvent.emit(_invitationsExpandEvent.value.not())
        }
    }

    override fun expandPending() {
        viewModelScope.launch {
            _pendingExpandEvent.emit(_pendingExpandEvent.value.not())
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private suspend fun fetchContacts() {
        contactsRepository.getContacts(ContactConnectionStatus.CONNECTED) { either ->
            either.fold({ error ->
                // TODO - Show contacts from local DB. Create LoadContactsUseCase
            }, { contacts ->
                viewModelScope.launch {
                    if (contacts.isEmpty())
                        setState(ContactsState.EMPTY)
                    else
                        setState(ContactsState.IDLE)

                    val sortedContacts = contacts
                        .toContactsUiModel(loggedUserId ?: EMPTY)
                        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.username })
                    _contacts.emit(sortedContacts)
                }
            })
        }
    }

    private suspend fun setState(state: ContactsState) {
        _uiState.emit(_uiState.value.copy(uiState = state))
    }
}