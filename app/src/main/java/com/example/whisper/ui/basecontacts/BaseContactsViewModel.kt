package com.example.whisper.ui.basecontacts

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.navigation.NavGraph
import com.example.whisper.ui.base.BaseViewModel
import com.example.whisper.ui.base.ConnectionStatus
import com.example.whisper.utils.common.*
import com.example.whisper.utils.isNetworkAvailable
import com.example.whisper.vo.basecontacts.BaseContactsUiModel
import com.sendbird.android.SendBird
import com.sendbird.android.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class BaseContactsViewModel @Inject constructor(
    private val application: Application,
    private val userRepository: UserRepository
) : BaseViewModel(), DefaultLifecycleObserver, BaseContactsPresenter,
    BottomNavigationChangesCallback {

    val uiModel
        get() = _uiModel.asStateFlow()

    val connectionStatus
        get() = _connectionStatus.asSharedFlow()

    private val _uiModel = MutableStateFlow(BaseContactsUiModel())
    private val _connectionStatus = MutableSharedFlow<ConnectionStatus>()

    private var currentUser: User? = null
    protected var loggedUserId: String? = null

    init {
        viewModelScope.launch {
            loggedUserId = userRepository.getLoggedUserId()
            loggedUserId?.let { id ->
                connectUser(id)
            }
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        viewModelScope.launch {
            currentUser?.let {
                _uiModel.emit(_uiModel.value.copy(profilePictureUrl = it.profileUrl))
            }
        }
    }

    override fun addContactOrCreateGroup() {
        when (_uiModel.value.viewPagerPosition) {
            RECENT_CHATS_PAGER_POSITION -> { /*navigateToAddContacts()*/ }
            CONTACTS_CHATS_PAGER_POSITION -> { navigateToAddContacts() }
            else -> { /* do nothing */ }
        }
    }

    // TODO: Couldn't make it work with binding adapter. Implement solutions with binding adapter
    override fun onPageChange(position: Int) {
        viewModelScope.launch {
            val itemId = when (position) {
                RECENT_CHATS_PAGER_POSITION -> R.id.recentChatsFragment
                CONTACTS_CHATS_PAGER_POSITION -> R.id.contactsFragment
                else -> R.id.profileFragment
            }
            _uiModel.emit(_uiModel.value.copy(bottomNavigationItemId = itemId))
        }
    }

    // TODO: Couldn't make it work with binding adapter. Implement solution with binding adapter
    override fun onItemChange(itemId: Int) {
        viewModelScope.launch {
            _uiModel.emit(_uiModel.value.copy(viewPagerPosition = itemId))
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Exposed
    ---------------------------------------------------------------------------------------------*/
    fun setCurrentPage(itemId: Int) {
        viewModelScope.launch {
            val position = when (itemId) {
                R.id.recentChatsFragment -> RECENT_CHATS_PAGER_POSITION
                R.id.contactsFragment -> CONTACTS_CHATS_PAGER_POSITION
                else -> PROFILE_CHATS_PAGER_POSITION
            }
            _uiModel.emit(
                _uiModel.value.copy(
                    pageTitle = getTitleByPagePosition(position),
                    searchVisible = position == RECENT_CHATS_PAGER_POSITION,
                    addContactOrCreateGroupButtonIconId = getAddContactOrCreateGroupIconId(position),
                    viewPagerPosition = position,
                    bottomNavigationItemId = itemId
                )
            )
        }
    }

    fun setCurrentBottomNavigationItem(position: Int) {
        viewModelScope.launch {
            val itemId = when (position) {
                RECENT_CHATS_PAGER_POSITION -> R.id.recentChatsFragment
                CONTACTS_CHATS_PAGER_POSITION -> R.id.contactsFragment
                else -> R.id.profileFragment
            }
            _uiModel.emit(
                _uiModel.value.copy(
                    pageTitle = getTitleByPagePosition(position),
                    searchVisible = position == RECENT_CHATS_PAGER_POSITION,
                    addContactOrCreateGroupButtonIconId = getAddContactOrCreateGroupIconId(position),
                    viewPagerPosition = position,
                    bottomNavigationItemId = itemId
                )
            )
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private suspend fun connectUser(userId: String) {
        _connectionStatus.emit(ConnectionStatus.CONNECTING)
        userRepository.connectToSendbird(userId) { either ->
            viewModelScope.launch {
                either.foldSuspend({ httpError ->
                    if (application.isNetworkAvailable().not()) {
                        _connectionStatus.emit(ConnectionStatus.NOT_CONNECTED)
                    }
                    connectUser(userId)
                }, { responseOk ->
                    currentUser = SendBird.getCurrentUser()
                    _connectionStatus.emit(ConnectionStatus.CONNECTED)
                    _uiModel.emit(
                        _uiModel.value.copy(
                            profilePictureUrl = currentUser?.profileUrl ?: EMPTY
                        )
                    )
                })
            }
        }
    }

    private fun getTitleByPagePosition(position: Int) =
        when (position) {
            RECENT_CHATS_PAGER_POSITION -> R.string.chats
            CONTACTS_CHATS_PAGER_POSITION -> R.string.contacts
            else -> R.string.profile
        }

    private fun getAddContactOrCreateGroupIconId(position: Int) =
        when (position) {
            RECENT_CHATS_PAGER_POSITION -> R.drawable.ic_feather
            CONTACTS_CHATS_PAGER_POSITION -> R.drawable.ic_contact_add
            else -> R.drawable.ic_feather
        }

    private fun navigateToAddContacts() {
        _navigationLiveData.value = NavGraph(R.id.action_baseContactsFragment_to_addContactFragment)
    }
}