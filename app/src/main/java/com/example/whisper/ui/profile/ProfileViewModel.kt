package com.example.whisper.ui.profile

import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.navigation.NavGraph
import com.example.whisper.ui.base.BaseViewModel
import com.example.whisper.vo.profile.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val contactsRepository: ContactsRepository,
    private val recentChatsRepository: RecentChatsRepository
) : BaseViewModel(), ProfilePresenter {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    val uiState
        get() = _uiState.asStateFlow()

    private val _uiState = MutableStateFlow(ProfileUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {

            _uiState.emit(
                ProfileUiState(
                    profilePictureUrl = userRepository.cachedUser.profilePicture,
                    username = userRepository.cachedUser.username,
                    userEmail = userRepository.cachedUser.email
                )
            )
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onLogoutClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(_uiState.value.copy(isLoading = true))
            userRepository.logout()
            contactsRepository.deleteAllContactDbCache()
            recentChatsRepository.deleteAllRecentChatsDbCache()
            navigateToWelcome()
        }
    }

    private suspend fun navigateToWelcome() {
        _navigationFlow.emit(NavGraph(R.id.action_baseContactsFragment_to_welcomeFragment))
    }
}