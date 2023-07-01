package com.example.whisper.ui.contacts

import android.app.Application
import androidx.core.os.bundleOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.domain.contact.*
import com.example.whisper.navigation.NavGraph
import com.example.whisper.ui.basecontacts.BaseContactsViewModel
import com.example.whisper.utils.common.CONTACT_BOTTOM_DIALOG_KEY
import com.example.whisper.utils.common.IS_RECENT_CHAT
import com.example.whisper.vo.contacts.ContactUiModel
import com.example.whisper.vo.contacts.ContactsState
import com.example.whisper.vo.contacts.ContactsUiState
import com.example.whisper.vo.contacts.toContactsUiModels
import com.example.whisper.vo.dialogs.ContactBottomDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    application: Application,
    userRepository: UserRepository,
    private val contactsRepository: ContactsRepository,
    recentChatsRepository: RecentChatsRepository
) : BaseContactsViewModel(application, userRepository, contactsRepository, recentChatsRepository),
    ContactPresenter, DefaultLifecycleObserver {

    val uiState
        get() = _uiState.asStateFlow()

    val contacts
        get() = _contacts.asStateFlow()

    val invitations
        get() = _invitations.asStateFlow()

    val pending
        get() = _pending.asStateFlow()

    val invitationsExpandEvent
        get() = _invitationsExpandEvent.asStateFlow()

    val pendingExpandEvent
        get() = _pendingExpandEvent.asStateFlow()

    private val _uiState = MutableStateFlow(ContactsUiState())
    private val _contacts = MutableStateFlow(emptyList<ContactUiModel>())
    private val _invitations = MutableStateFlow(emptyList<ContactUiModel>())
    private val _pending = MutableStateFlow(emptyList<ContactUiModel>())
    private val _invitationsExpandEvent = MutableStateFlow(false)
    private val _pendingExpandEvent = MutableStateFlow(false)

    init {
        viewModelScope.launch(Dispatchers.Default) {
            //_uiState.emit(_uiState.value.copy(uiState = ContactsState.LOADING))
            fetchContacts()
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/

    override fun navigateToAddContact() {
        viewModelScope.launch {
            _navigationFlow.emit(
                NavGraph(R.id.action_baseContactsFragment_to_addContactFragment)
            )
        }
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

    override fun showBottomDialog(contact: ContactUiModel): Boolean {
        viewModelScope.launch(Dispatchers.IO) {
            val contactModel = contactsRepository
                .getContactFromCacheOrDb(contact.channelUrl)
                ?: return@launch

            val bundle = bundleOf(
                CONTACT_BOTTOM_DIALOG_KEY to contactModel,
                IS_RECENT_CHAT to false
            )

            _dialogFlow.emit(ContactBottomDialog(bundle))
        }
        return true
    }

    override fun acceptInvite(contactId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            setLoadingState(contactId, true)

            val contact = _invitations.value
                .firstOrNull { it.contactId == contactId }
                ?: return@launch

            AcceptContactInviteUseCase(contactsRepository).invoke(
                contactUrl = contact.channelUrl,
                coroutineScope = viewModelScope
            ) {
                setLoadingState(contactId, false)
                when (it) {
                    is AcceptContactInviteState.ErrorState -> {

                    }
                    is AcceptContactInviteState.SuccessState -> {

                    }
                }
            }
        }
    }

    override fun declineInvite(contactId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            setLoadingState(contactId, true)

            val contact = _invitations.value
                .firstOrNull { it.contactId == contactId }
                ?: return@launch

            DeclineContactInviteUseCase(contactsRepository).invoke(
                contactUrl = contact.channelUrl,
                coroutineScope = viewModelScope
            ) {
                setLoadingState(contactId, false)
                when (it) {
                    is DeclineContactInviteState.ErrorState -> {

                    }
                    is DeclineContactInviteState.SuccessState -> {

                    }
                }
            }
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/

    private suspend fun fetchContacts() {
        contactsRepository.getContactsDbFlow().collect { contacts ->
            val result = GetContactsUseCase().invoke(contacts)

            setState(contacts.isEmpty())

            when (result) {
                is GetContactsState.ErrorState -> {}
                is GetContactsState.SuccessState -> {
                    _invitations.emit(result.contactsReceivedInvite.toContactsUiModels())
                    _pending.emit(result.contactsSentInvite.toContactsUiModels())
                    _contacts.emit(result.addedContacts.toContactsUiModels())
                    updateContactsCount()
                }
            }
        }
    }

    private suspend fun setState(contactsEmpty: Boolean = true) {
        val allContacts = _pending.value
            .plus(_invitations.value)
            .plus(_contacts.value)

        if (contactsEmpty && allContacts.isEmpty())
            _uiState.emit(_uiState.value.copy(uiState = ContactsState.EMPTY))
        else
            _uiState.emit(_uiState.value.copy(uiState = ContactsState.IDLE))
    }

    private suspend fun setLoadingState(contactId: String, isLoading: Boolean) {
        _invitations.value
            .map { uiModel ->
                if (uiModel.contactId == contactId) uiModel.copy(isLoading = isLoading) else uiModel
            }
            .toMutableList()
            .let { updatedContacts -> _invitations.emit(updatedContacts) }
    }

    private suspend fun updateContactsCount() {
        _uiState.emit(
            _uiState.value.copy(
                contactsCount = _contacts.value.size,
                invitationsCount = _invitations.value.size,
                pendingCount = _pending.value.size
            )
        )
    }

    private fun ContactUiModel.getContactStatus() = when {
        _contacts.value.any { it.contactId == contactId } -> ContactState.JOINED
        _pending.value.any { it.contactId == contactId } -> ContactState.PENDING
        else -> ContactState.INVITED
    }
}