package com.example.whisper.ui.signup

import androidx.lifecycle.ViewModel
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
class SignUpViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel(), SignUpPresenter {

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    private fun navigateToHome() {
        _navigationLiveData.value =
            NavigationGraph(R.id.action_signUpFragment_to_recentChatsFragment)
    }

    private fun navigateToSignIn() {
        _navigationLiveData.value = NavigationGraph(R.id.action_signUpFragment_to_signInFragment)
    }

    override fun onContinueClick() {
        navigateToHome()
    }

    override fun onSignInClick() {
        navigateToSignIn()
    }
}