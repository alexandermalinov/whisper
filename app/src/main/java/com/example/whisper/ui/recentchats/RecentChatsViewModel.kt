package com.example.whisper.ui.recentchats

import androidx.core.os.bundleOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.navigation.NavGraph
import com.example.whisper.ui.basecontacts.BaseContactsViewModel
import com.example.whisper.utils.common.CONTACT_BOTTOM_DIALOG_KEY
import com.example.whisper.utils.common.IS_RECENT_CHAT
import com.example.whisper.vo.dialogs.ContactBottomDialog
import com.example.whisper.vo.recentchats.ChatsRecyclerViewState
import com.example.whisper.vo.recentchats.RecentChatUiModel
import com.example.whisper.vo.recentchats.RecentChatsUiModel
import com.example.whisper.vo.recentchats.toListOfRecentChatsUiModel
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
    userRepository: UserRepository,
    private val recentChatsRepository: RecentChatsRepository
) : BaseContactsViewModel(userRepository),
    RecentChatsPresenter,
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
        viewModelScope.launch(Dispatchers.IO) {
            //_uiState.emit(_uiState.value.copy(uiState = RecentChatState.LOADING))

            recentChatsRepository.getRecentChatsDbFlow().collect { contacts ->
                val (pinned, notPinned) = contacts.partition { contact -> contact.isPinned }
                _pinnedChats.emit(pinned.toListOfRecentChatsUiModel())
                _recentChats.emit(notPinned.toListOfRecentChatsUiModel())
            }
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onResume(owner: LifecycleOwner) {
        super<BaseContactsViewModel>.onResume(owner)

        /*viewModelScope.launch(Dispatchers.IO) {

         }*/
    }

    override fun onRecentChatClicked(chatId: String) {
        // TODO("Not yet implemented")
    }

    override fun onRecentChatLongClicked(contact: RecentChatUiModel): Boolean {
        viewModelScope.launch(Dispatchers.IO) {
            val contactModel = recentChatsRepository
                .getRecentChatFromCacheOrDb(contact.chatUrl)
                ?: return@launch

            val bundle = bundleOf(
                CONTACT_BOTTOM_DIALOG_KEY to contactModel,
                IS_RECENT_CHAT to true
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
}