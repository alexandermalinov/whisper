package com.example.whisper.ui.signin

import androidx.lifecycle.ViewModel
import com.example.whisper.R
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.navigation.NavigationGraph
import com.example.whisper.ui.base.BaseViewModel
import com.example.whisper.ui.signup.SignUpPresenter
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
            NavigationGraph(R.id.action_signInFragment_to_recentChatsFragment)
    }

    private fun navigateToSignUp() {
        _navigationLiveData.value = NavigationGraph(R.id.action_signInFragment_to_signUpFragment)
    }

    override fun onContinueClick() {
        navigateToHome()
    }

    override fun onSignUpClick() {
        navigateToSignUp()
    }
}