package com.example.whisper.ui.splash

import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.navigation.NavigationGraph
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
            navigateUser()
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private suspend fun navigateUser() {
        viewModelScope.launch {
            delay(SPLASH_SCREEN_DELAY)
            navigateToSignIn()
        }
        /*userRepository.isSignedIn() { either ->
            viewModelScope.launch {
                either.foldSuspend({
                    navigateToSignIn()
                }, {
                    navigateToHome()
                })
            }
        }*/
    }

    private fun navigateToHome() {
        _navigationLiveData.value = NavigationGraph(R.id.action_splashFragment_to_recentChatsFragment)
    }

    private fun navigateToSignIn() {
        _navigationLiveData.value = NavigationGraph(R.id.action_splashFragment_to_signInFragment)
    }
}