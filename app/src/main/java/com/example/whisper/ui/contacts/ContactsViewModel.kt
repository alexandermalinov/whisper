package com.example.whisper.ui.contacts

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.contacts.ContactConnectionStatus
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.navigation.NavGraph
import com.example.whisper.ui.base.ConnectionStatus
import com.example.whisper.ui.basecontacts.BaseContactsViewModel
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.RECENT_CHAT_HANDLER_ID
import com.example.whisper.vo.contacts.ContactUiModel
import com.example.whisper.vo.contacts.ContactsState
import com.example.whisper.vo.contacts.ContactsUiState
import com.example.whisper.vo.contacts.toContactUiModel
import com.sendbird.android.*
import com.sendbird.android.Member.MemberState
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

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(_uiState.value.copy(uiState = ContactsState.LOADING))

            initContactListener()

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
    override fun onResume(owner: LifecycleOwner) {
        super<BaseContactsViewModel>.onResume(owner)

        viewModelScope.launch(Dispatchers.IO) {
            fetchContacts()
        }
    }

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

    override fun acceptInvite(contactId: String) {
        viewModelScope.launch(Dispatchers.IO) {
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
                        isLoading = true
                    )
                }
            }
            _invitations.emit(updatedContacts)

            val contact = _invitations.value
                .firstOrNull { it.contactId == contactId }
                ?: return@launch

            contactsRepository.acceptContactRequest(contact.channelUrl) { either ->
                either.fold({ error ->

                }, {
                    viewModelScope.launch {
                        _invitations.emit(_invitations.value.minus(contact))
                        _contacts.emit(_contacts.value.plus(contact))
                    }
                })
            }
        }
    }

    override fun declineInvite(contactId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val contact = _invitations.value
                .firstOrNull { it.contactId == contactId }
                ?: return@launch

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
                        isLoading = true
                    )
                }
            }
            _invitations.emit(updatedContacts)

            contactsRepository.declineContactRequest(contact.channelUrl) { either ->
                either.fold({ error ->

                }, {
                    viewModelScope.launch {
                        _invitations.emit(_invitations.value.minus(contact))
                    }
                })
            }
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private suspend fun initContactListener() {
        SendBird.addChannelHandler(
            RECENT_CHAT_HANDLER_ID,
            object : SendBird.ChannelHandler() {
                override fun onMessageReceived(p0: BaseChannel?, p1: BaseMessage?) {
                    // do nothing
                }

                override fun onUserJoined(channel: GroupChannel?, user: User?) {
                    super.onUserJoined(channel, user)

                    viewModelScope.launch {
                        loggedUserId?.let { id ->
                            val contact = channel?.toContactUiModel(id) ?: return@launch
                            _pending.emit(_pending.value.minus(contact))
                            _contacts.emit(_contacts.value.plus(contact))
                        }
                    }
                }

                override fun onUserDeclinedInvitation(
                    channel: GroupChannel?,
                    inviter: User?,
                    invitee: User?
                ) {
                    super.onUserDeclinedInvitation(channel, inviter, invitee)

                    viewModelScope.launch {
                        loggedUserId?.let { id ->
                            val contact = channel?.toContactUiModel(id) ?: return@launch
                            _pending.emit(_pending.value.minus(contact))
                        }
                    }
                }
            })
    }

    private suspend fun fetchContacts() {
        contactsRepository.getContacts(ContactConnectionStatus.CONNECTED) { either ->
            either.fold({ error ->

            }, { allContacts ->
                viewModelScope.launch(Dispatchers.Default) {
                    setState(allContacts)

                    val invitedContacts = mutableListOf<ContactUiModel>()
                    val pendingContacts = mutableListOf<ContactUiModel>()
                    val contacts = mutableListOf<ContactUiModel>()

                    addContacts(allContacts, invitedContacts, pendingContacts, contacts)

                    val sortedInvitations = invitedContacts.sortedBy { it.createdAt }
                    _invitations.emit(sortedInvitations)

                    val sortedPendingContacts = pendingContacts.sortedBy { it.createdAt }
                    _pending.emit(sortedPendingContacts)

                    val sortedContacts = contacts
                        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.username })
                    _contacts.emit(sortedContacts)

                    _uiState.emit(
                        _uiState.value.copy(
                            invitationsCount = _invitations.value.size.toString(),
                            pendingCount = _pending.value.size.toString()
                        )
                    )
                }
            })
        }
    }

    private suspend fun setState(contacts: List<GroupChannel>) {
        if (contacts.isEmpty())
            _uiState.emit(_uiState.value.copy(uiState = ContactsState.EMPTY))
        else
            _uiState.emit(_uiState.value.copy(uiState = ContactsState.IDLE))
    }

    private suspend fun addContacts(
        allContacts: List<GroupChannel>,
        invitedContacts: MutableList<ContactUiModel>,
        pendingContacts: MutableList<ContactUiModel>,
        contacts: MutableList<ContactUiModel>
    ) {
        allContacts.forEach { contact ->
            val contactModel = contact.toContactUiModel(loggedUserId ?: EMPTY)
            when {
                contact.joinedMemberCount == 1 && contact.myMemberState == MemberState.INVITED -> {
                    invitedContacts.add(contactModel)
                }
                contact.joinedMemberCount == 1 && contact.myMemberState == MemberState.JOINED -> {
                    pendingContacts.add(contactModel)
                }
                contact.joinedMemberCount == 2 -> {
                    contacts.add(contactModel)
                }
            }
        }
    }
}