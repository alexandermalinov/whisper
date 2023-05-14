package com.example.whisper.ui.signup

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.example.whisper.R
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.domain.signup.ValidateEmailUseCase
import com.example.whisper.domain.signup.ValidationStates
import com.example.whisper.navigation.GalleryNavigation
import com.example.whisper.navigation.NavGraph
import com.example.whisper.navigation.PopBackStack
import com.example.whisper.ui.base.BaseInputChangeViewModel
import com.example.whisper.utils.common.INVALID_RES
import com.example.whisper.utils.createFile
import com.example.whisper.utils.isNetworkAvailable
import com.example.whisper.vo.dialogs.TitleMessageDialog
import com.example.whisper.vo.signup.SignUpUiModel
import com.example.whisper.vo.signup.toUser
import com.example.whisper.vo.signup.toUserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val application: Application
) : BaseInputChangeViewModel(), SignUpPresenter {

    val uiState
        get() = _uiState.asStateFlow()

    private val _uiState = MutableStateFlow(SignUpUiModel())

    /* --------------------------------------------------------------------------------------------
     * Exposed
    ---------------------------------------------------------------------------------------------*/
    fun setProfilePicture(uri: Uri) {
        viewModelScope.launch {
            _uiState.emit(
                _uiState.value.copy(
                    pictureFile = application.createFile(uri),
                    profilePicture = uri
                )
            )
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onContinueClick() {
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(isLoading = true))
            userRepository.registerUserFirebase(_uiState.value.email, _uiState.value.password) {
                viewModelScope.launch {
                    it.foldSuspend(
                        { onFailure ->
                            if (application.isNetworkAvailable())
                                showValidationErrorDialog()
                            else
                                showNoNetworkErrorDialog()
                        },
                        { userId -> registerInSendbird(userId) }
                    )
                }
            }
        }
    }

    override fun onFinish() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            userRepository.updateUserSendbird(_uiState.value.username, _uiState.value.pictureFile) {
                viewModelScope.launch {
                    it.foldSuspend(
                        { onFailure ->
                            if (application.isNetworkAvailable())
                                showValidationErrorDialog()
                            else
                                showNoNetworkErrorDialog()
                        },
                        { onSuccess -> navigateToRecentChats() }
                    )
                }
            }
        }
    }

    override fun onBackClick() {
        viewModelScope.launch {
            _navigationFlow.emit(PopBackStack)
        }
    }

    override fun onEmailTextChanged(textFlow: Flow<CharSequence>) {
        onTextChanged(textFlow) { email ->
            when (validateEmailUseCase.invoke(email.toString())) {
                ValidationStates.VALID -> {
                    _uiState.emit(
                        _uiState.value.copy(
                            email = email.toString(),
                            emailErrorEnabled = false,
                            emailError = INVALID_RES,
                            emailEndIcon = R.drawable.ic_checkbox
                        )
                    )
                }
                ValidationStates.INVALID -> {
                    _uiState.emit(
                        _uiState.value.copy(
                            email = email.toString(),
                            emailErrorEnabled = true,
                            emailError = R.string.invalid_email,
                            emailEndIcon = null
                        )
                    )
                }
                ValidationStates.EMPTY -> {
                    _uiState.emit(
                        _uiState.value.copy(
                            email = email.toString(),
                            emailErrorEnabled = false,
                            emailError = INVALID_RES,
                            emailEndIcon = null
                        )
                    )
                }
            }
            setContinueButtonVisibility()
        }
    }

    override fun onPasswordTextChanged(textFlow: Flow<CharSequence>) {
        onTextChanged(textFlow) { password ->
            setIsPasswordValid(password.toString())
            setIsConfirmPasswordValid(_uiState.value.confirmPassword)
            setContinueButtonVisibility()
            _uiState.emit(_uiState.value.copy(password = password.toString()))
        }
    }

    override fun onConfirmPasswordTextChanged(textFlow: Flow<CharSequence>) {
        onTextChanged(textFlow) { confirmPassword ->
            setIsConfirmPasswordValid(confirmPassword.toString())
            setIsPasswordValid(_uiState.value.password)
            setContinueButtonVisibility()
            _uiState.emit(_uiState.value.copy(confirmPassword = confirmPassword.toString()))
        }
    }

    override fun onUsernameTextChanged(textFlow: Flow<CharSequence>) {
        onTextChanged(textFlow) { username ->
            setIsUsernameValid(username.toString())
            _uiState.emit(_uiState.value.copy(username = username.toString()))
        }
    }

    override fun onProfileImageClick() {
        viewModelScope.launch {
            _navigationFlow.emit(GalleryNavigation)
        }
    }

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private suspend fun setIsPasswordValid(password: String) {
        when (password.length) {
            in 1 until PASSWORD_MIN_LENGTH -> {
                _uiState.emit(
                    _uiState.value.copy(
                        passwordError = R.string.invalid_password,
                        passwordErrorEnabled = true
                    )
                )
            }
            else -> {
                _uiState.emit(
                    _uiState.value.copy(
                        passwordError = INVALID_RES,
                        passwordErrorEnabled = false
                    )
                )
            }
        }
    }

    private suspend fun setIsConfirmPasswordValid(confirmPassword: String) {
        _uiState.value.apply {
            if (confirmPassword.contentEquals(password).not()) {
                _uiState.emit(
                    _uiState.value.copy(
                        confirmPasswordError = R.string.invalid_confirm_password,
                        confirmPasswordErrorEnabled = true
                    )
                )
            } else {
                _uiState.emit(
                    _uiState.value.copy(
                        confirmPasswordError = INVALID_RES,
                        confirmPasswordErrorEnabled = false
                    )
                )
            }
        }
    }

    private suspend fun setIsUsernameValid(username: String) {
        when {
            username.length <= USERNAME_MIN_LENGTH -> {
                _uiState.emit(
                    _uiState.value.copy(
                        usernameError = R.string.invalid_username,
                        usernameErrorEnabled = true,
                        isFinishEnabled = false
                    )
                )
            }
            else -> {
                _uiState.emit(
                    _uiState.value.copy(
                        usernameError = INVALID_RES,
                        usernameErrorEnabled = false,
                        isFinishEnabled = true
                    )
                )
            }
        }
    }

    private suspend fun setContinueButtonVisibility() {
        _uiState.emit(_uiState.value.copy(isContinueEnabled = _uiState.value.enableContinueButton()))
    }

    private suspend fun showValidationErrorDialog() {
        _uiState.emit(_uiState.value.copy(isLoading = false))
        _dialogFlow.emit(
            TitleMessageDialog(
                R.string.error_dialog_title_try_again,
                R.string.error_dialog_message_body_invalid_credentials
            )
        )
    }

    private suspend fun showNoNetworkErrorDialog() {
        _uiState.emit(_uiState.value.copy(isLoading = false))
        _dialogFlow.emit(
            TitleMessageDialog(
                R.string.error_dialog_title_network,
                R.string.error_dialog_message_body_no_network
            )
        )
    }

    private suspend fun registerInSendbird(id: String) {
        userRepository.registerUserSendbird(_uiState.value.toUserModel(id)) { either ->
            viewModelScope.launch {
                either.foldSuspend(
                    { onFailure ->
                        if (application.isNetworkAvailable().not())
                            showNoNetworkErrorDialog()
                    },
                    { onSuccess ->
                        userRepository.registerUserLocalDB(_uiState.value.toUser(id))
                        _uiState.emit(_uiState.value.copy(isLoading = false))
                        navigateToStepTwo()
                    }
                )
            }
        }
    }

    private suspend fun navigateToRecentChats() {
        _navigationFlow.emit(NavGraph(R.id.action_signUpStepTwoFragment_to_baseContactsFragment))
    }

    private suspend fun navigateToStepTwo() {
        _navigationFlow.emit(NavGraph(R.id.action_signUpFragment_to_signUpStepTwoFragment))
    }
}