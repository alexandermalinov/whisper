package com.example.whisper.ui.splash

import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.navigation.NavGraph
import com.example.whisper.ui.base.BaseViewModel
import com.example.whisper.utils.common.SPLASH_SCREEN_DELAY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel() {

    init {
        viewModelScope.launch {
            delay(SPLASH_SCREEN_DELAY)
            navigateUser()
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private suspend fun navigateUser() {
        if (userRepository.isUserLoggedIn()) {
            navigateToHome()
        } else {
            navigateToWelcome()
        }
    }

    private suspend fun navigateToHome() {
        _navigationFlow.emit(NavGraph(R.id.action_splashFragment_to_baseContactsFragment))
    }

    private suspend fun navigateToWelcome() {
        _navigationFlow.emit(NavGraph(R.id.action_splashFragment_to_welcomeFragment))
    }
}