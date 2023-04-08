package com.example.whisper.ui.recentchats

import android.app.Application
import androidx.core.os.bundleOf
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
import com.example.whisper.ui.contacts.ContactState
import com.example.whisper.utils.DateTimeFormatter
import com.example.whisper.utils.common.*
import com.example.whisper.vo.dialogs.ContactBottomDialog
import com.example.whisper.vo.recentchats.*
import com.sendbird.android.BaseChannel
import com.sendbird.android.BaseMessage
import com.sendbird.android.SendBird
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RecentChatsViewModel @Inject constructor(
    application: Application,
    userRepository: UserRepository,
    private val contactsRepository: ContactsRepository,
) : BaseContactsViewModel(application, userRepository), RecentChatsPresenter,
    RecentChatPresenter, DefaultLifecycleObserver {

    val uiState
        get() = _uiState.asStateFlow()

    val recentChats
        get() = _recentChats.asStateFlow()

    val pinnedChats
        get() = _pinnedChats.asStateFlow()

    val chatsRecyclerViewState
        get() = _chatsRecyclerViewState.asSharedFlow()

    private val _uiState = MutableStateFlow(RecentChatsUiModel())
    private val _recentChats = MutableStateFlow(emptyList<RecentChatUiModel>())
    private val _pinnedChats = MutableStateFlow(emptyList<RecentChatUiModel>())
    private val _chatsRecyclerViewState = MutableSharedFlow<ChatsRecyclerViewState>()

    private var isAtTheTopOfRecyclerview = true

    init {
        loggedUserId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                _uiState.emit(_uiState.value.copy(uiState = RecentChatState.LOADING))

                connectionStatus.collect { connectionStatus ->
                    when (connectionStatus) {
                        ConnectionStatus.CONNECTED -> {
                            fetchRecentChats()
                            initChatHandler()
                        }
                        ConnectionStatus.CONNECTING -> {
                            _uiState.emit(_uiState.value.copy(uiState = RecentChatState.LOADING))
                        }
                        ConnectionStatus.NOT_CONNECTED -> {
                            _uiState.emit(_uiState.value.copy(uiState = RecentChatState.ERROR))
                        }
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
            fetchRecentChats()
        }
    }

    override fun onRecentChatClicked(chatId: String) {
        // TODO("Not yet implemented")
    }

    override fun onRecentChatLongClicked(contact: RecentChatUiModel): Boolean {
        viewModelScope.launch {
            val bundle = bundleOf(
                CHANNEL_URL to contact.chatUrl,
                CONTACT_ID to contact.contactId,
                CONTACT_PROFILE_IMAGE to contact.profilePicture,
                CONTACT_USERNAME to contact.username,
                CONTACT_EMAIL to contact.email,
                CONTACT_IS_MUTED to contact.isMuted,
                CONTACT_IS_PINNED to contact.isPinned,
                CONTACT_STATUS to ContactState.JOINED
            )

            _dialogFlow.emit(ContactBottomDialog(bundle))
        }
        return true
    }

    override fun navigateToAddContact() {
        viewModelScope.launch {
            _navigationFlow.emit(
                NavGraph(R.id.action_baseContactsFragment_to_addContactFragment)
            )
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Exposed
    ---------------------------------------------------------------------------------------------*/
    fun setIsAtTheTop(isAtTop: Boolean) {
        isAtTheTopOfRecyclerview = isAtTop
    }

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private suspend fun fetchRecentChats() {
        contactsRepository.getContacts(ContactConnectionStatus.CONNECTED) { either ->
            either.fold({ error ->
                Timber.tag("Contacts Fetching").d("Couldn't fetch contacts")
            }, { contacts ->
                currentUser?.let { user ->
                    viewModelScope.launch {
                        val pinned = contacts.filter { chat ->
                            user.metaData[PINNED_CONTACTS]
                                ?.filterNot { it.isWhitespace() }
                                ?.split(',')
                                ?.contains(chat.members.firstOrNull { it.userId != user.userId }?.userId)
                                ?: false
                        }
                        val chats = contacts.filterNot { chat -> pinned.any { it.url == chat.url } }

                        _pinnedChats.emit(pinned.toListOfRecentChatsUiModel(user))
                        _recentChats.emit(chats.toListOfRecentChatsUiModel(user))

                        if (contacts.isEmpty()) {
                            _uiState.emit(_uiState.value.copy(uiState = RecentChatState.EMPTY))
                        } else {
                            _uiState.emit(_uiState.value.copy(uiState = RecentChatState.IDLE))
                        }
                    }
                }
            })
        }
    }

    private suspend fun initChatHandler() {
        SendBird.addChannelHandler(
            RECENT_CHAT_HANDLER_ID,
            object : SendBird.ChannelHandler() {

                override fun onMessageReceived(channel: BaseChannel?, message: BaseMessage?) {
                    viewModelScope.launch {
                        val chat = _recentChats.value
                            .firstOrNull { chat -> chat.chatUrl == channel?.url }
                            ?: return@launch
                        val newChat = RecentChatUiModel(
                            chatUrl = chat.chatUrl,
                            username = chat.username,
                            profilePicture = chat.profilePicture,
                            unreadMessagesCount = chat.unreadMessagesCount + 1,
                            lastMessageText = message?.message ?: EMPTY,
                            lastMessageTimestamp = DateTimeFormatter.formatMessageDateTime(
                                message?.createdAt ?: ZERO.toLong()
                            )
                        )
                        val linkList = _recentChats.value.toMutableList()
                        linkList.removeAt(linkList.indexOf(chat))
                        linkList.add(0, newChat)
                        _recentChats.emit(linkList)

                        if (isAtTheTopOfRecyclerview) {
                            _chatsRecyclerViewState.emit(ChatsRecyclerViewState.SCROLL_TO_TOP)
                        }
                    }
                }
            }
        )
    }
}