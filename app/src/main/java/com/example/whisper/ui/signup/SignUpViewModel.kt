package com.example.whisper.ui.signup

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.domain.signing.ValidateEmailUseCase
import com.example.whisper.domain.signing.ValidationStates
import com.example.whisper.navigation.GalleryNavigation
import com.example.whisper.navigation.NavGraph
import com.example.whisper.navigation.PopBackStack
import com.example.whisper.ui.base.textchange.BaseTextViewModel
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.INVALID_RES
import com.example.whisper.vo.signup.SignUpUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val validateEmailUseCase: ValidateEmailUseCase
) : BaseTextViewModel(), SignUpPresenter {

    val uiState: LiveData<SignUpUiModel>
        get() = _uiState

    private val _uiState = MutableLiveData(SignUpUiModel())

    /* --------------------------------------------------------------------------------------------
     * Exposed
    ---------------------------------------------------------------------------------------------*/
    fun setProfilePicture(uri: Uri) {
        _uiState.value?.profilePicture = uri
    }

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private fun navigateToStepTwo() {
        _navigationLiveData.value =
            NavGraph(R.id.action_signUpFragment_to_signUpStepTwoFragment)
    }

    private fun setIsPasswordValid(password: String) {
        _uiState.value?.apply {
            when (password.length) {
                in 1 until PASSWORD_MIN_LENGTH -> {
                    passwordError = R.string.invalid_password
                    passwordErrorEnabled = true
                }
                else -> {
                    passwordError = INVALID_RES
                    passwordErrorEnabled = false
                }
            }
        }
    }

    private fun setIsConfirmPasswordValid(confirmPassword: String) {
        _uiState.value?.apply {
            if (confirmPassword.contentEquals(password).not()) {
                confirmPasswordError = R.string.invalid_confirm_password
                confirmPasswordErrorEnabled = true
            } else {
                confirmPasswordError = INVALID_RES
                confirmPasswordErrorEnabled = false
            }
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onContinueClick() {
        viewModelScope.launch {
            navigateToStepTwo()
        }
    }

    override fun onBackClick() {
        _navigationLiveData.value = PopBackStack
    }

    override fun onEmailTextChanged(textFlow: Flow<CharSequence>) {
        onTextChanged(textFlow) { email ->
            _uiState.value?.apply {
                when (validateEmailUseCase.invoke(email.toString())) {
                    ValidationStates.VALID -> {
                        emailErrorEnabled = false
                        emailError = INVALID_RES
                        emailEndIcon = R.drawable.ic_checkbox
                    }
                    ValidationStates.INVALID -> {
                        emailErrorEnabled = true
                        emailError = R.string.invalid_email
                        emailEndIcon = null
                    }
                    ValidationStates.EMPTY -> {
                        emailErrorEnabled = false
                        emailError = INVALID_RES
                        emailEndIcon = null
                    }
                }
            }
        }
    }

    override fun onPasswordTextChanged(textFlow: Flow<CharSequence>) {
        onTextChanged(textFlow) { password ->
            setIsPasswordValid(password.toString())
            setIsConfirmPasswordValid(_uiState.value?.confirmPassword ?: EMPTY)
        }
    }

    override fun onConfirmPasswordTextChanged(textFlow: Flow<CharSequence>) {
        onTextChanged(textFlow) { confirmPassword ->
            setIsConfirmPasswordValid(confirmPassword.toString())
            setIsPasswordValid(_uiState.value?.password ?: EMPTY)
        }
    }

    override fun onFinish() {
        // TODO("Not yet implemented")
    }

    override fun onProfileImageClick() {
        _navigationLiveData.value = GalleryNavigation
    }

    companion object {
        const val PASSWORD_MIN_LENGTH = 8
    }
}