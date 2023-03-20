package com.example.whisper.ui.profile

import androidx.lifecycle.viewModelScope
import com.example.whisper.data.repository.user.UserRepository
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
) : BaseViewModel() {

    val uiState
        get() = _uiState.asStateFlow()

    private val _uiState = MutableStateFlow(ProfileUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = SendBird.getCurrentUser()

            _uiState.emit(
                ProfileUiState(
                    profilePictureUrl = currentUser.profileUrl,
                    username = currentUser.nickname,
                    userEmail = currentUser.metaData[USER_EMAIL] ?: EMPTY
                )
            )
        }
    }
}