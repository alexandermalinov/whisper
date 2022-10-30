package com.example.whisper.ui.signin

import com.example.whisper.R
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.navigation.NavGraph
import com.example.whisper.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel(), SignInPresenter {

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    private fun navigateToHome() {
        _navigationLiveData.value =
            NavGraph(R.id.action_signInFragment_to_recentChatsFragment)
    }

    private fun navigateToSignUp() {
        _navigationLiveData.value = NavGraph(R.id.action_signInFragment_to_signUpFragment)
    }

    override fun onContinueClick() {
        navigateToHome()
    }

    override fun onSignUpClick() {
        navigateToSignUp()
    }
}