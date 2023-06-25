package com.example.whisper.ui.contacts

import android.app.Application
import androidx.core.os.bundleOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.domain.contact.*
import com.example.whisper.navigation.NavGraph
import com.example.whisper.ui.base.ConnectionStatus
import com.example.whisper.ui.base.ConnectionStatus.*
import com.example.whisper.ui.basecontacts.BaseContactsViewModel
import com.example.whisper.utils.common.*
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
    private val userRepository: UserRepository,
    private val contactsRepository: ContactsRepository
) : BaseContactsViewModel(application, userRepository), ContactPresenter, DefaultLifecycleObserver {

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

    private lateinit var userConnectionStatus: ConnectionStatus

    init {
        viewModelScope.launch(Dispatchers.IO) {
            //_uiState.emit(_uiState.value.copy(uiState = ContactsState.LOADING))
            fetchContacts()

            connectionStatus.collect { connectionStatus ->
                userConnectionStatus = when (connectionStatus) {
                    CONNECTED -> {
                        CONNECTED
                    }
                    CONNECTING -> {
                        CONNECTING
                        //_uiState.emit(_uiState.value.copy(uiState = ContactsState.LOADING))
                    }
                    NOT_CONNECTED -> {
                        NOT_CONNECTED
                        //_uiState.emit(_uiState.value.copy(uiState = ContactsState.ERROR))
                    }
                }
            }
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onResume(owner: LifecycleOwner) {
        super<BaseContactsViewModel>.onResume(owner)

        viewModelScope.launch(Dispatchers.IO) {
            fetchContacts()
        }
    }

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
        viewModelScope.launch {
            val bundle = bundleOf(
                CHANNEL_URL to contact.channelUrl,
                CONTACT_ID to contact.contactId,
                CONTACT_PROFILE_IMAGE to contact.pictureUrl,
                CONTACT_USERNAME to contact.username,
                CONTACT_EMAIL to contact.email,
                CONTACT_IS_MUTED to contact.isMuted,
                CONTACT_IS_PINNED to contact.isPinned,
                CONTACT_STATUS to getContactStatus(contact)
            )

            _dialogFlow.emit(ContactBottomDialog(bundle))
        }
        return true
    }

    override fun deleteContact(channelUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            setLoadingState(channelUrl, true)

            DeleteContactUseCase(contactsRepository).invoke(
                contactUrl = channelUrl,
                coroutineScope = viewModelScope
            ) {
                viewModelScope.launch {
                    when (it) {
                        is DeleteContactState.ErrorState -> {
                            setLoadingState(channelUrl, false)
                        }
                        is DeleteContactState.SuccessState -> {

                        }
                    }
                }
            }
        }
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
                when (it) {
                    is AcceptContactInviteState.ErrorState -> {
                        setLoadingState(contactId, false)
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
                when (it) {
                    is DeclineContactInviteState.ErrorState -> {
                        setLoadingState(contactId, false)
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
            viewModelScope.launch {
                val result = GetContactsUseCase(contactsRepository, userRepository.cachedUser)
                    .invoke(contacts)

                when (result) {
                    is GetContactsState.ErrorState -> {}
                    is GetContactsState.SuccessState -> {
                        setState(contacts.isEmpty())
                        userRepository.cachedUser.let { user ->
                            _invitations.emit(result.contactsReceivedInvite.toContactsUiModels(user))
                            _contacts.emit(result.addedContacts.toContactsUiModels(user))
                            _pending.emit(result.contactsSentInvite.toContactsUiModels(user))
                        }
                        updateContactsCount()
                    }
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
        val updatedContacts = _invitations.value.toMutableList()
        updatedContacts.mapIndexed { index, uiModel ->
            if (uiModel.contactId == contactId) {
                updatedContacts[index] = ContactUiModel(
                    contactId = uiModel.contactId,
                    pictureUrl = uiModel.pictureUrl,
                    username = uiModel.username,
                    email = uiModel.email,
                    channelUrl = uiModel.channelUrl,
                    isInvited = uiModel.isInvited,
                    isLoading = isLoading
                )
            }
        }
        _invitations.emit(updatedContacts)
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

    private fun getContactStatus(contact: ContactUiModel) = when {
        _contacts.value.any { it.contactId == contact.contactId } -> ContactState.JOINED
        pending.value.any { it.contactId == contact.contactId } -> ContactState.PENDING
        else -> ContactState.INVITED
    }
}