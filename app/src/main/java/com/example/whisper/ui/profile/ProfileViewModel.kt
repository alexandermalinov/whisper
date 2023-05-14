package com.example.whisper.ui.profile

import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.navigation.NavGraph
import com.example.whisper.ui.base.BaseViewModel
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.USER_EMAIL
import com.example.whisper.vo.profile.ProfileUiState
import com.sendbird.android.SendBird
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel(), ProfilePresenter {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    val uiState
        get() = _uiState.asStateFlow()

    private val _uiState = MutableStateFlow(ProfileUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = userRepository.getLoggedUser()

            _uiState.emit(
                ProfileUiState(
                    profilePictureUrl = currentUser?.picture ?: EMPTY,
                    username = currentUser?.username ?: EMPTY,
                    userEmail = currentUser?.email ?: EMPTY
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
            navigateToWelcome()
        }
    }

    private suspend fun navigateToWelcome() {
        _navigationFlow.emit(NavGraph(R.id.action_baseContactsFragment_to_welcomeFragment))
    }
}