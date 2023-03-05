package com.example.whisper.ui.recentchats

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
import com.example.whisper.utils.DateTimeFormatter
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.RECENT_CHAT_HANDLER_ID
import com.example.whisper.utils.common.ZERO
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

    val chatsRecyclerViewState
        get() = _chatsRecyclerViewState.asSharedFlow()

    private val _uiState = MutableStateFlow(RecentChatsUiModel())
    private val _recentChats = MutableStateFlow(emptyList<RecentChatUiModel>())
    private val _chatsRecyclerViewState = MutableSharedFlow<ChatsRecyclerViewState>()

    private var isAtTheTopOfRecyclerview = true

    init {
        loggedUserId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                _uiState.emit(_uiState.value.copy(uiState = RecentChatState.LOADING))
                connectionStatus.collect { connectionStatus ->
                    when (connectionStatus) {
                        ConnectionStatus.CONNECTED -> {
                            fetchRecentChats(id)
                            initChatHandler()
                        }
                        ConnectionStatus.NOT_CONNECTED -> {
                            _uiState.emit(_uiState.value.copy(uiState = RecentChatState.ERROR))
                        }
                        ConnectionStatus.CONNECTING -> {
                            _uiState.emit(_uiState.value.copy(uiState = RecentChatState.LOADING))
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
            loggedUserId?.let { id ->
                fetchRecentChats(id)
            }
        }
    }

    override fun onRecentChatClicked(chatId: String) {
        // TODO("Not yet implemented")
    }

    override fun navigateToAddContact() {
        _navigationLiveData.value = NavGraph(R.id.action_baseContactsFragment_to_addContactFragment)
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
    private suspend fun fetchRecentChats(id: String) {
        contactsRepository.getContacts(ContactConnectionStatus.CONNECTED) { either ->
            either.fold({ error ->
                // TODO - Show contacts from local DB. Create LoadContactsUseCase
            }, { contacts ->
                viewModelScope.launch {
                    _recentChats.emit(contacts.toListOfRecentChatsUiModel(id))
                    if (contacts.isEmpty()) {
                        _uiState.emit(_uiState.value.copy(uiState = RecentChatState.EMPTY))
                    } else {
                        _uiState.emit(_uiState.value.copy(uiState = RecentChatState.IDLE))
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
                        val chat = _recentChats.value.first { chat -> chat.id == channel?.url }
                        val newChat = RecentChatUiModel(
                            id = chat.id,
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