package com.example.whisper.ui.welcome

import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.navigation.NavGraph
import com.example.whisper.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel(), WelcomePresenter {

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onSignUpClick() {
        _navigationLiveData.value = NavGraph(R.id.action_welcomeFragment_to_signUpFragment)
    }

    override fun onSignInClick() {
        _navigationLiveData.value = NavGraph(R.id.action_welcomeFragment_to_signInFragment)
    }
}