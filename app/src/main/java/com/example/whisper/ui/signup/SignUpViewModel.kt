package com.example.whisper.ui.signup

import com.example.whisper.R
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.navigation.NavGraph
import com.example.whisper.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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
            NavGraph(R.id.action_signUpFragment_to_recentChatsFragment)
    }

    private fun navigateToSignIn() {
        _navigationLiveData.value = NavGraph(R.id.action_signUpFragment_to_signInFragment)
    }

    override fun onContinueClick() {
        navigateToHome()
    }

    override fun onSignInClick() {
        navigateToSignIn()
    }
}