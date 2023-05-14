package com.example.whisper.ui.contacts

import android.app.Application
import androidx.core.os.bundleOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.local.model.toContactModel
import com.example.whisper.data.repository.contacts.ContactConnectionStatus
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.navigation.NavGraph
import com.example.whisper.ui.base.ConnectionStatus
import com.example.whisper.ui.base.ConnectionStatus.*
import com.example.whisper.ui.basecontacts.BaseContactsViewModel
import com.example.whisper.utils.common.*
import com.example.whisper.vo.contacts.ContactUiModel
import com.example.whisper.vo.contacts.ContactsState
import com.example.whisper.vo.contacts.ContactsUiState
import com.example.whisper.vo.contacts.toContactUiModel
import com.example.whisper.vo.dialogs.ContactBottomDialog
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

    private lateinit var userConnectionStatus: ConnectionStatus

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(_uiState.value.copy(uiState = ContactsState.LOADING))

            connectionStatus.collect { connectionStatus ->
                when (connectionStatus) {
                    CONNECTED -> {
                        userConnectionStatus = CONNECTED
                        fetchContacts()
                        initContactListener()
                    }
                    CONNECTING -> {
                        userConnectionStatus = CONNECTING
                        _uiState.emit(_uiState.value.copy(uiState = ContactsState.LOADING))
                    }
                    NOT_CONNECTED -> {
                        userConnectionStatus = NOT_CONNECTED
                        _uiState.emit(_uiState.value.copy(uiState = ContactsState.ERROR))
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

            contactsRepository.deleteContact(channelUrl) { either ->
                viewModelScope.launch {
                    either.foldSuspend({ error ->
                        setLoadingState(channelUrl, false)
                    }, {
                        _contacts.value
                            .firstOrNull { contact -> contact.channelUrl == channelUrl }
                            ?.let { contactModel ->
                                _contacts.emit(_contacts.value.minus(contactModel))
                            }
                        updateContactsCount()
                    })
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

            contactsRepository.acceptContactRequest(contact.channelUrl) { either ->
                viewModelScope.launch {
                    either.foldSuspend({ error ->
                        setLoadingState(contactId, false)
                    }, {
                        _invitations.emit(_invitations.value.minus(contact))
                        updateContactsCount()
                    })
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

            contactsRepository.declineContactRequest(contact.channelUrl) { either ->
                viewModelScope.launch {
                    either.foldSuspend({ error ->
                        setLoadingState(contactId, false)
                    }, {
                        viewModelScope.launch {
                            _invitations.emit(_invitations.value.minus(contact))
                            updateContactsCount()
                        }
                    })
                }
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

                override fun onChannelDeleted(
                    channelUrl: String?,
                    channelType: BaseChannel.ChannelType?
                ) {
                    super.onChannelDeleted(channelUrl, channelType)

                    viewModelScope.launch {
                        loggedUserId?.let { id ->

                            _contacts.value.firstOrNull { it.channelUrl == channelUrl }
                                ?.let {
                                    _contacts.emit(_contacts.value.minus(it))
                                }
                                ?: _invitations.value.firstOrNull { it.channelUrl == channelUrl }
                                    ?.let {
                                        _invitations.emit(_invitations.value.minus(it))
                                    }
                                ?: _pending.value.firstOrNull { it.channelUrl == channelUrl }
                                    ?.let {
                                        _pending.emit(_pending.value.minus(it))
                                    }

                            setState()
                            updateContactsCount()
                        }
                    }
                }

                override fun onUserReceivedInvitation(
                    channel: GroupChannel?,
                    inviter: User?,
                    invitees: MutableList<User>?
                ) {
                    super.onUserReceivedInvitation(channel, inviter, invitees)

                    viewModelScope.launch {
                        currentUser?.let { user ->
                            val contact = channel?.toContactUiModel(user) ?: return@launch
                            _invitations.emit(_invitations.value.plus(contact))
                            setState()
                            updateContactsCount()
                        }
                    }
                }

                override fun onUserJoined(channel: GroupChannel?, user: User?) {
                    super.onUserJoined(channel, user)

                    viewModelScope.launch {
                        currentUser?.let { user ->
                            val contact = channel?.toContactUiModel(user) ?: return@launch
                            _pending.emit(_pending.value.minus(contact))
                            _contacts.emit(_contacts.value.plus(contact))
                            setState()
                            updateContactsCount()
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
                        currentUser?.let { user ->
                            val contact = channel?.toContactUiModel(user) ?: return@launch
                            _pending.emit(_pending.value.minus(contact))
                            setState()
                            updateContactsCount()
                        }
                    }
                }

                override fun onChannelFrozen(channel: BaseChannel?) {
                    super.onChannelFrozen(channel)

                    viewModelScope.launch {
                        loggedUserId?.let { id ->

                            _contacts.value.firstOrNull { it.channelUrl == channel?.url }
                                ?.let {
                                    _contacts.emit(_contacts.value.minus(it))
                                }
                                ?: _invitations.value.firstOrNull { it.channelUrl == channel?.url }
                                    ?.let {
                                        _invitations.emit(_invitations.value.minus(it))
                                    }
                                ?: _pending.value.firstOrNull { it.channelUrl == channel?.url }
                                    ?.let {
                                        _pending.emit(_pending.value.minus(it))
                                    }

                            setState()
                            updateContactsCount()
                        }
                    }
                }

                override fun onUserMuted(channel: BaseChannel?, user: User?) {
                    super.onUserMuted(channel, user)

                    viewModelScope.launch {
                        _contacts.value.map { contact ->
                            if (contact.contactId == user?.userId) contact.isMuted = true
                            contact
                        }.let {
                            _contacts.emit(it)
                        }
                    }
                }

                override fun onUserUnmuted(channel: BaseChannel?, user: User?) {
                    super.onUserUnmuted(channel, user)

                    viewModelScope.launch {
                        _contacts.value.map { contact ->
                            if (contact.contactId == user?.userId) contact.isMuted = false
                            contact
                        }.let {
                            _contacts.emit(it)
                        }
                    }
                }
            })
    }

    private suspend fun fetchContacts() {
        contactsRepository.getContacts(ContactConnectionStatus.CONNECTED) { either ->
            either.fold({ error ->
                // TODO - Implement me
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

                    updateContactsCount()
                }
            })
        }
    }

    private suspend fun setState(contacts: List<GroupChannel> = emptyList()) {
        val allContacts = _pending.value
            .plus(_invitations.value)
            .plus(_contacts.value)

        if (contacts.isEmpty() && allContacts.isEmpty())
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
        if (currentUser == null) return
        allContacts.forEach { contact ->
            val contactModel = contact.toContactUiModel(currentUser!!)
            when {
                contact.isFrozen -> return@forEach
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
            contactsRepository.addContactLocal(
                contact.toContactModel(currentUser?.userId ?: EMPTY)
            )
        }
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